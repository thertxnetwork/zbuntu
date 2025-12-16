#include <jni.h>
#include <android/log.h>

#define LOG_TAG "Zbuntu-Native-CPP"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_zbuntu_terminal_pty_JNI_getVersionInfo(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("Zbuntu Terminal Native Library v1.0");
}

} // extern "C"
