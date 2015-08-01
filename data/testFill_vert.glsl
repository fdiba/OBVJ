#define PROCESSING_POINT_SHADER

uniform mat4 projection;
uniform mat4 modelview;

uniform float weight;

uniform bool useColors; //use of colors
uniform sampler2D tex1; //color image
uniform float gWidth;
uniform float gHeight;
 
attribute vec4 vertex;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 texCoord;

void main() {

  vec4 pos = modelview * vertex;
  vec4 clip = projection * pos;
  
  gl_Position = clip + projection * vec4(offset, 0, 0);
  
  //texCoord = (vec2(0.5) + offset / weight);

  vec4 col = vec4(1.0, 1.0, 1.0, 1.0);

  if(useColors){
  	vec2 tex2Pos = vec2(vertex.x/gWidth, vertex.y/gHeight);
  	col = texture2D(tex1, tex2Pos);
  }

  //vertColor = color;
  vertColor = col;
}