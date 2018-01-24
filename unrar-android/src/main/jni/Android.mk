LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#출력 파일 명 
LOCAL_MODULE    := unrar
#입력 소스 명 
LOCAL_SRC_FILES := interface.cpp unrar.cpp rar.cpp strlist.cpp strfn.cpp pathfn.cpp smallfn.cpp global.cpp file.cpp filefn.cpp filcreat.cpp archive.cpp arcread.cpp unicode.cpp system.cpp isnt.cpp crypt.cpp crc.cpp rawread.cpp encname.cpp resource.cpp match.cpp timefn.cpp rdwrfn.cpp consio.cpp options.cpp errhnd.cpp rarvm.cpp secpassword.cpp rijndael.cpp getbits.cpp sha1.cpp sha256.cpp blake2s.cpp hash.cpp extinfo.cpp extract.cpp volume.cpp list.cpp find.cpp unpack.cpp headers.cpp threadpool.cpp rs16.cpp cmddata.cpp ui.cpp filestr.cpp scantree.cpp dll.cpp qopen.cpp 
LOCAL_CPPFLAGS += -std=c++14 -DLITTLE_ENDIAN -fexceptions -DSILENT -DRARDLL -w
LOCAL_LDLIBS := -llog -lc -landroid
include $(BUILD_SHARED_LIBRARY)