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
		
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
								   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, -100, -200, 6, 45, 0, 0, 50, 10, 112, 45, -90, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '1'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, 0, 20, 4, 0, 0, 0, 50, 12, 120, 45, -100, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '2'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, 0, 20, 14, 0, 0, 0, 150, 17, 172, 250, -150, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '3'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, 0, 20, 10, 0, 0, 0, 350, 4, 60, 20, -50, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '4') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, -50, 20, 16, 70, 0, 90, 70, 60, 60, 45, -55, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '5') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, -50, 20, 16, 70, 0, 90, 115, 10, 60, 45, -55, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '6') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, -50, 50, 50, 260, 0, 90, 450, 10, -72, 20, 75, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '7') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, 0, 374, 100, 0, 0, 315, 220, 44, 48, 20, -50, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '8') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, -100, -50, 4, 60, 180, 0, 100, 10, -200, 40, 100, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		} else if (key == '9') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
			int[] values = {0, 0, 50, 8, 0, 180, 0, 500, 10, -200, 250, 100, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = false;
			useFFT = false;
			
		}
	}
}
