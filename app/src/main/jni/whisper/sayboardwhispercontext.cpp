extern "C"{
#include "sayboardwhispercontext.h"
}
#include <cstdint>
#include <vector>

#include "whisper.h"
#include "../../../../../thirdparty/whisper.cpp/whisper.h"
#include "../../../../../thirdparty/whisper.cpp/examples/common.h"

#include<arpa/inet.h>
#include<sys/socket.h>
#include <unistd.h>

struct SayboardWhisperContext {
    struct whisper_context *ctx;
    int32_t step_ms;
    int32_t length_ms;
    int32_t keep_ms;
    int n_samples_step;
    int n_samples_len;
    int n_samples_keep;

    std::vector<float> pcmf32;
    std::vector<float> pcmf32_old;
    std::vector<float> pcmf32_new;
    std::vector<whisper_token> prompt_tokens;
    struct whisper_full_params wparams;

    int sockfd;
    struct sockaddr_in servaddr;
};

struct SayboardWhisperContext* SayboardWhisperContextInit(const char *model_path, int max_threads) {
    struct SayboardWhisperContext *context = new SayboardWhisperContext();
    struct whisper_context_params cparams;
    cparams.use_gpu = false;
    context->ctx = whisper_init_from_file_with_params(model_path, cparams);
    context->step_ms    = 3000;
    context->length_ms  = 10000;
    //context->length_ms  = 6000;
    context->keep_ms    = 200;
    context->n_samples_step = (1e-3*context->step_ms)*WHISPER_SAMPLE_RATE;
    context->n_samples_len  = (1e-3*context->length_ms)*WHISPER_SAMPLE_RATE;
    context->n_samples_keep = (1e-3*context->keep_ms  )*WHISPER_SAMPLE_RATE;

    //TODO: Later on enable voice activity detection
    const bool use_vad = false;
    const int max_tokens = 32;
    const char* language = "en";
    context->wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);

    context->wparams.print_progress   = false;
    context->wparams.print_special    = false;
    context->wparams.print_realtime   = false;
    context->wparams.print_timestamps = false;
    context->wparams.translate        = false;
    context->wparams.single_segment   = !use_vad;
    context->wparams.max_tokens       = max_tokens;
    context->wparams.language         = language;
    context->wparams.n_threads        = max_threads;

    context->wparams.audio_ctx        = 0;
    context->wparams.speed_up         = false;

    context->wparams.tdrz_enable      = false; // [TDRZ]

    // disable temperature fallback
    //wparams.temperature_inc  = -1.0f;

    // Add context from keyboard, based on the previous recognized sentence
    context->wparams.prompt_tokens    = context->prompt_tokens.data();
    context->wparams.prompt_n_tokens  = context->prompt_tokens.size();

    return context;
}

bool SayboardWhisperContextDestroy(struct SayboardWhisperContext *context) {
    if (context == nullptr) {
        return false;
    }

    whisper_free(context->ctx);
    delete context;
    return true;
}

int SayboardWhisperSampleRate() {
   return WHISPER_SAMPLE_RATE;
}

bool SayboardWhisperContextAcceptAudio(struct SayboardWhisperContext *context,
        float *audio_samples,
        int audio_samples_size) {
    if (!context) {
        return false;
    }

    if (audio_samples_size > 2 * context->n_samples_step) {
        LOGW("WARNING: cannot process audio fast enough, dropping audio ...");
        return false;
    }

    size_t currentSamplesCount = context->pcmf32_new.size();
    context->pcmf32_new.resize(currentSamplesCount + audio_samples_size);
    memcpy(context->pcmf32_new.data() + currentSamplesCount, audio_samples, audio_samples_size);

    if (context->pcmf32_new.size() < context->n_samples_step) {
        LOGI("Audio Samples not enough (%d): %lu", context->n_samples_step, context->pcmf32_new.size());
        return false;
    }

    const int n_samples_new = context->pcmf32_new.size();
    const int n_samples_take = min((int) context->pcmf32_old.size(),
                                   max(0,
                                       context->n_samples_keep + context->n_samples_len - n_samples_new));
    context->pcmf32.resize(n_samples_new + n_samples_take);

    for (int i = 0; i < n_samples_take; i++) {
        context->pcmf32[i] = context->pcmf32_old[context->pcmf32_old.size() - n_samples_take + i];
    }

    memcpy(context->pcmf32.data() + n_samples_take, context->pcmf32_new.data(), n_samples_new*sizeof(float));
    context->pcmf32_old = context->pcmf32;
    context->pcmf32_new.clear();

    return true;
}

bool SayboardWhisperContextTranscribe(struct SayboardWhisperContext *context) {
    if (!context) {
        return false;
    }

    bool result = true;
    if (whisper_full(context->ctx,
                     context->wparams,
                     context->pcmf32.data(),
                     context->pcmf32.size()) == 0) {
        context->prompt_tokens.clear();
        const int n_segments = whisper_full_n_segments(context->ctx);
        for (int i = 0; i < n_segments; ++i) {
            const int token_count = whisper_full_n_tokens(context->ctx, i);
            for (int j = 0; j < token_count; ++j) {
                context->prompt_tokens.push_back(whisper_full_get_token_id(context->ctx, i, j));
            }
        }
        context->wparams.prompt_tokens    = context->prompt_tokens.data();
        context->wparams.prompt_n_tokens  = context->prompt_tokens.size();
        context->pcmf32_old = std::vector<float>(context->pcmf32.end() - context->n_samples_keep, context->pcmf32.end());
    } else {
        LOGW("Failed to transcribe audio");
        result = false;
    }

    return result;
}

int SayboardWhisperContextGetTextSegmentCount(struct SayboardWhisperContext *context) {
    if (!context) {
        return -1;
    }

    return whisper_full_n_segments(context->ctx);
}

const char* SayboardWhisperContextGetTextSegment(struct SayboardWhisperContext *context, int index) {
    if (!context) {
        return nullptr;
    }

    return whisper_full_get_segment_text(context->ctx, index);
}