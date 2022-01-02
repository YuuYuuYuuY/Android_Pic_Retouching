package com.example.pic_retouching.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class ImageUtils {

    public static void copyFileFromAsset(Context context, String assets_path, String new_path) {
        File father_path = new File(new File(new_path).getParent());
        if (!father_path.exists()) {
            father_path.mkdirs();
        }
        try {
            File new_file = new File(new_path);
            InputStream is_temp = context.getAssets().open(assets_path);
            if (new_file.exists() && new_file.isFile()) {
                if (contrastFileMD5(new_file, is_temp)) {
                    Log.d("ImageUtils", new_path + " is exists!");
                    return;
                } else {
                    Log.d("ImageUtils", "delete old model file!");
                    new_file.delete();
                }
            }
            InputStream is = context.getAssets().open(assets_path);
            FileOutputStream fos = new FileOutputStream(new_file);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            Log.d("ImageUtils", "the model file is copied");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get bin file's md5 string
    private static boolean contrastFileMD5(File new_file, InputStream assets_file) {
        MessageDigest new_file_digest, assets_file_digest;
        int len;
        try {
            byte[] buffer = new byte[1024];
            new_file_digest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(new_file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                new_file_digest.update(buffer, 0, len);
            }

            assets_file_digest = MessageDigest.getInstance("MD5");
            while ((len = assets_file.read(buffer, 0, 1024)) != -1) {
                assets_file_digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String new_file_md5 = new BigInteger(1, new_file_digest.digest()).toString(16);
        String assets_file_md5 = new BigInteger(1, assets_file_digest.digest()).toString(16);
        Log.d("new_file_md5", new_file_md5);
        Log.d("assets_file_md5", assets_file_md5);
        return new_file_md5.equals(assets_file_md5);
    }

    // get the image path by using the Uri from album
    public static String getPathFromURI(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // save the image to SDCard
    public static String saveBitmap(Bitmap bitmap) {
        String sdCardDir = Environment.getExternalStorageDirectory() + "/changedBackgroundImage/";
        try {
            File dirFile = new File(sdCardDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file = new File(sdCardDir, System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
