#ifndef __UNRAR_H__
#define __UNRAR_H__

#include <android/log.h>
#include <jni.h>
#include <vector>
#include <string>
#include <map>

#define LOG_TAG "unrar"
#define  LOGUNK(...)  __android_log_print(ANDROID_LOG_UNKNOWN,LOG_TAG,__VA_ARGS__)
#define  LOGDEF(...)  __android_log_print(ANDROID_LOG_DEFAULT,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGF(...)  __android_log_print(ANDROID_FATAL_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGS(...)  __android_log_print(ANDROID_SILENT_ERROR,LOG_TAG,__VA_ARGS__)

struct RarFileHeader {
    std::string filename;
    long size;
    int time;
};

struct JNIArrayList {
    jclass arrayList;
    jobject objArrayList;
    jmethodID ctor;
    jmethodID add;
};

struct JNIHeader {
    jclass header;
    jmethodID ctor;
    jobject objHeader;
    jfieldID fileName;
    jfieldID sizeField;
    jfieldID timeField;
};

class Unrar {
public:
    Unrar(JNIEnv *env, const char *rarname) : id(0), count(0), handle(NULL),
        filesize(0), fileprogress(0), jcallback(0), jprocess(0) {
        this->env = env;
        this->rarname = rarname;
    }

    int getCount();
    bool unarchive(const char *file, const char *dest);
    bool unarchiveAll(const char *dest);
    std::vector<RarFileHeader*> *getHeaders();

public:
    void setCallback(jobject callback, jmethodID process) {
        jcallback = callback;
        jprocess = process;
    }

    void setFileName(const char *name) {
        filename = name;
    }

    void setFileSize(long size) {
        filesize = size;
    }

    void resetProgress() {
        fileprogress = 0;
    }

    void callback(long P2);

private:
    JNIEnv *env;
    int id;
    int count;

    std::string rarname;

    // file
    std::string filename;
    long filesize;
    long fileprogress;

    // callback
    jobject jcallback;
    jmethodID jprocess;

    HANDLE handle;
    RARHeaderData header;
};

int CallBack(UINT msg,LPARAM UserData,LPARAM P1,LPARAM P2);
Unrar *FindUnrar(int id);
jmethodID GetCallbackMethodID(JNIEnv *env, jobject jcallback);

JNIArrayList CreateArrayList(JNIEnv *env);
JNIHeader CreateHeader(JNIEnv *env);
void NewHeader(JNIEnv *env, JNIHeader *header);

void ReleaseArrayList(JNIEnv *env, JNIArrayList *arrayList);
void ReleaseHeader(JNIEnv *env, JNIHeader *header);

#endif//__UNRAR_H__