package com.timyr.opencv_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.adapters.itemAdapter;
import com.timyr.opencv_demo.controller.Sign;

import java.util.ArrayList;

public class RecognitionActivity extends Activity {
    private ArrayList<Sign> listSign;
    private itemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_row);
        TextView textView = (TextView) findViewById(R.id.textView);
        ListView list = (ListView) findViewById(R.id.lstDetectedSigns);
        listSign = getIntent().getParcelableArrayListExtra("key");
        if (listSign.size() == 0) {
            textView.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
        adapter = new itemAdapter(listSign, RecognitionActivity.this);
        list.setAdapter(adapter);
    }


}