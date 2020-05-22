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

public class PostFragment extends Fragment {

    private ImageButton buy_and_sell, gallery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        buy_and_sell = (ImageButton) rootView.findViewById(R.id.buy_and_sell);
        gallery = (ImageButton) rootView.findViewById(R.id.gallery);

        buy_and_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buyAndSellIntent = new Intent(getActivity(), BuyAndSellActivity.class);
                startActivity(buyAndSellIntent);
            }
        });

        return rootView;
    }
}
