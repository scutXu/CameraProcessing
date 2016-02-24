#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES tex;
varying vec2 uv;
void main() {
	vec3 rgb = (texture2D(tex,uv)).rgb;
	float gray = (rgb.r + rgb.g + rgb.b) / 3.0;
	gl_FragColor = vec4(gray,gray,gray,1);
}