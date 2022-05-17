package com.example.opencv4android.imgproc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.opencv4android.R;
import com.example.opencv4android.databinding.ActivityHelloBinding;
import com.example.opencv4android.natives.DetectionBasedTracker;
import com.example.opencv4android.utils.Utils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;


/**
 * 第一个OpenCV例子
 * Created by jiangdongguo on 2017/12/29.
 */

public class ImgProcessActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    private static final String TAG = "ImgProcessActivity";
    private ActivityHelloBinding binding;
    private Mat mRgba;
    private int mWidth;
    private int mHeight;

    private Uri fileUri;


    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // OpenCV引擎加载成功，渲染Camera数据
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully.");
//                    binding.HelloOpenCvView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityHelloBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        binding.HelloOpenCvView.setVisibility(SurfaceView.VISIBLE);
//        binding.HelloOpenCvView.setCvCameraViewListener(this);
        binding.btnSelectImage.setOnClickListener(this);
        binding.btnReadWritePix.setOnClickListener(this);
        binding.btnSplitChannel.setOnClickListener(this);
        binding.btnMergeChannel.setOnClickListener(this);
        binding.btnBgZeroChannel.setOnClickListener(this);
        binding.btnBrZeroChannel.setOnClickListener(this);
        binding.btnGrZeroChannel.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 静态加载
        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "static loading library fail,Using Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.w(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 释放Camera资源
