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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NewBuyAndSellItemActivity extends AppCompatActivity {

    private TextView itemName, itemPrice, itemDescription, itemPhone;
    private ImageView itemImage;
    private Button postNewItemBtn;

    private static final int GalleryPic = 1;
    private Uri imageUri;
    private String downloadUrl="";

    private FirebaseAuth mAuth;
    private String currentUserId, currentUserName="", currentUserEmail="", currentDate, currentTime;
    private DatabaseReference buyAndSellRef, userRef;
    private StorageReference buyAndSellItemStorageRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_buy_and_sell_item);

        itemName = findViewById(R.id.new_item_title);
        itemPrice = findViewById(R.id.new_item_price);
        itemDescription = findViewById(R.id.new_item_description);
        itemPhone = findViewById(R.id.new_item_mobile);
        itemImage = findViewById(R.id.new_item_image);
        postNewItemBtn = findViewById(R.id.post_new_item_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        buyAndSellRef = FirebaseDatabase.getInstance().getReference().child("BuyAndSell");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        buyAndSellItemStorageRef = FirebaseStorage.getInstance().getReference().child("BuyAndSell");

        loadingBar = new ProgressDialog(this);

        getUserInfo();

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic );
            }
        });

        postNewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoToDatabase();
            }
        });
    }

    private void getUserInfo()
    {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                currentUserName = dataSnapshot.child("name").getValue().toString();
                currentUserEmail = dataSnapshot.child("mail").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            itemImage.setImageURI(imageUri);
        }
    }

    private void saveInfoToDatabase()
    {
        final String itemKey = buyAndSellRef.push().getKey();

        final String setItemName = itemName.getText().toString();
        final String setItemPrice = itemPrice.getText().toString();
        final String setItemDescription = itemDescription.getText().toString();
        final String setItemPhone = itemPhone.getText().toString();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());


        if(TextUtils.isEmpty(setItemName))
        {
            Toast.makeText(this, "Item name is compulsory", Toast.LENGTH_SHORT).show();
        }
        else if(imageUri == null)
        {
            Toast.makeText(this, "Item photo is compulsory", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setItemPrice))
        {
            Toast.makeText(this, "Item price is compulsory", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Posting Item");
            loadingBar.setMessage("Please wait, while we are posting your item.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            final StorageReference filePath = buyAndSellItemStorageRef.child(itemKey);
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
            })
            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    downloadUrl = task.getResult().toString();

                    HashMap<String, Object> newItemMap = new HashMap<>();
                    newItemMap.put("createdBy", currentUserName);
                    newItemMap.put("userId", currentUserId);
                    newItemMap.put("userMail", currentUserEmail);
                    newItemMap.put("itemName", setItemName);
                    newItemMap.put("itemPrice", setItemPrice);
                    newItemMap.put("itemDescription", setItemDescription);
                    newItemMap.put("userMobile", setItemPhone);
                    newItemMap.put("itemImage", downloadUrl);
                    newItemMap.put("time", currentTime);
                    newItemMap.put("date", currentDate);

                    buyAndSellRef.child(itemKey).updateChildren(newItemMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(NewBuyAndSellItemActivity.this, "Item posted successfully", Toast.LENGTH_SHORT).show();
                                        itemName.setText("");
                                        itemDescription.setText("");
                                        itemPhone.setText("");
                                        itemPrice.setText("");
                                        itemImage.setImageResource(R.drawable.ic_buy_and_sell_image_black_24dp);
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        String error = task.getException().toString();
                                        Toast.makeText(NewBuyAndSellItemActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
    }
}
