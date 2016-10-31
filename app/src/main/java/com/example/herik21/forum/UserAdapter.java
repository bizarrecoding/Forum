package com.example.herik21.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

/**
 * Created by Herik21 on 29/10/2016.
 */

public class UserAdapter extends BaseAdapter{

    public ArrayList<User> data;
    public Context ctx;
    public boolean custom;

    public UserAdapter(Context context, ArrayList<User> data){
        this.data = data;
        this.ctx = context;
    }

    public void setData(ArrayList<User> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public User getItem(int i) {
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
            view = linf.inflate(R.layout.members,null);
        }
        Glide.with(ctx).load(getItem(i).PhotoURL).into((ImageView)view.findViewById(R.id.profile));
        ((TextView)view.findViewById(R.id.autoCompleteItem)).setText(getItem(i).Nickname);
        return view;
    }
}
