package com.example.opencv4android.imgproc;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.opencv4android.R;
import com.example.opencv4android.databinding.ActivitySmoothBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/** 图像滤波，包括线性、非线性
 * Created by jiangdongguo on 2018/5/4.
 */

public class ImageSmoothActivity extends AppCompatActivity {
    private final String TAG="ImageSmoothActivity";
    private ActivitySmoothBinding binding;
    private static final String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator+"yuwenwenn.jpg";
    private Mat mSrcImage;
    private Bitmap srcBm,boxBm,blurBm,gaussianBm;

    // 加载OpenCV库
//    static {
//        System.loadLibrary("OpenCV3416");
//    }

    private final LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // OpenCV引擎加载成功
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully.");
                // 读取图像,将BRG转换为RGB，否则会出现颜色问题
                mSrcImage = Imgcodecs.imread(imagePath);
                Imgproc.cvtColor(mSrcImage, mSrcImage, Imgproc.COLOR_BGR2RGB);

                int width = mSrcImage.width();
                int height = mSrcImage.height();
                srcBm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                boxBm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                blurBm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                gaussianBm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                // 将滤波处理后的mat转换为Bitmap
                Utils.matToBitmap(mSrcImage, srcBm);
                Utils.matToBitmap(boxFilterOperator(), boxBm);
                Utils.matToBitmap(blurFilterOperator(), blurBm);
                Utils.matToBitmap(GaussianBlurOperator(), gaussianBm);
                // 显示效果
                ((ImageView) findViewById(R.id.image1)).setImageBitmap(srcBm);
                ((ImageView) findViewById(R.id.image2)).setImageBitmap(boxBm);
                ((ImageView) findViewById(R.id.image3)).setImageBitmap(blurBm);
                ((ImageView) findViewById(R.id.image4)).setImageBitmap(gaussianBm);
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySmoothBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 静态加载
        if(!OpenCVLoader.initDebug()) {
            Log.w(TAG,"static loading library fail,Using Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.w(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /** 方框滤波
     *  ddepth -1输出图像深度同输入图像深度一致
     *  size  核大小为5x5
     * */
    private Mat boxFilterOperator() {
        double ksize = 5.0f;
        int ddepth = -1;
        Mat dstImage = mSrcImage.clone();
        Imgproc.boxFilter(mSrcImage,dstImage,ddepth,new Size(ksize,ksize));
        return dstImage;
    }

    /** 均值滤波
     *  size  核大小为3x3
     * */
    private Mat blurFilterOperator() {
        double ksize = 3.0f;
        Mat dstImage = mSrcImage.clone();
        Imgproc.blur(mSrcImage,dstImage,new Size(ksize,ksize));
        return dstImage;
    }

    /** 高斯平滑滤波
     *  size 核大小为7x7
     *  sigmaX
     * */
    private Mat GaussianBlurOperator() {
        double ksize = 7.0f;
        double sigmaX = 0;
        Mat dstImage = mSrcImage.clone();
        Imgproc.GaussianBlur(mSrcImage,dstImage,new Size(ksize,ksize),sigmaX);
        return dstImage;
    }
}
