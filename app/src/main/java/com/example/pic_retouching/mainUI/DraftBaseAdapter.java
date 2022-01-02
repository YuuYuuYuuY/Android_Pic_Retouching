package com.example.pic_retouching.mainUI;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.pic_retouching.R;

import java.util.List;

public class DraftBaseAdapter extends BaseQuickAdapter<Bitmap, BaseViewHolder> {


    public DraftBaseAdapter(int layoutResId, @Nullable List<Bitmap> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Bitmap item) {
        helper.setImageBitmap(R.id.draft_image, item);
//        helper.addOnClickListener(R.id.draft_image);
    }
}
