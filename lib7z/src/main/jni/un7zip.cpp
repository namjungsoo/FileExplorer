#include <string>
#include <vector>
#include <map>

extern "C" {
#include "un7zip.h"
#include "ndk-helper.h"
#include "src/7zVersion.h"

#include <stdint.h>
#include <android/asset_manager_jni.h>
#include "7zExtracter.h"
#include "src/7zTypes.h"
#include "src/7z.h"
#include "src/7zFile.h"
#include "src/7zAlloc.h"
#include "src/7zAssetFile.h"
#include "src/7zCrc.h"
#include "7zFunctions.h"

#define FUNC(f) Java_com_hzy_lib7z_Z7Extractor_##f

static const ISzAlloc g_Alloc = {SzAlloc, SzFree};

struct Z7Header {
    std::string filename;
    long size;
};

struct Z7Extracter {
    std::string z7Path;
    Z7Buffer buffer;
};

std::map<int, Z7Extracter*> z7Map;
int maxId = 0;

Z7Extracter *FindExtracter(int id) 
{
    if(z7Map.find(id) != z7Map.end())
        return z7Map.find(id)->second;
    return NULL;
}

JNIEXPORT jint JNICALL
FUNC(init)(JNIEnv *env, jclass type, jstring filePath_) {
    const char *filePath = env->GetStringUTFChars(filePath_, 0);
    maxId++;

    Z7Extracter *extractor = new Z7Extracter;
    extractor->z7Path = filePath;


    z7Map.insert(std::pair<int, Z7Extracter*>(maxId, extractor));

    env->ReleaseStringUTFChars(filePath_, filePath);
    return maxId;
}

JNIEXPORT void JNICALL
FUNC(destroy)(JNIEnv *env, jclass type, jint id) {
    Z7Extracter *extracter = FindExtracter(id);

    if(extracter) {
        ISzAlloc_Free(&g_Alloc, extracter->buffer.outBuffer);
    }
    z7Map.erase(id);
}

JNIEXPORT jstring JNICALL
FUNC(nGetLzmaVersion)(JNIEnv *env, jclass type) {
    return env->NewStringUTF(MY_VERSION_COPYRIGHT_DATE);
}

JNIEXPORT jboolean JNICALL
FUNC(nExtractAll)(JNIEnv *env, jclass type, jint id,
                   jstring outPath_, jobject callback, jlong inBufSize) {
    Z7Extracter *extracter = FindExtracter(id);
    if(!extracter)
        return false;
    const char *filePath = extracter->z7Path.c_str();
    const char *outPath = env->GetStringUTFChars(outPath_, 0);
    jboolean res = extractAll(env, filePath, outPath, callback, inBufSize);
    env->ReleaseStringUTFChars(outPath_, outPath);
    return res;
}

JNIEXPORT jboolean JNICALL
FUNC(nExtractFile)(JNIEnv *env, jclass type, jint id, jstring targetName_,
                   jstring outPath_, jobject callback, jlong inBufSize) {
    Z7Extracter *extracter = FindExtracter(id);
    if(!extracter)
        return false;
    const char *filePath = extracter->z7Path.c_str();
    const char *outPath = env->GetStringUTFChars(outPath_, 0);
    const char *targetName = env->GetStringUTFChars(targetName_, 0);
    jboolean res = extractFile(env, filePath, targetName, outPath, callback, inBufSize, &extracter->buffer);
    env->ReleaseStringUTFChars(outPath_, outPath);
    env->ReleaseStringUTFChars(targetName_, targetName);
    return res;
}

// JNIEXPORT jboolean JNICALL
// FUNC(nExtractAll)(JNIEnv *env, jclass type, jstring filePath_,
//                    jstring outPath_, jobject callback, jlong inBufSize) {
//     const char *filePath = env->GetStringUTFChars(filePath_, 0);
//     const char *outPath = env->GetStringUTFChars(outPath_, 0);
//     jboolean res = extractAll(env, filePath, outPath, callback, inBufSize);
//     env->ReleaseStringUTFChars(filePath_, filePath);
//     env->ReleaseStringUTFChars(outPath_, outPath);
//     return res;
// }

// JNIEXPORT jboolean JNICALL
// FUNC(nExtractFile)(JNIEnv *env, jclass type, jstring filePath_, jstring targetName_,
//                    jstring outPath_, jobject callback, jlong inBufSize) {
//     const char *filePath = env->GetStringUTFChars(filePath_, 0);
//     const char *outPath = env->GetStringUTFChars(outPath_, 0);
//     const char *targetName = env->GetStringUTFChars(targetName_, 0);
//     jboolean res = extractFile(env, filePath, targetName, outPath, callback, inBufSize);
//     env->ReleaseStringUTFChars(filePath_, filePath);
//     env->ReleaseStringUTFChars(outPath_, outPath);
//     env->ReleaseStringUTFChars(targetName_, targetName);
//     return res;
// }

// JNIEXPORT jboolean JNICALL
// FUNC(nExtractAsset)(JNIEnv *env, jclass type, jobject assetManager,
//                     jstring fileName_, jstring outPath_, jobject callback,
//                     jlong inBufSize) {
//     const char *fileName = env->GetStringUTFChars(fileName_, 0);
//     const char *outPath = env->GetStringUTFChars(outPath_, 0);
//     jboolean res = extractAsset(env, assetManager, fileName, outPath, callback, inBufSize);
//     env->ReleaseStringUTFChars(fileName_, fileName);
//     env->ReleaseStringUTFChars(outPath_, outPath);
//     return res;
// }

// 구현완료 
std::vector<Z7Header*> *getHeaders(ISeekInStream *seekStream, size_t inBufSize)
{
    std::vector<Z7Header*> *headers = new std::vector<Z7Header*>;

    ISzAlloc allocImp = g_Alloc;
    ISzAlloc allocTempImp = g_Alloc;
    CLookToRead2 lookStream;
    CSzArEx db;
    SRes res;
    UInt16 *temp = NULL;
    size_t tempSize = 0;

    LOGD("Stream In Buffer Size:[0X%lX]", inBufSize);
    LookToRead2_CreateVTable(&lookStream, False);
    lookStream.buf = NULL;
    res = SZ_OK;
    lookStream.buf = ISzAlloc_Alloc(&allocImp, inBufSize);
    if (!lookStream.buf)
        res = SZ_ERROR_MEM;
    else {
        lookStream.bufSize = inBufSize;
        lookStream.realStream = seekStream;
        LookToRead2_Init(&lookStream);
    }

    CrcGenerateTable();
    SzArEx_Init(&db);
    if (res == SZ_OK) {
        res = SzArEx_Open(&db, &lookStream.vt, &allocImp, &allocTempImp);
    }
    if (res == SZ_OK) {
        UInt32 i;
        /*
        if you need cache, use these 3 variables.
        if you use external function, you can make these variable as static.
        */
        UInt32 blockIndex = 0xFFFFFFFF; /* it can have any value before first call (if outBuffer = 0) */
        Byte *outBuffer = 0; /* it must be 0 before first call for each new archive. */
        size_t outBufferSize = 0;  /* it can have any value before first call (if outBuffer = 0) */
        CBuf fileNameBuf;
        Buf_Init(&fileNameBuf);

        for (i = 0; i < db.NumFiles; i++) {
            size_t offset = 0;
            size_t outSizeProcessed = 0;
            size_t len;
            unsigned isDir = (unsigned) SzArEx_IsDir(&db, i);
            len = SzArEx_GetFileNameUtf16(&db, i, NULL);
            if (len > tempSize) {
                SzFree(NULL, temp);
                tempSize = len;
                temp = (UInt16 *) SzAlloc(NULL, tempSize * sizeof(temp[0]));
                if (!temp) {
                    res = SZ_ERROR_MEM;
                    break;
                }
            }
            SzArEx_GetFileNameUtf16(&db, i, temp);
            res = Utf16_To_Char(&fileNameBuf, temp);
            if (res != SZ_OK) {
                break;
            }

            UInt64 fileSize = SzArEx_GetFileSize(&db, i);

            Z7Header *header = new Z7Header;
            header->filename = std::string(fileNameBuf.data, fileNameBuf.data+fileNameBuf.size);
            header->size = fileSize;

            // LOGE("filename=%s", header->filename.c_str());
            // LOGE("size=%ul", header->size);

            headers->push_back(header);
        }
        Buf_Free(&fileNameBuf, &g_Alloc);
        ISzAlloc_Free(&allocImp, outBuffer);
    }
    SzFree(NULL, temp);
    SzArEx_Free(&db, &allocImp);
    ISzAlloc_Free(&allocImp, lookStream.buf);
    if (res != SZ_OK) {
    }
    return headers;
}

JNIEXPORT jobject JNICALL
FUNC(getHeaders)(JNIEnv *env, jclass type, jint id, jlong inBufSize) {
    LOGE("getHeaders");
    Z7Extracter *extracter = FindExtracter(id);
    if(extracter == NULL)
        return 0;
//    const char *filePath = env->GetStringUTFChars(filePath_, 0);
    const char *filePath = extracter->z7Path.c_str();

    CFileInStream archiveStream;
    if (InFile_Open(&archiveStream.file, filePath)) {
        return 0;
    }

    FileInStream_CreateVTable(&archiveStream);
    std::vector<Z7Header*> *headers = getHeaders(&archiveStream.vt, inBufSize);
    File_Close(&archiveStream.file);
    if(headers == NULL) {
        LOGE("getHeaders NULL");
        return 0;
    }

    LOGE("getHeaders %d", headers->size());

    // header로 array list를 만든다.
    jclass arrayList = env->FindClass("java/util/ArrayList");
    jmethodID array_ctor = env->GetMethodID(arrayList, "<init>", "()V");
    jmethodID add = env->GetMethodID(arrayList, "add", "(Ljava/lang/Object;)Z");
    jobject objArrayList = env->NewObject(arrayList, array_ctor);

    // header를 접근 
    jclass header = env->FindClass("com/hzy/lib7z/Z7Header");
    jmethodID header_ctor = env->GetMethodID(header, "<init>", "()V");
    jfieldID fileNameField = env->GetFieldID(header, "fileName", "Ljava/lang/String;");
    jfieldID sizeField = env->GetFieldID(header, "size", "J");

    for(int i=0; i<headers->size(); i++) {
        jobject objHeader = env->NewObject(header, header_ctor);
        jstring fileName = env->NewStringUTF(headers->at(i)->filename.c_str());

        env->SetObjectField(objHeader, fileNameField, fileName);
        env->SetLongField(objHeader, sizeField, headers->at(i)->size);

        env->CallBooleanMethod(objArrayList, add, objHeader);

        env->DeleteLocalRef(fileName);
        env->DeleteLocalRef(objHeader);

        delete headers->at(i);
    }
    delete headers;
    env->DeleteLocalRef(header);
    env->DeleteLocalRef(arrayList);
//    env->ReleaseStringUTFChars(filePath_, filePath);
    return objArrayList;
}
}