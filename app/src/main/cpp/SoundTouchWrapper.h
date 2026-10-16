#ifndef REMIXFORGE_SOUNDTOUCHWRAPPER_H
#define REMIXFORGE_SOUNDTOUCHWRAPPER_H

#include <jni.h>
#include <vector>
#include <cstdint>

class SoundTouchWrapper {
public:
    SoundTouchWrapper(int sampleRate, int channels);
    ~SoundTouchWrapper();

    void setTempo(float tempo);        // 1.0 = normal
    void setPitch(float semitones);    // نیم‌پرده
    void setRate(float rate);

    std::vector<int16_t> process(const std::vector<int16_t>& input);
    std::vector<int16_t> flush();

private:
    void* soundTouch;
    int sampleRate;
    int channels;
};

#endif
