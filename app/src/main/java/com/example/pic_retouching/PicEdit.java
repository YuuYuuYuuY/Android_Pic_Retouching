package com.example.pic_retouching;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.pic_retouching.data.BitmapEntry;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pic_retouching.data.BitmapEntry;
import com.example.pic_retouching.data.currentURI;
import com.example.pic_retouching.model.EmailSharing;
import com.example.pic_retouching.model.ImageUtils;
import com.example.pic_retouching.model.PaddleSeg;

import com.example.pic_retouching.piceditUI.Filter;
import com.example.pic_retouching.piceditUI.NavItemDecoration;
import com.example.pic_retouching.piceditUI.NavbarAdapter;
import com.example.pic_retouching.piceditUI.RenderBitmap;
import com.example.pic_retouching.piceditUI.SizeAdapter;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;

/*
 * Implements some open source libraries as below:
 *  Glide: https://github.com/bumptech/glide
 *  uCrop: https://github.com/Yalantis/uCrop
 *  BRVAN: https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 *  Banner: https://github.com/zguop/banner
 */
public class PicEdit extends AppCompatActivity {

    private ImgOps currentImg;
    private Bitmap currentBitmap;
    private Bitmap updatedBitmap;
    private Uri currentUri;
    private Uri updatedUri;
    private String updatedPath;
    private String currentPath;
    private Integer currentIndex;
    private RecyclerView navbar;
    private RecyclerView filter;
    private NavbarAdapter navbarAdapter;
    private SizeAdapter sizeAdapter;
    private List<String> types = new ArrayList<>();
    private List<Filter> filters = new ArrayList<>();
    private List<Filter> sizes = new ArrayList<>();
    private List<Filter> transforms = new ArrayList<>();
    private List<Integer> cards = new ArrayList<>();
    private float[] mColorMatrix = new float[20];
    private currentURI handleURI;
    private boolean isChange = false;
    private ImageButton clear;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int index = 0;
    private String selectType;
    private PaddleSeg paddleSeg;
    private RenderBitmap renderBitmap;
    private Bitmap resultPicture;
    private Bitmap humanPicture;
    private Bitmap changeBackgroundPicture;
    private Bitmap mergeBitmap;
    private Handler inputImageHandler;
    private HandlerThread inputImageThread;


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_edit);

        renderBitmap = new RenderBitmap();
        sharedPreferences = getSharedPreferences("currentImage", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentImg = findViewById(R.id.currentImg);
        // init ImgOps
        selectType = "";
        // init type

        Intent intent = getIntent();
        currentPath = intent.getStringExtra("uri");
        currentIndex = intent.getIntExtra("position", 0);
        currentUri = Uri.parse(currentPath);
        // get path and uri

        handleURI = new currentURI(this);
        // init draft

        clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImg.setImageBitmap(currentBitmap);
                updatedBitmap = currentBitmap;
            }
        });

        final Object[] obj = handleURI.query(currentIndex);
        try {
            if((Integer) obj[1] != 0) {
                FileInputStream inputStream = new FileInputStream(currentPath);
                currentBitmap = BitmapFactory.decodeStream(inputStream);
            }else {
                currentBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(currentUri));
            }
            Glide.with(this).load(currentBitmap).into(currentImg);
            //currentImg.setImageBitmap(currentBitmap);
            // set image on screen
            updatedBitmap = currentBitmap;
            Log.e("get", "onCreate: " + currentBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // to check if the current bitmap has been changed or not
        // if already change, get it from database
        types.add("background");
        types.add("filter");
        types.add("size");
        types.add("render");
//        types.add("background");
        // init types

        float[] filter1 = new float[]{0.393f, 0.769f, 0.189f, 0, 0,
                                    0.349f, 0.686f, 0.168f, 0, 0,
                                    0.272f, 0.534f, 0.131f, 0, 0,
                                    0, 0, 0, 1f, 0};
        float[] filter2 = new float[]{0.33f, 0.59f, 0.11f, 0, 0,
                                    0.33f, 0.59f, 0.11f, 0, 0,
                                    0.33f, 0.59f, 0.11f, 0, 0,
                                    0, 0, 0, 1f, 0};
        float[] filter3 = new float[]{1.438f, -0.122f, -0.016f, 0, -0.03f,
                                    -0.062f, 1.378f, -0.016f, 0, 0.05f,
                                    -0.062f, -0.122f, 1.483f, 0, -0.02f,
                                    0, 0, 0, 1f, 0};
        float[] filter4 = new float[]{-1, 0, 0, 1, 1,
                                    0, -1, 0, 1, 1,
                                    0, 0, -1, 1, 1,
                                    0, 0, 0, 1, 0};
        float[] filter5 = new float[]{1.5f, 1.5f, 1.5f, 0, -1,
                                    1.5f, 1.5f, 1.5f, 0, -1,
                                    1.5f, 1.5f, 1.5f, 0, -1,
                                    0, 0, 0, 1, 0};
        filters.add(new Filter("reminiscence", R.drawable.example, filter1));
        filters.add(new Filter("grey", R.drawable.example, filter2));
        filters.add(new Filter("warm", R.drawable.example, filter3));
        filters.add(new Filter("inversion", R.drawable.example, filter4));
        filters.add(new Filter("discoloration", R.drawable.example, filter5));
        // init filters

        sizes.add(new Filter("16 : 9", R.drawable.landscape));
        sizes.add(new Filter("1 : 1", R.drawable.landscape));
        sizes.add(new Filter("3 : 2", R.drawable.landscape));
        sizes.add(new Filter("4 : 3", R.drawable.landscape));
        sizes.add(new Filter("9 : 16", R.drawable.landscape));
        sizes.add(new Filter("2 : 3", R.drawable.landscape));
        sizes.add(new Filter("3 : 4", R.drawable.landscape));
        // init size

        transforms.add(new Filter("Vignette", R.drawable.render));
        transforms.add(new Filter("Toon", R.drawable.render));
        transforms.add(new Filter("Swirl", R.drawable.render));
        transforms.add(new Filter("Sketch", R.drawable.render));
        transforms.add(new Filter("Sepia", R.drawable.render));
        transforms.add(new Filter("Pixelation", R.drawable.render));
        transforms.add(new Filter("Kuwahara", R.drawable.render));
        // init transform

        cards.add(R.drawable.adver1);


        navbar = findViewById(R.id.navbar_recycler);
        filter = findViewById(R.id.filter_recycler);
        navbarAdapter = new NavbarAdapter(types);
        sizeAdapter = new SizeAdapter(R.layout.filter_item, filters);
        filter.setVisibility(View.INVISIBLE);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        gridLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        navbar.addItemDecoration(new NavItemDecoration(0));
        navbar.setLayoutManager(gridLayoutManager);
        navbar.setAdapter(navbarAdapter);
        navbarAdapter.setRecyclerItemClickListener(new NavbarAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerItemClick(int position) {
                navbarAdapter.selectItem(position);
                selectType(position);
                // determine which one to show
            }
        });
        // set the layout and the listener of navbar


        GridLayoutManager filterLayoutManager = new GridLayoutManager(this, 1);
        filterLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        filter.addItemDecoration(new NavItemDecoration(0));
        filter.setLayoutManager(filterLayoutManager);
        filter.setAdapter(sizeAdapter);
        sizeAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                sizeAdapter.selectItem(position);
                isChange = true;
                String[] type = sizes.get(position).getName().split(":");
                switch (selectType){
                    case "filter":
                        updatedBitmap = currentBitmap;
                        updatedBitmap = setImageMatrix(filters.get(position).getColorMatrix());
                        currentImg.setImageBitmap(updatedBitmap);
                        try {
                            saveBitmapToCache(currentBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "crop":
                        try {
                            cropConfig(Integer.parseInt(type[0].trim()), Integer.parseInt(type[1].trim()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    case "render":
                        Glide.with(PicEdit.this).asBitmap()
                                .load(updatedBitmap)
                                .apply(renderBitmap.getRequestOptions(position))
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        currentImg.setImageBitmap(resource);
                                        updatedBitmap = resource;
                                    }
                                });
                        break;
                }
            }
        });

        // set the layout and the listener of filter bar
        String segmentationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model.nb";
        ImageUtils.copyFileFromAsset(PicEdit.this, "model.nb", segmentationModelPath);
        try {
            paddleSeg = new PaddleSeg(segmentationModelPath);
            Toast.makeText(PicEdit.this, "model load success！", Toast.LENGTH_SHORT).show();
            Log.d("model", "load success！");
        } catch (Exception e) {
            Toast.makeText(PicEdit.this, "model load fail！", Toast.LENGTH_SHORT).show();
            Log.d("model", "load fail！");
            e.printStackTrace();
            finish();
        }

        inputImageThread = new HandlerThread("inputImage");
        inputImageThread.start();

        inputImageHandler = new Handler(inputImageThread.getLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        try {
                            // merge two pictures
                            Uri uri = (Uri) msg.obj;
                            Log.e("model", "onActivityResult: " + uri );
                            changeBackgroundPicture = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                            mergeBitmap = draw();
                            updatedBitmap = mergeBitmap;
                            isChange = true;
                            currentImg.setImageBitmap(updatedBitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;

                }

            }
        };


    }

    protected void getBackgroundImage(){
        // intent to album
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityIfNeeded(intent, 2);
    }

    public Bitmap setImageMatrix(float[] para){
        Bitmap bmp = Bitmap.createBitmap(updatedBitmap.getWidth(), updatedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(para);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(updatedBitmap, 0, 0, paint);

        Log.e("Bitmap", "onClick: " + updatedBitmap);
        return bmp;
    }// return the bitmap that has set the filter


    public void saveBitmap(Bitmap bitmap){
        File dir = new File(getFilesDir().getAbsolutePath(), "current");
        if(!dir.exists())
            dir.mkdir();
        String imgName = "pic_" + System.currentTimeMillis() + ".jpg";
        File img = new File(dir, imgName);
        updatedPath = img.getAbsolutePath();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.e("dir", "saveBitmap: success");
        } catch (Exception e) {
            e.printStackTrace();
        }// save the image to file directory

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onStop() {
        // update the database
        if(isChange) {
            editor.putInt("edit_index", currentIndex);
            editor.putBoolean("isEdit", true);
            editor.commit();
            // update the database
            saveBitmap(updatedBitmap);
            if(handleURI.query(currentIndex) != null) {
                handleURI.update(currentIndex, updatedPath, 1);
                Toast.makeText(PicEdit.this, "updated", Toast.LENGTH_SHORT).show();
            }else {
                handleURI.insertAll(currentIndex, updatedPath, 1);
            }
        }
        super.onStop();
    }
    // when this activity stops, the database is updated

    private void selectType(int position){
        switch (position){
            case 0:
                filter.setVisibility(View.INVISIBLE);
                try{
                    readyForPredict();
                }catch (Exception e){
                    e.printStackTrace();
                }
                getBackgroundImage();
                break;
            case 1:
                selectType = "filter";
                filter.setVisibility(View.VISIBLE);
                filter.setAdapter(sizeAdapter);
                sizeAdapter.setNewData(filters);
                break;
            case 2:
                selectType = "crop";
                filter.setVisibility(View.VISIBLE);
                filter.setAdapter(sizeAdapter);
                sizeAdapter.setNewData(sizes);
                break;
            case 3:
                selectType = "render";
                filter.setVisibility(View.VISIBLE);
                filter.setAdapter(sizeAdapter);
                sizeAdapter.setNewData(transforms);
                break;

        }
        // set the visibilities of different types
    }

    public void saveBitmapToCache(Bitmap bitmap) throws IOException {
        File file = new File(getCacheDir(), "current.jpeg");
        if(!file.exists())
            file.createNewFile();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }// save the image to cache directory

    }

    public void cropConfig(int aspectRationX, int aspectRationY) throws IOException {
        updatedUri = Uri.fromFile(new File(getCacheDir(), "cache.jpeg"));
        //saveBitmapToCache(currentBitmap);
        UCrop ucrop = UCrop.of(currentUri, updatedUri);
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setFreeStyleCropEnabled(true);
        ucrop.withAspectRatio(aspectRationX, aspectRationY);
        ucrop.withMaxResultSize(1080, 1920);
        ucrop.start(PicEdit.this);
    }// ucrop are used



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // get the result from UCropActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == UCrop.REQUEST_CROP){
                final Uri resultUri = UCrop.getOutput(data);
                currentImg.setImageURI(resultUri);
                updatedPath = String.valueOf(resultUri);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    updatedBitmap = bitmap;
                    saveBitmapToCache(bitmap);
                    File file = new File(getCacheDir(), "current.jpeg");
                    currentUri = Uri.parse(file.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                // get the updatedBitmap
                Log.e("result", "onActivityResult: " + resultUri );
            }
            if(requestCode == 2){
                Log.e("model", "onActivityResult: " + data );
                if(data != null){
                    Uri uri = data.getData();

                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = uri;
                    inputImageHandler.sendMessage(msg);
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

    }

    public void readyForPredict() throws Exception {
        Log.e("model", "readyForPredict: " + "ready" );
        long start = System.currentTimeMillis();
        long[] result = paddleSeg.predictImage(updatedBitmap);
        long end = System.currentTimeMillis();

        humanPicture = updatedBitmap.copy(Bitmap.Config.ARGB_8888, true);
        final int[] colors_map = {0x00000000, 0xFF000000};
        int[] objectColor = new int[result.length];

        for (int i = 0; i < result.length; i++) {
            objectColor[i] = colors_map[(int) result[i]];
        }

        Bitmap.Config config = humanPicture.getConfig();
        Bitmap outputImage = Bitmap.createBitmap(objectColor, (int) PaddleSeg.inputShape[2], (int) PaddleSeg.inputShape[3], config);
        resultPicture = Bitmap.createScaledBitmap(outputImage, humanPicture.getWidth(), humanPicture.getHeight(), true);
        Log.e("model", "load tima: " + (end - start) + "ms" );
    }

    public Bitmap draw() {
        // Create a normal background image that is transparent to the position of the character
        Bitmap bgBitmap = Bitmap.createScaledBitmap(changeBackgroundPicture, resultPicture.getWidth(), resultPicture.getHeight(), true);
        for (int y = 0; y < resultPicture.getHeight(); y++) {
            for (int x = 0; x < resultPicture.getWidth(); x++) {
                int color = resultPicture.getPixel(x, y);
                int a = Color.alpha(color);
                if (a == 255) {
                    bgBitmap.setPixel(x, y, Color.TRANSPARENT);
                }
            }
        }

        // add canvas to make it transparent
        Bitmap bgBitmap2 = Bitmap.createBitmap(bgBitmap.getWidth(), bgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(bgBitmap2);
        canvas1.drawBitmap(bgBitmap, 0, 0, null);

        return mergeBitmap(humanPicture, bgBitmap2);
    }

    // merge two images
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }




}