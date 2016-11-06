package com.example.herik21.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Herik21 on 11/10/2016.
 */

public class MessageAdapter extends BaseAdapter {

    public ArrayList<Message> data;
    public Context ctx;

    public MessageAdapter(Context context, ArrayList<Message> data){
        this.data = data;
        this.ctx = context;
    }

    public void setData(ArrayList<Message> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Message getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater linf = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = linf.inflate(R.layout.message,null);
        }
        //((TextView)view.findViewById(R.id.mUser)).setText(getItem(i).getUser);
        //((TextView)view.findViewById(R.id.mTime)).setText(getItem(i).getTimestamp);
        //((TextView)view.findViewById(R.id.mContent)).setText(getItem(i).getContent);
        return view;
    }
}
