#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture;
uniform sampler2D tex0;
//uniform sampler2D tex1;

attribute vec4 vertex;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  vec4 myVertex = vertex;
  
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
  
  vertColor = texture2D(tex0, vertTexCoord.st) * color;
  
  myVertex.z = (vertColor.r) * 100 - 50;
  
  //myVertex.z = clamp(myVertex.z, -50, 50);
  
  //vertColor = texture2D(tex1, vec2(0.0, vertColor.r  )) * color;
      
  gl_Position = transform * myVertex;
  
  if( mod(myVertex.y, 5.0) > 0.5 ) vertColor.a = 0.0;
  
}