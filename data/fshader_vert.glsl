#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;
//uniform mat3 normalMatrix;

//uniform sampler2D texture; //not used
uniform sampler2D tex0; //color image
uniform sampler2D tex1; //depth image
uniform sampler2D tex2; //sound image

uniform bool useColors; //use of colors
uniform float colorTS; //offset colors tex1 x axis
uniform float depth; //create space between points z axis
uniform float alpha; //alpha for line borders
uniform float amplitude; //alpha for line borders


attribute vec4 vertex;
attribute vec4 color;
attribute vec2 texCoord;
//attribute vec3 normal;

varying vec4 vertColor;
varying vec4 vertTexCoord;
//varying vec3 vertNormal;

void main() {

  vec4 myVertex = vertex;
  
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
  
  //vertColor = texture2D(tex0, vertTexCoord.st) * color;
  vertColor = texture2D(tex0, vertTexCoord.st);
  
  //vertNormal = normalize(normalMatrix * normal);
    
  myVertex.z = vertColor.r * 255.0 * depth;
  
  //sound animation
  vec4 vertSoundColor = texture2D(tex2, vertTexCoord.st);
  float test = vertSoundColor.r - 0.5;
  //TODO ADD PARAM max 0 to 1 and texCoord[1]
  test *= texCoord[1];
  myVertex.z += test*amplitude;
  
  
  if(useColors){
	float xMin = vertColor.r + colorTS;
	xMin = clamp(xMin, 0.0, 1.0);
	vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }
    
  gl_Position = transform * myVertex;
  
  if( mod(myVertex.y, 5.0) > 0.5 ) vertColor.a = alpha;
  
}