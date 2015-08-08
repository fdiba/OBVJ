#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float weight;
float sharpness = 1.0;

uniform bool drawRoundRect;

varying vec4 vertColor;
varying vec2 center;
varying vec2 pos;

void main() {

  if(drawRoundRect){

    float len = weight/2.0 - length(pos);
    vec4 color = vec4(1.0, 1.0, 1.0, len);
    color = mix(vec4(0.0), color, sharpness);		  
    color = clamp(color, 0.0, 1.0);		
    gl_FragColor = color * vertColor; 
	
  } else {
    gl_FragColor = vertColor;
  }
  
}