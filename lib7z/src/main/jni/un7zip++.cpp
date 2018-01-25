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

std::vector<std::string> getHeaders(ISeekInStream *seekStream, size_t inBufSize)
{
    std::vector<std::string> headers;

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
            std::string fileName(fileNameBuf.data, fileNameBuf.data+fileNameBuf.size);
            headers.push_back(fileName);

            UInt64 fileSize = SzArEx_GetFileSize(&db, i);
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
    std::vector<std::string> headers = getHeaders(&archiveStream.vt, inBufSize);
    File_Close(&archiveStream.file);

    // header string으로 array list를 만든다.
    jclass arrayList = env->FindClass("java/util/ArrayList");
    jmethodID ctor = env->GetMethodID(arrayList, "<init>", "()V");
    jmethodID add = env->GetMethodID(arrayList, "add", "(Ljava/lang/Object;)Z");
    jobject objArrayList = env->NewObject(arrayList, ctor);

    for(int i=0; i<headers.size(); i++) {
        jstring fileName = env->NewStringUTF(headers[i].c_str());
        env->CallBooleanMethod(objArrayList, add, fileName);
    }

    env->ReleaseStringUTFChars(filePath_, filePath);
    return objArrayList;
}
}
