package com.example.iitjapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PollResultActivity extends AppCompatActivity {
    private String pollId;
    private BarChart barChart;
    private TextView question, vote_count;

    private DatabaseReference currentPollRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_result);

        pollId = getIntent().getExtras().get("pollId").toString();
        barChart = findViewById(R.id.bar_chart);

        question = findViewById(R.id.result_question);
        vote_count = findViewById(R.id.vote_count);

        currentPollRef = FirebaseDatabase.getInstance().getReference().child("Polls").child(pollId);

        currentPollRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String option1 = dataSnapshot.child("option 1").getValue().toString();
                String option2 = dataSnapshot.child("option 2").getValue().toString();
                String option3 = dataSnapshot.child("option 3").getValue().toString();
                String option4 = dataSnapshot.child("option 4").getValue().toString();
                String result_question = dataSnapshot.child("question").getValue().toString();

                int option1_result = Integer.parseInt(dataSnapshot.child("result").child("option 1").getValue().toString());
                int option2_result = Integer.parseInt(dataSnapshot.child("result").child("option 2").getValue().toString());
                int option3_result = Integer.parseInt(dataSnapshot.child("result").child("option 3").getValue().toString());
                int option4_result = Integer.parseInt(dataSnapshot.child("result").child("option 4").getValue().toString());
                int sum = option1_result + option2_result + option3_result + option4_result;

                question.setText(result_question);
                vote_count.setText("Total Votes: " + sum);

                ArrayList<BarEntry> barEntries = new ArrayList<>();
                barEntries.add(new BarEntry(option1_result, 0));
                barEntries.add(new BarEntry(option2_result, 1));
                if(!option3.equals(""))
                {
                    barEntries.add(new BarEntry(option3_result, 2));
                }
                if(!option4.equals(""))
                {
                    barEntries.add(new BarEntry(option4_result, 3));
                }

                BarDataSet barDataSet = new BarDataSet(barEntries, "Votes");

                barDataSet.setColor(Color.RED);

                ArrayList<String> options = new ArrayList<>();
                if(option1.length() > 10)
                {
                    options.add(option1.substring(0, 10));
                }
                else
                {
                    options.add(option1);
                }

                if(option2.length() > 10)
                {
                    options.add(option2.substring(0, 10));
                }
                else
                {
                    options.add(option2);
                }
                if(!option3.equals(""))
                {
                    if(option3.length() > 10)
                    {
                        options.add(option3.substring(0, 10));
                    }
                    else
                    {
                        options.add(option3);
                    }
                }
                if(!option4.equals(""))
                {
                    if(option4.length() > 10)
                    {
                        options.add(option4.substring(0, 10));
                    }
                    else
                    {
                        options.add(option4);
                    }
                }

                BarData theData = new BarData(options, barDataSet);
                barChart.setData(theData);

                barChart.setTouchEnabled(true);
                barChart.setDragEnabled(true);
                barChart.setScaleEnabled(true);

                barChart.setDescription("Options");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
