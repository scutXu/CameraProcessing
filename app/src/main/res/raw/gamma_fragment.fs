#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES tex;
varying vec2 uv;
void main() {
	vec3 pixelColor = texture2D(tex,uv).xyz;

	gl_FragColor = vec4(pow(pixelColor.x,0.4),pow(pixelColor.y,0.4),pow(pixelColor.z,0.4),1);
}