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
		
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", -50}, {"zTrans", -200}, {"strokeWeight", 6}, {"rotateX", 45}, {"rotateY", 0}, {"rotateZ", 0},
					{"amplitude", 50}, {"ySpace", 10}, {"depth", 112}, {"maxDist", 45}, {"depthTS", -90}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '1'){
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", 0}, {"zTrans", 20}, {"strokeWeight", 4}, {"rotateX", 0}, {"rotateY", 0}, {"rotateZ", 0},
					{"amplitude", 50}, {"ySpace", 12}, {"depth", 120}, {"maxDist", 45}, {"depthTS", -100}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '2'){
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", 0}, {"zTrans", 20}, {"strokeWeight", 14}, {"rotateX", 0}, {"rotateY", 0}, {"rotateZ", 0},
					{"amplitude", 150}, {"ySpace", 17}, {"depth", 172}, {"maxDist", 250}, {"depthTS", -150}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '3'){
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", 0}, {"zTrans", 20}, {"strokeWeight", 10}, {"rotateX", 0}, {"rotateY", 0}, {"rotateZ", 0},
					{"amplitude", 350}, {"ySpace", 4}, {"depth", 60}, {"maxDist", 20}, {"depthTS", -50}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '4') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", -50}, {"zTrans", 20}, {"strokeWeight", 16}, {"rotateX", 70}, {"rotateY", 0}, {"rotateZ", 90},
					{"amplitude", 70}, {"ySpace", 60}, {"depth", 60}, {"maxDist", 45}, {"depthTS", -55}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '5') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", -50}, {"zTrans", 20}, {"strokeWeight", 16}, {"rotateX", 70}, {"rotateY", 0}, {"rotateZ", 90},
					{"amplitude", 115}, {"ySpace", 10}, {"depth", 60}, {"maxDist", 45}, {"depthTS", -55}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '6') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", -50}, {"zTrans", 50}, {"strokeWeight", 50}, {"rotateX", 260}, {"rotateY", 0}, {"rotateZ", 90},
					{"amplitude", 450}, {"ySpace", 10}, {"depth", -72}, {"maxDist", 20}, {"depthTS", 75}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '7') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", 0}, {"zTrans", 374}, {"strokeWeight", 100}, {"rotateX", 0}, {"rotateY", 0}, {"rotateZ", 315},
					{"amplitude", 220}, {"ySpace", 44}, {"depth", 48}, {"maxDist", 20}, {"depthTS", -50}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '8') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", -100}, {"zTrans", -50}, {"strokeWeight", 4}, {"rotateX", 60}, {"rotateY", 180}, {"rotateZ", 0},
					{"amplitude", 100}, {"ySpace", 10}, {"depth", -200}, {"maxDist", 40}, {"depthTS", 100}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '9') {
			
			Object[][] parameters = {{"xTrans", 0}, {"yTrans", 0}, {"zTrans", 50}, {"strokeWeight", 4}, {"rotateX", 0}, {"rotateY", 180}, {"rotateZ", 0},
					{"amplitude", 500}, {"ySpace", 10}, {"depth", -200}, {"maxDist", 250}, {"depthTS", 100}, {"xSpace", 10}};

			App.editParameters(0, parameters);
			
			multipleBuffers = false;
			useFFT = false;
			
		}
	}
}
