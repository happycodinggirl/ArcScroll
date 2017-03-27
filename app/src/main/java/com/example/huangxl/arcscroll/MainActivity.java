package com.example.huangxl.arcscroll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Glide.with(this);
        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.recycleview);
        ImageAdapter imageAdapter=new ImageAdapter(this);
        recyclerView.setAdapter(imageAdapter);

        recyclerView.setLayoutManager(new ArcLayoutManager(this));



    }
}
