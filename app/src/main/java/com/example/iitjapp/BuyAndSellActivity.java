package com.example.iitjapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class BuyAndSellActivity extends AppCompatActivity {

    private ImageButton newItemPost;
    private RecyclerView itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_and_sell);

        newItemPost = findViewById(R.id.new_buy_and_sell_button);
        itemsList = findViewById(R.id.buy_and_sell_recyclerview);
        itemsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        newItemPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newBuyAndSellIntent = new Intent(BuyAndSellActivity.this, NewBuyAndSellItemActivity.class);
                startActivity(newBuyAndSellIntent);
            }
        });
    }
}
