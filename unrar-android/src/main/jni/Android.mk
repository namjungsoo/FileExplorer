LOCAL_PATH := $(call my-dir)

# include $(CLEAR_VARS)
# LOCAL_MODULE := static_unrar
# LOCAL_SRC_FILES := ../lib/libunrar.a
# include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
#출력 파일 명 
LOCAL_MODULE    := unrar
#입력 소스 명 
LOCAL_SRC_FILES := interface.cpp unrar.cpp rar.cpp strlist.cpp strfn.cpp pathfn.cpp smallfn.cpp global.cpp file.cpp filefn.cpp filcreat.cpp archive.cpp arcread.cpp unicode.cpp system.cpp isnt.cpp crypt.cpp crc.cpp rawread.cpp encname.cpp resource.cpp match.cpp timefn.cpp rdwrfn.cpp consio.cpp options.cpp errhnd.cpp rarvm.cpp secpassword.cpp rijndael.cpp getbits.cpp sha1.cpp sha256.cpp blake2s.cpp hash.cpp extinfo.cpp extract.cpp volume.cpp list.cpp find.cpp unpack.cpp headers.cpp threadpool.cpp rs16.cpp cmddata.cpp ui.cpp filestr.cpp scantree.cpp dll.cpp qopen.cpp 
#LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
#cpp 플래그
#LOCAL_CPPFLAGS += -std=c++14 -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -DLITTLE_ENDIAN -fexceptions -DSILENT -DRARDLL
LOCAL_CPPFLAGS += -std=c++14 -DLITTLE_ENDIAN -fexceptions -DSILENT -DRARDLL -w

#추가 라이브러리 
#LIB_PATH := $(LOCAL_PATH)/../lib
#LOCAL_SHARE_LIBRARIES := libunrar.so
#LOCAL_STATIC_LIBRARIES := static_unrar

LOCAL_LDLIBS := -llog -lc -landroid
#LOCAL_LDLIBS := -L$(LIB_PATH) -lunrar
include $(BUILD_SHARED_LIBRARY)