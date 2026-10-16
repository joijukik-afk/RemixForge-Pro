#include "LameWrapper.h"
#include "lame.h"
#include <android/log.h>

#define LOG_TAG "LameWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

LameWrapper::LameWrapper() : lame(nullptr), sampleRate(44100), channels(2), initialized(false) {}

LameWrapper::~LameWrapper() {
    if (lame) {
        lame_close(static_cast<lame_global_flags*>(lame));
    }
}

bool LameWrapper::init(int sr, int ch, int bitrateKbps) {
    sampleRate = sr;
    channels = ch;

    lame_global_flags* gfp = lame_init();
    if (!gfp) return false;

    lame_set_in_samplerate(gfp, sr);
    lame_set_num_channels(gfp, ch);
    lame_set_brate(gfp, bitrateKbps);
    lame_set_quality(gfp, 2);         // 2 = high quality
    lame_set_mode(gfp, ch == 1 ? MONO : JOINT_STEREO);

    if (lame_init_params(gfp) < 0) {
        lame_close(gfp);
        return false;
    }

    lame = gfp;
    initialized = true;
    return true;
}

bool LameWrapper::encode(const std::vector<int16_t>& pcm, std::vector<uint8_t>& outMp3) {
    if (!initialized) return false;

    auto* gfp = static_cast<lame_global_flags*>(lame);
    int numSamples = pcm.size() / channels;

    std::vector<uint8_t> buf(numSamples * 5 / 4 + 7200);
    int bytes;

    if (channels == 2) {
        bytes = lame_encode_buffer_interleaved(
            gfp,
            const_cast<short*>(reinterpret_cast<const short*>(pcm.data())),
            numSamples,
            buf.data(),
            buf.size()
        );
    } else {
        bytes = lame_encode_buffer(
            gfp,
            pcm.data(),
            pcm.data(),
            numSamples,
            buf.data(),
            buf.size()
        );
    }

    if (bytes < 0) return false;

    outMp3.insert(outMp3.end(), buf.begin(), buf.begin() + bytes);
    return true;
}

bool LameWrapper::finish(std::vector<uint8_t>& outMp3) {
    if (!initialized) return false;

    auto* gfp = static_cast<lame_global_flags*>(lame);
    std::vector<uint8_t> buf(7200);
    int bytes = lame_encode_flush(gfp, buf.data(), buf.size());
    if (bytes < 0) return false;
    outMp3.insert(outMp3.end(), buf.begin(), buf.begin() + bytes);

    lame_close(gfp);
    lame = nullptr;
    initialized = false;
    return true;
}
