#include "SoundTouchWrapper.h"
#include "SoundTouch.h"

SoundTouchWrapper::SoundTouchWrapper(int sr, int ch) : sampleRate(sr), channels(ch) {
    auto* s = new soundtouch::SoundTouch();
    s->setSampleRate(sr);
    s->setChannels(ch);
    s->setSetting(SETTING_USE_QUICKSEEK, 0);
    s->setSetting(SETTING_USE_AA_FILTER, 1);
    st = s;
}

SoundTouchWrapper::~SoundTouchWrapper() {
    delete static_cast<soundtouch::SoundTouch*>(st);
}

void SoundTouchWrapper::setTempo(float tempo) {
    static_cast<soundtouch::SoundTouch*>(st)->setTempo(tempo);
}

void SoundTouchWrapper::setPitch(float semitones) {
    static_cast<soundtouch::SoundTouch*>(st)->setPitchSemiTones(semitones);
}

std::vector<int16_t> SoundTouchWrapper::process(const std::vector<int16_t>& input) {
    auto* s = static_cast<soundtouch::SoundTouch*>(st);
    s->putSamples(input.data(), input.size() / channels);
    const int chunk = 8192;
    std::vector<int16_t> buf(chunk * channels);
    std::vector<int16_t> out;
    int n;
    do {
        n = s->receiveSamples(buf.data(), chunk);
        if (n > 0) out.insert(out.end(), buf.begin(), buf.begin() + n * channels);
    } while (n > 0);
    return out;
}

std::vector<int16_t> SoundTouchWrapper::flush() {
    auto* s = static_cast<soundtouch::SoundTouch*>(st);
    s->flush();
    const int chunk = 8192;
    std::vector<int16_t> buf(chunk * channels);
    std::vector<int16_t> out;
    int n;
    do {
        n = s->receiveSamples(buf.data(), chunk);
        if (n > 0) out.insert(out.end(), buf.begin(), buf.begin() + n * channels);
    } while (n > 0);
    return out;
}
