#include <jni.h>
#include "rar.hpp"
#include "unrar.hpp"

extern std::map<int, Unrar*> unrarMap;
extern int maxId;

extern "C" {
    JNIEXPORT jint Java_com_duongame_archive_Unrar_init(JNIEnv *env, jobject thiz, jstring jrarPath) {
        const char *rarPath  = env->GetStringUTFChars(jrarPath, NULL);

        Unrar *rar = new Unrar(env, rarPath);
        maxId++;
        unrarMap.insert(std::pair<int, Unrar*>(maxId, rar));

        LOGE("init %d %s", maxId, rarPath);
        env->ReleaseStringUTFChars(jrarPath, rarPath);
        return maxId;
    }

    JNIEXPORT jobject JNICALL Java_com_duongame_archive_Unrar_getHeaders(JNIEnv *env, jobject thiz, int id)
    {
        Unrar *rar = FindUnrar(id);
        if(rar == NULL) {
            return 0;
        }

        LOGE("getHeaders %d", id); 
        std::vector<RarFileHeader*> *headers = rar->getHeaders();

        if(headers != NULL) {
            // 우선 ArrayList의 클래스를 얻어 옴.
            JNIArrayList jniArrayList = CreateArrayList(env);
            JNIHeader jniHeader = CreateHeader(env);

            for(int i=0; i<headers->size(); i++) {
                RarFileHeader* header = headers->at(i);
                if(header != NULL) {
                    LOGE("getHeaders i=%d %s", i, header->filename.c_str()); 

                    NewHeader(env, &jniHeader);
                    jstring filename = env->NewStringUTF(header->filename.c_str());
                    env->SetObjectField(jniHeader.objHeader, jniHeader.fileName, filename);
                    env->SetLongField(jniHeader.objHeader, jniHeader.sizeField, header->size);
                    env->SetIntField(jniHeader.objHeader, jniHeader.timeField, header->time);

                    env->CallBooleanMethod(jniArrayList.objArrayList, jniArrayList.add, jniHeader.objHeader);

                    // jstring, jobject
                    env->DeleteLocalRef(filename);
                    env->DeleteLocalRef(jniHeader.objHeader);
                    delete header;
                }
            }

            // jclass
            env->DeleteLocalRef(jniHeader.header);
            env->DeleteLocalRef(jniArrayList.arrayList);
            delete headers;
            return jniArrayList.objArrayList;
        }
        return 0;
    }

    // rar 파일에 들어있는 갯수만 체크함
    JNIEXPORT jint JNICALL Java_com_duongame_archive_Unrar_getCount(JNIEnv *env, jobject thiz, int id)
    {
        Unrar *rar = FindUnrar(id);
        if(rar == NULL) {
            return 0;
        }
        return rar->getCount();
    }

    JNIEXPORT jboolean JNICALL Java_com_duongame_archive_Unrar_extractAll(JNIEnv *env, jobject thiz, int id, jstring jdest, jobject jcallback)
    {
        const char *dest = env->GetStringUTFChars(jdest, NULL);

        Unrar *rar = FindUnrar(id);
        if(rar == NULL) {
            env->ReleaseStringUTFChars(jdest, dest);
            return false;
        }

        if(jcallback != 0) {
            jmethodID jprocess = GetCallbackMethodID(env, jcallback);
            rar->setCallback(jcallback, jprocess);
        }

        bool ret = rar->unarchiveAll(dest);
        env->ReleaseStringUTFChars(jdest, dest);
        return ret;
    }

    JNIEXPORT jboolean JNICALL Java_com_duongame_archive_Unrar_extractFile(JNIEnv *env, jobject thiz, int id, jstring jfile, jstring jdest, jobject jcallback)
    {
        const char *dest = env->GetStringUTFChars(jdest, NULL);
        const char *file = env->GetStringUTFChars(jfile, NULL);

        Unrar *rar = FindUnrar(id);
        if(rar == NULL) {
            env->ReleaseStringUTFChars(jdest, dest);
            env->ReleaseStringUTFChars(jfile, file);
            return false;
        }

        if(jcallback != 0) {
            jmethodID jprocess = GetCallbackMethodID(env, jcallback);
            rar->setCallback(jcallback, jprocess);
        }

        bool ret = rar->unarchive(file, dest);
        env->ReleaseStringUTFChars(jdest, dest);
        env->ReleaseStringUTFChars(jfile, file);
        return ret;
    }
}
