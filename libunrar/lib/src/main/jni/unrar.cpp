#include <stdio.h>
#include <stdlib.h>

#include <string.h>
#include <math.h>
#include <signal.h>

#include "rar.hpp"
#include "unrar.hpp"

std::map<int, Unrar*> unrarMap;
int maxId = 0;

void Unrar::callback(long P2) {
    int percent = (int)(P2*100 / filesize);
    fileprogress += percent;

    jstring jfilename = env->NewStringUTF(filename.c_str());
    env->CallVoidMethod(jcallback, jprocess, jfilename, fileprogress);
    env->DeleteLocalRef(jfilename);
}

std::vector<RarFileHeader*> *Unrar::getHeaders() {
    char cmt[1024];
    RAROpenArchiveData data;
    data.OpenMode = RAR_OM_LIST;
    data.ArcName = (char*)rarname.c_str();
    data.CmtBuf = cmt;
    data.CmtBufSize = 1024;

    HANDLE hArchive = RAROpenArchive(&data);

    //RARSetCallback(hArchive, CallBack, (LPARAM)this);
    if(data.OpenResult == ERAR_SUCCESS) {
        std::vector<RarFileHeader*> *headerVec = new std::vector<RarFileHeader*>;
        RARHeaderData header;
        int ret;
        while(ERAR_SUCCESS == (ret = RARReadHeader(hArchive, &header))) {
            RarFileHeader *rarheader = new RarFileHeader;
            if (ERAR_SUCCESS != (ret = RARProcessFile(hArchive, RAR_SKIP, NULL, NULL))) {
                return NULL;
            }

            rarheader->filename = header.FileName;
            rarheader->size = header.UnpSize;
            rarheader->time = header.FileTime;

            headerVec->push_back(rarheader);
        }
        RARCloseArchive(hArchive);
        return headerVec;
    }
    return NULL;
}

int Unrar::getCount()
{
    char cmt[1024];
    RAROpenArchiveData data;
    data.OpenMode = RAR_OM_LIST;
    data.ArcName = (char*)rarname.c_str();
    data.CmtBuf = cmt;
    data.CmtBufSize = 1024;

    HANDLE hArchive = RAROpenArchive(&data);
    int count = 0;

    //RARSetCallback(hArchive, CallBack, (LPARAM)this);
    if(data.OpenResult == ERAR_SUCCESS) {
        RARHeaderData header;
        int ret;
        while(ERAR_SUCCESS == (ret = RARReadHeader(hArchive, &header))) {
            if (ERAR_SUCCESS != (ret = RARProcessFile(hArchive, RAR_SKIP, NULL, NULL))) {
                return 0;
            }
            count++;
        }
        RARCloseArchive(hArchive);
        return count;
    }
    return 0;
}

bool Unrar::unarchive(const char *file, const char *dest)
{
    char cmt[1024];
    RAROpenArchiveData data;
    data.OpenMode = RAR_OM_EXTRACT;
    data.ArcName = (char*)rarname.c_str();
    data.CmtBuf = cmt;
    data.CmtBufSize = 1024;

    HANDLE hArchive = RAROpenArchive(&data);
    if(jcallback != 0)
        RARSetCallback(hArchive, CallBack, (LPARAM)this);

    if(data.OpenResult == ERAR_SUCCESS) {
        RARHeaderData header;
        int ret;
        while(ERAR_SUCCESS == (ret = RARReadHeader(hArchive, &header))) {
            if(!strcmp(file, header.FileName)) {
                char fullPath[1024];
                sprintf(fullPath, "%s/%s", dest, header.FileName);

                setFileName(header.FileName);
                setFileSize(header.UnpSize);
                resetProgress();

                if (ERAR_SUCCESS != (ret = RARProcessFile(hArchive, RAR_EXTRACT, NULL, fullPath))) {
                    return false;
                }
            } else {
                if (ERAR_SUCCESS != (ret = RARProcessFile(hArchive, RAR_SKIP, NULL, NULL))) {
                    return false;
                }                
            }
        }
        RARCloseArchive(hArchive);
        return true;
    }
    return false;
}

bool Unrar::unarchiveAll(const char *dest)
{
    char cmt[1024];
    RAROpenArchiveData data;
    data.OpenMode = RAR_OM_EXTRACT;
    data.ArcName = (char*)rarname.c_str();
    data.CmtBuf = cmt;
    data.CmtBufSize = 1024;

    HANDLE hArchive = RAROpenArchive(&data);
    if(jcallback != 0)
        RARSetCallback(hArchive, CallBack, (LPARAM)this);

    if(data.OpenResult == ERAR_SUCCESS) {
        RARHeaderData header;
        int ret;
        while(ERAR_SUCCESS == (ret = RARReadHeader(hArchive, &header))) {
            char fullPath[1024];
            sprintf(fullPath, "%s/%s", dest, header.FileName);

            setFileName(header.FileName);
            setFileSize(header.UnpSize);
            resetProgress();

            if (ERAR_SUCCESS != (ret = RARProcessFile(hArchive, RAR_EXTRACT, NULL, fullPath))) {
                return false;
            }
        }
        RARCloseArchive(hArchive);
        return true;
    }
    return false;
}

// P1: addr
// P2: count
int CallBack(UINT msg,LPARAM UserData,LPARAM P1,LPARAM P2) {
    Unrar *rar = (Unrar*)UserData;
    if(rar != NULL) {
        if(msg == UCM_PROCESSDATA) {
            rar->callback(P2);
        }
    }
    return 0;
}

Unrar *FindUnrar(int id) {
    Unrar *rar = NULL;
    if(unrarMap.find(id) != unrarMap.end()) {
        rar = unrarMap.find(id)->second;
    }
    return rar;
}

jmethodID GetCallbackMethodID(JNIEnv *env, jobject jcallback) {
    jclass callback = env->GetObjectClass(jcallback);
    jmethodID jprocess = env->GetMethodID(callback, "process", "(Ljava/lang/String;I)V");

    return jprocess;
}

JNIArrayList CreateArrayList(JNIEnv *env) {
    jclass arrayList = env->FindClass("java/util/ArrayList");
    jmethodID ctor = env->GetMethodID(arrayList, "<init>", "()V");
    jmethodID add = env->GetMethodID(arrayList, "add", "(Ljava/lang/Object;)Z");

    jobject objArrayList = env->NewObject(arrayList, ctor);
    
    JNIArrayList jniArrayList;
    jniArrayList.arrayList = arrayList;
    jniArrayList.objArrayList = objArrayList;
    jniArrayList.add = add;
    jniArrayList.ctor = ctor;
    return jniArrayList;
}

JNIHeader CreateHeader(JNIEnv *env) {
    jclass header = env->FindClass("com/duongame/archive/UnrarHeader");
    jmethodID ctor = env->GetMethodID(header, "<init>", "()V");

    // Field 접근
    jfieldID fileName = env->GetFieldID(header, "fileName", "Ljava/lang/String;");
    jfieldID sizeField = env->GetFieldID(header, "size", "J");
    jfieldID timeField = env->GetFieldID(header, "time", "I");

    JNIHeader jniHeader;
    jniHeader.header = header;
    jniHeader.ctor = ctor;
    jniHeader.objHeader = 0;
    jniHeader.fileName = fileName;
    jniHeader.sizeField = sizeField;
    jniHeader.timeField = timeField;
    return jniHeader;
}

void NewHeader(JNIEnv *env, JNIHeader *header)
{
    header->objHeader = env->NewObject(header->header, header->ctor);
}
