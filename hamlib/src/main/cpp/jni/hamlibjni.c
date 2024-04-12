#include <android/log.h>
#include <jni.h>
#include <hamlib/rig.h>
#include <fcntl.h>
#include <termios.h>
#include <stdlib.h>
#include <unistd.h>
#include "hamlibjni.h"


static int ptm = -1;
static RIG *my_rig;

static int throw_runtime_exception(JNIEnv *env, char const *message) {
    jclass exClass = (*env)->FindClass(env, "java/lang/RuntimeException");
    (*env)->ThrowNew(env, exClass, message);
    return -1;
}

static int createPtm(JNIEnv *env, jobject thiz) {

    if (fcntl(ptm, F_GETFD) != -1) {
        close(ptm);
    }

    ptm = open("/dev/ptmx", O_RDWR | O_CLOEXEC | O_NOCTTY | O_NDELAY);
    if (ptm < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "jni.pty", "Cannot open /dev/ptm\n");
        return throw_runtime_exception(env, "Cannot open /dev/ptm\n");
    }

    struct termios ts;
    if (tcgetattr(ptm, &ts)) {
        perror("tcgetattr");
        exit(1);
    }

    cfmakeraw(&ts);
    tcsetattr(ptm, TCSANOW, &ts);

    return ptm;
}


JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigLoadAllBackends(JNIEnv *env, jobject thiz) {
    return rig_load_all_backends();
}

static int __rig_list_size(const struct rig_caps *caps, rig_ptr_t data) {
    int *size = (int *) data;
    (*size)++;
    return -1;
}

static int rig_list_size() {
    int size = 0;
    rig_list_foreach(__rig_list_size, &size);
    return size;
}

static int __insert_rig(const struct rig_caps *caps, rig_ptr_t data) {
    struct rig_data *rig_data = (struct rig_data *) data;
    rig_data->data[rig_data->index++] = *caps;
    return -1;
}

JNIEXPORT jobjectArray JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_getAllRigs(JNIEnv *env, jobject thiz) {

    int size = rig_list_size();
    __android_log_print(ANDROID_LOG_DEBUG, "getAllRigs", "%d", size);

    struct rig_data rig_data;
    rig_data.index = 0;
    struct rig_caps rigcaps[size];
    rig_data.data = rigcaps;
    rig_list_foreach(__insert_rig, &rig_data);

    jclass rig_class = (*env)->FindClass(env, "xyz/edward_p/hamlib/data/Rig");
    jobjectArray arr = (*env)->NewObjectArray(env, size, rig_class, NULL);

    for (int i = 0; i < rig_data.index; i++) {
        struct rig_caps caps = rig_data.data[i];
        jmethodID methodId = (*env)->GetMethodID(env, rig_class, "<init>",
                                                 "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
        jobject obj = (*env)->NewObject(env,
                                        rig_class,
                                        methodId,
                                        (int) caps.rig_model,
                                        (*env)->NewStringUTF(env, caps.model_name),
                                        (*env)->NewStringUTF(env, caps.mfg_name),
                                        (*env)->NewStringUTF(env, caps.version),
                                        (*env)->NewStringUTF(env, caps.copyright),
                                        (int) caps.port_type);

        (*env)->SetObjectArrayElement(env, arr, i, obj);
    }

    return arr;
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigInit(JNIEnv *env, jobject thiz, jint rig_model) {

    if (my_rig != NULL) {
        rig_cleanup(my_rig);
    }

    my_rig = rig_init(rig_model);

    if (!my_rig) {
        __android_log_print(ANDROID_LOG_ERROR, "rigInit", "Unknown rig num: %d\n", rig_model);
        __android_log_print(ANDROID_LOG_ERROR, "rigInit", "Please check riglist.h\n");
    }

    // disable auto_power_on and auto_power_off
    my_rig->state.auto_power_on = 0;
    my_rig->state.auto_power_off = 0;

    // set rig port
    createPtm(env, thiz);
    char devname[64];
    if (grantpt(ptm) || unlockpt(ptm) ||
        ptsname_r(ptm, devname, sizeof(devname))) {
        __android_log_print(ANDROID_LOG_ERROR, "jni.pty",
                            "Cannot grantpt()/unlockpt()/ptsname_r() on /dev/ptmx\n");
        throw_runtime_exception(env,
                                "Cannot grantpt()/unlockpt()/ptsname_r() on /dev/ptmx\n");
        return -1;
    }
    __android_log_print(ANDROID_LOG_INFO, "jni.pty", "opened: %s, fd:%d", devname, ptm);

    strncpy(my_rig->state.rigport.pathname, devname, HAMLIB_FILPATHLEN - 1);

    return 0;
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigOpen(JNIEnv *env, jobject thiz) {
    return rig_open(my_rig);
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigCleanUp(JNIEnv *env, jobject thiz) {
    rig_cleanup(my_rig);

    my_rig = NULL;

    if (fcntl(ptm, F_GETFD) != -1) {
        close(ptm);
        ptm = -1;
    }

    return 1;
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigSetFreq(JNIEnv *env, jobject thiz, jint vfo, jdouble freq) {
    return rig_set_freq(my_rig, vfo, freq);
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_getPtm(JNIEnv *env, jobject thiz) {
    return ptm;
}