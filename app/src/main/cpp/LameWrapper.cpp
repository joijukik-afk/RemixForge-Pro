#include "LameWrapper.h"
#include "lame.h"

LameWrapper::LameWrapper() : gfp(nullptr), sampleRate(44100), channels(2), ok(false) {}

LameWrapper::~LameWrapper() {
    if (gfp) lame_close(static_cast<lame_global_flags*>(gfp));
}

bool LameWrapper::init(int sr, int ch, int br) {
    sampleRate = sr; channels = ch;
    lame_global_flags* g = lame_init();
    if (!g) return false;
    lame_set_in_samplerate(g, sr);
    lame_set_num_channels(g, ch);
    lame_set_brate(g, br);
    lame_set_quality(g, 2);
    lame_set_mode(g, ch == 1 ? MONO : JOINT_STEREO);
    if (lame_init_params(g) < 0) { lame_close(g); return false; }
    gfp = g; ok = true;
    return true;
}

bool LameWrapper::encode(const std::vector<int16_t>& pcm, std::vector<uint8_t>& out) {
    if (!ok) return false;
    auto* g = static_cast<lame_global_flags*>(gfp);
    int samples = pcm.size() / channels;
    std::vector<uint8_t> buf(samples * 5 / 4 + 7200);
    int bytes;
    if (channels == 2) {
        bytes = lame_encode_buffer_interleaved(g,
            const_cast<short*>(reinterpret_cast<const short*>(pcm.data())),
            samples, buf.data(), buf.size());
    } else {
        bytes = lame_encode_buffer(g, pcm.data(), pcm.data(), samples, buf.data(), buf.size());
    }
    if (bytes < 0) return false;
    out.insert(out.end(), buf.begin(), buf.begin() + bytes);
    return true;
}

bool LameWrapper::finish(std::vector<uint8_t>& out) {
    if (!ok) return false;
    auto* g = static_cast<lame_global_flags*>(gfp);
    std::vector<uint8_t> buf(7200);
    int bytes = lame_encode_flush(g, buf.data(), buf.size());
    if (bytes < 0) return false;
    out.insert(out.end(), buf.begin(), buf.begin() + bytes);
    lame_close(g);
    gfp = nullptr; ok = false;
    return true;
}
