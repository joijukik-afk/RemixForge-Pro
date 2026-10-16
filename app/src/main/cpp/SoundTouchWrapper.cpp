#include "SoundTouchWrapper.h"
#include "SoundTouch.h"
#include <android/log.h>

#define LOG_TAG "SoundTouchWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

SoundTouchWrapper::SoundTouchWrapper(int sr, int ch)
    : sampleRate(sr), channels(ch) {
    soundtouch::SoundTouch* st = new soundtouch::SoundTouch();
    st->setSampleRate(sr);
    st->setChannels(ch);
    st->setSetting(SETTING_USE_QUICKSEEK, 0);
    st->setSetting(SETTING_USE_AA_FILTER, 1);
    soundTouch = st;
}

SoundTouchWrapper::~SoundTouchWrapper() {
    delete static_cast<soundtouch::SoundTouch*>(soundTouch);
}

void SoundTouchWrapper::setTempo(float tempo) {
    static_cast<soundtouch::SoundTouch*>(soundTouch)->setTempo(tempo);
}

void SoundTouchWrapper::setPitch(float semitones) {
    static_cast<soundtouch::SoundTouch*>(soundTouch)->setPitchSemiTones(semitones);
}

void SoundTouchWrapper::setRate(float rate) {
    static_cast<soundtouch::SoundTouch*>(soundTouch)->setRate(rate);
}

std::vector<int16_t> SoundTouchWrapper::process(const std::vector<int16_t>& input) {
    auto* st = static_cast<soundtouch::SoundTouch*>(soundTouch);
    st->putSamples(input.data(), input.size() / channels);

    const int chunk = 8192;
    std::vector<int16_t> output(chunk * channels);
    std::vector<int16_t> result;

    int received;
    do {
        received = st->receiveSamples(output.data(), chunk);
        if (received > 0) {
            result.insert(result.end(), output.begin(), output.begin() + received * channels);
        }
    } while (received > 0);

    return result;
}

std::vector<int16_t> SoundTouchWrapper::flush() {
    auto* st = static_cast<soundtouch::SoundTouch*>(soundTouch);
    st->flush();

    const int chunk = 8192;
    std::vector<int16_t> output(chunk * channels);
    std::vector<int16_t> result;

    int received;
    do {
        received = st->receiveSamples(output.data(), chunk);
        if (received > 0) {
            result.insert(result.end(), output.begin(), output.begin() + received * channels);
        }
    } while (received > 0);

    return result;
}
