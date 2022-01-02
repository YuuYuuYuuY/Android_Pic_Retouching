package com.example.pic_retouching;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.pic_retouching.data.currentURI;
import com.example.pic_retouching.data.draftURI;

import com.example.pic_retouching.mainUI.BannerAdapterOrigin;
import com.example.pic_retouching.mainUI.DraftBaseAdapter;
import com.example.pic_retouching.mainUI.DraftPopupWindow;
import com.to.aboomy.pager2banner.Banner;
import com.to.aboomy.pager2banner.IndicatorView;
import com.to.aboomy.pager2banner.ScaleInTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.gemus.openingstartanimation.LineDrawStrategy;
import site.gemus.openingstartanimation.NormalDrawStrategy;
import site.gemus.openingstartanimation.OpeningStartAnimation;
import site.gemus.openingstartanimation.RotationDrawStrategy;

/*
 * Implements some open source libraries as below:
 *  Glide: https://github.com/bumptech/glide
 *  uCrop: https://github.com/Yalantis/uCrop
 *  BRVAN: https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 *  Banner: https://github.com/zguop/banner
 *  PopupWindow:
 */

public class MainActivity extends AppCompatActivity implements popObserver{

    private CardView start;
    private RecyclerView draft;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> paths;
    private String filePath;
    private currentURI handler;
    private draftURI draftURI;
    private DraftBaseAdapter baseAdapter;
    private Banner banner;
    private boolean first = true;
    private int num;
    private File dir;
    private int index = 0;
    private int removedNumber = 0;
    private BannerAdapterOrigin bannerAdapter;
    private ArrayList<DraftPopupWindow> popupWindowArrayList;
    private DraftPopupWindow popupWindow;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setDrawStategy(new RotationDrawStrategy())
                .setAppStatement("Make Editing image Easy")
                .setAppName("Edit pic")
                .setColorOfBackground(Color.YELLOW)
                .setAnimationInterval(2500)
                .setAnimationFinishTime(1000)
                .create();
        openingStartAnimation.show(this);

        bitmaps = new ArrayList<>();
        paths = new ArrayList<>();
        banner = findViewById(R.id.banner);

        sharedPreferences = getSharedPreferences("draft", MODE_PRIVATE);
        editor = sharedPreferences.edit();
//        editor.putInt("delete", 0);
//        editor.commit();
        // when testing, clear the delete value
        removedNumber = sharedPreferences.getInt("delete", removedNumber);
        // init sharedPreference and removedNumber

        dir = new File(getFilesDir().getAbsolutePath(), "draft");
        if(!dir.exists()){
            dir.mkdir();
        }
        // init draft dir

        draftURI = new draftURI(this);
        handler = new currentURI(this);

