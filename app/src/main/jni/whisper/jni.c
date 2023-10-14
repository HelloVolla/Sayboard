#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include <stdlib.h>
#include <sys/sysinfo.h>
#include <string.h>
#include "sayboardwhispercontext.h"

JNIEXPORT jlong JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_initContext(
        JNIEnv *env, jobject thiz, jstring model_path_str) {
    UNUSED(thiz);
    struct SayboardWhisperContext *context = NULL;
    const char *model_path_chars = (*env)->GetStringUTFChars(env, model_path_str, NULL);
    // Leave 2 processors free (i.e. the high-efficiency cores).
    context = SayboardWhisperContextInit(model_path_chars,
                                         max(1, min(8, get_nprocs() - 2)));
    (*env)->ReleaseStringUTFChars(env, model_path_str, model_path_chars);
    return (jlong) context;
}

JNIEXPORT void JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_freeContext(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    SayboardWhisperContextDestroy((struct SayboardWhisperContext*)context_ptr);
}

JNIEXPORT void JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_fullTranscribe(
        JNIEnv *env, jobject thiz, jlong context_ptr, jfloatArray audio_data) {
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    jfloat *audio_data_arr = (*env)->GetFloatArrayElements(env, audio_data, NULL);
    const jsize audio_data_length = (*env)->GetArrayLength(env, audio_data);


    // get audio samples

    if (SayboardWhisperContextAcceptAudio((struct SayboardWhisperContext*) context_ptr,
            audio_data_arr,
            audio_data_length))
    {
        SayboardWhisperContextTranscribe((struct SayboardWhisperContext*) context_ptr);
    }

    (*env)->ReleaseFloatArrayElements(env, audio_data, audio_data_arr, JNI_ABORT);
}

JNIEXPORT jint JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_getTextSegmentCount(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    return SayboardWhisperContextGetTextSegmentCount((struct SayboardWhisperContext*) context_ptr);
}

JNIEXPORT jstring JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_getTextSegment(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint index) {
    UNUSED(thiz);
    const char *text = SayboardWhisperContextGetTextSegment(
            (struct SayboardWhisperContext*)context_ptr,
            index);
    jstring string = (*env)->NewStringUTF(env, text);
    return string;
}

JNIEXPORT int JNICALL
Java_com_elishaazaria_sayboard_ime_recognizers_WhisperLib_getSampleRate(
        JNIEnv *env, jobject thiz) {
    UNUSED(thiz);
    return SayboardWhisperSampleRate();
}