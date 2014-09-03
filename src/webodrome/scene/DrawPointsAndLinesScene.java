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
	private ArrayList<FloatList> buffers;
	
	private int lineNumber;
	private int[] depthValues;
	
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers;
	public static boolean useColors;
	
	private int w;
	private int h;
	
	private float xRatio;
	private float yRatio; 
	
	private Ramp ramp;
	
	
	//-----------//
	private PVector oldVector, actualVector;
	private float oldBufferValue, actualBufferValue;
	private float oldDepthValue, actualDepthValue;
	
	public DrawPointsAndLinesScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		w = 640;
		h = 480;
			
		xRatio = (float) _width/w;
		yRatio = (float) _height/h;
		
		PApplet.println(xRatio+" "+yRatio);
		
		setVectors();
		
		ramp = new Ramp();
		
		buffers = new ArrayList<FloatList>();
		//lineNumber = 0;
		
		setBuffers(params.get("ySpace"));
		  		  
		PApplet.println("----------------------------------" + "\n" +
		                "depth limits: press l + UP OR DOWN" + "\n" +
		                "dark lines visibility: press v" + "\n" +
		                "use multiple buffers: press b" + "\n" +
		          		"use colors: press c");
				
	}
	public void update(SimpleOpenNI context){
		
		super.update();
		
		lineNumber = 0;
		
		depthValues = context.depthMap();
		
		addAndEraseBuffers();
		
	}
	public void display(){
		drawVectors(params.get("ySpace"));
	}
	private void setVectors(){

		pvectors = new PVector[w*h]; 
		  
		for (int i=0; i<h; i++){			
			for(int j=0; j<w; j++){
				pvectors[j+i*w] = new PVector(j, i, 0);
		    }
		} 
		
	}
	private void drawVectors(int _ySpace){
	    
		for (int i=10; i<h; i+= _ySpace){
	    
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
		    	editPointsPosition(i, actualBufferValues);
		    }
		    
		    lineNumber++;    
		  
		}
	}
	private void editPointsPosition(int i, FloatList actualBufferValues){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<w; j+=10){
      		    
			actualVector = pvectors[j+i*w];
		    actualBufferValue = actualBufferValues.get(j);
		    actualDepthValue = depthValues[j+i*w];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){
		    		
		    		drawLine(lVal, hVal, true);
		    	
		    	} else {
	                
			        if(actualDepthValue < lVal) {
			        	//depthValue = lVal;
			        	actualDepthValue = hVal;
			        } else if(actualDepthValue > hVal){
			        	actualDepthValue = hVal;
			        }
	
			        drawLine(lVal, hVal, false);
			        
		    	}
		    	
		    }
		    
		    oldVector = actualVector;
		    oldDepthValue = actualDepthValue;
		    oldBufferValue = actualBufferValue;
		
		}  

	}
	
	private void drawLine(int lVal, int hVal, boolean isUser){
		
		int c;
		int blackAndWhiteColor;
		
		if(isUser){
			blackAndWhiteColor = 255;
		} else {
			blackAndWhiteColor = 75;
		}
        
        if(useColors){
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
        
        
        if(	(isUser && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){

        	int alpha = 255;
        		
        	/*pApplet.stroke(c, (float) (alpha-75*3));
    		pApplet.strokeWeight((float) (weight + .5*3));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
        	
        	pApplet.stroke(c, (float) (alpha-75*2));
    		pApplet.strokeWeight((float) (weight + .5*2));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);*/
        	
        	pApplet.stroke(c, (float) (alpha-75));
    		pApplet.strokeWeight((float) (weight + .5));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
        	
        	pApplet.stroke(c, alpha);
    		pApplet.strokeWeight(weight);
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
			
		
		}

	}
	private void addAndEraseBuffers(){
		
		FloatList bufferValues = new FloatList();
		  
		for(int i = 0; i < App.player.bufferSize(); i++) {
			bufferValues.append(App.player.left.get(i));
		}
	   
		if(buffers.size() > 0) buffers.remove(0);
		buffers.add(bufferValues);
		
	}
	private void setBuffers(int _ySpace){
		
		for (int i=10; i<h; i+= _ySpace){
			FloatList bufferValues = new FloatList();
			buffers.add(bufferValues);
		}
  
	}

}
