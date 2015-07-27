#define PROCESSING_TEXTURE_SHADER

uniform mat4 transform;
uniform mat4 texMatrix;

//uniform sampler2D texture; //not used
uniform sampler2D tex0; //depth image
uniform sampler2D tex1; //color image
uniform sampler2D tex2; //sound image
uniform float gWidth;
uniform float gHeight;

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
  
  //------ fft ------/
  if(useFFT){
	
	//TODO create variable

	if(texCutStraight){
		tex2Pos = vec2(abs(vertex.x/gWidth*2-1), vertex.y/gHeight); //x >> 1 0 1 and y >> 0 1
	} else {
		tex2Pos = vec2(abs(vertex.x/gWidth*2-1)*(1-texFftEnd), vertex.y/gHeight); //x >> 1 0 1 and y >> 0 1
	}
	
	tex2Pos[0] += texFftStart;
	if(texCutStraight)tex2Pos[0] -= texFftEnd; //second way to use texFftEnd >> create flat middle

	tex2Pos[0] = clamp(tex2Pos[0], 0 , 1);
	
  } else {
	//tex2Pos = pos;
	tex2Pos = vertTexCoord.xy;
  }
  
  vertColor = texture2D(tex0, vertTexCoord.st);
  
  vec4 myVertex = vertex;
  myVertex.z = vertColor.r * 255.0 * depth;
  
  //TODO ANIMATE ONLY IF VERTEX Z < VALUE
  //------------ sound animation -------------//
  vec4 vertSoundColor = texture2D(tex2, tex2Pos);
  float soundZOffet = vertSoundColor.r - 0.5;

  float minHeight = max(texCoord[1], damper); 
  soundZOffet *= minHeight;
  
  myVertex.z += soundZOffet*amplitude;
  
  //------------ end sound animation -------------//

  //DO IT AT THE END
  if(useColors){
	float xMin = vertColor.r + colorTS;
	xMin = clamp(xMin, 0.0, 1.0);
	vertColor = texture2D(tex1, vec2(xMin, 0.0));
  }
    
  gl_Position = transform * myVertex;
  
  if( mod(myVertex.y, 5.0) > 0.5 ) vertColor.a = alpha;
  
}