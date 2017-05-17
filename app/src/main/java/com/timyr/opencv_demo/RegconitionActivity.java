package com.timyr.opencv_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.timyr.opencv_demo.adapters.itemAdapter;
import com.timyr.opencv_demo.controller.Sign;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RegconitionActivity extends Activity {
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
        adapter = new itemAdapter(listSign, RegconitionActivity.this);
        list.setAdapter(adapter);
    }


}