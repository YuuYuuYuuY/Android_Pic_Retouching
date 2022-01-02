package com.example.pic_retouching.piceditUI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.pic_retouching.R;

import java.util.List;

public class CardAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {
    public CardAdapter(int layoutResId, @Nullable List<Integer> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Integer item) {
        //helper.setImageResource(R.id.card, item);
    }

}
