#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture; //not used
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
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
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
    //second way to use texFftEnd >> create flat middle
  	if(texCutStraight)tex2Pos[0] -= texFftEnd;

  	tex2Pos[0] = clamp(tex2Pos[0], 0 , 1);
	
  } else {
	 //tex2Pos = pos;
	 tex2Pos = vertTexCoord.xy;
  }
    
  //--------- depthmap ----------//
  vertColor = texture2D(tex0, vertTexCoord.st);

  if(vertColor.r<depthTS)underTS= true;
  
  vec4 myVertex = vertex;
  myVertex.z = vertColor.r * 255.0 * depth;
  
  //TODO ANIMATE ONLY IF VERTEX Z < VALUE
  //sound animation
  vec4 vertSoundColor = texture2D(tex2, tex2Pos);
  float soundZOffet = vertSoundColor.r - 0.5;  
  float minHeight = max(texCoord[1], damper); 
  soundZOffet *= minHeight;
  myVertex.z += soundZOffet*amplitude;
  
  /* not used create a grid
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
   
  //DO IT AT THE END
  if(useColors){
	 float xMin = vertColor.r + colorTS;
	 xMin = clamp(xMin, 0.0, 1.0);
	 vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }
   
  if(underTS) vertColor.a = .0;
  else vertColor.a = alpha;

  if(myVertex.z < finalTS) vertColor.a = .0;
  
  gl_Position = transform * myVertex;  
  
}