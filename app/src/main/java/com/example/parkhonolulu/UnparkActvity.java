package com.example.parkhonolulu;

import android.os.Bundle;

public class UnparkActvity extends BaseDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_unpark, findViewById(R.id.content_frame), true);
    }
}
