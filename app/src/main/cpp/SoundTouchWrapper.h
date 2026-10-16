#ifndef REMIXFORGE_SOUNDTOUCHWRAPPER_H
#define REMIXFORGE_SOUNDTOUCHWRAPPER_H

#include <vector>
#include <cstdint>

class SoundTouchWrapper {
public:
    SoundTouchWrapper(int sampleRate, int channels);
    ~SoundTouchWrapper();
    void setTempo(float tempo);
    void setPitch(float semitones);
    std::vector<int16_t> process(const std::vector<int16_t>& input);
    std::vector<int16_t> flush();
private:
    void* st;
    int sampleRate;
    int channels;
};

#endif
