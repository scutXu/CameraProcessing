attribute vec2 pos;
varying vec2 uv;
void main() {
	uv = (pos + vec2(1,1)) / 2.0;
	gl_Position = vec4(pos,0,1);
}