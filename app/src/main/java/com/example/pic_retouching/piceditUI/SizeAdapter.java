package com.example.pic_retouching.piceditUI;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.pic_retouching.R;

import java.util.List;

public class SizeAdapter extends BaseQuickAdapter<Filter, BaseViewHolder> {
    private int selectPos = -1;
    public SizeAdapter(int layoutResId, @Nullable List<Filter> data) {
        super(layoutResId, data);
    }
    public void selectItem(int position){
        selectPos = position;
        notifyDataSetChanged();
    }// set selection according to the position
    @Override
    protected void convert(BaseViewHolder helper, Filter item) {
        helper.setImageResource(R.id.filter_image, item.getFilterID());
        helper.setText(R.id.filter_text, item.getName());

        if(selectPos == helper.getAdapterPosition()){
            helper.getView(R.id.filter_image).setSelected(true);
        }else {
            helper.getView(R.id.filter_image).setSelected(false);
        }
    }
}
