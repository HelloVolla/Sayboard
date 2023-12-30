#pragma once

#include <stdbool.h>
#include <android/log.h>


#define UNUSED(x) (void)(x)
#define TAG "JNI"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,     TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,     TAG, __VA_ARGS__)

static inline int min(int a, int b) {
    return (a < b) ? a : b;
}

static inline int max(int a, int b) {
    return (a > b) ? a : b;
}

struct SayboardWhisperContext;

struct SayboardWhisperContext *SayboardWhisperContextInit(const char *model_path, int max_threads);
bool SayboardWhisperContextDestroy(struct SayboardWhisperContext *context);
int SayboardWhisperSampleRate();

// Returns true when there is enough audio to perform inference
bool SayboardWhisperContextAcceptAudio(struct SayboardWhisperContext *context,
                                       float *audio_samples,
                                       int audio_samples_size);
bool SayboardWhisperContextTranscribe(struct SayboardWhisperContext *context);
int SayboardWhisperContextGetTextSegmentCount(struct SayboardWhisperContext *context);
const char *SayboardWhisperContextGetTextSegment(struct SayboardWhisperContext *context, int index);