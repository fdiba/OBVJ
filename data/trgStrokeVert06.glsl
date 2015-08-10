#define PROCESSING_LINE_SHADER

uniform mat4 transform;
uniform vec4 viewport;

uniform mat4 texMatrix; //ADDED

uniform sampler2D tex0; //depth image
uniform sampler2D tex1; //color image
uniform sampler2D tex2; //sound image
uniform float gWidth;
uniform float gHeight;

uniform float depthTS;
uniform float finalTS;

uniform bool useFFT; //use fft
uniform bool texCutStraight; //change how texFftEnd is used
uniform float texFftStart; //0 1
uniform float texFftEnd; //0 1

uniform bool useColors; //use of colors
uniform float colorTS; //offset colors tex1 x axis
uniform float depth; //create space between points z axis
uniform float alpha; //alpha for line borders
uniform float amplitude; //alpha for line borders
uniform float damper; //damp sound height in time 0 to 1

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

  //pos = vertTexCoord
  vec2 pos = vec2(vertex.x/gWidth, vertex.y/gHeight); //xy >> 0 1
  vec2 tex2Pos; //sound

  //-- threshold --//
  bool underTS;

  //------ fft ------/
  if(useFFT){
	
	//TODO create variable

	 if(texCutStraight){
    //x >> 1 0 1 and y >> 0 1
    tex2Pos = vec2(abs(vertex.x/gWidth*2-1), vertex.y/gHeight);
	 } else {
    //x >> 1 0 1 and y >> 0 1
    tex2Pos = vec2(abs(vertex.x/gWidth*2-1)*(1-texFftEnd), vertex.y/gHeight);
	 }
	
	 tex2Pos[0] += texFftStart;
   //second way to use texFftEnd >>  create flat middle
	 if(texCutStraight)tex2Pos[0] -= texFftEnd;	

	 tex2Pos[0] = clamp(tex2Pos[0], 0 , 1);
	
  } else {
	 tex2Pos = pos;
  }
  
  //--------- depthmap ----------//
  vertColor = texture2D(tex0, pos);

  if(vertColor.r<depthTS)underTS= true;
  
  vec4 myVertex = vertex;
  myVertex.z = vertColor.r * 255.0 * depth;
  
  //TODO ANIMATE ONLY IF VERTEX Z < VALUE
  //sound animation
  vec4 vertSoundColor = texture2D(tex2, tex2Pos);
  float soundZOffet = vertSoundColor.r - 0.5;  
  float minHeight = max(pos[1], damper); 
  soundZOffet *= minHeight;
  myVertex.z += soundZOffet*amplitude;
  
  //DO IT AT THE END
  if(useColors){
	 float xMin = vertColor.r + colorTS;
	 xMin = clamp(xMin, 0.0, 1.0);
	 vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }

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

  //vertColor = vec4(0.0,1.0,0.5,1.0);
  
  if(underTS) vertColor.a = .0;
  else vertColor.a = alpha;

  if(myVertex.z < finalTS) vertColor.a = .0;

  gl_Position.xy = clip0.xy + offset.xy;
  gl_Position.zw = clip0.zw;
  
}