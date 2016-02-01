#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES tex;
varying vec2 uv;
void main() {
	gl_FragColor = texture2D(tex,uv);
}