package webodrome.scene;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.FloatList;
import processing.opengl.PShader;
import webodrome.App;
import webodrome.Ramp;
import SimpleOpenNI.SimpleOpenNI;

public class DrawLineScene extends Scene {
		
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	public static int mode = 2;
	
	private static boolean useFFT = true;
	private static boolean texCutStraight = true;
	private static boolean sameSize = false; //TODO not used points with different sizes
	private static boolean drawRoundRect = true;
	
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
	private static PShader testFillShader;
	
	private PImage depthImage;
	
	private PImage[] images;
	
	private Ramp ramp;
	
	private int xSpace;
	private int ySpace;
	
	private float zMin, zMax;
	
	private static StringBuilder varStatus = new StringBuilder();
	
	public DrawLineScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		depthImage = pApplet.createImage(App.KWIDTH, App.KHEIGHT, PConstants.RGB);
		
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
		setUniformConstants(fshader);
		
		basicShader = pApplet.loadShader("bshader_frag.glsl", "bshader_vert.glsl");
		setUniformConstants(basicShader);
		
		bLineShader = pApplet.loadShader("bLShader_frag.glsl", "bLShader_vert.glsl");
		setUniformConstants(bLineShader);
		
		lineShader = pApplet.loadShader("lshader_frag.glsl", "lshader_vert.glsl");
		setUniformConstants(lineShader);
		
		pointShader = pApplet.loadShader("pointShader_frag.glsl", "pointShader_vert.glsl");
		setUniformConstants(pointShader);
		
		//--- triangles -----//
		
		trgFillShader = pApplet.loadShader("trgFill_frag.glsl", "trgFill_vert.glsl");
		setUniformConstants(trgFillShader);
		
		trgStrokeShader = pApplet.loadShader("trgStroke_frag.glsl", "trgStroke_vert.glsl");
		setUniformConstants(trgStrokeShader);
		
		testFillShader = pApplet.loadShader("testFill_frag.glsl", "testFill_vert.glsl");
		testFillShader.set("tex1", images[1]); //color
		testFillShader.set("gWidth", (float) App.width);
		testFillShader.set("gHeight", (float) App.height);
		
		//----------- shaders -----------//
		
		ramp = new Ramp(1, true);
		
		buffers = new ArrayList<FloatList>();
		
		setBuffers(params.get("ySpace"));
		
