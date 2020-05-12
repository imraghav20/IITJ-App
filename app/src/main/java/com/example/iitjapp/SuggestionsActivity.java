package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class SuggestionsActivity extends AppCompatActivity {

    private RecyclerView suggestionsList;
    private ImageButton sendButton;
    private EditText inputMessage;

    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef, SuggestionsRef, MessageKeyRef;

    private String currentUserId, currentUserName, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        SuggestionsRef = FirebaseDatabase.getInstance().getReference().child("Suggestions");


        InitializeFields();

        getUserInfo();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SaveMessageInfoToDatabase();

                inputMessage.setText("");

                suggestionsList.smoothScrollToPosition(suggestionsList.getAdapter().getItemCount());
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Suggestions>()
                .setQuery(SuggestionsRef, Suggestions.class)
                .build();

        FirebaseRecyclerAdapter<Suggestions, SuggestionsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Suggestions, SuggestionsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final SuggestionsViewHolder suggestionsViewHolder, int i, @NonNull Suggestions suggestions)
            {
                final String messageId = getRef(i).getKey();

                SuggestionsRef.child(messageId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String suggestionBy = dataSnapshot.child("name").getValue().toString();
                            String suggestionMessage = dataSnapshot.child("message").getValue().toString();
                            String suggestionDate = dataSnapshot.child("date").getValue().toString();
                            String suggestionTime = dataSnapshot.child("time").getValue().toString();

                            suggestionsViewHolder.userName.setText(suggestionBy);
                            suggestionsViewHolder.message.setText(suggestionMessage);
                            suggestionsViewHolder.date.setText(suggestionDate);
                            suggestionsViewHolder.time.setText(suggestionTime);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_design, parent, false);
                SuggestionsViewHolder viewHolder = new SuggestionsViewHolder(view);
                return viewHolder;
            }
        };
        suggestionsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class SuggestionsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, message, date, time;

        public SuggestionsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.suggestion_by);
            message = itemView.findViewById(R.id.suggestion_message);
            date = itemView.findViewById(R.id.suggestion_date);
            time = itemView.findViewById(R.id.suggestion_time);
        }
    }



    private void SaveMessageInfoToDatabase()
    {
        String message = inputMessage.getText().toString();
        String messageKey = SuggestionsRef.push().getKey();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please enter a message first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> suggestionMessageKey = new HashMap<>();
            SuggestionsRef.updateChildren(suggestionMessageKey);

            MessageKeyRef = SuggestionsRef.child(messageKey);

            HashMap<String, Object> suggestionInfoMap = new HashMap<>();
            suggestionInfoMap.put("name", currentUserName);
            suggestionInfoMap.put("message", message);
            suggestionInfoMap.put("date", currentDate);
            suggestionInfoMap.put("time", currentTime);

            MessageKeyRef.updateChildren(suggestionInfoMap);
        }
    }

    private void getUserInfo()
    {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void InitializeFields()
    {
        sendButton = (ImageButton) findViewById(R.id.send_suggestion_button);
        inputMessage = (EditText) findViewById(R.id.input_suggestion);
        suggestionsList = (RecyclerView) findViewById(R.id.suggestions_recyclerview);
        suggestionsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
}
