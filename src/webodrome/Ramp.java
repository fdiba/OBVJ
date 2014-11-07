package webodrome;

import processing.core.PApplet;

public class Ramp{
	  	  
	public int[] colors;
	private boolean redToGreen;
  	private int step = 1;
 
  
  	public Ramp(boolean _useColors, boolean _redToGreen){
  		
  		redToGreen = _redToGreen;
    	  		
  		if(_useColors){
  			// from red (FF0000) to yellow (FFFF00) to green (00FF00)
  			useColors();
  		} else {
  			useBlackAndWhite();
  		}
	    
  }
  private void useBlackAndWhite(){
	  
	  int threshold = 0;
	  threshold = 127;
	  int length = 255 - threshold;
	  
	  
	  colors = new int[length];
	  int id = 0;
	  
	  for (int i=colors.length-1; i>=0; i--){	  
		  colors[id] = (255 << 24) | (i+threshold << 16) | (i+threshold << 8) | i+threshold;
		  id++;
	  }
	  
	  /*for (int i=0; i<colors.length; i++){	  
		  colors[i] = (255 << 24) | (i << 16) | (i << 8) | i;
	  }*/
  
  }
  private void useColors(){
	  
	  if(redToGreen){
		  colors = new int[255*2];
	  } else {
		  colors = new int[255*4];
	  }
  
	    
	  int i = 0;
	  int r = 255;
	  int g = 0;
	  int b = 0;
	      
	  boolean isDone = false;
	    
	  while(!isDone){
	        
		  if(g < 255){
	        
			  g += step;	        
			  g = PApplet.constrain(g, 0, 255);
			  colors[i] =  (255 << 24) | (r << 16) | (g << 8) | b; // 255 0 0 --> 255 255 0
	    
		  } else if(g == 255){ // 255 255 0 to 0 255 0
	  
			  r -= step;
			  r = PApplet.constrain(r, 0, 255);
	        
			  colors[i] =  (255 << 24) | (r << 16) | (g << 8) | b;
	        
			  if(r==0 && redToGreen){
				  isDone = true;
			  } else if (r==0 && !redToGreen) {
				  isDone = true;
				  addBlueColors(i);
			  }
		  }
	    	
		  i++;	 	
	  }	  	  
  }
  private void addBlueColors(int _id){ // 0 255 0 to 0 0 255
    
    int id = _id + 1;
  
    int r = 0;
    int g = 255;
    int b = 0;
      
    boolean isDone = false;
    
    while(!isDone){
        
      if(b < 255){
        
        b += step;
        b = PApplet.constrain(b, 0, 255);
        
        //colors[i] = color(r, g, b);
        colors[id] =  (255 << 24) | (r << 16) | (g << 8) | b; // 0 255 0 --> 0 255 255
        
      } else if(b == 255){ // 0 255 255 to 0 0 255
  
        g -= step;
        g = PApplet.constrain(g, 0, 255);
        
        colors[id] =  (255 << 24) | (r << 16) | (g << 8) | b;
        
        if(g==0){
          isDone = true;
        }
        
      }
      
      id++;
      
    }

  }
  public int pickColor(float depthValue, int lowestValue, int highestValue){
	  int colorId = (int) PApplet.map(depthValue, lowestValue, highestValue, 0, colors.length-1);
	  return colors[colorId];  
  }
}