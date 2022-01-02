package com.example.pic_retouching.piceditUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pic_retouching.R;

import java.util.List;

public class NavbarAdapter extends RecyclerView.Adapter<NavbarAdapter.NavHolder> {
    private List<String> nav;
    private OnRecyclerItemClickListener itemClickListener;
    private int selectPos = -1;

    public void selectItem(int position){
        selectPos = position;
        notifyDataSetChanged();
    }// set selection


    public NavbarAdapter(List<String> list){
        nav = list;
    }

    @NonNull
    @Override
    public NavHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NavbarAdapter.NavHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.navbar_item, parent, false));
        // inflate the layout
    }

    @Override
    public void onBindViewHolder(@NonNull NavHolder holder, int position) {
        holder.textView.setText(nav.get(position));
        if(selectPos == position){
            holder.textView.setSelected(true);
        }else {
            holder.textView.setSelected(false);
        }
        // whether selected or not
    }

    @Override
    public int getItemCount() {
        return nav == null ? 0 : nav.size();
    }



    public class NavHolder extends RecyclerView.ViewHolder{
        // set up a ViewHolder called NavHolder
        private TextView textView;

        public NavHolder(@NonNull View itemView) {
            // constructor of the NavHolder
            super(itemView);
            textView = itemView.findViewById(R.id.navbar_item);
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

    public void setRecyclerItemClickListener(OnRecyclerItemClickListener listener){
        itemClickListener = listener;
    }// create a method to set up a RecyclerItemClickListener (be careful this is a method)

    public interface OnRecyclerItemClickListener{
        void onRecyclerItemClick(int position);
    }// create an interface
}
