package com.laulee.idcardnumber;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by laulee on 2020-03-04.
 */
public class Utils {

    /**
     * 将asset文件拷贝到系统目录下
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String copyAssetAndWrite(Context context, String fileName) {
        try {
            File cacheDir = context.getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outFile = context.getFileStreamPath(fileName);
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            InputStream inputStream = context.getAssets().open(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[inputStream.available()];
            int byteCount;

            while ((byteCount = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteCount);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            return outFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
