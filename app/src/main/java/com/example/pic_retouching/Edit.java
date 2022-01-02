package com.example.pic_retouching;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.example.pic_retouching.data.BitmapEntry;
import com.example.pic_retouching.data.currentURI;
import com.example.pic_retouching.model.EmailSharing;
import com.example.pic_retouching.videoeditUI.PicAdapter;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;



public class Edit extends AppCompatActivity {
    private Toolbar toolbar;
    private boolean is_fixed = false;
    private Uri uri;
    private Bitmap photo;
    private HashMap<Integer, Uri> BitmapUri;
    private HashMap<Integer, Bitmap> BitmapMaps;
    private Integer index = 0;
    private PicAdapter picAdapter;
    private ViewPager2 picViews;
    private Bitmap currentBitmap;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Handler inputImageHandler;
    private HandlerThread inputImageThread;
    private boolean isAdded = false;
    private Uri updatedUri;
    private ImageButton send;
    private EmailSharing emailSharing;
    private EditText from;
    private EditText to;
    private EditText content;
    private EditText subject;
    private SharedPreferences record;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

//        bottomBar = findViewById(R.id.expandable_bottom_bar);
        picViews = findViewById(R.id.picViewPager);
        toolbar = (Toolbar) findViewById(R.id.toolbar_edit);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // initialize toolbar and add NavigationOnclickListener

        BitmapUri = new HashMap<>();
        BitmapMaps = new HashMap<>();
        picAdapter = new PicAdapter(BitmapMaps, this);
        // initialize BitmapMaps and picAdapter

