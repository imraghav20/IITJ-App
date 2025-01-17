package com.example.iitjapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private Button updateProfile;
    private EditText userName, userRoll, userClubs;
    private ImageView profileImage;

    private Uri ImageUri;
    private String downloadUrl;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference RootRef;

    private static final int GalleryPic = 1;
    private StorageReference userProfileImageStorageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        updateProfile = (Button) rootView.findViewById(R.id.update_profile_button);
        userName = (EditText) rootView.findViewById(R.id.user_name);
        userRoll = (EditText) rootView.findViewById(R.id.user_roll);
        userClubs = (EditText) rootView.findViewById(R.id.user_clubs);
        profileImage = (ImageView) rootView.findViewById(R.id.profile_image);

        loadingBar = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageStorageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UpdateProfile();
            }
        });

        retrieveUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic );
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPic && resultCode == RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            profileImage.setImageURI(ImageUri);
        }
    }

    private void retrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name"))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserRoll = dataSnapshot.child("roll").getValue().toString();

                            userName.setText(retrieveUserName);
                            userRoll.setText(retrieveUserRoll);


                            if(dataSnapshot.hasChild("clubs"))
                            {
                                String retrieveUserClubs = dataSnapshot.child("clubs").getValue().toString();
                                userClubs.setText(retrieveUserClubs);
                            }
                            if(dataSnapshot.hasChild("image"))
                            {
                                String retrieveUserImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retrieveUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                            }
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Please update your profile.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateProfile()
    {
        final String setUserName = userName.getText().toString();
        final String setUserRoll = userRoll.getText().toString();
        final String setUserClubs = userClubs.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(getActivity(), "Please fill your name.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setUserRoll))
        {
            Toast.makeText(getActivity(), "Please fill your Roll no.", Toast.LENGTH_SHORT).show();
        }
        else if(ImageUri == null)
        {
            loadingBar.setTitle("Updating Profile");
            loadingBar.setMessage("Please wait, while we are updating your profile.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("roll", setUserRoll);
            profileMap.put("clubs", setUserClubs);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        loadingBar.dismiss();
                        Toast.makeText(getActivity(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        loadingBar.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), "Error: "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            loadingBar.setTitle("Updating Profile");
            loadingBar.setMessage("Please wait, while we are updating your profile.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            final StorageReference filePath = userProfileImageStorageRef.child(currentUserId);

            final UploadTask uploadTask = filePath.putFile(ImageUri);

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

                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", currentUserId);
                    profileMap.put("name", setUserName);
                    profileMap.put("roll", setUserRoll);
                    profileMap.put("clubs", setUserClubs);
                    profileMap.put("image", downloadUrl);

                    RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        String error = task.getException().toString();
                                        Toast.makeText(getActivity(), "Error: "+error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

        }
    }

}
