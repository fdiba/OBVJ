#define PROCESSING_POINT_SHADER

uniform mat4 projection;
uniform mat4 modelview;

uniform sampler2D tex0; //depth image
uniform sampler2D tex1; //color image
uniform sampler2D tex2; //sound image
uniform float gWidth;
uniform float gHeight;

uniform bool useFFT; //use fft
uniform bool texCutStraight; //change how texFftEnd is used
uniform float texFftStart; //0 1
uniform float texFftEnd; //0 1

uniform bool useColors; //use of colors
uniform float colorTS; //offset colors tex1 x axis
uniform float depth; //create space between points z axis
uniform float amplitude; //alpha for line borders
uniform float damper; //damp sound height in time 0 to 1

attribute vec4 vertex;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;

void main() {

  vec4 myVertex = vertex;
  //myVertex.z = vertColor.r * 255.0 * depth;  

  vec4 pos = modelview * myVertex;
  vec4 clip = projection * pos;
  
  gl_Position = clip + projection * vec4(offset, 0, 0);
   
  //----------- COLOR -----------//
  /*
  vec2 mpos = vec2(vertex.x/gWidth, vertex.y/gHeight); //xy >> 0 1
 
  vertColor = texture2D(tex0, mpos);
  
  
  //DO IT AT THE END
  if(useColors){
	float xMin = vertColor.r + colorTS;
	xMin = clamp(xMin, 0.0, 1.0);
	vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }*/
  
  vertColor = color;

}
