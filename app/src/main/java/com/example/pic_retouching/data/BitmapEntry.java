package com.example.pic_retouching.data;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class BitmapEntry {
    // encapsulate the position, bitmap and byte[] in to BitmapEntry class
    private int position;
    private Bitmap bitmap;
    private byte[] byteArray;
    private Uri uri;

    public BitmapEntry(int position, Bitmap bitmap){
        this.position = position;
        this.bitmap = bitmap;
    }

    public BitmapEntry(int position, Uri uri){
        this.position = position;
        this.uri = uri;
    }

    public BitmapEntry(int position, byte[] byteArray){
        this.position = position;
        this.byteArray = byteArray;
    }

    public int getPosition() {
        return position;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    // getter
}
