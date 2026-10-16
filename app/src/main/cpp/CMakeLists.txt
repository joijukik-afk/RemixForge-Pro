cmake_minimum_required(VERSION 3.22.1)
project("remixforge")

set(CMAKE_CXX_STANDARD 17)

# SoundTouch sources
add_library(soundtouch STATIC
    soundtouch/SoundTouch.cpp
    soundtouch/FIFOSampleBuffer.cpp
    soundtouch/RateTransposer.cpp
    soundtouch/AAFilter.cpp
    soundtouch/TDStretch.cpp
    soundtouch/FIRFilter.cpp
)

target_include_directories(soundtouch PUBLIC soundtouch)

# LAME sources
add_library(mp3lame STATIC
    lame/bitstream.c
    lame/encoder.c
    lame/fft.c
    lame/gain_analysis.c
    lame/id3tag.c
    lame/lame.c
    lame/mpglib_interface.c
    lame/newmdct.c
    lame/presets.c
    lame/psymodel.c
    lame/quantize.c
    lame/quantize_pvt.c
    lame/reservoir.c
    lame/set_get.c
    lame/tables.c
    lame/takehiro.c
    lame/util.c
    lame/vbrquantize.c
    lame/VbrTag.c
)

target_include_directories(mp3lame PUBLIC lame)
target_compile_definitions(mp3lame PUBLIC HAVE_CONFIG_H=1)

# Native wrapper
add_library(remixforge SHARED
    native-lib.cpp
    SoundTouchWrapper.cpp
    LameWrapper.cpp
)

target_link_libraries(remixforge
    soundtouch
    mp3lame
    android
    log
)
