#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D sprite;

uniform float weight;
uniform bool drawRoundRect;

varying vec4 vertColor;
varying vec2 texCoord;

varying vec2 pos;

void main() {

	gl_FragColor = vertColor;
	//gl_FragColor = texture2D(sprite, texCoord) * vertColor;  

	if(drawRoundRect){

		float len = weight/2.0 - length(pos);
	    gl_FragColor.a = min(len, vertColor.a);
	
  	}


}