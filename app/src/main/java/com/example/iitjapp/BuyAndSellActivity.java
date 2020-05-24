package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class BuyAndSellActivity extends AppCompatActivity {

    private ImageButton newItemPost;
    private RecyclerView itemsList;

    private DatabaseReference buyAndSellRef;
    private StorageReference buyAndSellStorageRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_and_sell);

        newItemPost = findViewById(R.id.new_buy_and_sell_button);
        itemsList = findViewById(R.id.buy_and_sell_recyclerview);
        itemsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        buyAndSellRef = FirebaseDatabase.getInstance().getReference().child("BuyAndSell");
        buyAndSellStorageRef = FirebaseStorage.getInstance().getReference().child("BuyAndSell");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        newItemPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newBuyAndSellIntent = new Intent(BuyAndSellActivity.this, NewBuyAndSellItemActivity.class);
                startActivity(newBuyAndSellIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<BuyAndSellItem>()
                .setQuery(buyAndSellRef, BuyAndSellItem.class)
                .build();

        FirebaseRecyclerAdapter<BuyAndSellItem, ItemPostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BuyAndSellItem, ItemPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemPostViewHolder itemPostViewHolder, int i, @NonNull BuyAndSellItem buyAndSellItem)
            {
                final String ItemId = getRef(i).getKey();

                buyAndSellRef.child(ItemId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                    {
                        String itemUser = dataSnapshot.child("createdBy").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String description = dataSnapshot.child("itemDescription").getValue().toString();
                        String image = dataSnapshot.child("itemImage").getValue().toString();
                        final String name = dataSnapshot.child("itemName").getValue().toString();
                        String price = dataSnapshot.child("itemPrice").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String id = dataSnapshot.child("userId").getValue().toString();
                        final String mail = dataSnapshot.child("userMail").getValue().toString();
                        final String mobile = dataSnapshot.child("userMobile").getValue().toString();

                        itemPostViewHolder.userName.setText("Posted by: "+itemUser);
                        itemPostViewHolder.itemName.setText(name);
                        itemPostViewHolder.itemDescription.setText(description);
                        itemPostViewHolder.itemPrice.setText("₹"+price);
                        itemPostViewHolder.date.setText(date);
                        itemPostViewHolder.time.setText(time);
                        Picasso.get().load(image).placeholder(R.drawable.ic_buy_and_sell_image_black_24dp).into(itemPostViewHolder.itemImage);

                        if(currentUserId.equals(id))
                        {
                            itemPostViewHolder.editAndDelete.setVisibility(View.VISIBLE);
                            itemPostViewHolder.contact.setVisibility(View.GONE);
                        }
                        if(mobile.equals(""))
                        {
                            itemPostViewHolder.callButton.setVisibility(View.GONE);
                        }
                        if(description.equals(""))
                        {
                            itemPostViewHolder.itemDescription.setVisibility(View.GONE);
                        }

                        itemPostViewHolder.emailButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String [] recipient = mail.split(",");
                                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mail, null));
                                intent.putExtra(Intent.EXTRA_EMAIL, recipient);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Buy and Sell- "+name);

                                startActivity(Intent.createChooser(intent, "Mail via: "));
                            }
                        });

                        itemPostViewHolder.callButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Uri number = Uri.parse("tel:"+mobile);
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                itemPostViewHolder.deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        buyAndSellRef.child(ItemId).removeValue();
                        buyAndSellStorageRef.child(ItemId).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        Toast.makeText(BuyAndSellActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                String error = e.toString();
                                Toast.makeText(BuyAndSellActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                itemPostViewHolder.editItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent editItemIntent = new Intent(BuyAndSellActivity.this, EditBuyAndSellItemActivity.class);
                        editItemIntent.putExtra("itemId", ItemId);
                        startActivity(editItemIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ItemPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.buy_and_sell_design, parent, false);
                ItemPostViewHolder viewHolder = new ItemPostViewHolder(view);
                return viewHolder;
            }
        };
        itemsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ItemPostViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, itemName, itemDescription, itemPrice, date, time;
        ImageView itemImage, emailButton, callButton;
        Button editItem, deleteItem;
        LinearLayout editAndDelete, contact;

        public ItemPostViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.item_user_name);
            itemName = itemView.findViewById(R.id.item_name);
            itemDescription = itemView.findViewById(R.id.item_description);
            itemPrice = itemView.findViewById(R.id.item_price);
            date = itemView.findViewById(R.id.item_post_date);
            time = itemView.findViewById(R.id.item_post_time);
            itemImage = itemView.findViewById(R.id.item_image);
            emailButton = itemView.findViewById(R.id.email_contact_button);
            callButton = itemView.findViewById(R.id.phone_contact_button);
            editItem = itemView.findViewById(R.id.item_edit_button);
            deleteItem = itemView.findViewById(R.id.item_delete_button);
            editAndDelete = itemView.findViewById(R.id.user_edit_and_delete);
            contact = itemView.findViewById(R.id.contact);
        }
    }
}
