package com.example.pic_retouching.mainUI;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.pic_retouching.R;
import com.example.pic_retouching.popObserver;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.TranslationConfig;

public class DraftPopupWindow extends BasePopupWindow{
    private ImageView imageView;
    public ImageButton save;
    public ImageButton delete;
    private Bitmap bitmap;
    private Integer type = 0;
    private popObserver observer;
    private int position;

    public DraftPopupWindow(Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
        this.setContentView(R.layout.popupwindow);
        imageView = findViewById(R.id.pop_image);
        imageView.setImageBitmap(bitmap);
        save = findViewById(R.id.pop_save);
        delete = findViewById(R.id.pop_delete);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = 1;
                observer.update(1, position);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = 2;
                observer.update(2, position);
            }
        });
    }

    public void setObserver(popObserver observer, int position) {
        this.observer = observer;
        this.position = position;
    }


    @Override
    protected Animator onCreateShowAnimator() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(getDisplayAnimateView(), View.TRANSLATION_Y, getHeight() * 0.75f, 0);
        showAnimator.setDuration(1000);
        showAnimator.setInterpolator(new OvershootInterpolator(6));
        return showAnimator;
    }// animation for creating

    @Override
    protected Animation onCreateDismissAnimation() {
        return AnimationHelper.asAnimation()
                .withTranslation(TranslationConfig.TO_BOTTOM)
                .toDismiss();
    }// animation for dismissing
}
