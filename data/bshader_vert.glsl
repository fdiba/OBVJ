#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture; //not used
uniform sampler2D tex0; //depth image
uniform sampler2D tex1; //color image
uniform sampler2D tex2; //sound image

uniform float depth; //create space between points z axis

attribute vec4 vertex;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  vec4 myVertex = vertex;
  
  //color
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
  vertColor = texture2D(tex0, vertTexCoord.st);
  
  myVertex.z = vertColor.r * 255.0 * depth;
  
  /*
  //TODO AS PARAM
  //xVal AND yVal CAN NOT BE TOO SMALL
  float yVal = 40.0;
  float xVal = 40.0;
  
  if (mod(vertex.x, xVal)==0){
	vertColor = vec4(0, 1.0, 0.0, 1.0); //green
  } else if(mod(vertex.y, yVal)==0.0){
	vertColor = vec4(0, 1.0, 0.0, 1.0); //green
  } else {
	//vertColor = vec4(1.0, 0.0, 0.0, 1.0); //red
  }*/
   
  //position
  gl_Position = transform * myVertex;
  
  
}