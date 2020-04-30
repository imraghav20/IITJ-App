package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

    private TextView heading, messageDisplay;
    private ImageButton sendButton;
    private EditText inputMessage;
    private ScrollView messageScroll;

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

                messageScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        SuggestionsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            String suggestion = "<small><font color='blue'>" + chatName + ": <br/>" + "</font></small>" + chatMessage + "<br/>" + "<small><font color='blue'>" + chatTime + "&nbsp;&nbsp;&nbsp;" + chatDate +  "</font></small>" + "<br/><br/>";
            messageDisplay.append(Html.fromHtml(suggestion));

            messageScroll.fullScroll(ScrollView.FOCUS_DOWN);
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
        heading = (TextView) findViewById(R.id.suggestions_title);
        messageDisplay = (TextView) findViewById(R.id.suggestions_display);
        sendButton = (ImageButton) findViewById(R.id.send_suggestion_button);
        inputMessage = (EditText) findViewById(R.id.input_suggestion);
        messageScroll = (ScrollView) findViewById(R.id.suggestions_scroll_view);
    }
}
