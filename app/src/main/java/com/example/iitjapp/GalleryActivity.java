package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView imageUploadList, imageList;
    private ImageButton newPostBtn;
    private Button uploadBtn;

    private static final int RESULT_LOAD_IMAGE = 1;

    private List<String> fileNameList;
    private List<Uri> fileImageList;

    private UploadListAdapter uploadListAdapter;

    private DatabaseReference galleryRef, usersRef;
    private StorageReference galleryStorageRef;
    private FirebaseAuth mAuth;
    private String currentUserId, currentUserName = "", currentDate, currentTime;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        imageUploadList = findViewById(R.id.image_upload_recyclerview);
        imageList = findViewById(R.id.gallery_recyclerview);
        imageList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        newPostBtn = findViewById(R.id.new_pic_post_button);
        uploadBtn = findViewById(R.id.upload_images_btn);

        galleryRef = FirebaseDatabase.getInstance().getReference().child("Gallery");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        galleryStorageRef = FirebaseStorage.getInstance().getReference().child("Gallery");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

//        newPostBtn.setVisibility(View.VISIBLE);
//        uploadBtn.setVisibility(View.GONE);
//        imageUploadList.setVisibility(View.GONE);
//        imageList.setVisibility(View.VISIBLE);

        getUserName();

        fileNameList = new ArrayList<>();
        fileImageList = new ArrayList<>();

        uploadListAdapter = new UploadListAdapter(fileNameList, fileImageList);

        imageUploadList.setLayoutManager(new LinearLayoutManager(this));
        imageUploadList.setHasFixedSize(true);
        imageUploadList.setAdapter(uploadListAdapter);


        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Gallery>()
                .setQuery(galleryRef, Gallery.class)
                .build();

        FirebaseRecyclerAdapter<Gallery, ImageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Gallery, ImageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ImageViewHolder imageViewHolder, int i, @NonNull Gallery gallery)
            {
                String imageId = getRef(i).getKey();

                galleryRef.child(imageId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        String userName = dataSnapshot.child("postBy").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        String userId = dataSnapshot.child("userId").getValue().toString();

                        imageViewHolder.userName.setText("Posted by: "+userName);
//                        imageViewHolder.picDate.setText(date);
//                        imageViewHolder.picTime.setText(time);
                        Picasso.get().load(image).placeholder(R.drawable.ic_buy_and_sell_image_black_24dp).into(imageViewHolder.galleryPic);

                        if(currentUserId.equals(userId))
                        {
                            imageViewHolder.delete.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_images_design, parent, false);
                ImageViewHolder viewHolder = new ImageViewHolder(view);
                return viewHolder;
            }
        };
        imageList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void getUserName()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                currentUserName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK)
        {
            if(data.getClipData() != null)
            {
                int totalItemsSelected = data.getClipData().getItemCount();

                for (int i = 0; i < totalItemsSelected; i++)
                {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    String fileName = getFileName(fileUri);

                    fileNameList.add(fileName);
                    fileImageList.add(fileUri);
                    uploadListAdapter.notifyDataSetChanged();
                }
            }

            newPostBtn.setVisibility(View.GONE);
            uploadBtn.setVisibility(View.VISIBLE);
            imageUploadList.setVisibility(View.VISIBLE);
            imageList.setVisibility(View.GONE);
        }

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveImagesToDatabase();
            }
        });
    }

    public String getFileName(Uri uri)
    {
        String result = null;
        if(uri.getScheme().equals("content"))
        {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try
            {
                if(cursor!=null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally
            {
                cursor.close();
            }
        }
        if(result == null)
        {
            result =uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1)
            {
                result = result.substring(cut + 1);
            }
        }
        return  result;
    }

    private void saveImagesToDatabase()
    {
        loadingBar.setTitle("Posting Images");
        loadingBar.setMessage("Please wait, while we are posting your images.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        for(int i = 0; i < fileNameList.size(); i++)
        {
            final String imageKey = galleryRef.push().getKey();

            final StorageReference filePath = galleryStorageRef.child(imageKey);
            final UploadTask uploadTask = filePath.putFile(fileImageList.get(i));

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    String downloadUrl = task.getResult().toString();

                    HashMap<String, Object> galleryImageMap = new HashMap<>();
                    galleryImageMap.put("image", downloadUrl);
                    galleryImageMap.put("postBy", currentUserName);
                    galleryImageMap.put("userId", currentUserId);
                    galleryImageMap.put("date", currentDate);
                    galleryImageMap.put("time", currentTime);

                    galleryRef.child(imageKey).updateChildren(galleryImageMap);
                }
            });
        }

        loadingBar.dismiss();

        Toast.makeText(this, "Images posted successfully", Toast.LENGTH_SHORT).show();
        fileNameList.clear();
        fileImageList.clear();
        uploadListAdapter.notifyDataSetChanged();
        uploadBtn.setVisibility(View.GONE);
        newPostBtn.setVisibility(View.VISIBLE);
        imageUploadList.setVisibility(View.GONE);
        imageList.setVisibility(View.VISIBLE);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, picTime, picDate;
        ImageView galleryPic;
        Button delete, download;

        public ImageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.gallery_image_user_name);
            picTime = itemView.findViewById(R.id.item_post_time);
            picDate = itemView.findViewById(R.id.item_post_date);
            galleryPic = itemView.findViewById(R.id.gallery_image);
            delete = itemView.findViewById(R.id.image_delete_button);
            download = itemView.findViewById(R.id.image_download_button);
        }
    }
}
