package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.FloatList;
import processing.opengl.PShader;
import webodrome.App;
import webodrome.Ramp;

public class DrawLineScene extends Scene {
		
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	public static boolean useFFT = true;
	public static int mode = 3;
	
	//-------- shaders ----------//
	private static PShader fshader;
	private PImage[] images;
	private FloatList bufferValues;
	
	private Ramp ramp;
	
	private int xSpace;
	private int ySpace;
	
	private float zMin, zMax;	
	
	public DrawLineScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		//----------- shaders -----------//
		
		App.soundImage = pApplet.createImage(App.imgSoundWidth, 1, PConstants.ARGB);
		App.soundImage.loadPixels();
		for (int i=0; i<App.soundImage.pixels.length; i++) {
			App.soundImage.pixels[i] = pApplet.color(127, 255); 
		}
		App.soundImage.updatePixels();
		
		bufferValues = new FloatList();
		for(int i=0; i<App.imgSoundWidth; i++){
			bufferValues.append(0);
		}
		
		
		
		images = new PImage[1];
		images[0] = pApplet.loadImage("colors.jpg");
		
		fshader = pApplet.loadShader("fshader_frag.glsl", "fshader_vert.glsl");
		fshader.set("tex0", pApplet.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
		fshader.set("tex1", images[0]);
		fshader.set("tex2", App.soundImage);
		//----------- shaders -----------//
		
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
		                "use lowResGrid: press r" + "\n" +
		          		"use colors: press c");
				
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		//PSHAPE MODE
		if(mode==3){
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateShapeGrid();			
				App.recreateShapeGrid = false;
			}
			
			depthImage = context.depthImage();
			fshader.set("tex0", depthImage);
			
			fshader.set("tex2", App.soundImage);
			
			fshader.set("useColors", App.useColors);
			
			float amplitude = params.get("amplitude");
			fshader.set("amplitude", amplitude);
							
			float colorTS = params.get("colorTS"); 
			colorTS = PApplet.map(colorTS, 0, 254, 0, 1);
			fshader.set("colorTS", colorTS);
			
			float depth = params.get("depth");
			depth = PApplet.map(depth, -1000, 1000, -10, 10);
			fshader.set("depth", depth);
			
			float alpha = params.get("alpha");
			alpha = PApplet.map(alpha, 0, 255, 0, 1);
			fshader.set("alpha", alpha);
			
		} else {
			
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

	}
	private void updateSoundV2(){
		
		if(App.updateSound){ //edit image each two frames
			updateBuffersV2();
		} else {
			
		}
		App.updateSound = !App.updateSound;
		
	}
	private void updateBuffersV2(){
		
		if(!App.useLiveMusic){
			
			
		} else {
			
			App.soundImage.loadPixels();
			for (int i = 0; i < App.soundImage.width; i++) {
				
				float value = App.in.left.get(i); //-1 to 1
				
				
				
				value = PApplet.map(value, -1, 1, 0, 255);
				
				//float lastVal = bufferValues.get(i);
						
				//lastVal *= 0.1;
				//value = PApplet.max(lastVal, value);
				
				
				
				App.soundImage.pixels[i] = pApplet.color(value);
				//bufferValues.set(i, value);
				
				
			}
			App.soundImage.updatePixels();
			
		}
		
		
		
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
	private void displayShape(){
		
		if(App.usePeasyCam){

		} else {
			pApplet.shapeMode(PConstants.CENTER);
		}
	
		pApplet.shader(fshader);
		pApplet.shape(App.mainGrid);
		
	}
	private void displayLines(PVector[] pvectors){
		
		int c;
		int blackAndWhiteColor = 255;
		int colorTS = params.get("colorTS");
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
				        	c = ramp.pickColor((int) v.z, (int)zMax, (int)zMin, colorTS);
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
	private void displayTextures(PVector[] pvectors){

		float alpha = params.get("alpha");
		
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
	private void displayUncutLines(PVector[] pvectors){
		
		float alpha = params.get("alpha");
		if(alpha<50.0)alpha = (float) 50.0;

		
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
		
		switch (mode) {
		case 0:
			displayLines(App.pvectors);
			break;
		case 1:
			displayUncutLines(App.pvectors);
			break;
		case 2:
			displayTextures(App.pvectors);
			break;
		case 3:
			if(App.usePeasyCam){
				displayShape();
			} else {
				pApplet.pushMatrix();
				pApplet.translate(w/2, h/2, -200);
				displayShape();
				pApplet.popMatrix();
			}
			break;
		default:
			displayLines(App.pvectors);
			break;
		}
	
	}
}
