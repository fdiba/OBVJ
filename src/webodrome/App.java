package webodrome;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import themidibus.MidiBus;
import webodrome.ctrl.BehringerBCF;
import webodrome.ctrl.Menu;
import webodrome.ctrl.SecondApplet;
import webodrome.scene.ChunkyScene;
import webodrome.scene.DrawLineScene;
import webodrome.scene.DrawPointScene;
import webodrome.scene.Scene;
import webodrome.scene.ShapeScene;

public class App {
	
	public static int width = 1024;
	public static int height = 768;
	
	//--------- cam ----------------//
	public static PeasyCam cam;
	public static boolean usePeasyCam = true;
	public static float camDist1 = 500;
	public static float camDist2 = 1600; //use with ps
	
	private static float cameraRate = .1f;
	public static PVector cameraCenter = new PVector(width/2, height/2);
	
	
	public static PApplet objv;
	public static SecondApplet secondApplet;
	
	public static boolean useLiveMusic = true;
	
	//-------- SHADER ---------------//
	
	public static PShader defaultShader;
	
	//-------- KINECT CONST ----------//
	public static boolean useKinect = true; //-------------------------- WARNING -----------------------------//
	public static int KWIDTH = 640;
	public static int KHEIGHT = 480;
	
	//-------- depthMapControl ----------//
	public static int lowestValue = 550;
	public static int highestValue = 5100;
	public static boolean switchValue;
	
	//colors : red, green, blue green, orange, dark blue
	public final static int[] colorsPanel = {255 << 24 | 240 << 16 | 65 << 8 | 50,
											 255 << 24 | 135 << 16 | 205 << 8 | 137,
											 255 << 24 | 40 << 16 | 135 << 8 | 145,
											 255 << 24 | 252 << 16 | 177 << 8 | 135,
											 255 << 24 | 15 << 16 | 65 << 8 | 85};
	
	public final static int[] colors = {-8410437,-9998215,-1849945,-5517090,-4250587,-14178341,-5804972,-3498634};
		
	private static Scene actualScene;
	public static Menu actualMenu;
	
	public static Minim minim;
	public static AudioPlayer player;
	public static AudioInput in;
	public static FFT fft;
	
	public static boolean updateSound = true;
	public static boolean duplicateFFT = true;
	
	//TODO UPDATE/ERASE IT
	public static PVector[] pvectors;
	public static PShape mainGrid;
	public static PShape partSysGrid; //not used
	
	public static boolean useColors;
	
	public static boolean lowResGrid;
	public static boolean recreateShapeGrid;
	
	public static PImage lineSoundImage;
	public static PImage basicSoundImage;
	public static int imgSoundWidth;
	public static int imgSoundHeight = 40;
	
	//-------- behringer ----------//
	public final static boolean BCF2000 = true;
	public static MidiBus midiBus;
	public static BehringerBCF behringer;
	
	public static int[] transValues;
	
	//-----------------------//
	
	public static boolean fscreen = false;
	
	private static int sceneId = 0;
	public static int oldSceneId = 999;
	
	public static int sl_frameRate;
	
	//----------- PS ----------//
	public static boolean psRunning = false; //true auto when nokinect mode is activated
	public static boolean pausedPS = true;
	
	private static PVector globalOffset = new PVector(0f, 1/3f, 2/3f);
	private static PVector avgPos = new PVector();
	private static int npartTotal = 10000;
	private static PVector positions[] = new PVector[npartTotal];
	private static PVector velocities[] = new PVector[npartTotal];
	private static PVector localOffsets[] = new PVector[npartTotal];
	private static int n;
	private static float neighborhood = 700f;
	private static float independence = .15f;
	private static float turbulence = 1.3f;
	private static float viscosity = .1f;
	private static float spread = 100f;
	private static float speed = 24f;
	//----------- end PS ----------//
	
