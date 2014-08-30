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
		  
		PVector oldVector;
		float oldBufferValue;
		float oldDepthValue;
	    
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
		    	editPointsPosition(oldVector, oldBufferValue, i, actualBufferValues, oldDepthValue);
		    }
		    
		    lineNumber++;    
		  
		}
	}
	private void editPointsPosition(PVector oldVector, float oldBufferValue, int i, FloatList actualBufferValues, float oldDepthValue){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<w; j+=10){
      
			//stroke(255);
		    PVector actualVector = pvectors[j+i*w];
		    
		    float actualBufferValue = actualBufferValues.get(j);
		    float depthValue = depthValues[j+i*w];
	    
		    //point(actualVector.x, actualVector.y, actualVector.z);
	    
		    if(oldVector != null){
	            
		    	if(depthValue >= lVal && depthValue <= hVal){
	        
		        int c;
		        
		        if(useColors){
		        	c = ramp.pickColor(depthValue, lVal, hVal);  
		        } else {
		        	c = pApplet.color(255);
		        }
		        
		        depthValue = setColorWeightDepthValue(depthValue, c, lVal, hVal);
		        
		        float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
		        float avz = actualVector.z - depthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
		        
		        float distance = PApplet.abs(ovz-avz); 
		        
		        
		        if(distance < params.get("maxDist")) { //user lines
		        	pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
		        }
		
		    	} else {
	                
			        if(depthValue < lVal) {
			        	//depthValue = lVal;
			        	depthValue = hVal;
			        } else if(depthValue > hVal){
			        	depthValue = hVal;
			        }
		
			        int c;
			        
			        if(useColors){
			        	c = ramp.pickColor(depthValue, lVal, hVal);  
			        } else {
			        	c = pApplet.color(75);
			        }
		        
			        depthValue = setColorWeightDepthValue(depthValue, c, lVal, hVal);
		        
			        float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
			        float avz = actualVector.z - depthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
			        
			        float distance = PApplet.abs(ovz-avz); 
			        
			        if(distance < params.get("maxDist") && linesVisibility) { // background lines
			        	pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
			        }
			             
			      }
		      
		    	}
		    
		    oldVector = actualVector;
		    oldDepthValue = depthValue;
		    oldBufferValue = actualBufferValue;  
		
		}  

	}
	private float setColorWeightDepthValue(float depthValue, int couleur, int lVal, int hVal){
		  
		float weight = PApplet.map(depthValue, lVal, hVal, 4, 1);
		weight *= xRatio;
		depthValue = PApplet.map(depthValue, lVal, hVal, -1, 1);
      
		pApplet.stroke(couleur);
		pApplet.strokeWeight(weight);
        
		return depthValue;
  
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
