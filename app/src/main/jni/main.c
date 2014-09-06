#include "com_dip_angrycamera_angrycamera_MyActivity.h"

JNIEXPORT jstring JNICALL Java_com_dip_angrycamera_angrycamera_MyActivity_getStringFromNative
  (JNIEnv * env, jobject obj)
  {
    return (*env)->NewStringUTF(env,"Hello from JNI");
  }