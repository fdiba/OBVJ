#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D sprite;

varying vec4 vertColor;
varying vec2 texCoord;

void main() {
	vec4 col = vec4(1.0, .0, .0, 1.0);
	//gl_FragColor = texture2D(sprite, texCoord) * vertColor;  
  	gl_FragColor = col;
}