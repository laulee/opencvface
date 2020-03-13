#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <opencv2/imgproc/types_c.h>
#include <android/log.h>

#define LOG_TAG "System.out"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
{

using namespace cv;
using namespace std;

ANativeWindow *nativeWindow = 0;
CascadeClassifier *faceClassifier = 0;

void bitmap2Mat(JNIEnv *env, jobject bitmap, Mat &dst) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    //获取bitmap信息
    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    //设置bitmap
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
    //lock获取数据
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);
    dst.create(info.height, info.width, CV_8UC3);

    Mat temp(info.height, info.width, CV_8UC4, pixels);
    cvtColor(temp, dst, COLOR_RGB2BGR);
    temp.release();
    AndroidBitmap_unlockPixels(env, bitmap);
}

jstring
Java_com_laulee_idcardnumber_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

jobject
Java_com_laulee_idcardnumber_MainActivity_findIdNumber(JNIEnv *env, jobject instance,
                                                       jobject bitmap, jobject argb8888) {


}

void
Java_com_laulee_idcardnumber_face_FaceActivity_loadModel(JNIEnv *env, jobject instance,
                                                         jstring detectModel_) {
    const char *detectModel = env->GetStringUTFChars(detectModel_, 0);

    faceClassifier = new CascadeClassifier(detectModel);

    env->ReleaseStringUTFChars(detectModel_, detectModel);
}

void
Java_com_laulee_idcardnumber_face_FaceActivity_setSurfaceView(JNIEnv *env, jobject instance,
                                                              jobject surfaceView, jint w, jint h) {

    LOGD("setSurfaceView");
    if (surfaceView && w && h) {
        if (nativeWindow) {
            LOGD("setSurfaceView release");
            ANativeWindow_release(nativeWindow);
            nativeWindow = 0;
        }
        LOGD("setSurfaceView 创建");
        nativeWindow = ANativeWindow_fromSurface(env, surfaceView);
        LOGD("setSurfaceView 创建成功");
        if (nativeWindow) {
            LOGD("setSurfaceView 宽高");
            ANativeWindow_setBuffersGeometry(nativeWindow, w, h, WINDOW_FORMAT_RGBA_8888);
        }
    } else {
        if (nativeWindow) {
            LOGD("setSurfaceView release");
            ANativeWindow_release(nativeWindow);
            nativeWindow = 0;
        }
    }
}

jboolean
Java_com_laulee_idcardnumber_face_FaceActivity_process(JNIEnv *env, jobject instance,
                                                       jobject bitmap) {

    Mat grayMat;//存储灰度图
    Mat src;//c、c++层图片数据结构
    //将bitmap转成矩阵
    LOGD("将bitmap转成矩阵");
    bitmap2Mat(env, bitmap, src);
    //将src灰度化
    LOGD("将src灰度化");
    cvtColor(src, grayMat, CV_BGR2GRAY);
    //直方图均衡化
    LOGD("直方图均衡化");
    equalizeHist(grayMat, grayMat);

    //分类器找人脸
    vector<Rect> faces;

    LOGD("分类器找人脸");
    faceClassifier->detectMultiScale(grayMat, faces);

    LOGD("分类器找人脸结束 %d 张人脸", faces.size());

    for (int i = 0; i < faces.size(); i++) {
        Rect face = faces[i];
        //绘制矩阵
        LOGD("绘制矩阵");
        rectangle(src, face.tl(), face.br(), Scalar(0, 255, 255));
    }

    LOGD("分类器找人脸结束 %d 张人脸", faces.size());
    //显示到视图上
    ANativeWindow_Buffer window_buffer;
    //得到绘制空间
    if (ANativeWindow_lock(nativeWindow, &window_buffer, 0)) {
        LOGD("没找到");
        goto end;
    }

    //直接在native层进行数据填充

    LOGD("直接在native层进行数据填充");
    cvtColor(src, src, CV_BGR2RGBA);

    //先对被填充的图片进行归一
    resize(src, src, Size(window_buffer.width, window_buffer.height));

    memcpy(window_buffer.bits, src.data, window_buffer.height * window_buffer.stride * 4);

    ANativeWindow_unlockAndPost(nativeWindow);

    end:

    src.release();
    grayMat.release();

    return true;

}

void
Java_com_laulee_idcardnumber_face_FaceActivity_destory(JNIEnv *env, jobject instance) {


}
}