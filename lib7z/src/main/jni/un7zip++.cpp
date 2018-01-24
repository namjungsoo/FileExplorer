#include "ndk-helper.h"
#include "src/7zVersion.h"
#include "7zExtracter.h"

#define FUNC(f) Java_com_hzy_lib7z_Z7Extractor_##f

JNIEXPORT jobject JNICALL
FUNC(getHeaders)(JNIEnv *env, jclass type, jstring filePath_) {
    return 0;
    //return env->NewStringUTF(env, MY_VERSION_COPYRIGHT_DATE);
}
