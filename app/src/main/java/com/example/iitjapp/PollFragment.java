package com.example.iitjapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PollFragment extends Fragment {

    private ImageButton new_poll_button;
    private RecyclerView pollsList;

    private DatabaseReference pollsRef;

    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poll, container, false);

        pollsList = (RecyclerView) rootView.findViewById(R.id.poll_recyclerview);
        pollsList.setLayoutManager(new LinearLayoutManager(getContext()));

        new_poll_button = (ImageButton) rootView.findViewById(R.id.new_poll_button);

        pollsRef = FirebaseDatabase.getInstance().getReference().child("Polls");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        new_poll_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToNewPollActivity();
            }
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Poll>()
                .setQuery(pollsRef, Poll.class)
                .build();

        FirebaseRecyclerAdapter<Poll, PollsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Poll, PollsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PollsViewHolder pollsViewHolder, int i, @NonNull Poll poll)
            {
                final String pollId = getRef(i).getKey();

                pollsRef.child(pollId).child("users").child(currentUserId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(!dataSnapshot.exists())
                                {
                                    pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("");
                                }
                                else
                                {
                                    String selected = dataSnapshot.child("selected").getValue().toString();

                                    if(selected.equals("option 1"))
                                    {
                                        pollsViewHolder.option1.setBackgroundResource(R.drawable.selected_button);
                                    }
                                    if(selected.equals("option 2"))
                                    {
                                        pollsViewHolder.option2.setBackgroundResource(R.drawable.selected_button);
                                    }
                                    if(selected.equals("option 3"))
                                    {
                                        pollsViewHolder.option3.setBackgroundResource(R.drawable.selected_button);
                                    }
                                    if(selected.equals("option 4"))
                                    {
                                        pollsViewHolder.option4.setBackgroundResource(R.drawable.selected_button);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                pollsRef.child(pollId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                    {
                        final String pollBy = dataSnapshot.child("createdBy").getValue().toString();
                        String question = dataSnapshot.child("question").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String option1 = dataSnapshot.child("option 1").getValue().toString();
                        String option2 = dataSnapshot.child("option 2").getValue().toString();
                        String option3 = dataSnapshot.child("option 3").getValue().toString();
                        String option4 = dataSnapshot.child("option 4").getValue().toString();

                        pollsViewHolder.userName.setText(pollBy);
                        pollsViewHolder.question.setText(question);
                        pollsViewHolder.date.setText(date);
                        pollsViewHolder.time.setText(time);
                        pollsViewHolder.option1.setText(option1);
                        pollsViewHolder.option2.setText(option2);
                        pollsViewHolder.option3.setText(option3);
                        pollsViewHolder.option4.setText(option4);

                        if(option3.equals(""))
                        {
                            pollsViewHolder.option3.setVisibility(View.GONE);
                        }

                        if(option4.equals(""))
                        {
                            pollsViewHolder.option4.setVisibility(View.GONE);
                        }

                        pollsViewHolder.option1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String selected = dataSnapshot.child("users").child(currentUserId).child("selected").getValue().toString();
                                pollsViewHolder.option1.setBackgroundResource(R.drawable.selected_button);

                                if(selected.equals(""))
                                {
                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 1").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 1").setValue(count + 1);

                                    pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("option 1");
                                }
                                else if(!selected.equals("option 1"))
                                {
                                    int preCount = Integer.parseInt(dataSnapshot.child("result").child(selected).getValue().toString());
                                    pollsRef.child(pollId).child("result").child(selected).setValue(preCount-1);

                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 1").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 1").setValue(count + 1);

                                    pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("option 1");

                                    if(selected.equals("option 2"))
                                    {
                                        pollsViewHolder.option2.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 3"))
                                    {
                                        pollsViewHolder.option3.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 4"))
                                    {
                                        pollsViewHolder.option4.setBackgroundResource(R.drawable.button);
                                    }
                                }
                            }
                        });

                        pollsViewHolder.option2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String selected = dataSnapshot.child("users").child(currentUserId).child("selected").getValue().toString();
                                pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("option 2");
                                pollsViewHolder.option2.setBackgroundResource(R.drawable.selected_button);

                                if(selected.equals(""))
                                {
                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 2").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 2").setValue(count + 1);
                                }
                                else if(!selected.equals("option 2"))
                                {
                                    int preCount = Integer.parseInt(dataSnapshot.child("result").child(selected).getValue().toString());
                                    pollsRef.child(pollId).child("result").child(selected).setValue(preCount-1);

                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 2").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 2").setValue(count + 1);

                                    if(selected.equals("option 1"))
                                    {
                                        pollsViewHolder.option1.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 3"))
                                    {
                                        pollsViewHolder.option3.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 4"))
                                    {
                                        pollsViewHolder.option4.setBackgroundResource(R.drawable.button);
                                    }
                                }
                            }
                        });

                        pollsViewHolder.option3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String selected = dataSnapshot.child("users").child(currentUserId).child("selected").getValue().toString();
                                pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("option 3");
                                pollsViewHolder.option3.setBackgroundResource(R.drawable.selected_button);

                                if(selected.equals(""))
                                {
                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 3").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 3").setValue(count + 1);
                                }
                                else if(!selected.equals("option 3"))
                                {
                                    int preCount = Integer.parseInt(dataSnapshot.child("result").child(selected).getValue().toString());
                                    pollsRef.child(pollId).child("result").child(selected).setValue(preCount-1);

                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 3").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 3").setValue(count + 1);

                                    if(selected.equals("option 2"))
                                    {
                                        pollsViewHolder.option2.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 1"))
                                    {
                                        pollsViewHolder.option1.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 4"))
                                    {
                                        pollsViewHolder.option4.setBackgroundResource(R.drawable.button);
                                    }
                                }
                            }
                        });

                        pollsViewHolder.option4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String selected = dataSnapshot.child("users").child(currentUserId).child("selected").getValue().toString();
                                pollsRef.child(pollId).child("users").child(currentUserId).child("selected").setValue("option 4");
                                pollsViewHolder.option4.setBackgroundResource(R.drawable.selected_button);

                                if(selected.equals(""))
                                {
                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 4").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 4").setValue(count + 1);
                                }
                                else if(!selected.equals("option 4"))
                                {
                                    int preCount = Integer.parseInt(dataSnapshot.child("result").child(selected).getValue().toString());
                                    pollsRef.child(pollId).child("result").child(selected).setValue(preCount-1);

                                    int count = Integer.parseInt(dataSnapshot.child("result").child("option 4").getValue().toString());
                                    pollsRef.child(pollId).child("result").child("option 4").setValue(count + 1);

                                    if(selected.equals("option 2"))
                                    {
                                        pollsViewHolder.option2.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 3"))
                                    {
                                        pollsViewHolder.option3.setBackgroundResource(R.drawable.button);
                                    }
                                    if(selected.equals("option 1"))
                                    {
                                        pollsViewHolder.option1.setBackgroundResource(R.drawable.button);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poll_design, parent, false);
                PollsViewHolder viewHolder = new PollsViewHolder(view);
                return viewHolder;

            }
        };
        pollsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void sendUserToNewPollActivity()
    {
        Intent newPollIntent = new Intent(getActivity(), NewPollActivity.class);
        startActivity(newPollIntent);
    }

    public static class PollsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, question, date, time;
        Button option1, option2, option3, option4, results;

        public PollsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.poll_by);
            question = itemView.findViewById(R.id.poll_question);
            date = itemView.findViewById(R.id.poll_date);
            time = itemView.findViewById(R.id.poll_time);

            option1 = itemView.findViewById(R.id.option_1);
            option2 = itemView.findViewById(R.id.option_2);
            option3 = itemView.findViewById(R.id.option_3);
            option4 = itemView.findViewById(R.id.option_4);

            results = itemView.findViewById(R.id.poll_results_button);
        }
    }
}
