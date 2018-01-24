APP_ABI := armeabi-v7a
APP_PLATFORM := android-14
APP_DEPRECATED_HEADERS := true

# 4.9로 설정해야 한다. 
NDK_TOOLCHAIN_VERSION := 4.9
# APP_STL := stlport_shared  --> does not seem to contain C++11 features
APP_STL := gnustl_shared

# Enable c++11 extentions in source code
APP_CPPFLAGS += -std=c++14