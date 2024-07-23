
#include "csl_types.h"

int get_bytes_in_sample(CSL_DTYPE in) {
    switch(in) {
        case CSL_U8: return CSL_BYTES_IN_SAMPLE_8; break;
        case CSL_S8: return CSL_BYTES_IN_SAMPLE_8; break;
        case CSL_U16: return CSL_BYTES_IN_SAMPLE_16; break;
        case CSL_S16: return CSL_BYTES_IN_SAMPLE_16; break;
        case CSL_U24: return CSL_BYTES_IN_SAMPLE_24; break;
        case CSL_S24: return CSL_BYTES_IN_SAMPLE_24; break;
        case CSL_U32: return CSL_BYTES_IN_SAMPLE_32; break;
        case CSL_S32: return CSL_BYTES_IN_SAMPLE_32; break;
        default: return 0;
    }
}

int get_bytes_in_buffer(CSL_DTYPE in) {
    switch(in) {
        case CSL_U8: return CSL_BYTES_IN_BUFFER_8; break;
        case CSL_S8: return CSL_BYTES_IN_BUFFER_8; break;
        case CSL_U16: return CSL_BYTES_IN_BUFFER_16; break;
        case CSL_S16: return CSL_BYTES_IN_BUFFER_16; break;
        case CSL_U24: return CSL_BYTES_IN_BUFFER_24; break;
        case CSL_S24: return CSL_BYTES_IN_BUFFER_24; break;
        case CSL_U32: return CSL_BYTES_IN_BUFFER_32; break;
        case CSL_S32: return CSL_BYTES_IN_BUFFER_32; break;
        default: return 0;
    }
}

int get_bit_depth(CSL_DTYPE in) {
    switch(in) {
        case CSL_U8: return 8; break;
        case CSL_S8: return 8; break;
        case CSL_U16: return 16; break;
        case CSL_S16: return 16; break;
        case CSL_U24: return 24; break;
        case CSL_S24: return 24; break;
        case CSL_U32: return 32; break;
        case CSL_S32: return 32; break;
        default: return 0;
    }
}

int get_max_value(CSL_DTYPE in) {
    switch(in) {
        case CSL_U8: return CSL_U8_MAX; break;
        case CSL_S8: return CSL_S8_MAX; break;
        case CSL_U16: return CSL_U16_MAX; break;
        case CSL_S16: return CSL_S16_MAX; break;
        case CSL_U24: return CSL_U24_MAX; break;
        case CSL_S24: return CSL_S24_MAX; break;
        case CSL_U32: return CSL_U32_MAX; break;
        case CSL_S32: return CSL_S32_MAX; break;
        default: return 0;
    }
}

int get_min_value(CSL_DTYPE in) {
    switch(in) {
        case CSL_S8: return CSL_S8_MIN; break;
        case CSL_S16: return CSL_S16_MIN; break;
        case CSL_S24: return CSL_S24_MIN; break;
        case CSL_S32: return CSL_S32_MIN; break;
        default: return 0;
    }
}

bool is_signed_type(CSL_DTYPE in) {
    switch(in) {
        case CSL_U8: return false; break;
        case CSL_S8: return true; break;
        case CSL_U16: return false; break;
        case CSL_S16: return true; break;
        case CSL_U24: return false; break;
        case CSL_S24: return true; break;
        case CSL_U32: return false; break;
        case CSL_S32: return true; break;
        default: return 0;
    }
}