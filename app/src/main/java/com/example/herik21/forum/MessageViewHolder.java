package com.example.herik21.forum;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Herik21 on 22/10/2016.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView time;
    public TextView time2;
    public TextView content;
    public TextView user;
    public ImageView icon;
    public ImageView image;

    public MessageViewHolder(View v) {
        super(v);
        content = (TextView) itemView.findViewById(R.id.mContent);
        user = (TextView) itemView.findViewById(R.id.mUser);
        icon = (ImageView) itemView.findViewById(R.id.icon);
        time = (TextView) itemView.findViewById(R.id.mTime);
        time2 = (TextView) itemView.findViewById(R.id.mTime2);
        image = (ImageView) itemView.findViewById(R.id.image);
    }
}
