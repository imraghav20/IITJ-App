package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.HashMap;

public class EditBuyAndSellItemActivity extends AppCompatActivity {

    private String itemId;

    private DatabaseReference buyAndSellRef;
    private StorageReference buyAndSellItemStorageRef;

    private EditText editTitle, editDescription, editPrice, editMobile;
    private ImageView editImage;
    private Button editItemBtn;

    private static final int GalleryPic = 1;
    private Uri imageUri;
    private String downloadUrl="", currentDate, currentTime;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_buy_and_sell_item);

        itemId = getIntent().getExtras().get("itemId").toString();
        buyAndSellRef = FirebaseDatabase.getInstance().getReference().child("BuyAndSell");

        buyAndSellItemStorageRef = FirebaseStorage.getInstance().getReference().child("BuyAndSell");

        editTitle = findViewById(R.id.edit_item_title);
        editDescription = findViewById(R.id.edit_item_description);
        editPrice = findViewById(R.id.edit_item_price);
        editMobile = findViewById(R.id.edit_item_mobile);
        editImage = findViewById(R.id.edit_item_image);
        editItemBtn = findViewById(R.id.post_edit_item_button);

        loadingBar = new ProgressDialog(this);

        retrieveItemInfo();

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic );
            }
        });

        editItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                updateDatabase();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            editImage.setImageURI(imageUri);
        }
    }

    private void retrieveItemInfo()
    {
        buyAndSellRef.child(itemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("itemName").getValue().toString();
                String description = dataSnapshot.child("itemDescription").getValue().toString();
                String price = dataSnapshot.child("itemPrice").getValue().toString();
                String mobile = dataSnapshot.child("userMobile").getValue().toString();
                String image = dataSnapshot.child("itemImage").getValue().toString();

                editTitle.setText(name);
                editDescription.setText(description);
                editPrice.setText(price);
                editMobile.setText(mobile);
                Picasso.get().load(image).placeholder(R.drawable.ic_buy_and_sell_image_black_24dp).into(editImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateDatabase()
    {
        final String name = editTitle.getText().toString();
        final String description = editDescription.getText().toString();
        final String price = editPrice.getText().toString();
        final String mobile = editMobile.getText().toString();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Item name is compulsory", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(price))
        {
            Toast.makeText(this, "Item price is compulsory", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Updating Item");
            loadingBar.setMessage("Please wait, while we are updating your item.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            if(imageUri!=null)
            {
                final StorageReference filePath = buyAndSellItemStorageRef.child(itemId);
                final UploadTask uploadTask = filePath.putFile(imageUri);

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        downloadUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> updateItemMap = new HashMap<>();
                        updateItemMap.put("itemName", name);
                        updateItemMap.put("itemDescription", description);
                        updateItemMap.put("itemPrice", price);
                        updateItemMap.put("itemImage", downloadUrl);
                        updateItemMap.put("userMobile", mobile);
                        updateItemMap.put("date", currentDate);
                        updateItemMap.put("time", currentTime);

                        buyAndSellRef.child(itemId).updateChildren(updateItemMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            loadingBar.dismiss();
                                            Toast.makeText(EditBuyAndSellItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            loadingBar.dismiss();
                                            String error = task.getException().toString();
                                            Toast.makeText(EditBuyAndSellItemActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });

            }
            else
            {
                HashMap<String, Object> updateItemMap = new HashMap<>();
                updateItemMap.put("itemName", name);
                updateItemMap.put("itemDescription", description);
                updateItemMap.put("itemPrice", price);
                updateItemMap.put("userMobile", mobile);
                updateItemMap.put("date", currentDate);
                updateItemMap.put("time", currentTime);

                buyAndSellRef.child(itemId).updateChildren(updateItemMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(EditBuyAndSellItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    String error = task.getException().toString();
                                    Toast.makeText(EditBuyAndSellItemActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
}
