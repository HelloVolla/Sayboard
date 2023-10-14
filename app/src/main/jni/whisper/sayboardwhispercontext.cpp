extern "C"{
#include "sayboardwhispercontext.h"
}
#include <cstdint>
#include <vector>

#include "whisper.h"
#include "../../../../../thirdparty/whisper.cpp/whisper.h"


struct SayboardWhisperContext {
    struct whisper_context *ctx;
    int32_t step_ms;
    int32_t length_ms;
    int32_t keep_ms;
    int n_samples_step;
    int n_samples_len;
    int n_samples_keep;
    int n_samples_30s;
    std::vector<float> pcmf32;
    std::vector<float> pcmf32_old;
    std::vector<whisper_token> prompt_tokens;
    struct whisper_full_params wparams;
};

struct SayboardWhisperContext* SayboardWhisperContextInit(const char *model_path, int max_threads) {
    struct SayboardWhisperContext *context = new SayboardWhisperContext();
    context->ctx = whisper_init_from_file(model_path);
    context->step_ms    = 3000;
    context->length_ms  = 10000;
    context->keep_ms    = 200;
    context->n_samples_step = (1e-3*context->step_ms)*WHISPER_SAMPLE_RATE;
    context->n_samples_len  = (1e-3*context->length_ms)*WHISPER_SAMPLE_RATE;
    context->n_samples_keep = (1e-3*context->keep_ms  )*WHISPER_SAMPLE_RATE;
    context->n_samples_30s  = (1e-3*30000.0         )*WHISPER_SAMPLE_RATE;
    context->pcmf32 = std::vector<float>(context->n_samples_30s, 0.0f);
    //TODO: Later on enable voice activity detection
    const bool use_vad = false;
    const int max_tokens = 32;
    const char* language = "en";
    context->wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);

    context->wparams.print_progress   = false;
    context->wparams.print_special    = false;
    context-> wparams.print_realtime   = false;
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

    // Add context from keyboard later on
    context->wparams.prompt_tokens    = 0; //params.no_context ? nullptr : prompt_tokens.data();
    context->wparams.prompt_n_tokens  = 0; //params.no_context ? 0       : prompt_tokens.size();
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

    if (audio_samples_size >= context->n_samples_step) {
        return true;
    }

    const int n_samples_new = audio_samples_size;
    const int n_samples_take = min((int) context->pcmf32_old.size(),
                                   max(0,
                                       context->n_samples_keep + context->n_samples_len - n_samples_new));
    context->pcmf32.resize(n_samples_new + n_samples_take);

    for (int i = 0; i < n_samples_take; i++) {
        context->pcmf32[i] = context->pcmf32_old[context->pcmf32_old.size() - n_samples_take + i];
    }

    memcpy(context->pcmf32.data() + n_samples_take, audio_samples, n_samples_new*sizeof(float));
    context->pcmf32_old = context->pcmf32;
    return true;
}

bool SayboardWhisperContextTranscribe(struct SayboardWhisperContext *context) {
    if (!context) {
        return false;
    }

    if (whisper_full(context->ctx,
                     context->wparams,
                     context->pcmf32.data(),
                     context->pcmf32.size()) != 0) {
        LOGW("Failed to transcribe audio");
        return false;
    }

    return true;
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