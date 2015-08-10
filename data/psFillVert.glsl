#define PROCESSING_POINT_SHADER

uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 transform;

uniform bool useColors; //use of colors
uniform sampler2D tex1; //color image
uniform float gWidth;
uniform float gHeight;
uniform float normal[3];
uniform float focalPlane[3];
uniform float strokeAlpha;

 
attribute vec4 vertex;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 center;
varying vec2 texCoord;
varying vec2 pos;

float getDistToPoint(){

  vec3 norm = vec3(normal[0], normal[1], normal[2]);
  vec3 origin =  vec3(focalPlane[0], focalPlane[1], focalPlane[2]);

  vec3 hypotenuse = norm-origin;
  //vec3 hypotenuse = origin-norm;

  float c = length(hypotenuse);
  
  hypotenuse = normalize(hypotenuse);
  norm = normalize(norm);

  float cos = dot(norm, hypotenuse);

  return cos*c;

}
void main() {

  //----------  not used because I can not edit the size with the alpha ------------//
  float distanceToFocalPlane = getDistToPoint();
  float alpha = 255/(distanceToFocalPlane*distanceToFocalPlane)/255;
  alpha += strokeAlpha;
  alpha = clamp(alpha, .1, 1.);

  //alpha = strokeAlpha;

  vec4 col = vec4(1.0, 1.0, 1.0, alpha);
  //vec4 col = vec4(1.0, 1.0, 1.0, 1.0);


  //vec4 pos = modelview * vertex;
  //vec4 clip = projection * pos;

  vec4 clip = transform * vertex;
  gl_Position = clip + projection * vec4(offset, 0, 0);
  

  if(useColors){
    

    vec2 tex2Pos = vec2(vertex.x/gWidth+.5, vertex.y/gHeight+.5);
    //vec2 tex2Pos = vec2(vertex.x/gWidth, vertex.y/gHeight);
    
    col.rgb = texture2D(tex1, tex2Pos).rgb;
    
    //vertColor.rgb = col.rgb;
    //vertColor.a = color.a;

     

  } 

  vertColor = col;   

  center = clip.xy;
  pos = offset;

}
