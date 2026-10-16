#ifndef REMIXFORGE_LAMEWRAPPER_H
#define REMIXFORGE_LAMEWRAPPER_H

#include <string>
#include <vector>
#include <cstdint>

class LameWrapper {
public:
    LameWrapper();
    ~LameWrapper();

    bool init(int sampleRate, int channels, int bitrateKbps);
    bool encode(const std::vector<int16_t>& pcm, std::vector<uint8_t>& outMp3);
    bool finish(std::vector<uint8_t>& outMp3);

private:
    void* lame;
    int sampleRate;
    int channels;
    bool initialized;
};

#endif
