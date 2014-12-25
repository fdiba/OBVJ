package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.Ramp;

public class DrawPointsAndLinesScene extends Scene {
	
	private PVector[] pvectors;	
	
	private int lineNumber;
	
	
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	
	private Ramp ramp;
	
	//-----------//
	private PVector oldVector, actualVector;
	private float oldBufferValue, actualBufferValue;
	private float oldDepthValue, actualDepthValue;
	
	
	public DrawPointsAndLinesScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
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
		
		//depthValues = context.depthMap();
		
		addAndEraseBuffers();
		
	}
	public void display(){
		
		int ySpace = params.get("ySpace");
		
		
		for (int i=10; i<imgHeight; i+= ySpace){
		    
			oldVector = null;
			oldDepthValue = 0;
			oldBufferValue = 0;
	    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(lineNumber);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) {
		    	
		    	editPointsPosition(i, actualBufferValues, lineNumber);
		    
		    }
		    
		    lineNumber++;    
		  
		}
		
	}
	public void display1(){
		
		int ySpace = params.get("ySpace");
		
		
		for (int i=10; i<imgHeight; i+= ySpace){
		    
			oldVector = null;
			oldDepthValue = 0;
			oldBufferValue = 0;
	    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(lineNumber);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) editPointsPosition1(i, actualBufferValues, lineNumber);
		    
		    lineNumber++;    
		  
		}
		
	}
	private void editPointsPosition1(int i, FloatList actualBufferValues, int lineNumber){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<imgWidth; j+=10){
      		    
			actualVector = pvectors[j+i*imgWidth];
		    actualBufferValue = actualBufferValues.get(j);
		    actualDepthValue = depthValues[j+i*imgWidth];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){ //foreground
		    		
		    		drawPoint(lVal, hVal, true, lineNumber);
		    	
		    	} else { //background
	                
			        if(actualDepthValue < lVal) {
			        	//depthValue = lVal;
			        	actualDepthValue = hVal;
			        } else if(actualDepthValue > hVal){
			        	actualDepthValue = hVal;
			        }
	
			        drawPoint(lVal, hVal, false, lineNumber);
			        
		    	}
		    	
		    }
		    
		    oldVector = actualVector;
		    oldDepthValue = actualDepthValue;
		    oldBufferValue = actualBufferValue;
		
		}  

	}
	private void drawPoint(int lVal, int hVal, boolean isInFront, int lineNumber){
		
		int c;
		int blackAndWhiteColor;
		
		if(isInFront){
			blackAndWhiteColor = 255;
		} else {
			blackAndWhiteColor = 75;
		}
        
        if(App.useColors){
        	c = ramp.pickColor(actualDepthValue, lVal, hVal);  
        } else {
        	c = pApplet.color(blackAndWhiteColor);
        }
        
        float weight = (float) PApplet.map(actualDepthValue, lVal, hVal, 4, 1);
		//weight *= xRatio;
        actualDepthValue = PApplet.map(actualDepthValue, lVal, hVal, -1, 1);
      
        pApplet.stroke(c);
		pApplet.strokeWeight(weight);

		float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
        float avz = actualVector.z - actualDepthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
        
        float distance = PApplet.abs(ovz-avz);
             
        if(	(isInFront && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){
        	
        	float yPos = params.get("ySpace") * lineNumber;
        	float alpha = PApplet.map(yPos, 0, imgHeight, params.get("alpha"), 255);
        				
			
        	pApplet.stroke(c, alpha);
    		pApplet.strokeWeight(weight);
        	pApplet.point(actualVector.x*xRatio, actualVector.y*yRatio, avz);
			
		
		}

	}
	private void editPointsPosition(int i, FloatList actualBufferValues, int lineNumber){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<imgWidth; j+=10){
      		    
			actualVector = pvectors[j+i*imgWidth];
		    actualBufferValue = actualBufferValues.get(j);
		    actualDepthValue = depthValues[j+i*imgWidth];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){ //foreground
		    		
		    		drawLine(lVal, hVal, true, lineNumber);
		    	
		    	} else { //background
	                
			        if(actualDepthValue < lVal) {
			        	//depthValue = lVal;
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

		float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
        float avz = actualVector.z - actualDepthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
        
        float distance = PApplet.abs(ovz-avz);
             
        if(	(isInFront && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){
        	
        	//float yPos = params.get("ySpace") * lineNumber;

        	//float alpha = PApplet.map(yPos, 0, imgHeight, params.get("alpha"), 255);     	
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
		
		for (int i=10; i<imgHeight; i+= _ySpace){
			FloatList bufferValues = new FloatList();
			buffers.add(bufferValues);
		}
  
	}
}
