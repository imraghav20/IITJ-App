package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NewPollActivity extends AppCompatActivity {

    private EditText question, option1, option2, option3, option4;
    private Button createPoll;

    private DatabaseReference pollRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId, createdBy="", currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poll);

        question = (EditText) findViewById(R.id.new_poll_question);
        option1 = (EditText) findViewById(R.id.new_poll_option_1);
        option2 = (EditText) findViewById(R.id.new_poll_option_2);
        option3 = (EditText) findViewById(R.id.new_poll_option_3);
        option4 = (EditText) findViewById(R.id.new_poll_option_4);
        createPoll = (Button) findViewById(R.id.create_poll_button);

        pollRef = FirebaseDatabase.getInstance().getReference().child("Polls");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        getUserInfo();

        createPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoToDatabase();
            }
        });
    }

    private void getUserInfo()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    createdBy = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveInfoToDatabase()
    {
        final String pollKey = pollRef.push().getKey();
        String question_value = question.getText().toString();
        final String option1_value = option1.getText().toString();
        final String option2_value = option2.getText().toString();
        final String option3_value = option3.getText().toString();
        final String option4_value = option4.getText().toString();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        if(TextUtils.isEmpty(question_value))
        {
            Toast.makeText(this, "Please enter question first", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(option1_value) | TextUtils.isEmpty(option2_value))
        {
            Toast.makeText(this, "Poll should have at least two options", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> pollMap = new HashMap<>();
            pollMap.put("question", question_value);
            pollMap.put("createdBy", createdBy);
            pollMap.put("date", currentDate);
            pollMap.put("time", currentTime);
            pollRef.child(pollKey).updateChildren(pollMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            HashMap<String, Object> optionsMap = new HashMap<>();
                            optionsMap.put("option 1", option1_value);
                            optionsMap.put("option 2", option2_value);
                            optionsMap.put("option 3", option3_value);
                            optionsMap.put("option 4", option4_value);
                            pollRef.child(pollKey).child("options").updateChildren(optionsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            Toast.makeText(NewPollActivity.this, "Poll created successfully", Toast.LENGTH_SHORT).show();

                                            question.setText("");
                                            option1.setText("");
                                            option2.setText("");
                                            option3.setText("");
                                            option4.setText("");
                                        }
                                    });
                        }
                    });
        }

    }
}
