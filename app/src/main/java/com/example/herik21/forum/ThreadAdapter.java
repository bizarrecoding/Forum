package com.example.herik21.forum;

/**
 * Created by Herik21 on 15/10/2016.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ThreadAdapter extends BaseAdapter {

    private Context context;
    private String[] data;

    public ThreadAdapter(Context context, String[] values){
        this.context=context;
        this.data=values;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String text=data[position];

        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.threadrow,null);
        }

        TextView tv1,tv2;
        tv1= (TextView) convertView.findViewById(R.id.tvThreadtitle);
        tv2= (TextView) convertView.findViewById(R.id.tvThreadDescription);
        tv1.setText(text);
        return convertView;
    }
}
