LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
               hamlibjni.c \
               pty.c

LOCAL_MODULE := hamlibjni

LOCAL_C_INCLUDES := $(PROJECT_PATH)/hamlib/include $(PROJECT_PATH)/hamlib

LOCAL_SHARED_LIBRARIES := hamlib

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
