#include <string>
#include <vector>

extern "C" {
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

            LOGE("filename=%s", header->filename.c_str());
            LOGE("size=%ul", header->size);

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
FUNC(getHeaders)(JNIEnv *env, jclass type, jstring filePath_, jlong inBufSize) {
    const char *filePath = env->GetStringUTFChars(filePath_, 0);

    CFileInStream archiveStream;
    if (InFile_Open(&archiveStream.file, filePath)) {
        return 0;
    }

    FileInStream_CreateVTable(&archiveStream);
    std::vector<Z7Header*> *headers = getHeaders(&archiveStream.vt, inBufSize);
    File_Close(&archiveStream.file);
    if(headers == NULL)
        return 0;

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
    }

    env->ReleaseStringUTFChars(filePath_, filePath);
    return objArrayList;
}
}
