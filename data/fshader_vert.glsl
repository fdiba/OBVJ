#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture; //not used
uniform sampler2D tex0;
uniform sampler2D tex1;

uniform bool useColors; //use of colors
uniform float colorTS; //offset colors tex1 x axis
uniform float depth; //create space between points z axis
uniform float alpha; //alpha for line borders


attribute vec4 vertex;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  vec4 myVertex = vertex;
  
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
  
  vertColor = texture2D(tex0, vertTexCoord.st) * color;
  
  myVertex.z = vertColor.r * 255.0 * depth;
    
  if(useColors){
  
	float xMin = vertColor.r + colorTS;
	xMin = clamp(xMin, 0.0, 1.0);
  
	vertColor = texture2D(tex1, vec2(xMin, 0.0)) * color;
  }
    
  gl_Position = transform * myVertex;
  
  if( mod(myVertex.y, 5.0) > 0.5 ) vertColor.a = alpha;
  
}