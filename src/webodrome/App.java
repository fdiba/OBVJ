package webodrome;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import themidibus.MidiBus;
import webodrome.ctrl.BehringerBCF;
import webodrome.ctrl.Menu;
import webodrome.ctrl.SecondApplet;
import webodrome.scene.Scene;

public class App {
	
	public static int width = 1024;
	public static int height = 768;
	
	public static boolean usePeasyCam = true;
	
	public static PApplet objv;
	public static SecondApplet secondApplet;
	
	public static boolean useLiveMusic = true;
	
	//-------- KINECT CONST ----------//
	public static int KWIDTH = 640;
	public static int KHEIGHT = 480;
	
	//-------- depthMapControl ----------//
	public static int lowestValue = 550;
	public static int highestValue = 2350;
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
	
	public static boolean useColors;
	
	public static boolean lowResGrid;
	public static boolean recreateShapeGrid;
	
	//-------- behringer ----------//
	public final static boolean BCF2000 = true;
	public static MidiBus midiBus;
	public static BehringerBCF behringer;
	
	public static int[] transValues;
	
	//-----------------------//
	
	public static boolean fscreen = false;
	
	private static int sceneId = 0;
	public static int oldSceneId = 999;
	
	public App() {
		// TODO Auto-generated constructor stub
	}
	public static void recreateShapeGrid(){
		mainGrid = createShapeGrid(objv.createImage(App.KWIDTH, App.KHEIGHT, PConstants.ARGB));
		mainGrid.setStroke(false);
	}
	public static PShape createShapeGrid(PImage image){
				
		int kwidth = App.KWIDTH;
		int kheight = App.KHEIGHT;
		
		float xRatio = (float) width/kwidth;
		float yRatio = (float) height/kheight;
		
		PShape shape = objv.createShape();
		
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
		    	
		    	//TODO do it in the shader! /LINE BREAKING
		    	if (lowResGrid) { //press 'r' key
		    		
		    		shape.vertex(tl.x, tl.y, tl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		shape.vertex(bl.x, bl.y, bl.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		shape.vertex(br.x, br.y, br.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		    		shape.vertex(tr.x, tr.y, tr.z, tl.x/kwidth/xRatio, tl.y/kheight/yRatio);
		      
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
}
