#ifndef REMIXFORGE_LAMEWRAPPER_H
#define REMIXFORGE_LAMEWRAPPER_H

#include <vector>
#include <cstdint>

class LameWrapper {
public:
    LameWrapper();
    ~LameWrapper();
    bool init(int sampleRate, int channels, int bitrateKbps);
    bool encode(const std::vector<int16_t>& pcm, std::vector<uint8_t>& out);
    bool finish(std::vector<uint8_t>& out);
private:
    void* gfp;
    int sampleRate;
    int channels;
    bool ok;
};

#endif
