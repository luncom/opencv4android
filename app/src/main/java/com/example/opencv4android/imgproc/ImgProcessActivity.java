package com.example.opencv4android.imgproc;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import kotlin.Triple;


/**
 * 第一个OpenCV例子
 * Created by jiangdongguo on 2017/12/29.
 */

public class ImgProcessActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
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
                    initBrightnessAndContrast();
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
        binding.btnAdd.setOnClickListener(this);
        binding.btnSubtract.setOnClickListener(this);
        binding.btnMultiply.setOnClickListener(this);
        binding.btnDevide.setOnClickListener(this);
        binding.btnAddWeight.setOnClickListener(this);
        binding.btnBitwiseAnd.setOnClickListener(this);
        binding.btnBitwiseNot.setOnClickListener(this);
        binding.btnBitwiseXor.setOnClickListener(this);
        binding.btnBitwiseOr.setOnClickListener(this);
        binding.sbBright.setOnSeekBarChangeListener(this);
        binding.sbContrast.setOnSeekBarChangeListener(this);
        binding.btnNormalize.setOnClickListener(this);
        binding.btnBlur.setOnClickListener(this);
        binding.btnGaussianBlur.setOnClickListener(this);
        binding.btnMedianBlur.setOnClickListener(this);
        binding.btnDilate.setOnClickListener(this);
        binding.btnErode.setOnClickListener(this);
        binding.btnBilateralFilter.setOnClickListener(this);
        binding.btnMeanShiftFilter.setOnClickListener(this);
        binding.btnCustomFilter.setOnClickListener(this);
        binding.btnMorphology.setOnClickListener(this);
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
            case R.id.btn_add:
                add(commonCalculate());
                break;
            case R.id.btn_subtract:
                subtract(commonCalculate());
                break;
            case R.id.btn_multiply:
                multiply(commonCalculate());
                break;
            case R.id.btn_devide:
                devide(commonCalculate());
                break;
            case R.id.btn_add_weight:
                addWeight(commonCalculate());
                break;
            case R.id.btn_bitwise_and:
                bitWiseAnd(commonCalculate());
                break;
            case R.id.btn_bitwise_not:
                bitWiseNot(commonCalculate());
                break;
            case R.id.btn_bitwise_xor:
                bitWiseXor(commonCalculate());
                break;
            case R.id.btn_bitwise_or:
                bitWiseOr(commonCalculate());
                break;
            case R.id.btn_normalize:
                normalize();
                break;
            case R.id.btn_blur:
                blur();
                break;
            case R.id.btn_gaussian_blur:
                gaussianBlur();
                break;
            case R.id.btn_median_blur:
                medianBlur();
                break;
            case R.id.btn_dilate:
                dilate();
                break;
            case R.id.btn_erode:
                erode();
                break;
            case R.id.btn_bilateralFilter:
                bilateralFilter();
                break;
            case R.id.btn_meanShiftFilter:
                meanShiftFilter();
                break;
            case R.id.btn_customFilter:
                customFilter();
                break;
            case R.id.btn_morphology:
                morphology();
                break;
            case R.id.btn_threshold:
                thresHold();
                break;
        }
    }

    /**
     * 阈值操作
     */
    private void thresHold(){
        int t =127;
        int maxValue=255;
        Mat src = null;
        try {
            src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat gray = new Mat();
            Mat dst = new Mat();
            Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(gray,dst,t,maxValue,Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 形态学操作
     */
    private void morphology() {
        try {

            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();
            Mat k = Imgproc.getStructuringElement(MORPH_RECT, new Size(15, 15), new Point(-1, -1));

            //膨胀
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_DILATE, k);
            //腐蚀
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_ERODE, k);
//            //开操作
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_OPEN, k);
//            //闭操作
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_CLOSE, k);
//            //黑帽
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_BLACKHAT, k);
//            //顶帽
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_TOPHAT, k);
//            //梯度
//            Imgproc.morphologyEx(mRgb, dst, Imgproc.MORPH_GRADIENT, k);

            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));

            k.release();
            src.release();
            mRgb.release();
            dst.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义滤波
     */
    private void customFilter() {
        try {

            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();

            Mat k = new Mat(3, 3, CvType.CV_32FC1);
            //均值模糊
//            float[] data = new float[]{
//                    1f / 9f, 1f / 9f, 1f / 9f,
//                    1f / 9f, 1f / 9f, 1f / 9f,
//                    1f / 9f, 1f / 9f, 1f / 9f
//            };
            //近似高斯模糊
//        float[] data = new float[]{
//                0, 1f / 8f, 0,
//                1f / 8f, 1f / 2f, 1f / 8f,
//                0, 1f / 8f, 0
//        };
            //锐化算子
            float[] data = new float[]{
                    0, -1, 0,
                    -1, 5, -1,
                    0, -1, 0
            };
            k.put(0, 0, data);
            Imgproc.filter2D(mRgb, dst, -1, k);

//            //梯度
//            Mat kx = new Mat(3, 3, CvType.CV_32FC1);
//            Mat ky = new Mat(3, 3, CvType.CV_32FC1);
//            //X方向梯度算子
//            float[] robert_x = new float[]{-1, 0, 0, 1};
//            kx.put(0, 0, robert_x);
//            //Y方向算子
//            float[] robert_y = new float[]{0, 1, -1, 0};
//            ky.put(0, 0, robert_y);
//
//            Imgproc.filter2D(mRgb,dst,-1,kx);
//            Imgproc.filter2D(mRgb,dst,-1,ky);

            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));


            src.release();
            mRgb.release();
            dst.release();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 均值漂移滤波
     */
    private void meanShiftFilter() {
        try {

            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();

            Imgproc.pyrMeanShiftFiltering(mRgb, dst, 40.0, 40.0);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));


            src.release();
            mRgb.release();
            dst.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 高斯双边滤波
     */
    private void bilateralFilter() {
        try {
            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();
//            Imgproc.bilateralFilter(src, dst, 9, 10, 10);
//            Imgproc.bilateralFilter(src, dst, 25, 10, 10);
//            Imgproc.bilateralFilter(src, dst, 25, 100, 100);
            Imgproc.bilateralFilter(mRgb, dst, 25, 200, 200);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));

            src.release();
            mRgb.release();
            dst.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 膨胀（最大值滤波）
     */
    private void dilate() {
        try {
            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();
            Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3));
            Imgproc.dilate(src, dst, kernel);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 腐蚀（最小值滤波）
     */
    private void erode() {
        try {
            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat dst = new Mat();
            Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3));
            Imgproc.erode(src, dst, kernel);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(dst));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 中值滤波
     */
    private void medianBlur() {
        Mat src = null;
        try {
            src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            binding.ivSrc.setImageBitmap(formatMat2Bitmap(mRgb));

            Mat result = new Mat();
            Imgproc.medianBlur(src, result, 5);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(result));

            src.release();
//            mRgb.release();
            result.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 高斯模糊
     */
    private void gaussianBlur() {
        Mat src = null;
        try {
            src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat mRgb = new Mat();
            Imgproc.cvtColor(src, mRgb, Imgproc.COLOR_BGR2RGB);
            Mat result = new Mat();
            Imgproc.GaussianBlur(mRgb, result, new Size(9, 9), 10, 20);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(result));

            src.release();
            mRgb.release();
            result.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 均值滤波
     */
    private void blur() {
        try {
            Mat src = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
            if (src.empty()) {
                return;
            }
            Mat dst = new Mat();
            Imgproc.blur(src, dst, new Size(12, 12));
            Mat result = new Mat();
            Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGB);
            binding.ivTarget.setImageBitmap(formatMat2Bitmap(result));
            src.release();
            dst.release();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Core.convertScaleAbs(dst, dst);线性绝对值放缩变换，把任意像素值变化到0~255的CV_8U图像像素值
    // Core.normalize();归一化，把数据re-scale到制定的范围内

    /**
     * 归一化
     */
    private void normalize() {
        Mat src = Mat.zeros(400, 400, CvType.CV_32FC3);
        float[] data = new float[400 * 400 * 3];
        Random random = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) random.nextGaussian();
        }
        src.put(0, 0, data);

        Mat dst = new Mat();
        Core.normalize(src, dst, 0, 255, Core.NORM_MINMAX, -1);
        Mat dst8u = new Mat();
        dst.convertTo(dst8u, CvType.CV_8UC3);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst8u));
    }

    /**
     * 1.调整亮度：加减法，亮度值0-255，new Scalar(b,b,b),b>0 调高亮度，b<0 调低亮度
     * 2.调整对比度：乘除法，new Scalar(b,b,b),b>1 调高对比度，b<1 调低对比度，b的取值（0~3）
     */
    private void adjustBrightnessAndContrast() {
        Mat pre = new Mat();
        Mat source = brightnessAndContrastPair.second;
        Core.add(source,
                new Scalar(brightness - originBrightness, brightness - originBrightness, brightness - originBrightness),
                pre);
        Mat dst = new Mat();
        Core.multiply(pre, new Scalar(contrast / 100, contrast / 100, contrast / 100, contrast / 100), dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));
        pre.release();
        dst.release();
    }


    private Pair<Mat, Mat> commonCalculate() {
        binding.ivSrc.setImageResource(R.mipmap.lena);
        Mat bgr = null;
        try {
            bgr = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat source = new Mat();
        Imgproc.cvtColor(bgr, source, Imgproc.COLOR_BGR2RGB);
        Bitmap bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888);
        bitmap.setDensity(DisplayMetrics.DENSITY_XXHIGH);
        org.opencv.android.Utils.matToBitmap(bgr, bitmap);
        binding.ivTarget.setImageBitmap(bitmap);
        return new Pair<>(bgr, source);
    }

    private Pair<Mat, Mat> initBrightnessAndContrast() {
        binding.ivSrc.setImageResource(R.mipmap.lena);
        Mat bgr = null;
        try {
            bgr = org.opencv.android.Utils.loadResource(this, R.mipmap.lena);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat source = new Mat();
        Imgproc.cvtColor(bgr, source, Imgproc.COLOR_BGR2RGB);
        Bitmap bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(source, bitmap);
        binding.ivTarget.setImageBitmap(bitmap);
        originBrightness = Core.mean(source).val[0];
        Log.d(TAG, "originBrightness:" + originBrightness + ",sbBright:" + (binding.sbBright == null));
        brightnessAndContrastPair = new Pair<>(bgr, source);
        binding.sbBright.setProgress((int) originBrightness);
        binding.sbContrast.setProgress(100);
        return new Pair<>(bgr, source);
    }


    /**
     * 取反操作
     *
     * @param pair
     */
    private void bitWiseNot(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.bitwise_not(source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    /**
     * 异或操作
     * 对输入图像的叠加取反效果
     *
     * @param pair
     */
    private void bitWiseXor(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.bitwise_xor(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    /**
     * 与操作
     * 对两张图像混合后的输出图像有降低混合图像亮度的效果，会让输出的像素小于等于对应位置的任意一张输入图像的像素值
     *
     * @param pair
     */
    private void bitWiseAnd(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.bitwise_and(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    /**
     * 或操作
     * 对两张图像混合后的输出图像有强化混合图像亮度的效果，会让输出的像素大于等于对应位置的任意一张输入图像的像素值
     *
     * @param pair
     */
    private void bitWiseOr(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.bitwise_or(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    private void add(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.add(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
//            bitmap.recycle();

    }

    private void subtract(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.subtract(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    private void multiply(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.multiply(bgr, source, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }

    private void devide(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.divide(bgr, source, dst, 50f, -1);
        //线性绝对值放缩变换，把任意像素值变化到0~255的CV_8U图像像素值
        Core.convertScaleAbs(dst, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
    }


    private void addWeight(Pair<Mat, Mat> pair) {
        Mat bgr = pair.first;
        Mat source = pair.second;
        Mat dst = new Mat();
        Core.addWeighted(bgr, 0.2, source, 0.8, 100, dst);
        binding.ivTarget2.setImageBitmap(formatMat2Bitmap(dst));

        dst.release();
        bgr.release();
        source.release();
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

        Core.meanStdDev(src, mean, stdDev);

        Log.d(TAG, "均值mean:" + mean.toList() + ",标准方差stdDev:" + stdDev.toList());

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


    /**
     * @param mat is a valid input Mat object of the types 'CV_8UC1', 'CV_8UC3' or 'CV_8UC4'.
     * @return
     */
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

    private boolean initBrightAndContrast = false;
    private double brightness = 0, contrast = 100;
    private double originBrightness;
    private Pair<Mat, Mat> brightnessAndContrastPair;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        if (seekBar.getId() == R.id.sb_bright) {
            brightness = i;
            adjustBrightnessAndContrast();
        } else if (seekBar.getId() == R.id.sb_contrast) {
            contrast = i;
            adjustBrightnessAndContrast();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