//        if (binding.HelloOpenCvView != null) {
//            binding.HelloOpenCvView.disableView();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放Camera资源
//        if (binding.HelloOpenCvView != null) {
//            binding.HelloOpenCvView.disableView();
//        }
        channelB.release();
        channelG.release();
        channelR.release();
        mRgba.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mWidth = width;
        mHeight = height;
        // 创建一个Mat数据对象
        mRgba = new Mat(width, height, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // 获取当前一帧图像的内存地址
        long address = mRgba.getNativeObjAddr();
        // 对图像进行处理
        DetectionBasedTracker.nativeRgba(address, mWidth, mHeight);
        // 处理后，屏幕回显
        return mRgba;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select_image:
                Utils.pickUpImage(this);
                break;
            case R.id.btn_read_write_pix:
//                readAndWritePixelOneByOne();
//                readAndWritePixelOneRowByOneRow();
                readAndWritePixelAll();
                break;
            case R.id.btn_split_channel:
                splitChannel();
                break;
            case R.id.btn_merge_channel:
                mergeChannel();
                break;
            case R.id.btn_bgZero_channel:
                showBGZero();
                break;
            case R.id.btn_brZero_channel:
                showBRZero();
                break;
            case R.id.btn_grZero_channel:
                showGRZero();
                break;
        }
    }

    /**
     * 获取图片通道，宽，高
     */
    private Triple<Integer, Integer, Integer> getChannelAndWidthAndHeight() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if (src == null) return null;
        int channels = src.channels();
        int width = src.cols();
        int height = src.rows();
        int depth = src.depth();
        Log.d(TAG, "channels:" + channels + ",width:" + width + ",height:" + height + ",depth:" + depth);
        return new Triple<>(channels, width, height);
    }


    /**
     * 一个一个像素读取,适用于随机少量像素读写
     * 频繁访问jni，效率低,内存需求小
     */
    private void readAndWritePixelOneByOne() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        int channels = src.channels();
        int width = src.cols();
        int height = src.rows();
        int depth = src.depth();
        Log.d(TAG, "channels:" + channels + ",width:" + width + ",height:" + height + ",depth:" + depth);

        byte[] data = new byte[channels];
        int b = 0, g = 0, r = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                //读取
                src.get(row, col, data);
                b = data[0] & 0xff;
                g = data[1] & 0xff;
                r = data[2] & 0xff;
                //修改
                b = 255 - b;
                g = 255 - g;
                r = 255 - r;
                //写入
                data[0] = (byte) b;
                data[1] = (byte) g;
                data[2] = (byte) r;

                src.put(row, col, data);
            }
        }

        Bitmap target = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2BGRA);
        org.opencv.android.Utils.matToBitmap(dst, target);
        binding.ivTarget.setImageBitmap(target);
    }

    /**
     * 逐行读取像素
     */
    private void readAndWritePixelOneRowByOneRow() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        int channels = src.channels();
        int width = src.cols();
        int height = src.rows();
        int depth = src.depth();
        Log.d(TAG, "channels:" + channels + ",width:" + width + ",height:" + height + ",depth:" + depth);
        byte[] data = new byte[channels * width];
        int b = 0, g = 0, r = 0;
        int pv = 0;
        for (int row = 0; row < height; row++) {
            src.get(row, 0, data);
            for (int col = 0; col < data.length; col++) {
                pv = data[col] & 0xff;
                pv = 255 - pv;
                data[col] = (byte) pv;
            }
            src.put(row, 0, data);
        }

        Bitmap target = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2BGRA);
        org.opencv.android.Utils.matToBitmap(dst, target);
        binding.ivTarget.setImageBitmap(target);
    }

    /**
     * 一次读取，效率最快，但是内存消耗高，容易oom
     */
    private void readAndWritePixelAll() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        int channels = src.channels();
        int width = src.cols();
        int height = src.rows();
        int depth = src.depth();
        Log.d(TAG, "channels:" + channels + ",width:" + width + ",height:" + height + ",depth:" + depth);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();

        Core.meanStdDev(src,mean,stdDev);

        Log.d(TAG,"均值mean:"+mean.toList()+",标准方差stdDev:"+stdDev.toList());

        int pv = 0;
        byte[] data = new byte[channels * width * height];
        src.get(0, 0, data);
        for (int i = 0; i < data.length; i++) {
            pv = data[i] & 0xff;
            pv = 255 - pv;
            data[i] = (byte) pv;
        }
        src.put(0, 0, data);


        Bitmap target = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2BGRA);
        org.opencv.android.Utils.matToBitmap(dst, target);
        binding.ivTarget.setImageBitmap(target);

    }

    /**
     * 通道分离
     */
    private Mat channelB;
    private Mat channelG;
    private Mat channelR;

    private void splitChannel() {
        channelB = new Mat();
        channelG = new Mat();
        channelR = new Mat();
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if (src.empty()) return;
        List<Mat> mv = new ArrayList<>();
        Core.split(src, mv);
        channelB = mv.get(0);
        channelG = mv.get(1);
        channelR = mv.get(2);

        binding.ivTarget.setImageBitmap(formatMat2Bitmap(channelB));
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(channelG));
        binding.ivTarget3.setImageBitmap(formatMat2Bitmap(channelR));

    }

    /**
     * 合并通道
     */
    private void mergeChannel() {
        Mat dst = new Mat();
        List<Mat> mv = new ArrayList<>();
        mv.add(channelB);
        mv.add(channelG);
        mv.add(channelR);
        Core.merge(mv, dst);
        binding.ivSrc.setImageBitmap(formatMat2Bitmap(dst));
    }

    private void showBGZero() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        Mat zero = Mat.zeros(src.rows(), src.cols(), CvType.CV_8UC1);
        List<Mat> list = new ArrayList<>();
        list.add(zero);
        list.add(zero);
        list.add(channelR);
        Mat dst = new Mat();
        Core.merge(list, dst);
        binding.ivTarget3.setImageBitmap(formatMat2Bitmap(dst));
        src.release();
        zero.release();
        dst.release();
    }

    private void showBRZero() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        Mat zero = Mat.zeros(src.rows(), src.cols(), CvType.CV_8UC1);
        List<Mat> list = new ArrayList<>();
        list.add(zero);
        list.add(channelG);
        list.add(zero);
        Mat dst = new Mat();
        Core.merge(list, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));
        src.release();
        zero.release();
        dst.release();
    }

    private void showGRZero() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        Mat zero = Mat.zeros(src.rows(), src.cols(), CvType.CV_8UC1);
        List<Mat> list = new ArrayList<>();
        list.add(channelB);
        list.add(zero);
        list.add(zero);
        Mat dst = new Mat();
        Core.merge(list, dst);
        binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));
        src.release();
        zero.release();
        dst.release();
    }


    private Bitmap formatMat2Bitmap(Mat mat) {
        Bitmap target = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mat, target);
        return target;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileUri = Utils.onActivityResult(this, requestCode, resultCode, data);
        if (fileUri != null)
            binding.ivSrc.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath()));
    }


}
