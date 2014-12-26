package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.Ramp;

public class DrawLineScene extends Scene {
	
	private PVector[] pvectors;	
	
	private int lineNumber;
	
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	
	private Ramp ramp;
	
	//-----------//
	private PVector oldVector, actualVector;
	private float oldBufferValue, actualBufferValue;
	private float oldDepthValue, actualDepthValue;
	
	
	public DrawLineScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		PApplet.println(xRatio+" "+yRatio);
		
		setVectors();
		
		ramp = new Ramp(0, true);
		
		buffers = new ArrayList<FloatList>();
		
		setBuffers(params.get("ySpace"));		
		  
		PApplet.println("----------------------------------" + "\n" +
		                "depth limits: press l + UP OR DOWN" + "\n" +
		                "dark lines visibility: press v" + "\n" +
		                "use multiple buffers: press b" + "\n" +
		          		"use colors: press c");
				
	}
	private void setVectors(){
		pvectors = new PVector[imgWidth*imgHeight]; 
		for (int i=0; i<imgHeight; i++){			
			for(int j=0; j<imgWidth; j++){
				pvectors[j+i*imgWidth] = new PVector(j, i, 0);
		    }
		} 
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		lineNumber = 0;
				
		updateBuffers();
				
		//editPoints();
		
	}
	public void display(){
		
		int ySpace = params.get("ySpace");
		
		//TODO reverse it
		for (int i=0; i<imgHeight; i+= ySpace){
		    
			oldVector = null;
			oldDepthValue = 0;
			oldBufferValue = 0;
	    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	
		    	if(lineNumber>buffers.size()-1){
		    		
		    		actualBufferValues = buffers.get(lineNumber-1);
		    		
		    		FloatList bufferValues = new FloatList();
		    		bufferValues = actualBufferValues.copy();
					buffers.add(bufferValues);
		    		
		    	} else {
		    		actualBufferValues = buffers.get(lineNumber);
		    	}
		    	
		    	
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) drawLines(i, actualBufferValues, lineNumber);
		    		    
		    lineNumber++;    
	
		}
		
		checkNumBuffers();
		
	}
	private void checkNumBuffers(){
		
		while(buffers.size()>lineNumber){
			buffers.remove(0);
		}
	}
	protected void editPoints(){
		
		
		int ySpace = params.get("ySpace");
		int foo=0;
		
		for (int i=0; i<imgHeight; i+= ySpace){
			
			foo=i;
		}
		
		PApplet.println(foo+" "+imgHeight/ySpace);
		
		/*int ySpace = params.get("ySpace");
		
		for (int i=10; i<imgHeight; i+= ySpace){
		    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(lineNumber);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) {
		    	
		    	for(int j=0; j<imgWidth; j+=10){
	      		    
					actualVector = pvectors[j+i*imgWidth];
				   
					int k = (int) PApplet.map(j, 0, imgWidth-1, 0, actualBufferValues.size()-1);
				    
					actualBufferValue = actualBufferValues.get(k);
					
					actualDepthValue = depthValues[j+i*imgWidth];
				
		    	}  
		    
		    }
		
		}*/
		
	}
	private void drawLines(int i, FloatList actualBufferValues, int lineNumber){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<imgWidth; j+=10){
      		    
			actualVector = pvectors[j+i*imgWidth];
		   
			int k = (int) PApplet.map(j, 0, imgWidth-1, 0, actualBufferValues.size()-1);
		    
			actualBufferValue = actualBufferValues.get(k);
			
			actualDepthValue = depthValues[j+i*imgWidth];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){ //foreground
		    		
		    		drawLine(lVal, hVal, true, lineNumber);
		    	
		    	} else { //background
	                
			        if(actualDepthValue < lVal) {
			        	//actualDepthValue = lVal;
			        	actualDepthValue = hVal;
			        } else if(actualDepthValue > hVal){
			        	actualDepthValue = hVal;
			        }
	
			        drawLine(lVal, hVal, false, lineNumber);
			        
		    	}
		    	
		    }
		    
		    oldVector = actualVector;
		    oldDepthValue = actualDepthValue;
		    oldBufferValue = actualBufferValue;
		
		}  

	}
	private void drawLine(int lVal, int hVal, boolean isInFront, int lineNumber){
		
		int c;
		int blackAndWhiteColor;
		
		if(isInFront){
			blackAndWhiteColor = 255;
		} else {
			blackAndWhiteColor = 75;
		}
        
        if(App.useColors){
        	c = ramp.pickColor(actualDepthValue, lVal, hVal, params.get("alpha"));
        } else {
        	c = pApplet.color(blackAndWhiteColor);
        }
        
        int strokeMax = params.get("strokeWeight");
        
        float weight = (float) PApplet.map(actualDepthValue, lVal, hVal, strokeMax, 1);
		//weight *= xRatio;
        actualDepthValue = PApplet.map(actualDepthValue, lVal, hVal, -1, 1);
      
        pApplet.stroke(c);
		pApplet.strokeWeight(weight);
		
		float ovz = -(oldDepthValue*params.get("depth") + oldBufferValue);
        float avz = -(actualDepthValue*params.get("depth") + actualBufferValue);
        
        float distance = PApplet.abs(ovz-avz);
             
        if(	(isInFront && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){
 	
        	float alpha = 255;
        	
        	//PApplet.println(params.get("ySpace")+ " " + lineNumber + " "+ test+" "+alpha);
        	//int alpha = lineNumber;
        	
        	//alpha = (int) PApplet.map(alpha, 0, h, params.get("alpha"), 255);
        		
        	/*pApplet.stroke(c, (float) (alpha-75*3));
    		pApplet.strokeWeight((float) (weight + .5*3));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
        	
        	pApplet.stroke(c, (float) (alpha-75*2));
    		pApplet.strokeWeight((float) (weight + .5*2));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);*/
        	
        	pApplet.stroke(c, alpha);
    		pApplet.strokeWeight(weight);
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);	
			
			//TODO DRAWING
        	float tmp_alpha  = alpha-75;
        	tmp_alpha = PApplet.constrain(tmp_alpha, 0, 255);
        	pApplet.stroke(c, (float) (tmp_alpha));
    		pApplet.strokeWeight((float) (weight + .5));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
		
		}

	}
	private void setBuffers(int _ySpace){
		
		for (int i=0; i<imgHeight; i+= _ySpace){
			FloatList bufferValues = new FloatList();
			buffers.add(bufferValues);
		}
  
	}
}
