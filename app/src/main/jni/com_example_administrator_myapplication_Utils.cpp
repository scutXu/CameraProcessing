#include"com_example_administrator_myapplication_Utils.h"
#include "LookupTable.h"
JNIEXPORT void Java_com_example_administrator_myapplication_Utils_nativeColorSpaceConvertion
  (JNIEnv *env, jclass obj, jobject yBuffer, jobject uBuffer, jobject vBuffer, jobject argbBuffer,
  	jint yRowStride, jint uvRowStride, jint width, jint height)
{
	static LookupTable table;

	unsigned char * yBufPtr = (unsigned char *)(env->GetDirectBufferAddress(yBuffer));
	unsigned char * uBufPtr = (unsigned char *)(env->GetDirectBufferAddress(uBuffer));
	unsigned char * vBufPtr = (unsigned char *)(env->GetDirectBufferAddress(vBuffer));
	unsigned int * argbBufPtr = (unsigned int *)(env->GetDirectBufferAddress(argbBuffer));


	int y, u, v, r, g, b, argb;
	unsigned char * yLinePtr, * uLinePtr, * vLinePtr;
	unsigned int * argbLinePtr;


	int t;
	for(int i=0;i<height;++i) {

		yLinePtr = yBufPtr + (i * yRowStride);
		argbLinePtr = argbBufPtr + i * width;
		int uvOffset = (i >> 1) * uvRowStride;
		uLinePtr = uBufPtr + uvOffset;
		vLinePtr = vBufPtr + uvOffset;

		t = 0;
		for(int j=0;j<width;++j) {

			y = *(yLinePtr++);
			u = *uLinePtr;
			uLinePtr += t;
			v = *vLinePtr;
			vLinePtr += t;
			t = 1 - t;

			/*r = (int)(1.164f * (y - 16)  + (1.596f * (v - 128)));
            g = (int)(1.164f * (y - 16) - (0.813f * (v - 128)) - (0.392f * (u - 128)));
            b = (int)(1.164f * (y - 16) + (2.017f * (u - 128)));*/
			int ty = table.yTable[y];
			r = (int)(ty + table.vTable1[v]);
			g = (int)(ty - table.vTable2[v] - table.uTable1[u]);
			b = (int)(ty + table.uTable2[u]);


            if(r > 255) {
            	r = 255;
            }
            else {
            	if(r < 0) {
            		r = 0;
            	}
            }

            if(g > 255) {
            	g = 255;
            }
            else {
            	if(g < 0) {
            		g = 0;
            	}
            }

            if(b > 255) {
            	b = 255;
            }
            else {
            	if(b < 0) {
            		b = 0;
            	}
            }

            argb = 0;
            argb |= r;
            argb |= g << 8;
            argb |= b << 16;
            argb |= 255 << 24;

            *argbLinePtr++ = argb;
		}
	}
}