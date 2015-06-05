#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture; //not used
uniform sampler2D tex0; //color image

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
  vertColor = texture2D(tex0, vertTexCoord.st) * color;
  
  myVertex.z = vertColor.r * 255.0 * depth;
   
  //position
  gl_Position = transform * myVertex;
  
  
}