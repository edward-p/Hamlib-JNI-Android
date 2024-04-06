#include <android/log.h>
#include <jni.h>
#include <hamlib/rig.h>
#include "hamlibjni.h"


static RIG *my_rig;


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
    my_rig = rig_init(rig_model);

    if (!my_rig) {
        __android_log_print(ANDROID_LOG_ERROR, "rigInit", "Unknown rig num: %d\n", rig_model);
        __android_log_print(ANDROID_LOG_ERROR, "rigInit", "Please check riglist.h\n");
    }
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigOpen(JNIEnv *env, jobject thiz, jstring dev_name) {
    strncpy(my_rig->state.rigport.pathname, (*env)->GetStringUTFChars(env, dev_name, 0),
            HAMLIB_FILPATHLEN - 1);
    return rig_open(my_rig);
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigClose(JNIEnv *env, jobject thiz) {
    return rig_close(my_rig);
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigCleanUp(JNIEnv *env, jobject thiz) {
    return rig_cleanup(my_rig);
}

JNIEXPORT jint JNICALL
Java_xyz_edward_1p_hamlib_HamlibJNI_rigSetFreq(JNIEnv *env, jobject thiz, jint vfo, jdouble freq) {
    return rig_set_freq(my_rig,vfo,freq);
}