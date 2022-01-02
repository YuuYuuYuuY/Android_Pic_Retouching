package com.example.pic_retouching.videoeditUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pic_retouching.ImgOps;
import com.example.pic_retouching.R;

import java.util.HashMap;
import java.util.List;

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.ViewPagerViewHolder> {
    Context PicContext;
    HashMap<Integer, Bitmap> BitmapMap;

    public PicAdapter (HashMap<Integer, Bitmap> views, Context context){
        PicContext = context;
        BitmapMap = views;
    }

    @NonNull
    @Override
    public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewPagerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
        //holder.imgOps.setImageBitmap(BitmapList.get(position));
        holder.imageView.setImageBitmap(BitmapMap.get(position));
        //Log.e("uri", "onBindViewHolder: " + BitmapMap.get(position + 1));
    }

    @Override
    public int getItemCount() {
        //Log.e("uri", "onBindViewHolder: " + BitmapMap.size() );
        return BitmapMap == null ? 0 : BitmapMap.size();
        // if no items in BitmapList then return 0
        // else return the size of BitmapMaps
    }

    public void removeView(int position){
        if(BitmapMap.size() != 0 )
            BitmapMap.remove(position);
    }


    public class ViewPagerViewHolder extends RecyclerView.ViewHolder{
        // set up a ViewHolder called ViewPagerViewHolder
        private ImgOps imgOps;
        private ImageView imageView;

        public ViewPagerViewHolder(@NonNull View itemView) {
            // constructor of the ViewPagerViewHolder
            super(itemView);
            //imgOps = itemView.findViewById(R.id.image_item);
            imageView = itemView.findViewById(R.id.image_item);

        }// to actually find views and put views into the ViewHolder

    }
}
