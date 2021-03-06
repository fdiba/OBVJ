#define PROCESSING_POINT_SHADER

uniform mat4 projection;
//uniform mat4 modelview;
uniform mat4 transform;

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
uniform float amplitude; //alpha for line borders
uniform float damper; //damp sound height in time 0 to 1
uniform bool sameSize; //not used

attribute vec4 vertex;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 center;
varying vec2 pos;

void main() {

  //to edit strokeWeight
  vec2 mOffset = offset;

  //xy >> 0 1
  vec2 mpos = vec2(vertex.x/gWidth, vertex.y/gHeight);
  vec2 tex2Pos; //sound

  //-- threshold --//
  bool underTS;
  
  //------ fft ------/
  //set tex2Pos
  if(useFFT){
	
  	if(texCutStraight){
      //x >> 1 0 1 and y >> 0 1
  		tex2Pos = vec2(abs(vertex.x/gWidth*2-1), vertex.y/gHeight);
  	} else {
      //x >> 1 0 1 and y >> 0 1
  		tex2Pos = vec2(abs(vertex.x/gWidth*2-1)*(1-texFftEnd), vertex.y/gHeight);
  	}
  	
  	tex2Pos[0] += texFftStart;
    //second way to use texFftEnd >> create flat middle
  	if(texCutStraight)tex2Pos[0] -= texFftEnd;	

  	tex2Pos[0] = clamp(tex2Pos[0], 0 , 1);
	
  } else {
	 tex2Pos = mpos;
  }
 
  //--------- depthmap ----------//
  vertColor = texture2D(tex0, mpos);
  
  if(vertColor.r<depthTS)underTS= true;

  //if(!sameSize)mOffset *= 1+5*vertColor.r;
  
  //depthmap
  vec4 myVertex = vertex;
  myVertex.z = vertColor.r * 255.0 * depth;

  //TODO ANIMATE ONLY IF VERTEX Z < VALUE
  //sound animation | use tex2Pos
  vec4 vertSoundColor = texture2D(tex2, tex2Pos);
  float soundZOffet = vertSoundColor.r - 0.5;  
  float minHeight = max(mpos[1], damper); 
  soundZOffet *= minHeight;
  myVertex.z += soundZOffet*amplitude; 
  
  //TODO map color after music
  //TODO map mOffset after music
  //TODO make it round
  
  vec4 clip = transform * myVertex;
  //vec4 pos = modelview * myVertex;
  //vec4 clip = projection * pos;
  
  //----------- COLOR -----------//  
  
  //DO IT AT THE END
  if(useColors){
  	float xMin = vertColor.r + colorTS;
  	xMin = clamp(xMin, 0.0, 1.0);
  	vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }

  if(underTS) vertColor.a = .0;

  if(myVertex.z < finalTS) vertColor.a = .0;
  
  //vertColor = color;

  gl_Position = clip + projection * vec4(mOffset, 0, 0);
  center = clip.xy;
  pos = mOffset;

}
