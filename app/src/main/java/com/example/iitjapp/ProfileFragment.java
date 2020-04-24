package com.example.iitjapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private Button updateProfile;
    private EditText userName, userRoll, userClubs;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference RootRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        updateProfile = (Button) rootView.findViewById(R.id.update_profile_button);
        userName = (EditText) rootView.findViewById(R.id.user_name);
        userRoll = (EditText) rootView.findViewById(R.id.user_roll);
        userClubs = (EditText) rootView.findViewById(R.id.user_clubs);
        profileImage = (CircleImageView) rootView.findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UpdateProfile();
            }
        });

        return rootView;
    }

    private void UpdateProfile()
    {
        String setUserName = userName.getText().toString();
        String setUserRoll = userRoll.getText().toString();
        String setUserClubs = userClubs.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(getActivity(), "Please fill your name.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setUserRoll))
        {
            Toast.makeText(getActivity(), "Please fill your Roll no.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("roll", setUserRoll);
            profileMap.put("clubs", setUserClubs);
            RootRef.child("Users").child(currentUserId).setValue(profileMap)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getActivity(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), "Error: "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
