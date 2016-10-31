package com.example.herik21.forum;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Herik21 on 30/10/2016.
 */

public class SuggestionAdapter extends BaseAdapter implements Filterable{
    public ArrayList<User> data;
    public ArrayList<User> filteredData;
    public Context ctx;

    public SuggestionAdapter(Context context, ArrayList<User> data){
        this.data = data;
        this.filteredData = data;
        this.ctx = context;
    }
    public void setData(ArrayList<User> data) {
        this.data = data;
    }

    @Override
    public int getCount() {return filteredData.size();}

    @Override
    public User getItem(int i) { return filteredData.get(i); }

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
        Log.d("Displayname",getItem(i).Nickname);
        ((TextView)view.findViewById(R.id.autoCompleteItem)).setText(getItem(i).Nickname);
        ((TextView)view.findViewById(R.id.userSid)).setText(getItem(i).idUsuario);
        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                if(charSequence == null || charSequence.length() == 0){
                    results.values = data;
                    results.count = data.size();
                }else{
                    ArrayList<User> ulist = new ArrayList<>();
                    for (User u : data){
                        if(u.Nickname.toLowerCase().contains(charSequence)){
                            ulist.add(u);
                        }
                    }
                    results.values = ulist;
                    results.count = ulist.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<User>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
