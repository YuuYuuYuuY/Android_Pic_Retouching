package com.example.pic_retouching.piceditUI;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.ArrayMap;

import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.KuwaharaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SwirlFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;


public class RenderBitmap {
    private List<RequestOptions> optionsArrayMap;

    public RenderBitmap(){
        optionsArrayMap = new ArrayList<>();
        setValue();
    }


    private void setValue(){
        RequestOptions requestOptions0 = RequestOptions.bitmapTransform(new VignetteFilterTransformation());
        RequestOptions requestOptions1 = RequestOptions.bitmapTransform(new ToonFilterTransformation());
        RequestOptions requestOptions2 = RequestOptions.bitmapTransform(new SwirlFilterTransformation());
        RequestOptions requestOptions3 = RequestOptions.bitmapTransform(new SketchFilterTransformation());
        RequestOptions requestOptions4 = RequestOptions.bitmapTransform(new SepiaFilterTransformation());
        RequestOptions requestOptions5 = RequestOptions.bitmapTransform(new PixelationFilterTransformation());
        RequestOptions requestOptions6 = RequestOptions.bitmapTransform(new KuwaharaFilterTransformation());
        optionsArrayMap.add(requestOptions0);
        optionsArrayMap.add(requestOptions1);
        optionsArrayMap.add(requestOptions2);
        optionsArrayMap.add(requestOptions3);
        optionsArrayMap.add(requestOptions4);
        optionsArrayMap.add(requestOptions5);
        optionsArrayMap.add(requestOptions6);
    }

    public RequestOptions getRequestOptions(int index){
        return optionsArrayMap.get(index);
    }
}
