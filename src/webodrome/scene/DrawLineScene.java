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
	public static boolean texCutStraight = true;
	public static boolean sameSize = false; //points with different sizes
	public static boolean drawRoundRect = true;
	public static int mode = 7;
	
	//TODO PARAM 1 = no jump 
	private static int bufferJump = 2;
	private static int lineBufferJump = 2;
	
	//-------- shaders ----------//
	private static PShader fshader;
	private static PShader basicShader;
	private static PShader bLineShader;
	private static PShader lineShader;
	private static PShader pointShader;
	private static PShader trgFillShader;
	private static PShader trgStrokeShader;
	
	private PImage[] images;
	
	private Ramp ramp;
	
	private int xSpace;
	private int ySpace;
	
	private float zMin, zMax;	
	
	public DrawLineScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		//----------- shaders -----------//
		
		//create lineSoundImage
		App.lineSoundImage = pApplet.createImage(App.imgSoundWidth/lineBufferJump, 1, PConstants.ARGB);
		App.lineSoundImage.loadPixels();
		for (int i=0; i<App.lineSoundImage.pixels.length; i++) {
			App.lineSoundImage.pixels[i] = pApplet.color(127, 255); 
		}
		App.lineSoundImage.updatePixels();
		
		//create basicSoundImage
		App.basicSoundImage = pApplet.createImage(App.imgSoundWidth/bufferJump, App.imgSoundHeight, PConstants.ARGB);
		App.resetBasicSoundImage();
		
		images = new PImage[2];
		images[0] = pApplet.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB); //depth
		images[1] = pApplet.loadImage("colors.jpg");
		
		
		fshader = pApplet.loadShader("fshader_frag.glsl", "fshader_vert.glsl");
		fshader.set("tex0", images[0]); //depth
		fshader.set("tex1", images[1]); //color
		fshader.set("tex2", App.lineSoundImage);	
		fshader.set("gWidth", (float) App.width);
		fshader.set("gHeight", (float) App.height);
		
		basicShader = pApplet.loadShader("bshader_frag.glsl", "bshader_vert.glsl");
		basicShader.set("tex0", images[0]);
		basicShader.set("tex1", images[1]); //color
		basicShader.set("tex2", App.lineSoundImage);
		basicShader.set("gWidth", (float) App.width);
		basicShader.set("gHeight", (float) App.height);
		
		bLineShader = pApplet.loadShader("bLShader_frag.glsl", "bLShader_vert.glsl");
		bLineShader.set("tex0", images[0]);
		bLineShader.set("tex1", images[1]); //color
		bLineShader.set("tex2", App.lineSoundImage);
		bLineShader.set("gWidth", (float) App.width);
		bLineShader.set("gHeight", (float) App.height);
		
		lineShader = pApplet.loadShader("lshader_frag.glsl", "lshader_vert.glsl");
		lineShader.set("tex0", images[0]); //depth
		lineShader.set("tex1", images[1]); //color
		lineShader.set("tex2", App.lineSoundImage);
		lineShader.set("gWidth", (float) App.width);
		lineShader.set("gHeight", (float) App.height);
		
		pointShader = pApplet.loadShader("pointShader_frag.glsl", "pointShader_vert.glsl");
		pointShader.set("tex0", images[0]); //depth
		pointShader.set("tex1", images[1]); //color
		pointShader.set("tex2", App.lineSoundImage);
		pointShader.set("gWidth", (float) App.width);
		pointShader.set("gHeight", (float) App.height);
		
		
		//--- triangles -----//
		
		//trgFillShader = pApplet.loadShader("bshader_frag.glsl", "bshader_vert.glsl");
		trgFillShader = pApplet.loadShader("trgFill_frag.glsl", "trgFill_vert.glsl");
		trgFillShader.set("tex0", images[0]);
		trgFillShader.set("tex1", images[1]); //color
		trgFillShader.set("tex2", App.lineSoundImage);
		trgFillShader.set("gWidth", (float) App.width);
		trgFillShader.set("gHeight", (float) App.height);
		
		trgStrokeShader = pApplet.loadShader("trgStroke_frag.glsl", "trgStroke_vert.glsl");
		trgStrokeShader.set("tex0", images[0]);
		trgStrokeShader.set("tex1", images[1]); //color
		trgStrokeShader.set("tex2", App.lineSoundImage);
		trgStrokeShader.set("gWidth", (float) App.width);
		trgStrokeShader.set("gHeight", (float) App.height);
		
		//----------- shaders -----------//
		
		ramp = new Ramp(1, true);
		
		buffers = new ArrayList<FloatList>();
		
		setBuffers(params.get("ySpace"));		
		
		PApplet.println("make it count");
				
	}
	private void setUniformVariables(PShader shader){
		
		shader.set("useColors", App.useColors);
		
		float amplitude = params.get("amplitude");
		shader.set("amplitude", amplitude);
						
		float colorTS = params.get("colorTS"); 
		colorTS = PApplet.map(colorTS, 0, 254, 0, 1);
		shader.set("colorTS", colorTS);
		
		float damper = params.get("damper");
		damper = PApplet.map(damper, 0f, 10f, 1f, 0f);
		shader.set("damper", damper);
		
		float depth = params.get("depth");
		depth = PApplet.map(depth, -1000, 1000, -10, 10);
		shader.set("depth", depth);
		
		//------------ new ones ------------//
		
		shader.set("useFFT", useFFT);
		shader.set("texCutStraight", texCutStraight);
		
		float texFftStart = params.get("texFftStart");
		texFftStart = PApplet.map(texFftStart, 0f, 10f, 0f, 1f);
		shader.set("texFftStart", texFftStart);
		
		float texFftEnd = params.get("texFftEnd");
		texFftEnd = PApplet.map(texFftEnd, 0f, 10f, 0f, 1f);
		shader.set("texFftEnd", texFftEnd);

	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		//PApplet.println(params.get("damper"));

		if(mode==2){ // shape composed of multiples quads + only fill
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateShapeGrid();			
				App.recreateShapeGrid = false;
			}
						
			depthImage = context.depthImage();
			fshader.set("tex0", depthImage);
			
			if(multipleBuffers) fshader.set("tex2", App.basicSoundImage);
			else fshader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(fshader);
			
			float fillAlpha = params.get("fillAlpha");
			fillAlpha = PApplet.map(fillAlpha, 0, 255, 0, 1);
			fshader.set("alpha", fillAlpha);
			
		} else if(mode==3){ // shape composed of multiples quads + stroke and fill
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateBasicShapeGrid();			
				App.recreateShapeGrid = false;
			}
			
			//USE OF TWO SHADERS
			depthImage = context.depthImage();
			basicShader.set("tex0", depthImage);
			bLineShader.set("tex0", depthImage);
			
			if(multipleBuffers) {
				basicShader.set("tex2", App.basicSoundImage);
				bLineShader.set("tex2", App.basicSoundImage);
			} else {
				basicShader.set("tex2", App.lineSoundImage);
				bLineShader.set("tex2", App.lineSoundImage);
			}
			
			setUniformVariables(basicShader);
			setUniformVariables(bLineShader);
			
			float fillAlpha = params.get("fillAlpha");
			fillAlpha = PApplet.map(fillAlpha, 0, 255, 0, 1);
			basicShader.set("alpha", fillAlpha);
			
			float strokeAlpha = params.get("strokeAlpha");
			strokeAlpha = PApplet.map(strokeAlpha, 0, 255, 0, 1);
			bLineShader.set("alpha", strokeAlpha);
						
		} else if(mode==4){ //shape created with lines (two vertices)
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateLineShapeGrid();			
				App.recreateShapeGrid = false;
			}
			
			depthImage = context.depthImage();
			lineShader.set("tex0", depthImage);
			
			if(multipleBuffers) lineShader.set("tex2", App.basicSoundImage);
			else lineShader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(lineShader);
						
		} else if(mode==5){ //shape created with single vertices display as square
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreatePointShapeGrid();			
				App.recreateShapeGrid = false;
			}
			
			depthImage = context.depthImage();
			pointShader.set("tex0", depthImage);
			
			if(multipleBuffers) pointShader.set("tex2", App.basicSoundImage);
			else pointShader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(pointShader);
			
			pointShader.set("sameSize", sameSize); //change the size of the points with z
			pointShader.set("weight", (float) params.get("strokeWeight"));
			pointShader.set("drawRoundRect", drawRoundRect);
			
		} else if(mode==6){ // shape group composed of multiples quads
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateQuadGroupShapeGrid();			
				App.recreateShapeGrid = false;
			}
						
			depthImage = context.depthImage();
			fshader.set("tex0", depthImage);
			
			if(multipleBuffers) fshader.set("tex2", App.basicSoundImage);
			else fshader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(fshader);
			
			float fillAlpha = params.get("fillAlpha");
			fillAlpha = PApplet.map(fillAlpha, 0, 255, 0, 1);
			fshader.set("alpha", fillAlpha);
		
		} else if(mode==7){ // shape composed of multiples quads + stroke and fill
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateTriangleShapeGrid();			
				App.recreateShapeGrid = false;
			}
			
			//USE OF TWO SHADERS
			depthImage = context.depthImage();
			trgFillShader.set("tex0", depthImage);
			trgStrokeShader.set("tex0", depthImage);
			
			if(multipleBuffers) {
				trgFillShader.set("tex2", App.basicSoundImage);
				trgStrokeShader.set("tex2", App.basicSoundImage);
			} else {
				trgFillShader.set("tex2", App.lineSoundImage);
				trgStrokeShader.set("tex2", App.lineSoundImage);
			}
			
			setUniformVariables(trgFillShader);
			setUniformVariables(trgStrokeShader);
			
			float fillAlpha = params.get("fillAlpha");
			fillAlpha = PApplet.map(fillAlpha, 0, 255, 0, 1);
			trgFillShader.set("alpha", fillAlpha);
			
			float strokeAlpha = params.get("strokeAlpha");
			strokeAlpha = PApplet.map(strokeAlpha, 0, 255, 0, 1);
			trgStrokeShader.set("alpha", strokeAlpha);
			
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
			if(useFFT){
				updateFFTV2();
			} else {
				updateVolume();
			}	
		}
		
		App.updateSound = !App.updateSound;
		
	}
	private void updateFFTV2(){
		
		if(!App.useLiveMusic){
			//TODO
		} else {
			
			if(App.useLiveMusic)App.fft.forward(App.in.left);
			else App.fft.forward(App.player.left);
		
			updateSoundTexture();
			
		}
		
	}
	private void updateVolume(){
		
		if(!App.useLiveMusic){
			//TODO
		} else {
			updateSoundTexture();
		}
		
	}
	private void updateSoundTexture(){
		if(multipleBuffers)updateBasicSoundImage();
		else updateLineSoundImage();
	}
	private void updateBasicSoundImage(){
		
		int tmpWidth = App.basicSoundImage.width;
		int numOfPixels = App.basicSoundImage.pixels.length;
		
		App.basicSoundImage.loadPixels();

		//shift all lines except last one
		for (int j = 0; j < numOfPixels-tmpWidth; j++) {
			int c = App.basicSoundImage.pixels[j+tmpWidth];
			App.basicSoundImage.pixels[j] = c;
		}
		
		//edit last line
		int firstId = numOfPixels-tmpWidth;
		for (int i = firstId; i < numOfPixels; i++) {
			
			int pointer = i-firstId;
			float value;
			
			if(useFFT){
				value = App.fft.getBand(pointer*bufferJump);
			} else {
				value = App.in.left.get(pointer*bufferJump); //-1 to 1
				value *= 10;
			}

			value = PApplet.map(value, -1, 1, 0, 255);
			
			App.basicSoundImage.pixels[i] = pApplet.color(value);
						
		}
		
		App.basicSoundImage.updatePixels();

	}
	private void updateLineSoundImage(){
		
		App.lineSoundImage.loadPixels();
		for (int i = App.lineSoundImage.width-1; i>=0; i--) {
			
			float value;
			
			if(useFFT){
				value = App.fft.getBand(i*lineBufferJump); //-1 to 1
			} else {
				value = App.in.left.get(i*lineBufferJump); //-1 to 1
				value *= 10;
			}
						
			value = PApplet.map(value, -1, 1, 0, 255);
			
			//move slowly values from left to right
			if(!useFFT){

				int shiftX = 5;
				
				if(i>shiftX){
					int pCOlor = App.lineSoundImage.pixels[i-shiftX];
					float red = pCOlor >> 16 & 0xFF;
					
					if(red>value){
						value += Math.abs(red-value)*.8;
					} else if(red<value){
						value -= Math.abs(red-value)*.8;
					}
					
					value = Math.max(0, Math.min(255, value));//clamp 0 255
				}
			}
			
			App.lineSoundImage.pixels[i] = pApplet.color(value);
			
		}
		
		App.lineSoundImage.updatePixels();
		
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
		
		pApplet.shapeMode(PConstants.CENTER);
		
		if(mode==2){
			pApplet.shader(fshader);
		} else if(mode==3){
			pApplet.shader(basicShader);
			pApplet.shader(bLineShader);
		} else if(mode==4){
			pApplet.shader(lineShader);
		} else if(mode==5){
			pApplet.shader(pointShader);
		} else if(mode==6){
			pApplet.shader(fshader);
		} else if(mode==7){
			pApplet.shader(trgFillShader);
			pApplet.shader(trgStrokeShader);
		}
		
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

		float alpha = params.get("fillAlpha");
		
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
	private void translateAndDisplayShape(){
		if(App.usePeasyCam){
			displayShape();
		} else {
			pApplet.pushMatrix();
			pApplet.translate(w/2, h/2, -200);
			displayShape();
			pApplet.popMatrix();
		}
	}
	public void display(){
		
		if(mode==0){
			pApplet.shader(App.defaultShader);
			displayLines(App.pvectors);
		} else if (mode==1){
			pApplet.shader(App.defaultShader);
			displayTextures(App.pvectors);	
		} else {
			translateAndDisplayShape();
		}
		
	}
}
