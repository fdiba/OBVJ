package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.Ramp;

public class DrawLineScene extends Scene {
		
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	public static boolean useFFT = false;
	public static int mode = 0;
	
	private Ramp ramp;
	
	private int xSpace;
	private int ySpace;
	
	private float zMin, zMax;	
	
	public DrawLineScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
				
		setVectors();
		
		ramp = new Ramp(1, true);
		
		buffers = new ArrayList<FloatList>();
		
		setBuffers(params.get("ySpace"));		
		  
		PApplet.println("----------------------------------" + "\n" +
		                "depth limits: press l + UP OR DOWN" + "\n" +
		                "dark lines visibility: press v" + "\n" +
		                "use multiple buffers: press b" + "\n" +
		                "use fourier transform: press f" + "\n" +
		                "duplicate fourier values: press d" + "\n" +
		                "use shapes: press h" + "\n" +
		          		"use colors: press c");
				
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		
		if(useFFT)updateFTT();
		else updateBuffers();
		
		ySpace = params.get("ySpace");
		int actualNumberOfHLines=0;
		
		for (int i=0; i<imgHeight; i+= ySpace){
			actualNumberOfHLines++;
		}
		
		checkNumBuffers(actualNumberOfHLines);
		
		xSpace = params.get("xSpace");

		editVectorsPos();

	}
	private void editVectorsPos(){
		
		int lVal = App.lowestValue;
		int hVal = App.highestValue;
		
		zMin = 0;
		zMax = -9999999;
				
		int actualHLines=0;
		int depth = params.get("depth");
		float rawData = (float) (params.get("rawData")*0.1);
		
		for (int i=0; i<imgHeight; i+= ySpace){
			
			FloatList actualBufferValues;
			
			if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(actualHLines);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
			
			int bSize = actualBufferValues.size()-1;
			
			for(int j=0; j<imgWidth; j+=xSpace){
				
				PVector v = pvectors[j+i*imgWidth];
				PVector target = new PVector();
				
				int valueId = (int) PApplet.map(j, 0, imgWidth-1, 0, bSize);
				float bValue = actualBufferValues.get(valueId);
				float depthValue = depthValues[j+i*imgWidth];
				
				if(depthValue < lVal) {
		        	//depthValue = lVal;
					depthValue = hVal;
		        } else if(depthValue > hVal){
		        	depthValue = hVal;
		        }
				
				depthValue = PApplet.map(depthValue, lVal, hVal, -1, 1);
				
				float mvt = depthValue*depth + bValue;
				
				target.z -= mvt;
				
				PVector sub = PVector.sub(target, v);

				v.z += sub.z*rawData;
				
				if(v.z>zMax)zMax=v.z;
				if(v.z<zMin)zMin=v.z;

			}
			
			actualHLines++;
			
		}
		
	}
	private void displayLines(){
		
		int c;
		int blackAndWhiteColor = 255;
		int alphaTS = params.get("alphaTS");
		int strokeMax = params.get("strokeWeight");
		int maxDist = params.get("maxDist");
		int depthTS = params.get("depthTS");
		boolean isInFront;
				
		for (int i=0; i<imgHeight; i+= ySpace){
			
			PVector pVector = new PVector();
			
			for(int j=0; j<imgWidth; j+=xSpace){
				
				PVector v = pvectors[j+i*imgWidth];
				
				if(j==0){
					
					pVector = v.get();
					
				} else {
					
					float distance = PApplet.dist(v.x, v.y, v.z, pVector.x, pVector.y, pVector.z);
					
					if(v.z > depthTS){
						isInFront = true;
					} else {
						isInFront = false;
					}
					
					if( (distance < maxDist && isInFront) || (distance < maxDist && linesVisibility) ){
						
						if(App.useColors){
				        	c = ramp.pickColor((int) v.z, (int)zMax, (int)zMin, alphaTS);
				        } else {

				        	if(isInFront)blackAndWhiteColor = 0xFFFFFFFF;
				        	else blackAndWhiteColor = 0xFF333333;
				        	
				        	c = blackAndWhiteColor;
				        }
				        
				        float weight = (float) PApplet.map(v.z, (int)zMin, (int)zMax, 1, strokeMax);
						
						pApplet.stroke(c);
						pApplet.strokeWeight(weight);
						
						
			    		pApplet.line(pVector.x, pVector.y, pVector.z, v.x, v.y, v.z);	
					}
					
					pVector = v.get();
				}
				
			}
				
		}	
		
	}
	private void displayTextures(){

		int alpha = params.get("alpha");
		pApplet.fill(0xFF666666, alpha);
		pApplet.strokeWeight(1);
		pApplet.stroke(0xFF999999, 125);
		
		int xJump=xSpace*2;
		int yJump=ySpace;
		
		int xPosMax = imgWidth-xJump;
		int yPosMax = imgHeight;
		
		for (int i=ySpace; i<yPosMax; i+=yJump){
			
			pApplet.beginShape(PApplet.TRIANGLE_STRIP);
			int prevLine = i-ySpace;
			
			for(int j=0; j<xPosMax; j+=xJump){
								
				int nextId = j+xSpace;
				
				PVector v1 = pvectors[j+i*imgWidth];
				PVector v2 = pvectors[j+prevLine*imgWidth];
				PVector v3 = pvectors[nextId+i*imgWidth];
				PVector v4 = pvectors[nextId+prevLine*imgWidth];
				
				pApplet.vertex(v1.x, v1.y, v1.z);
				pApplet.vertex(v2.x, v2.y, v2.z);
				pApplet.vertex(v3.x, v3.y, v3.z);
				pApplet.vertex(v4.x, v4.y, v4.z);
				
			}
			pApplet.endShape();	
		}
	}
	private void displayUncutLines(){
		
		int alpha = params.get("alpha");
		
		pApplet.noFill();
		pApplet.strokeWeight(1);
		pApplet.stroke(0xFFFFFFFF, alpha);
		
		for (int i=0; i<imgHeight; i+= ySpace){
			
			pApplet.beginShape();
			
			for(int j=0; j<imgWidth; j+=xSpace){
				
				PVector v = pvectors[j+i*imgWidth];
				pApplet.vertex(v.x, v.y, v.z);
				
			}
			pApplet.endShape();	
		}	
		
	}
	public void display(){
		
		//setCap();
		//setJoin();
		
		switch (mode) {
		case 0:
			displayLines();
			break;
		case 1:
			displayUncutLines();
			break;
		case 2:
			displayTextures();
			break;
		default:
			displayLines();
			break;
		}
	
	}
}
