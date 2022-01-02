package com.example.pic_retouching.mainUI;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;

import android.gesture.GestureLibraries;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.pic_retouching.R;
import com.example.pic_retouching.piceditUI.NavbarAdapter;


import java.util.ArrayList;
import java.util.List;


public class BannerAdapterOrigin extends RecyclerView.Adapter<BannerAdapterOrigin.BannerHolder> {

    private List<Bitmap> nav;
    private BannerAdapterOrigin.OnRecyclerItemClickListener itemClickListener;
    private int selectPos = -1;

    public void selectItem(int position){
        selectPos = position;
        notifyDataSetChanged();
    }// set selection


    public BannerAdapterOrigin(List<Bitmap> list){
        nav = list;
    }

    @NonNull
    @Override
    public BannerAdapterOrigin.BannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BannerAdapterOrigin.BannerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.base_item, parent, false));
        // inflate the layout
    }

    @Override
    public void onBindViewHolder(@NonNull BannerAdapterOrigin.BannerHolder holder, int position) {
        holder.imageView.setImageBitmap(nav.get(position));
        if(selectPos == position){
            holder.imageView.setSelected(true);
        }else {
            holder.imageView.setSelected(false);
        }
        // whether selected or not
    }

    @Override
    public int getItemCount() {
        return nav == null ? 0 : nav.size();
    }



    public class BannerHolder extends RecyclerView.ViewHolder{
        // set up a ViewHolder called NavHolder
        private ImageView imageView;

        public BannerHolder(@NonNull View itemView) {
            // constructor of the NavHolder
            super(itemView);
            imageView = itemView.findViewById(R.id.baseImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        itemClickListener.onRecyclerItemClick(getAdapterPosition());
                        // call method onRecyclerItemClick
                    }
                }
            });

        }// to actually find views and put views into the ViewHolder

    }

    public void setRecyclerItemClickListener(BannerAdapterOrigin.OnRecyclerItemClickListener listener){
        itemClickListener = listener;
    }// create a method to set up a RecyclerItemClickListener (be careful this is a method)

    public interface OnRecyclerItemClickListener{
        void onRecyclerItemClick(int position);
    }// create an interface
}
