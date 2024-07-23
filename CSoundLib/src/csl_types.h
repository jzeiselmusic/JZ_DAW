#ifndef CSL_TYPES_H
#define CSL_TYPES_H

#include <soundio/soundio.h>
#include <stdint.h>

typedef enum {
    CSL_U8,   
    CSL_S8,                     
    CSL_U16,
    CSL_U32,
    CSL_S16,
    CSL_S32,
    CSL_U24,
    CSL_S24,
    CSL_FL32,
    CSL_FL64,
} CSL_DTYPE;

#define emptyString                   ""
#define ATTACK                        0.001
#define RELEASE                       0.15

#define CSL_U8_MAX                    UINT8_MAX
#define CSL_S8_MAX                    INT8_MAX
#define CSL_S8_MIN                    INT8_MIN
#define CSL_U16_MAX                   UINT16_MAX
#define CSL_S16_MAX                   INT16_MAX
#define CSL_S16_MIN                   INT16_MIN
#define CSL_U24_MAX                   16777215
#define CSL_S24_MAX                   8388607
#define CSL_S24_MIN                   -8388608
#define CSL_U32_MAX                   UINT32_MAX
#define CSL_S32_MAX                   INT32_MAX
#define CSL_S32_MIN                   INT32_MIN

#define CSL_BYTES_IN_SAMPLE_8         1
#define CSL_BYTES_IN_SAMPLE_16        2
#define CSL_BYTES_IN_SAMPLE_24        3
#define CSL_BYTES_IN_SAMPLE_32        4

#define CSL_BYTES_IN_BUFFER_8         1
#define CSL_BYTES_IN_BUFFER_16        2
#define CSL_BYTES_IN_BUFFER_24        4
#define CSL_BYTES_IN_BUFFER_32        4

#define CSL_MAX_METRONOME_BUF_SIZE    4 * 48000 * CSL_BYTES_IN_BUFFER_32

int get_bytes_in_sample(CSL_DTYPE in);

int get_bytes_in_buffer(CSL_DTYPE in);

int get_bit_depth(CSL_DTYPE in);

int get_max_value(CSL_DTYPE in);

int get_min_value(CSL_DTYPE in);

bool is_signed_type(CSL_DTYPE in);

#endif