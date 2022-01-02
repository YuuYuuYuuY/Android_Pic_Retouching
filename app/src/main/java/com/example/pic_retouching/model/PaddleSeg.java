package com.example.pic_retouching.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;
import com.baidu.paddle.lite.Tensor;

import java.io.File;
import java.util.Arrays;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class PaddleSeg {
    private PaddlePredictor predictor;
    private Tensor inputTensor;
    public static long[] inputShape = new long[]{1, 3, 513, 513};
    private static final int NUM_THREADS = 4;

    public long[] predictImage(Bitmap bitmap) throws Exception {
        return predict(bitmap);
    }

    public PaddleSeg(String modelPath) throws Exception {
        // config the PaddleSegmentation
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file is not exists!");
        }
        try {
            MobileConfig config = new MobileConfig();
            config.setModelFromFile(modelPath);
            config.setThreads(NUM_THREADS);
            config.setPowerMode(PowerMode.LITE_POWER_HIGH);
            predictor = PaddlePredictor.createPaddlePredictor(config);

            inputTensor = predictor.getInput(0);
            inputTensor.resize(inputShape);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("load model fail!");
        }
    }


    private long[] predict(Bitmap bmp) throws Exception {
        Log.e("model", "predict: do predict"  );
        float[] inputData = getScaledMatrix(bmp);
        inputTensor.setData(inputData);

        try {
            predictor.run();
        } catch (Exception e) {
            throw new Exception("predict image fail! log:" + e);
        }
        Tensor outputTensor = predictor.getOutput(0);
        long[] output = outputTensor.getLongData();
        long[] outputShape = outputTensor.shape();
        Log.d("model", "shape："+ Arrays.toString(outputShape));
        return output;
    }

    private float[] getScaledMatrix(Bitmap bitmap) {
        int channels = (int) inputShape[1];
        int width = (int) inputShape[2];
        int height = (int) inputShape[3];
        float[] inputData = new float[channels * width * height];
        Bitmap rgbaImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap scaleImage = Bitmap.createScaledBitmap(rgbaImage, width, height, true);
        Log.d("result", scaleImage.getWidth() +  ", " + scaleImage.getHeight());

        if (channels == 3) {
            // RGB = {0, 1, 2}, BGR = {2, 1, 0}
            int[] channelIdx = new int[]{0, 1, 2};
            int[] channelStride = new int[]{width * height, width * height * 2};
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = scaleImage.getPixel(x, y);
                    float[] rgb = new float[]{(float) red(color), (float) green(color), (float) blue(color)};
                    inputData[y * width + x] = rgb[channelIdx[0]];
                    inputData[y * width + x + channelStride[0]] = rgb[channelIdx[1]];
                    inputData[y * width + x + channelStride[1]] = rgb[channelIdx[2]];
                }
            }
        } else if (channels == 1) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = scaleImage.getPixel(x, y);
                    float gray = (float) (red(color) + green(color) + blue(color));
                    inputData[y * width + x] = gray;
                }
            }
        } else {
            Log.e("result", "The channel of the image should be 1 or 3");
        }
        return inputData;
    }


}