	public App() {
		// TODO Auto-generated constructor stub
	}
	public static void resetBasicSoundImage(){
		
		App.basicSoundImage.loadPixels();
		for (int i=0; i<App.basicSoundImage.pixels.length; i++) {
			App.basicSoundImage.pixels[i] = objv.color(127, 255); 
		}
		App.basicSoundImage.updatePixels();
		
	}
	public static void recreateShapeGrid(int mode){
		switch(mode){
			case 1: //ps system not used
				partSysGrid = createPointShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 2: //main grid
				mainGrid = createShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 3:
				mainGrid = createBasicShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 4:
				mainGrid = createLineShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 5:
				mainGrid = createPointShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 6:
				mainGrid = createQuadGroupShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			case 7:
				mainGrid = createTriangleShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
			default:
				mainGrid = createShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
				break;
		}	
	}
	//------------- ps functions ------------------//
	public static void recreatePS(){
		createPS();
	}
	public static void createPS(){
		
		//positions = new PVector[npartTotal];

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
		
		n=0;
		
		//System.out.println("max particles: "+ npartTotal);
		
		outerloop:
		for (int y=0; y<height-ySpace; y+=ySpace) {
		    for (int x=0; x<width-xSpace; x+=xSpace) {
		    	
		    	positions[n] = new PVector(x, y);
		    	velocities[n] = new PVector();
		    	localOffsets[n] = PVector.random3D();
		    	
		    	n++;
		    	
		    	if(n>=npartTotal){
		    		System.out.println("too much particles: "+ n);
		    		break outerloop;
		    	}
		    }
		}
		
	}
	public static void resetPS(){
		cam.setDistance(App.camDist2);
		//cameraCenter = new PVector();
		//getAVGposAndCamCenter();
		//globalOffset = new PVector(0f, 1/3f, 2/3f);
	}
	private static PVector applyFlockingForce(PVector pos, PVector lOffset){
		PVector p = PVector.div(pos, neighborhood);
		PVector f = new PVector();
		f.add(objv.noise(p.x + globalOffset.x + lOffset.x, p.y, p.z)-.5f,
				objv.noise(p.x, p.y + globalOffset.y + lOffset.y, p.z)-.5f,
				objv.noise(p.x, p.y, p.z + globalOffset.z + lOffset.z)-.5f);
		return f;
	}
	private static PVector applyCenteringForce(PVector pos, PVector avg){
		
		PVector cForce = PVector.sub(pos, avg);
		//cForce.set(pos);
		//cForce.sub(avg);
		//PVector cForce = PVector.sub(avg, pos);
		
		float distToCenter = cForce.mag(); 
		
		cForce.normalize();
		
		cForce.mult(-distToCenter/(spread*spread));
		//cForce.mult(distToCenter/(spread*spread));
		
		return cForce;
	}
	private static void getAVGposAndCamCenter(){
		avgPos.mult(0);
		for (int i=0; i<n; i++) avgPos.add(positions[i]);
		avgPos.div(n);
		
		cameraCenter.mult(1f-cameraRate);
		cameraCenter.add(PVector.mult(avgPos, cameraRate));
	}
	public static void updatePS(){
		
		getAVGposAndCamCenter();
		
		for (int i=0; i<n; i++) {
			
			PVector pos = positions[i];
			PVector localOffset = PVector.mult(localOffsets[i], independence); 
					
			PVector force = applyFlockingForce(pos, localOffset);
			
			//apply viscosity force
			PVector vel = velocities[i];
			force.add(PVector.mult(vel, -viscosity));
			
			//TODO NEXT
			force.add(applyCenteringForce(pos, avgPos));
			
			vel.add(force);
			pos.add(PVector.mult(vel, speed));
			
			
		}
		
		float value = turbulence/neighborhood;
		globalOffset.add(value, value, value);

	}
	public static void displayPS(){
		
		/*int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;*/
		
		objv.translate(-cameraCenter.x, -cameraCenter.y, -cameraCenter.z);
		
		for (int i=0; i<n; i++) {
			
			PVector p = positions[i];
			
			objv.beginShape(PConstants.POINTS);
			objv.textureMode(PConstants.NORMAL);
			objv.stroke(255);
			objv.strokeWeight(getActualScene().params.get("strokeWeight"));
			objv.strokeCap(PConstants.SQUARE);
			//noStroke();
			//tint(255, opacity * 255);
			//texture(sprite);
			//objv.normal(0, 0, 1);
			objv.vertex(p.x, p.y, p.z); //UV set in shader
			/*vertex(center.x - partSize/2, center.y - partSize/2, 0, 0);
			vertex(center.x + partSize/2, center.y - partSize/2, sprite.width, 0);
			vertex(center.x + partSize/2, center.y + partSize/2, sprite.width, sprite.height);
			vertex(center.x - partSize/2, center.y + partSize/2, 0, sprite.height);*/                
			objv.endShape(); 
			
		}
		
		if(objv.frameCount%sl_frameRate==0)System.out.println(objv.frameRate);
		
	}
	//------------- end ps functions ------------------//
	private static PShape createQuadGroupShapeGrid(PImage image){ //show DynamicParticlesRetained
		
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape(PShape.GROUP);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
		
		//TODO use it
		float borderXSize = getActualScene().params.get("borderXSize");
		float borderYSize = getActualScene().params.get("borderYSize");
				
		float yInnerSpace = borderYSize/2;
		
		if(yInnerSpace>=ySpace)yInnerSpace = ySpace-1;
		
		int numberOfRows = 0;
		
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {

		    	PShape part = objv.createShape();
		    	part.beginShape(PConstants.QUAD);
		    	//TODO use stroke ?
		    	part.noStroke();
		    	part.textureMode(PConstants.NORMAL);
		    	part.texture(image);
		    	part.normal(0, 0, 1);
		    	
		    	PVector tl, bl, br, tr;

		    	if (numberOfRows==0) {
		    		tl = new PVector(x, y+yInnerSpace);
		    		bl = new PVector(x, y+ySpace);
		    		br = new PVector(x+xSpace, y+ySpace);
		    		tr = new PVector(x+xSpace, y+yInnerSpace);
		    	} else {
		    		tl = new PVector(x, y);
		    		bl = new PVector(x, y+ySpace-yInnerSpace);
		    		br = new PVector(x+xSpace, y+ySpace-yInnerSpace);
		        	tr = new PVector(x+xSpace, y);
		    	}
		    	
		    	//TODO do it in the shader! /LINE BREAKER
		    	if (lowResGrid) { //press 'r' key
		    		
		    		part.vertex(tl.x, tl.y, tl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		part.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		part.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		part.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		      
		    	} else {
		    		
		    		if (numberOfRows==0) { //use bottom line as texture
		    					    			
		    			part.vertex(tl.x, tl.y, tl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
		    			part.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
		    			part.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    			part.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		
		    		} else { //use top line as texture
		    			
		    			part.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    			part.vertex(bl.x, bl.y, bl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    			part.vertex(br.x, br.y, br.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    			part.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    			
		    		}
		    	}
		    	
		    	part.endShape(PApplet.CLOSE);
		    	shape.addChild(part);
		    }

		    numberOfRows++;
		    if (numberOfRows==2)numberOfRows=0;
		  
		}

		return shape;
		
	}
	private static PShape createPointShapeGrid(PImage image){
		
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape();
		shape.setStroke(true);
		shape.setStrokeWeight(getActualScene().params.get("strokeWeight"));
			
		shape.setStrokeCap(PConstants.SQUARE);
		shape.setStroke(objv.color(255, 0, 0));
		
		//TODO ADD PARAM
		shape.beginShape(PConstants.POINTS);
		shape.textureMode(PConstants.NORMAL);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
				
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {
		    	
		    	PVector tl = new PVector(x, y);
		    	shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    	
		    }

		}

		shape.endShape(PApplet.CLOSE);
		return shape;
		
	}
	private static PShape createLineShapeGrid(PImage image){
		
		PShape shape = objv.createShape();
		shape.setStroke(true);
		shape.setStrokeWeight(getActualScene().params.get("strokeWeight"));
		//shape.setStroke(objv.color(255, 0, 0));
		
		//TODO ADD PARAM
		shape.beginShape(PConstants.LINES);
		shape.textureMode(PConstants.NORMAL);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
		
		//xSpace = 10;
		//ySpace = 10;
				
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {

		    	PVector tl, tr;
		    	
		    	tl = new PVector(x, y);
		    	tr = new PVector(x+xSpace, y);
		    	
		    					    			
		    	shape.vertex(tl.x, tl.y, 0);
		    	shape.vertex(tr.x, tr.y, 0);

		    }

		}

		shape.endShape(PApplet.CLOSE);
		return shape;
		
	}
	private static PShape createTriangleShapeGrid(PImage image){
		
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape();
		shape.setStroke(true);
		shape.setStrokeWeight(getActualScene().params.get("strokeWeight"));
		//shape.setStroke(objv.color(255, 0, 0));
		
		//TODO ADD PARAM
		//shape.beginShape(PConstants.TRIANGLE_STRIP);
		shape.beginShape(PConstants.TRIANGLE);
		shape.textureMode(PConstants.NORMAL);
		shape.texture(image);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
				
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {

		    	PVector tl, bl, br, tr;
		    	tl = new PVector(x, y);
		    	bl = new PVector(x, y+ySpace);
		    	br = new PVector(x+xSpace, y+ySpace);
		    	tr = new PVector(x+xSpace, y);
		    	
		    	//TODO do it in the shader! /LINE BREAKER
		    	if (lowResGrid) { //press 'r' key
		    		
		    		//shape.vertex(tl.x, tl.y, tl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		//shape.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		//shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		//shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		      
		    		shape.vertex(tl.x, tl.y, tl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		
		    		shape.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		
		    	} else {
		    					    			
		    		//shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		//shape.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
			    	//shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
			    	//shape.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    		
		    		shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
		    		
		    		shape.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    		shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    	}
		    	
		    }

		}

		shape.endShape(PApplet.CLOSE);
		return shape;
		
	}
	private static PShape createBasicShapeGrid(PImage image){
		
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape();
		shape.setStroke(true);
		shape.setStrokeWeight(getActualScene().params.get("strokeWeight"));
		//shape.setStroke(objv.color(255, 0, 0));
		
		//TODO ADD PARAM
		shape.beginShape(PConstants.QUADS);
		shape.textureMode(PConstants.NORMAL);
		shape.texture(image);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
				
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {

		    	PVector tl, bl, br, tr;
		    	tl = new PVector(x, y);
		    	bl = new PVector(x, y+ySpace);
		    	br = new PVector(x+xSpace, y+ySpace);
		    	tr = new PVector(x+xSpace, y);
		    	
		    	//TODO do it in the shader! /LINE BREAKER
		    	if (lowResGrid) { //press 'r' key
		    		
		    		shape.vertex(tl.x, tl.y, tl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		      
		    	} else {
		    					    			
		    		shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
			    	shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
			    	shape.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    		
		    	}
		    	
		    }

		}

		shape.endShape(PApplet.CLOSE);
		return shape;
		
	}
	private static PShape createShapeGrid(PImage image){
				
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape();
		shape.setStroke(false);
		
		//TODO ADD PARAM
		shape.beginShape(PConstants.QUADS);
		shape.textureMode(PConstants.NORMAL);
		shape.texture(image);

		int xSpace = getActualScene().params.get("xSpace");
		int ySpace = getActualScene().params.get("ySpace");
		
		//TODO use it
		float borderXSize = getActualScene().params.get("borderXSize");
		float borderYSize = getActualScene().params.get("borderYSize");
				
		float yInnerSpace = borderYSize/2;
		
		if(yInnerSpace>=ySpace)yInnerSpace = ySpace-1;
		
		int numberOfRows = 0;
		
		for (int y=0; y<height-ySpace; y+=ySpace) {
			
		    for (int x=0; x<width-xSpace; x+=xSpace) {

		    	PVector tl, bl, br, tr;

		    	if (numberOfRows==0) {
		    		tl = new PVector(x, y+yInnerSpace);
		    		bl = new PVector(x, y+ySpace);
		    		br = new PVector(x+xSpace, y+ySpace);
		    		tr = new PVector(x+xSpace, y+yInnerSpace);
		    	} else {
		    		tl = new PVector(x, y);
		    		bl = new PVector(x, y+ySpace-yInnerSpace);
		    		br = new PVector(x+xSpace, y+ySpace-yInnerSpace);
		        	tr = new PVector(x+xSpace, y);
		    	}
		    	
		    	//TODO do it in the shader! /LINE BREAKER
		    	if (lowResGrid) { //press 'r' key
		    		
		    		shape.vertex(tl.x, tl.y, tl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		      
		    	} else {
		    		
		    		if (numberOfRows==0) { //use bottom line as texture
		    					    			
			    		shape.vertex(tl.x, tl.y, tl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
			    		shape.vertex(bl.x, bl.y, bl.z, bl.x/kwidth/xRatio, bl.y/kheight/yRatio);
			    		shape.vertex(br.x, br.y, br.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
			    		shape.vertex(tr.x, tr.y, tr.z, br.x/kwidth/xRatio, br.y/kheight/yRatio);
		    		
		    		} else { //use top line as texture
		    			
		    			shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
			    		shape.vertex(bl.x, bl.y, bl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
			    		shape.vertex(br.x, br.y, br.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
			    		shape.vertex(tr.x, tr.y, tr.z, tr.x/kwidth/xRatio, tr.y/kheight/yRatio);
		    			
		    		}
		    	}
		    	
		    }

		    numberOfRows++;
		    if (numberOfRows==2)numberOfRows=0;
		  
		}

		shape.endShape(PApplet.CLOSE);
		return shape;
	}
	public static int getSceneId(){
		return sceneId;
	}
	public static void setSceneId(int _sceneId) {
		sceneId = _sceneId;
	}
	public static Scene getActualScene() {
		return actualScene;
	}
	public static void setActualScene(Scene actualScene) {
		App.actualScene = actualScene;
		App.actualMenu = actualScene.menu;
	}
	public static void editParams(int key, String[] parameters, int[] values){
		
		switch (key) {
			case 0:
				for (int i=0; i<parameters.length; i++){
					getActualScene().params.put(parameters[i], values[i]);
				}
				getActualScene().menu.reinitSlidersValueAndPos();
				break;
			default:
				break;
		}
		
	}
	//-------- key strokes --------//
	public static void keyPressed(char key) {
		
		switch (key) {
		case 'a':
			pausedPS = !pausedPS;
			break;
		case 'l':
			toggleValue();
			break;
		case 'm'://TODO update it
			if(useLiveMusic){
				if(in.isMonitoring())in.disableMonitoring();
			    else in.enableMonitoring();
			}
			break;
		case 'n':
			nextScene();
			break;
		case 'p':
			prevScene();
			break;
		case 'c':
			toogleColors();
			break;
		case 'r':
			editUVPos();
			break;
		default: //------- scenes -------// 
			if(App.getSceneId() == 0){	
				 DrawLineScene.keyPressed(key);
			} else if(App.getSceneId() == 1){
				DrawPointScene.keyPressed(key);
			} else if(App.getSceneId() == 3){
				ShapeScene.keyPressed(key);
			} else if(App.getSceneId() == 4){
				ChunkyScene.keyPressed(key);
			}
			return;
		}
		
	}
	private static void toggleValue() {
		  switchValue = !switchValue;
	}
	public static void setSelectedValue(int value) {    
		if (switchValue) {
			lowestValue += value;
			lowestValue = Math.max(50, Math.min(highestValue-10, lowestValue));
		} else {
			highestValue += value;
			highestValue = Math.max(lowestValue+10, Math.min(15000, highestValue));
		}
	}
	private static void nextScene(){	
		int id = getSceneId();
		id++;
		if(id>5)id=0;
		setSceneId(id);
	}
	private static void prevScene(){
		int id = getSceneId();
		id--;
		if(id<0)id=4;
		setSceneId(id);
	}
	private static void toogleColors(){
		useColors = !useColors;
	}
	private static void editUVPos(){
		lowResGrid = !lowResGrid;
		recreateShapeGrid = true;
	}
}
