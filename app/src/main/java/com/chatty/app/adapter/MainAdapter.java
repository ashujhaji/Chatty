package com.chatty.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chatty.app.R;
import com.chatty.app.activity.ChatActivity;
import com.chatty.app.model.ChatOptions;


import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<ChatOptions> list;
    private Context context;

    public MainAdapter(List<ChatOptions> listdata, Context context) {
        this.list = listdata;
        this.context = context;
    }

    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.layout_chat_options, parent, false);
        return new ViewHolder(listItem, context);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getTitle());
        holder.imageView.setBackground(ContextCompat.getDrawable(context, list.get(position).getBackground()));
        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, list.get(position).getIcon()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(View itemView, final Context context) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.icon);
            this.textView = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition()==0){
                        /*Intent intent = new Intent(context, ChatActivity.class);
                        context.startActivity(intent);*/
                    }else if (getAdapterPosition()==1){
                        Intent intent = new Intent(context, ChatActivity.class);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
