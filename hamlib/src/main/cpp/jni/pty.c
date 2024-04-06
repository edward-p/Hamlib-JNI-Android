//
// Created by edward on 4/5/24.
//

#include "pty.h"
#include <jni.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <termio.h>

void foo(JNIEnv const *env, int ptm);

static jobject throw_runtime_exception(JNIEnv *env, char const *message) {
    jclass exClass = (*env)->FindClass(env, "java/lang/RuntimeException");
    (*env)->ThrowNew(env, exClass, message);
    return NULL;
}

jobject JNICALL
Java_xyz_edward_1p_hamlib_Pty_00024Companion_getInstanceNative(JNIEnv *env, jobject thiz) {
    int ptm = open("/dev/ptmx", O_RDWR | O_CLOEXEC | O_NOCTTY | O_NDELAY);
    if (ptm < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "jni.pty", "Cannot open /dev/ptm\n");
        return throw_runtime_exception(env, "Cannot open /dev/ptm\n");
    }

    char devname[64];
    if (grantpt(ptm) || unlockpt(ptm) ||
        ptsname_r(ptm, devname, sizeof(devname))) {
        __android_log_print(ANDROID_LOG_ERROR, "jni.pty",
                            "Cannot grantpt()/unlockpt()/ptsname_r() on /dev/ptmx\n");
        return throw_runtime_exception(env,
                                       "Cannot grantpt()/unlockpt()/ptsname_r() on /dev/ptmx\n");
    }

    struct termios ts;
    if(tcgetattr(ptm, &ts))
    {
        perror("tcgetattr");
        exit(1);
    }

    cfmakeraw(&ts);
    tcsetattr (ptm, TCSANOW, &ts);


    __android_log_print(ANDROID_LOG_INFO, "jni.pty", "opened: %s, fd:%d", devname, ptm);

    jclass pty_class = (*env)->FindClass(env, "xyz/edward_p/hamlib/Pty");
    jmethodID methodId = (*env)->GetMethodID(env, pty_class, "<init>",
                                             "(ILjava/lang/String;)V");
    jobject instance = (*env)->NewObject(env,
                                         pty_class,
                                         methodId,
                                         ptm,
                                         (*env)->NewStringUTF(env,devname));
    return instance;
}