        draft = findViewById(R.id.draft);
        start = findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Edit.class);
                startActivity(intent);
            }
        });
        draft.setLayoutManager(new GridLayoutManager(this ,3));
        filePath = getFilesDir().getAbsolutePath();


        ArrayList<String> list = draftURI.queryEdit(1);
        Log.e("content", "add: "+ list.toString() );
        for (String i : list) {
            try {
                bitmaps.add(BitmapFactory.decodeStream(new FileInputStream(i)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        baseAdapter = new DraftBaseAdapter(R.layout.draft_item, bitmaps);
        draft.setAdapter(baseAdapter);

        popupWindowArrayList = new ArrayList<>();
        for(int i = 0; i < bitmaps.size(); i ++){
            popupWindowArrayList.add(new DraftPopupWindow(MainActivity.this, bitmaps.get(i)));
        }
        // initialize PopupWindow

        baseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                popupWindow = popupWindowArrayList.get(position);
                popupWindow.showPopupWindow();
                popupWindow.setPopupGravity(Gravity.CENTER);
                popupWindow.setOutSideTouchable(true);
                popupWindow.setObserver(MainActivity.this, position);
            }
        });


        ArrayList<Integer> advertisement =  new ArrayList<>();
        advertisement.add(R.drawable.adver1);
        bannerAdapter = new BannerAdapterOrigin(bitmaps);
        banner.setAdapter(bannerAdapter);


    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void update(int type, int position) {
        if(type == 1){
            // save the image to local
            saveImageToLocal(this,bitmaps.get(position));
            Toast.makeText(MainActivity.this, "Save to local", Toast.LENGTH_SHORT).show();
        }else if(type == 2){
            // delete the image from draft
            removeBitmap(position);
            draft.setAdapter(baseAdapter);
            popupWindowArrayList.clear();
            for(int i = 0; i < bitmaps.size(); i ++){
                popupWindowArrayList.add(new DraftPopupWindow(MainActivity.this, bitmaps.get(i)));
            }
        }

        popupWindow.dismiss();
    }

    public void saveImageToLocal(Context context, Bitmap bmp){
        // !!! there is an problem of wrong imgDir path
        File imgDir = new File(Environment.getExternalStorageDirectory(), "pic_retouching");
        Log.e("dir", "saveImageToLocal: " + getFilesDir().getAbsolutePath() );
        if(!imgDir.exists())
            imgDir.mkdir();
        // set the image directory

        String imgName = "pic_" + System.currentTimeMillis() + ".jpg";
        File img = new File(imgDir, imgName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(img);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }// init the image

        String imgPath = img.getAbsolutePath();
        Log.e("dir", "saveImageToLocal: " + Environment.getExternalStorageDirectory() );

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), imgPath, imgName, null);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }// insert the image into local

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(img);
        intent.setData(uri);
        context.sendBroadcast(intent);
        // broadcast

        Log.e("uri", "saveImageToLocal: " + imgDir);

    }


    @Override
    protected void onStart() {
        super.onStart();
        num = handler.queryEdit(1).size();

        Log.e("load", "onStart: do this step" + num);
        Log.e("load", "onStart: do this mappp" + bitmaps.size());
        if ( first == false) {
            try {
                Log.e("load", "do");
                getBitmapFromCurrent();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.e("load", "onStart: do this step");
        }
        first = false;
        popupWindowArrayList.clear();
        for(int i = 0; i < bitmaps.size(); i ++){
            popupWindowArrayList.add(new DraftPopupWindow(MainActivity.this, bitmaps.get(i)));
        }
//
    }

    public void getBitmapFromCurrent() throws FileNotFoundException {
        setInputIndex();
        // update the index
        ArrayList<String> paths = handler.queryEdit(1);
        if(paths.size() != 0) {
            for (String i : paths) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(i));
                bitmaps.add(bitmap);
                // add to UI (bitmaps)
                String imgName = "pic_" + index + ".jpg";
                File img = new File(dir, imgName);
                // create the new image in draft dir
                draftURI.insert(index, img.getAbsolutePath());
                // save the draft path in database
                FileOutputStream out = new FileOutputStream(img);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                // save the new image into the draft dir
                index++;
                // increment the index
                File clearFile = new File(i);
                clearFile.delete();
                // delete the current file from current directory

            }
        }
        handler.clearCurrent();
        // clear the current table
        baseAdapter.setNewData(bitmaps);
        Log.e("load", "getBitmapFromCurrent: " + bitmaps.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void removeBitmap(int position){
        int removedIndex = position + removedNumber;
        // get the removedIndex in draft table and draft dir
        String removedPath = draftURI.queryUri(removedIndex);
        if(removedPath != null) {
            File removedFile = new File(removedPath);
            Log.e("delete", "removePath " + removedPath);
            Log.e("delete", "removeBitmap: " + removedFile.getAbsolutePath());
            removedFile.delete();
            // delete the image from draft dir
            draftURI.delete(removedIndex);
            // delete from database
            bitmaps.remove(position);
            // delete form UI
        }
        removedNumber ++;
        editor.putInt("delete", removedNumber);
        editor.commit();
        Log.e("delete", "removeNum " + removedNumber);
        // preserve the removedNumber permanently
    }

    public void setInputIndex(){
        int count = baseAdapter.getItemCount();
        // get the current number of drafts
        index = count + removedNumber;
        // set the next input index;
    }

    public void updateOffset(int removedIndex){
        int num = bitmaps.size();
        for(int i = 0; i < num; i ++){
            int index = i;
            if(index >= removedIndex){
//                indexOffset.replace()
            }
        }
    }



}