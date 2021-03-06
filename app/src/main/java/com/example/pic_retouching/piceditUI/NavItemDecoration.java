package com.example.pic_retouching.piceditUI;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NavItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    public NavItemDecoration(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(parent.getChildAdapterPosition(view) != 0) {
            outRect.left = space;
            outRect.right = space;
        }
        // set the space between each item in the recyclerView

    }
}
