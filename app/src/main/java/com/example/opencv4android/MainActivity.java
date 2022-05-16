package com.example.opencv4android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.opencv4android.databinding.ActivityMainBinding;
import com.example.opencv4android.imgproc.ImgProcessActivity;
import com.example.opencv4android.objdetect.FaceDetectActivity;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;


    private String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private final int RC_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnHello.setOnClickListener(this);
        binding.btnFaceDetect.setOnClickListener(this);
        initPermission();
    }

    @AfterPermissionGranted(RC_REQUEST)
    private void initPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            //
            Log.d(TAG, "已经有权限了");
        } else {
            EasyPermissions.requestPermissions(this, "需要权限", RC_REQUEST, perms);
        }
    }


    @Override
    public void onClick(View view) {
        int vId = view.getId();
        switch (vId) {
            case R.id.btn_hello:
                Intent intentHello = new Intent(MainActivity.this, ImgProcessActivity.class);
                startActivity(intentHello);
                break;
            case R.id.btn_faceDetect:
                Intent intentFd = new Intent(MainActivity.this, FaceDetectActivity.class);
                startActivity(intentFd);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG,"onPermissionsGranted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG,"onPermissionsDenied");
    }
}