        sharedPreferences = getSharedPreferences("currentImage", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        send = findViewById(R.id.send);
        from = findViewById(R.id.edit_from);
        to = findViewById(R.id.edit_to);
        subject = findViewById(R.id.edit_subject);
        content = findViewById(R.id.content);
        record = getSharedPreferences("record", MODE_PRIVATE);
        from.setText(record.getString("from", ""));
        to.setText(record.getString("to", ""));
        subject.setText(record.getString("subject", ""));
        content.setText(record.getString("content", ""));

        currentURI currentURI = new currentURI(this);
        currentURI.clearCurrent();
        //Log.e("", "onCreate: " + currentURI.queryEdit(0).toString() + currentURI.queryEdit(1).toString() );
        // clear the currentURI table

        // create and start a thread by using HandlerThread
        inputImageThread = new HandlerThread("inputImage");
        inputImageThread.start();

        inputImageHandler = new Handler(inputImageThread.getLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        // get 1 and save the selected image into CURRENT table
                        try {
                            BitmapEntry bitmapEntry = (BitmapEntry) msg.obj;
                            saveToCurrent(bitmapEntry.getPosition(), bitmapEntry.getUri());
//                            Log.e("debug", "thread" + bitmapEntry.getPosition());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        // get 2 and save the image to local gallery
                        if (isAdded) {
                            saveImageToLocal(Edit.this, BitmapMaps.get(msg.obj));
                        }
                        break;
                    case 3:
                        String str_from = String.valueOf(from.getText());
                        String str_to = String.valueOf(to.getText());
                        String str_content = String.valueOf(content.getText());
                        String str_subject = String.valueOf(subject.getText());
                        try {
                            emailSharing = new EmailSharing(str_from, str_to, str_content, str_subject, BitmapMaps.get(picViews.getCurrentItem()));
                        } catch (AddressException e) {
                            e.printStackTrace();
                        }
                        try {
                            Log.e("email", "handleMessage: " + str_from);
                            boolean success = emailSharing.sendEmail();
                            if (success) {
                                Toast.makeText(Edit.this, "Sent Accomplished", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Edit.this, "Sent fail", Toast.LENGTH_SHORT).show();
                            }

                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        break;

                }

            }
        };
        // the I/O and bitmap compress operations can be put into thread
        // so that the duration of UI response decrease significantly


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = getSharedPreferences("record", MODE_PRIVATE);
                SharedPreferences.Editor record_editor = record.edit();
                record_editor.putString("from", String.valueOf(from.getText()));
                record_editor.putString("to", String.valueOf(to.getText()));
                record_editor.putString("subject", String.valueOf(subject.getText()));
                record_editor.putString("content", String.valueOf(content.getText()));
                record_editor.commit();
                Message msg = Message.obtain();
                msg.what = 3;
                inputImageHandler.sendMessage(msg);


            }
        });
    }


    protected void getImageFromAlbum() {
        // intent to album
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityIfNeeded(intent, 1);
        picViews.setUserInputEnabled(false);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // get the image form album
        super.onActivityResult(requestCode, resultCode, data);
        picViews.setUserInputEnabled(true);
        if (requestCode == 1) {
            // return data from album
            if (data != null) {
                // get path of image
                uri = data.getData();
                try {
                    photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                BitmapUri.put(index, uri);
                BitmapMaps.put(index, photo);
                // add values into BitmapMaps
                picViews.setAdapter(picAdapter);
                // set adapter to show in UI
//                Log.e("debug", "onActivityResult: has " + index + " " + photo);

                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = new BitmapEntry(index, uri);
                inputImageHandler.sendMessage(msg);
                // create a new message when an image is added and execute in thread
                // add the picture to the bottom of the current table (thread does this operation)
                isAdded = true;
                index++;
                // be careful the index is added by one at last
            }
            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }// inflate the toolbar by using R.menu.toolbar

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                // click save
                Message msg = Message.obtain();
                msg.what = 2;
                msg.obj = picViews.getCurrentItem();
                inputImageHandler.sendMessage(msg);
                break;
            case R.id.album:
                // click album to call the method
                getImageFromAlbum();
                break;
            case R.id.picedit:
                // click edit to enter editing picture page
                if (isAdded) {
                    // if already added pictures
                    Intent intent = new Intent(Edit.this, PicEdit.class);
                    intent.putExtra("position", picViews.getCurrentItem());
                    intent.putExtra("uri", BitmapUri.get(picViews.getCurrentItem()).toString());
                    Log.e("dir", "intent: " + BitmapUri.get(picViews.getCurrentItem()));
                    // transmit position and image uri into next activity
                    startActivity(intent);

                } else {
                    Toast.makeText(Edit.this, "No pictures to be edited, pleas add some pictures", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.synchronize:
                // update the viewpager2
                if (isAdded) {
                    try {
                        updateBitmapMaps();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(Edit.this, "No pictures to be edited, pleas add some pictures", Toast.LENGTH_SHORT).show();

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }//


    @RequiresApi(api = Build.VERSION_CODES.P)
    public void saveToCurrent(int position, Uri uri) throws IOException {
        String path = uri.toString();
        currentURI handler = new currentURI(this);
        if (handler.query(position) != null) {
            handler.update(position, path, 0);
        } else {
            handler.insert(position, path);
        }
    }


    public void saveImageToLocal(Context context, Bitmap bmp) {
        // !!! there is an problem of wrong imgDir path
        File imgDir = new File(Environment.getExternalStorageDirectory(), "pic_retouching");
        Log.e("dir", "saveImageToLocal: " + getFilesDir().getAbsolutePath());
        if (!imgDir.exists())
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
        Log.e("dir", "saveImageToLocal: " + Environment.getExternalStorageDirectory());

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), imgPath, imgName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }// insert the image into local

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(img);
        intent.setData(uri);
        context.sendBroadcast(intent);
        // broadcast

        Log.e("uri", "saveImageToLocal: " + imgDir);
        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void updateBitmapMaps() throws FileNotFoundException {
        currentURI handler = new currentURI(this);
        boolean isEdit = sharedPreferences.getBoolean("isEdit", false);
        int edit_index = sharedPreferences.getInt("edit_index", 0);
        String path = (String) handler.query(edit_index)[0];
        Uri uri = Uri.parse(path);
        Log.e("dir", "updateBitmapMaps: " + uri);
        //if(isEdit == true){

        FileInputStream inputStream = new FileInputStream(path);
        BitmapUri.replace(edit_index, uri);
        Log.e("dir", "do this step 1");
        Bitmap temp = BitmapFactory.decodeStream(inputStream);
        // cannot use getContentResolver().openInputStream();

        Log.e("dir", "do this step 2");
        BitmapMaps.replace(edit_index, temp);
        Log.e("dir", "updateBitmapMaps: " + BitmapMaps.get(edit_index));
        picViews.setAdapter(picAdapter);
        Toast.makeText(Edit.this, "Successfully synchronized", Toast.LENGTH_SHORT).show();

        // update the Bitmap and setAdapter
        //}
    }





}