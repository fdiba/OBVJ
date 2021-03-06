


import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Date;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import SimpleOpenNI.SimpleOpenNI;
import peasy.PeasyCam;
import processing.core.*;
import themidibus.MidiBus;
import webodrome.App;
import webodrome.ctrl.BehringerBCF;
import webodrome.ctrl.PFrame;
import webodrome.scene.ChunkyScene;
import webodrome.scene.DrawLineScene;
import webodrome.scene.DrawPointScene;
import webodrome.scene.MonitorScene;
import webodrome.scene.RadarScene;
import webodrome.scene.ShapeScene;

@SuppressWarnings("serial")
public class OBVJ extends PApplet {
	
	private static Rectangle monitor;
	private SimpleOpenNI context;

	private int timeToTakeASnapShot;
		
	//-------- scenes -----------//
	
	private DrawLineScene drawLineScene; //scene 0
	private DrawPointScene drawPointScene; //scene 1
	private RadarScene radarScene; //scene 2
	private ShapeScene shapeScene; //scene 3
	private ChunkyScene chunkyScene; //scene 4
	private MonitorScene monitorScene; //scene 5
	
	//---------------------------//

	public static void main(String[] args) {
		
		GraphicsEnvironment gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = gEnvironment.getScreenDevices();
		
		if(graphicsDevices.length>1){
			GraphicsDevice graphicsDevice = graphicsDevices[1];
			GraphicsConfiguration[] gConfigurations = graphicsDevice.getConfigurations();
			monitor = gConfigurations[0].getBounds();
			//choose your screen: 0, 1, ...
			PApplet.main( new String[] { "--display=1", OBVJ.class.getSimpleName() });
		} else {
			GraphicsDevice graphicsDevice = graphicsDevices[0];
			GraphicsConfiguration[] gConfigurations = graphicsDevice.getConfigurations();
			monitor = gConfigurations[0].getBounds();
			PApplet.main(OBVJ.class.getSimpleName());
		}

	}
	public void init() {
		frame.removeNotify();
		//frame.setUndecorated(true);
		super.init();
	}
	public void setup(){		
		
		if(App.fscreen){
			App.width = monitor.width;
			App.height = monitor.height;
		}		
		
		size(App.width, App.height, OPENGL);
		smooth(8);
		
		//noCursor();
		
		PFrame pFrame = new PFrame(360+640/2 + 1024/2-200, 480+40*2);
		pFrame.setTitle("ctrl board");
		
		if(App.usePeasyCam){
			//App.cam = new PeasyCam(this, 1600);
			App.cam = new PeasyCam(this, App.camDist1);
			//App.cam.setMinimumDistance(50);
			//App.cam.setMaximumDistance(1500);
		}
				
		if (App.useKinect) {
			
			App.sl_frameRate=24;
			context = new SimpleOpenNI(this);
			System.out.println("kinect mode");
			
			if (context.isInit() == false){
				System.out.println("Can't init SimpleOpenNI, maybe the camera is not connected!");
				exit();
			    return;
			} else {
				//TODO DO NOT WORK
				//context.setMirror(true);
							
				context.enableDepth();
				context.enableUser();
			}
			
		} else {
			
			App.sl_frameRate=60; //TODO fps should be changed with psRunning
			System.out.println("no kinect mode");
			App.psRunning = true;
			App.cam.setDistance(App.camDist2);
			
		}
		
		App.objv = this;
		
		App.minim = new Minim(this);
					
		if(!App.useLiveMusic){
			App.player = App.minim.loadFile("DwaMillioneMSTRDrev11644.wav");
			App.player.play();	
			//App.player.loop();			
			App.player.mute();
			App.fft = new FFT(App.player.bufferSize(), App.player.sampleRate());
			App.imgSoundWidth = App.player.bufferSize();
		} else {			
			App.in = App.minim.getLineIn(Minim.MONO);
			App.fft = new FFT(App.in.bufferSize(), App.in.sampleRate());
			App.imgSoundWidth = App.in.bufferSize();
			//println("App.imgSoundWidth: " + App.imgSoundWidth);
		}
		
		//TODO ERASE IT WHEN SOFT UPDATED
		setVectorsGrid();
		App.recreateShapeGrid = true;
		
		App.defaultShader = loadShader("defaultShader_frag.glsl", "defaultShader_vert.glsl");
		
		//----------- behringer -----------//		  
		if(App.BCF2000){
			MidiBus.list();
			App.midiBus = new MidiBus(this, "BCF2000", "BCF2000");
			App.behringer = new BehringerBCF(App.midiBus);
		}
		
		App.transValues = new int[8];
	    for(int i=0; i<App.transValues.length; i++) {
	    	App.transValues[i]=0;
	    }
		//-------------------------//
		  
		//hint(DISABLE_DEPTH_MASK);

	}
	public void draw(){	
		
		App.secondApplet.redraw();
				
		if(!App.psRunning)context.update();
		
		int sceneId = App.getSceneId();
		
		switch (sceneId) {
		case 0:
			scene0(); //points and lines
			break;
		case 1:
			scene1();
			break;
		case 2:
			scene2();
			break;
		case 3:
			scene3();
			break;
		case 4:
			scene4();
			break;
		case 5:
			scene5();
			break;
		default:
			scene0();
			break;
		}

		//timeToTakeASnapShot--;
		//if(timeToTakeASnapShot == 0 || timeToTakeASnapShot == 24 || timeToTakeASnapShot == 24) savePicture();
		
		//drawCuePoints();		
	
	}
	//-------------------- GRIDS --------------------//
	//TODO ERASE IT
	private void setVectorsGrid(){
		
		int imgWidth = 640;
		int imgHeight = 480;
			
		float xRatio = (float) App.width/imgWidth;
		float yRatio = (float) App.height/imgHeight;
		
		App.pvectors = new PVector[imgWidth*imgHeight]; 
		for (int i=0; i<imgHeight; i++){			
			for(int j=0; j<imgWidth; j++){
				App.pvectors[j+i*imgWidth] = new PVector(j*xRatio, i*yRatio, 0);				
		    }
		} 
	}
	//-------------------- GRIDS --------------------//
	private void drawCuePoints(){
		
		int time = millis();
		
		if( (time >= 20000 && time <= 30000) || (time >= 80000 && time <= 81000) ){
		
			fill(0xFFFF0000);
			noStroke();
			rect(10, 10, 10, 10);
			
			//println(millis());
		
		}
	}
	//---------- scenes ----------------//
	private void scene0(){
		
		//background(0xFFFFFF);
		background(0x000000);
		//background(0xFF0000);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			frameRate(App.sl_frameRate);
			App.oldSceneId = sceneId;
			
			Object[][] objects = (Object[][]) App.loadParameters("scene0.txt");	
			
			drawLineScene = new DrawLineScene(this, objects, App.width, App.height);
			App.setActualScene(drawLineScene);
					
		}
		
