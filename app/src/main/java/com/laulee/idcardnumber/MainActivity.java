package com.laulee.idcardnumber;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.img_card);
        imageView.setImageResource(R.mipmap.id);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void getNumber(View view) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.id);
        Bitmap idNumber = findIdNumber(bitmap, Bitmap.Config.ARGB_8888);
        bitmap.recycle();
        if (idNumber != null) {
            imageView.setImageBitmap(idNumber);
        }else {
            idNumber.recycle();
        }
    }

    private native Bitmap findIdNumber(Bitmap bitmap, Bitmap.Config argb8888);
}
