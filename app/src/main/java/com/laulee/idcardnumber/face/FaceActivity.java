package com.laulee.idcardnumber.face;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.laulee.idcardnumber.R;
import com.laulee.idcardnumber.Utils;

import java.io.File;

public class FaceActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    SurfaceView surfaceView;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }

    //加载记忆样本
    private native void loadModel(String detectModel);

    //设置绘制区域
    private native void setSurfaceView(SurfaceView surfaceView, int w, int h);

    //处理图片并识别
    private native boolean process(Bitmap bitmap);

    public native void destory();

    private Bitmap bitmap;
    private ProgressDialog dialog;
    private final static String TAG = "FaceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                String path = Utils.copyAssetAndWrite(FaceActivity.this, "haarcascade_frontalface_alt.xml");
                Log.d(TAG, "haarcascade_frontalface_alt path = " + path);
                loadModel(path);
                return null;
            }
        }.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void selectPic(View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "选择识别图片"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "requestCode = " + requestCode);
        if (requestCode == 100 && data != null) {
            getResult(data.getData());
        }
    }

    private void getResult(Uri uri) {
        safeRecycled();
        String imagePath = null;
        if (uri != null) {
            if ("file".equals(uri.getScheme())) {
                Log.d(TAG, "path 获取图片 地址 = " + uri.getPath());
                imagePath = uri.getPath();
            } else if ("content".equals(uri.getScheme())) {
                Log.d(TAG, "content 获取图片 ");
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imagePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            }
        }
        if (!TextUtils.isEmpty(imagePath)) {
            Log.d(TAG, "imagePath " + imagePath);
            bitmap = toBitmap(imagePath);

            Log.d(TAG, "safeProcess ");
            safeProcess();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destory();
        safeRecycled();
    }

    public void safeProcess() {
        if (bitmap != null && !bitmap.isRecycled()) {
            process(bitmap);
        }
    }

    private void safeRecycled() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = null;
    }

    public Bitmap toBitmap(String path) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        int width = o.outWidth;
        int height = o.outHeight;
        int scale = 1;

        while (true) {
            if (width <= 640 && height <= 400) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        options.outHeight = height;
        options.outWidth = width;
        return BitmapFactory.decodeFile(path, options);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        setSurfaceView(surfaceView, 640, 400);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