		updateVarInfos();
						
	}
	private void setUniformConstants(PShader shader){
		shader.set("tex0", images[0]); //depth
		shader.set("tex1", images[1]); //color
		shader.set("tex2", App.lineSoundImage);	
		shader.set("gWidth", (float) App.width);
		shader.set("gHeight", (float) App.height);
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
	private PImage createDepthTexture(SimpleOpenNI context){
		
		float rawData = params.get("rawData")/10f;
		//pApplet.println(rawData);
		
		if(oldDepthValues==null) oldDepthValues = depthValues.clone();
		
		depthImage.loadPixels();
		for(int i=0; i<depthValues.length; i++){
						
			float value = depthValues[i];
			
			//------------- remove black zones --------------//
			if(value==0){
				
				int pv, nv;
				pv = nv = 0;
				
				if(i>0){	
					int j = i-1;
					pv = depthValues[j];
					while(pv<=0 && j>0){
						j--;
						pv = depthValues[j];
					}
				}
				
				if(i<depthValues.length-1){	
					int k = i+1;
					nv = depthValues[k];
					while(nv<=0 && k<depthValues.length-2){
						k++;
						nv = depthValues[k];
					}
				}
				
				value = Math.max(pv, nv);

			}
			//------------ end remove black zones --------------//
			
			//---------- smooth values ----------//
			if(value < oldDepthValues[i]) value = (float) (oldDepthValues[i] - (oldDepthValues[i]-value)*rawData);
			else if(value > oldDepthValues[i]) value = (float) (oldDepthValues[i] + (value-oldDepthValues[i])*rawData);
						
			oldDepthValues[i] = (int) value;
			
			//TODO ADD PARAM ?
			value = PApplet.map(value, App.lowestValue, App.highestValue, 255, 0);
			value = Math.max(0, Math.min(255, value));//clamp 0 255
			
			depthImage.pixels[i] = ((int)value << 16) | ((int)value << 8) | (int)value;

		}
		
		//oldDepthValues = depthValues.clone();
		
		depthImage.updatePixels();
		return depthImage;
		
	}
	private void updateShape(PShape shape){
		
		for(int i=0; i<shape.getVertexCount(); i++){
			
			PVector v = shape.getVertex(i);
			v.z += -1 + Math.random()*2;
			shape.setVertex(i, v);
			
		}
		
	}
	public void update(SimpleOpenNI context){
		
		if(!App.psRunning)super.update(context);
		else super.update();
		
		if(App.psRunning){
			
			if(App.recreateShapeGrid){
				App.recreatePS();		
				App.recreateShapeGrid = false;
			}
			
			testFillShader.set("useColors", App.useColors);
			
			//updateShape(App.partSysGrid);
			if(!App.pausedPS)App.updatePS();
			
		} else if(mode==2){ // shape composed of multiples quads + only fill
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateShapeGrid(mode);			
				App.recreateShapeGrid = false;
			}
						
			depthImage = createDepthTexture(context);
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
				App.recreateShapeGrid(mode);
				App.recreateShapeGrid = false;
			}
			
			//USE OF TWO SHADERS
			depthImage = createDepthTexture(context);
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
				App.recreateShapeGrid(mode);
				App.recreateShapeGrid = false;
			}
			
			depthImage = createDepthTexture(context);
			lineShader.set("tex0", depthImage);
			
			if(multipleBuffers) lineShader.set("tex2", App.basicSoundImage);
			else lineShader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(lineShader);
						
		} else if(mode==5){ //shape created with single vertices display as square
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateShapeGrid(mode);
				App.recreateShapeGrid = false;
			}
			
			depthImage = createDepthTexture(context);
			pointShader.set("tex0", depthImage);
			
			if(multipleBuffers) pointShader.set("tex2", App.basicSoundImage);
			else pointShader.set("tex2", App.lineSoundImage);
			
			setUniformVariables(pointShader);
			
			//pointShader.set("sameSize", sameSize); //change the size of the points with z
			pointShader.set("weight", (float) params.get("strokeWeight"));
			pointShader.set("drawRoundRect", drawRoundRect);
			
		} else if(mode==6){ // shape group composed of multiples quads
			
			updateSoundV2();
			
			if(App.recreateShapeGrid){
				App.recreateShapeGrid(mode);
				App.recreateShapeGrid = false;
			}
						
			depthImage = createDepthTexture(context);
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
				App.recreateShapeGrid(mode);
				App.recreateShapeGrid = false;
			}
			
			//USE OF TWO SHADERS
			depthImage = createDepthTexture(context);
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
		
		if(!App.psRunning){
		
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
		
		} else {
			pApplet.shader(testFillShader);
			//pApplet.shape(App.partSysGrid);
			App.displayPS();
		}
		
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
		
		if(App.psRunning){ //use always pcam
			displayShape();
		} else if(mode==0){
			pApplet.shader(App.defaultShader);
			displayLines(App.pvectors);
		} else if (mode==1){
			pApplet.shader(App.defaultShader);
			displayTextures(App.pvectors);	
		} else {
			translateAndDisplayShape();
		}
		
	}
	public static void updateVarInfos(){
		
		varStatus.replace(0, varStatus.length(), "toggle and edit depth limits: press l AND UP OR DOWN\n");
		
		if(App.switchValue){
			varStatus.append("App.lowestValue | " + App.lowestValue + " SL\n"
							+ "App.highestValue | " + App.highestValue + "\n");
		} else {
			varStatus.append("App.lowestValue | " + App.lowestValue + "\n"
							+ "App.highestValue | " + App.highestValue + " SL\n");
		}
		
		varStatus.append("\n"
				   + "change scene | n & p | " + App.getSceneId() + "\n"
				   + "\n"
				   + "change mode | g & h | " + mode + "\n"
				   + "\n"
				   + "App.useColors | c | " + App.useColors + "\n"
				   + "\n"
				   + "App.lowResGrid  | r | " + App.lowResGrid + "\n"
				   + "\n"
				   + "multipleBuffers | b | " + multipleBuffers + "\n"
				   + "\n"
				   + "useFFT | f | " + useFFT + "\n"
				   + "\n"
				   + "texCutStraight | o | " + texCutStraight + "\n"
				   + "\n"
				   + "sameSize | o | mode 5 | not used | " + sameSize + "\n"
				   + "\n"
				   + "drawRoundRect | k | mode 5 | " + drawRoundRect + "\n"
				   + "\n"
				   
				   + "OLD ONES\n\n"
				   + "duplicate fourier values | d | mode 0 | " + App.duplicateFFT + "\n"
				   + "linesVisibility | v | mode 0 | " + linesVisibility + "\n");
		
	}
	public static StringBuilder getVarStatus(){
		return varStatus;
	}
	public static void keyPressed(char key){
		
		if (key == 'b') {
			multipleBuffers = !multipleBuffers;
			if(multipleBuffers==false){
				//make possible the reconstruction of the basicSoundImage when multipleBuffers became true 
				App.resetBasicSoundImage();
			}
		} else if (key == 'd') { //TODO old variable use it to create mirror effect
			App.duplicateFFT = !App.duplicateFFT;
		} else if (key == 'f') {
			useFFT = !useFFT;
		} else if (key == 'g') {
			mode--;
			if(mode<0)mode=6;
			if(mode>1)App.recreateShapeGrid = true;
		} else if (key == 'h') {
			mode++;
			if(mode>7)mode=0;
			if(mode>1)App.recreateShapeGrid = true;	
		} else if (key == 'k') { //draw rect or circle when using points shader
			drawRoundRect = !drawRoundRect;
		} else if (key == 'o') { //change how texFftEnd is used in the shaders
			texCutStraight = !texCutStraight;
		}  else if (key == 't') { //TODO not used change the size of the points with z 
			sameSize = !sameSize;
		} else if (key == 'v') {
			linesVisibility = !linesVisibility;
		} else if (key == 'x') {
			App.recreateShapeGrid = true;
			
			
			
			if(!App.useKinect)App.psRunning = true;
			else App.psRunning = !App.psRunning;

			
			if(App.psRunning){
				App.resetPS();
			} else {
				App.cam.setDistance(App.camDist1);
			}
			
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
			
			multipleBuffers = true;
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
			
			multipleBuffers = true;
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
			int[] values = {0, 0, 50, 4, 0, 180, 0, 500, 10, -200, 250, 100, 10};
			App.editParams(0, parameters, values);
			
			multipleBuffers = true;
			useFFT = false;
			
		}
	}
}