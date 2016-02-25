//
// Created by Administrator on 2016/2/25.
//

#include "LookupTable.h"
LookupTable::LookupTable()
{
    for(int i=0;i<256;++i) {
        yTable[i] = 1.164f * (i - 16);
        uTable1[i] = 0.392f * (i - 128);
        uTable2[i] = 2.017f * (i - 128);
        vTable1[i] = 1.596f * (i - 128);
        vTable2[i] = 0.813f * (i - 128);
    }
}