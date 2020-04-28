package com.example.iitjapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InterfaceFragment extends Fragment {

    private ImageButton suggestions, invitation, books, testpapers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_interface, container, false);

        suggestions = (ImageButton) rootView.findViewById(R.id.suggestions);
        invitation = (ImageButton) rootView.findViewById(R.id.invitation);
        books = (ImageButton) rootView.findViewById(R.id.books);
        testpapers = (ImageButton) rootView.findViewById(R.id.testpapers);

        suggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent suggestionsIntent = new Intent(getActivity(), SuggestionsActivity.class );
                startActivity(suggestionsIntent);
            }
        });

        return rootView;
    }
}
