package com.example.iitjapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PollFragment extends Fragment {

    ImageButton new_poll_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poll, container, false);

        new_poll_button = (ImageButton) rootView.findViewById(R.id.new_poll_button);

        new_poll_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToNewPollActivity();
            }
        });

        return rootView;
    }

    private void sendUserToNewPollActivity()
    {
        Intent newPollIntent = new Intent(getActivity(), NewPollActivity.class);
        startActivity(newPollIntent);
    }
}