		if(App.psRunning && App.sl_frameRate < 60) {
			App.sl_frameRate = 60;
			frameRate(App.sl_frameRate);
		} else if(!App.psRunning && App.sl_frameRate > 24){
			App.sl_frameRate = 24;
			frameRate(App.sl_frameRate);
		}
		
		//-------------- draw ------------------//
		
		drawLineScene.update(context);
		//else drawLineScene.update();
			
		pushMatrix();
		
		if(!App.psRunning)translateAndRotateV2();
		  
		drawLineScene.display();
		  
		popMatrix();
						
	}
	private void scene1(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			frameRate(App.sl_frameRate);
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = (Object[][]) App.loadParameters("scene1.txt");
			
			drawPointScene = new DrawPointScene(this, objects, App.width, App.height);
			App.setActualScene(drawPointScene);
					
		}
		
		//-------------- draw ------------------//
		
		drawPointScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		drawPointScene.display(App.pvectors);
		  
		popMatrix();
		
	}
	private void scene2(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			frameRate(App.sl_frameRate);
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = (Object[][]) App.loadParameters("scene2.txt");
			
			radarScene = new RadarScene(this, objects, App.width, App.height);
			App.setActualScene(radarScene);
					
		}
		
		//-------------- draw ------------------//
		
		radarScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		radarScene.display();
		  
		popMatrix();
		
	}
	private void scene3(){
		
		background(0);
				
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = (Object[][]) App.loadParameters("scene3.txt");
			
			shapeScene = new ShapeScene(this, objects, App.width, App.height);
			App.setActualScene(shapeScene);
						
		}
		
		//-------------- draw ------------------//

		shapeScene.update(context);
		
		pushMatrix();
		
		translateAndRotateV2();
		  
		shapeScene.display();
		  
		popMatrix();
		
	}
	private void scene4(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = (Object[][]) App.loadParameters("scene4.txt");
			
			chunkyScene = new ChunkyScene(this, objects, App.width, App.height);
			App.setActualScene(chunkyScene);
						
		}
		
		//-------------- draw ------------------//
		chunkyScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		chunkyScene.display();
		  
		popMatrix();
	
	}
	private void scene5(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = (Object[][]) App.loadParameters("scene5.txt");
			
			monitorScene = new MonitorScene(this, objects, App.width, App.height);
			App.setActualScene(monitorScene);
						
		}
		
		//-------------- draw ------------------//

		monitorScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		monitorScene.display();
		  
		popMatrix();
		
		
	}
	private void translateAndRotateV2(){ //only for DrawLineScene for the moment
		
		Map<String, Integer> params = App.getActualScene().params;
		
		if(App.usePeasyCam){
			translate(getTrans(params.get("xTrans"), 0), getTrans(params.get("yTrans"), 1), getTrans(params.get("zTrans"), 2));
		} else {
			translate(App.width/2 + getTrans(params.get("xTrans"), 0), App.height/2 + getTrans(params.get("yTrans"), 1), getTrans(params.get("zTrans"), 2));
		}
		
		rotateX(radians(getRotation(params.get("rotateX"), 3)));
		rotateY(radians(getRotation(params.get("rotateY"), 4)));
		rotateZ(radians(getRotation(params.get("rotateZ"), 5)));	
		
		if(DrawLineScene.mode<=1){ //old mode
			translate(-App.width/2, -App.height/2, 0);
		}

		if(App.usePeasyCam){
		} else {
			if(DrawLineScene.mode>1){ 
				translate(-App.width/2, -App.height/2, 0);
			}
		}
 
	}
	private void translateAndRotate(){
		
		Map<String, Integer> params = App.getActualScene().params;
		
		if(App.usePeasyCam){
			//translate(params.get("xTrans"), params.get("yTrans"), params.get("zTrans"));
			translate(getTrans(params.get("xTrans"), 0), getTrans(params.get("yTrans"), 1), getTrans(params.get("zTrans"), 2));
		} else {
			//translate(App.width/2 + params.get("xTrans"), App.height/2 + params.get("yTrans"), params.get("zTrans"));
			translate(App.width/2 + getTrans(params.get("xTrans"), 0), App.height/2 + getTrans(params.get("yTrans"), 1), getTrans(params.get("zTrans"), 2));
		}
  
		rotateX(radians(params.get("rotateX")));
		rotateY(radians(params.get("rotateY")));
		rotateZ(radians(params.get("rotateZ")));
		
		translate(-App.width/2, -App.height/2, 0);
 
	}
	private int getTrans(int pValue, int id){
		
		int value = App.transValues[id];
		int bValue = BehringerBCF.potValues[id];
		
		if(bValue==0 && value==pValue){
			return pValue;
		} else if(bValue==0 && value!=pValue){
						
			int sub = pValue - value;
			value = (int) (value + sub*0.2);
			App.transValues[id] = value;
			return value;
			
		} else {
			
			bValue-=5;
			if(bValue<0)bValue=0;
			
			value += bValue;		
			
			if(id==0 && value>1500)value*=-1;
			if(id==1 && value>1000)value*=-1;
			if(id==2 && value>1200)value*=-1;
			
			App.transValues[id] = value;
			
			return value;
		}
		
	}
	private int getRotation(int pValue, int id){
		
		int value = App.transValues[id];
		int bValue = BehringerBCF.potValues[id];
		
		if(bValue==0 && value==pValue){
			return pValue;
		} else if(bValue==0 && value!=pValue){
						
			int sub = pValue - value;
			value = (int) (value + sub*0.2);
			App.transValues[id] = value;
			return value;
			
		} else {
			
			bValue-=5;
			if(bValue<0)bValue=0;
			
			value = (value+bValue)%360;
			App.transValues[id] = value;
			return value;
		}
		
	}
	//--------------- keys ---------------------//
	public void keyPressed() {
		
		App.keyPressed(key);
				
		if (keyCode==UP) {
			App.setSelectedValue(+50);
		} else if (keyCode==DOWN) {
			App.setSelectedValue(-50);
		} else if (key == 's'){ //------- save things -------//
		    saveScreenPicture();			
		}
	}
	
	public void saveScreenPicture() {
		Date date = new Date();
		//String name = "data/images/objv-"+date.getTime()+".png";
		String name = "data/images/objv-"+date.getTime()+".jpg";
		save(name);	
	}
	//------------- MIDI ------------------//
	public void midiMessage(MidiMessage message, long timestamp, String bus_name) {
	  
	   int channel = message.getMessage()[0] & 0xFF;
	   int number = message.getMessage()[1] & 0xFF;
	   int value = message.getMessage()[2] & 0xFF;
	   
	   //System.out.println("bus " + bus_name + " | channel " + channel + " | num " + number + " | val " + value);
	   if(App.BCF2000 && App.getActualScene().menu != null) App.behringer.midiMessage(channel, number, value);
	   
	}
	//--------------- mouse ---------------------//
	public void mousePressed() {
	  //savePicture();
	  //savePictureWithDelay();
	}
	@SuppressWarnings("unused")
	private void savePictureWithDelay(){
		timeToTakeASnapShot = 24*4;
	}
	// -----------------------------------------------------------------
	// SimpleOpenNI events

	public void onNewUser(SimpleOpenNI curContext, int userId) {
	  println("onNewUser - userId: " + userId);
	  println("\tstart tracking skeleton");
	  
	  curContext.startTrackingSkeleton(userId);
	}

	public void onLostUser(SimpleOpenNI curContext, int userId) {
	  println("onLostUser - userId: " + userId);
	}

	public void onVisibleUser(SimpleOpenNI curContext, int userId) {
	  //println("onVisibleUser - userId: " + userId);
	}
}
