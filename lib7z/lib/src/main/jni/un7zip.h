#ifndef __UN7ZIP_H__
#define __UN7ZIP_H__

#include "7zTypes.h"

typedef struct {
    UInt32 blockIndex;
    Byte *outBuffer;
    size_t outBufferSize;
} Z7Buffer;

#endif//__UN7ZIP_H__