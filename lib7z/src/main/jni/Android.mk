LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#출력 파일 명 
LOCAL_MODULE    := 7z

#입력 소스 명 
LOCAL_SRC_FILES := un7zip.cpp 7zExtracter.c 7zFunctions.c \
	src/7zAlloc.c src/7zArcIn.c src/7zAssetFile.c src/7zBuf.c src/7zBuf2.c \
	src/7zCrc.c src/7zCrcOpt.c src/7zDec.c src/7zFile.c src/7zStream.c \
	src/Bcj2.c src/Bra.c src/Bra86.c src/BraIA64.c src/CpuArch.c \
	src/Delta.c src/Lzma2Dec.c src/LzmaDec.c src/Ppmd7.c src/Ppmd7Dec.c

#c 플래그
LOCAL_CFLAGS += -w -fpermissive -DNATIVE_LOG
LOCAL_C_INCLUDES += $(LOCAL_PATH)/src
LOCAL_LDLIBS := -landroid -llog

include $(BUILD_SHARED_LIBRARY)