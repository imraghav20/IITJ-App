package com.example.iitjapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class NewPollActivity extends AppCompatActivity {

    private EditText question;
    private Button createPoll, addOption;
    private int opt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poll);

        question = (EditText) findViewById(R.id.new_poll_question);
        createPoll = (Button) findViewById(R.id.create_poll_button);
        addOption = (Button) findViewById(R.id.add_option_button);
        opt = 1;

        final Drawable originalDrawable = question.getBackground();

        addOption.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        RelativeLayout new_poll_layout = (RelativeLayout) findViewById(R.id.new_poll_relative_layout);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        if(opt == 1)
                        {
                            lp.addRule(RelativeLayout.BELOW, R.id.new_poll_question);
                        }
                        else
                        {
                            lp.addRule(RelativeLayout.BELOW, opt-1);
                        }
                        lp.setMargins(20, 20, 20, 0);
                        EditText option = new EditText(NewPollActivity.this);
                        option.setLayoutParams(lp);
                        option.setId(opt);
                        option.setHint("Option " + opt);
                        option.setBackground(originalDrawable);
                        option.setPadding(10, 10, 10, 10);
                        new_poll_layout.addView(option);
                        opt += 1;
                    }
                }
        );
    }
}
