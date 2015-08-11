package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.Ramp;

public class DrawPointScene extends Scene {
		
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	public static boolean useFFT = false;
	
	private Ramp ramp;
	
	private int xSpace;
	private int ySpace;
	
	private float zMin, zMax;	
	
	public DrawPointScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
						
		ramp = new Ramp(1, true);
		
		buffers = new ArrayList<FloatList>();
		
		setBuffers(params.get("ySpace"));		
				
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
						
		updateSound(useFFT);
		
		ySpace = params.get("ySpace");
		int actualNumberOfHLines=0;
		
		for (int i=0; i<imgHeight; i+= ySpace){
			actualNumberOfHLines++;
		}
		
		checkNumBuffers(actualNumberOfHLines);
		
		xSpace = params.get("xSpace");
		editVectorsPos(App.pvectors);
		
	}
	private void editVectorsPos(PVector[] pvectors){
		
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
	public void display(PVector[] pvectors){
		
		int c;
		int colorTS = params.get("colorTS");
		int strokeMax = params.get("strokeWeight");
		//int maxDist = params.get("maxDist");
		int depthTS = params.get("depthTS");
		boolean isInFront;
		
		pApplet.noStroke();
				
		for (int i=0; i<imgHeight; i+= ySpace){
						
			for(int j=0; j<imgWidth; j+=xSpace){
				
				PVector v = pvectors[j+i*imgWidth];

				if(v.z > depthTS) isInFront = true;
				else isInFront = false;
				
				if(isInFront || linesVisibility){
					
					if(App.useColors){
			        	c = ramp.pickColor((int) v.z, (int)zMax, (int)zMin, colorTS);
			        } else {

			        	if(isInFront) c = 0xFFFFFFFF;
			        	else c = 0xFF666666;
			        	
			        }
			        
			        float weight = (float) PApplet.map(v.z, (int)zMin, (int)zMax, 1, strokeMax);
					
					pApplet.fill(c);

					pApplet.pushMatrix();
			    	pApplet.translate(0, 0, v.z);
			    	pApplet.ellipse(v.x, v.y, weight, weight);
			    	pApplet.popMatrix();
					
				}
			}		
		}
	}
	public static void keyPressed(char key){
		
		if (key == 'b') {
			multipleBuffers = !multipleBuffers;
		} else if (key == 'd') {
			App.duplicateFFT = !App.duplicateFFT;
		} else if (key == 'f') {
			useFFT = !useFFT;
		} else if (key == 'v') {
			linesVisibility = !linesVisibility;
		} else if (key == '0'){
		
			App.editParameters("s0k0.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '1'){
			
			App.editParameters("s0k1.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '2'){
			
			App.editParameters("s0k2.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '3'){
			
			App.editParameters("s0k3.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '4') {
			
			App.editParameters("s0k4.txt");	
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '5') {
			
			App.editParameters("s0k5.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '6') {
			
			App.editParameters("s0k6.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '7') {
			
			App.editParameters("s0k7.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '8') {
			
			App.editParameters("s0k8.txt");
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '9') {
			
			App.editParameters("s0k9.txt");
			multipleBuffers = false;
			useFFT = false;
			
		}
	}
}
