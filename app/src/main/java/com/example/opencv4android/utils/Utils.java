package com.example.opencv4android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class Utils {

    public final static int REQUEST_CAPTURE_IMAGE = 1;

    /**
     * 打开相册
     *
     * @param context
     */
    public static void pickUpImage(Context context) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }


    public static Uri onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        Uri fileUri = null;
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri, context));
                fileUri = Uri.fromFile(f);
            }
        }
        return fileUri;
    }

}
