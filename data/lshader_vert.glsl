#define PROCESSING_LINE_SHADER

uniform mat4 transform;
uniform vec4 viewport;

uniform mat4 texMatrix; //ADDED

uniform sampler2D tex0; //ADDED
uniform float gWidth;
uniform float gHeight;

uniform float depth; //create space between points z axis

attribute vec4 vertex;
attribute vec4 color;
attribute vec4 direction;

varying vec4 vertColor;

vec3 clipToWindow(vec4 clip, vec4 viewport) {
  vec3 dclip = clip.xyz / clip.w;
  vec2 xypos = (dclip.xy + vec2(1.0, 1.0)) * 0.5 * viewport.zw;
  return vec3(xypos, dclip.z * 0.5 + 0.5);
}
  
void main() {

  //ADDED
  vec2 pos = vec2(vertex.x/gWidth, vertex.y/gHeight);
  vertColor = texture2D(tex0, pos);
  
  vec4 myVertex = vertex;
  myVertex.z = vertColor.r * 255.0 * depth;

  vec4 clip0 = transform * myVertex;
  //vec4 clip0 = transform * vertex;
  vec4 clip1 = clip0 + transform * vec4(direction.xyz, 0);
  
  //TODO PARAM
  float thickness = direction.w;
  
  vec3 win0 = clipToWindow(clip0, viewport); 
  vec3 win1 = clipToWindow(clip1, viewport); 
  vec2 tangent = win1.xy - win0.xy;
    
  vec2 normal = normalize(vec2(-tangent.y, tangent.x));
  vec2 offset = normal * thickness;
  
  gl_Position.xy = clip0.xy + offset.xy;
  gl_Position.zw = clip0.zw;
  
}