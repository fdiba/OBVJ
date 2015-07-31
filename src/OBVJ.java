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
	private PeasyCam cam;

	@SuppressWarnings("unused")
	private int timeToTakeASnapShot;
	
	private int sl_frameRate = 24;
		
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
		frameRate(24); //TODO PARAM
		
		//noCursor();
		
		PFrame pFrame = new PFrame(360+640/2 + 1024/2-200, 480+40*2);
		pFrame.setTitle("ctrl board");
		
		if(App.usePeasyCam){
			cam = new PeasyCam(this, 500);
			cam.setMinimumDistance(50);
			cam.setMaximumDistance(1500);
		}
		
		context = new SimpleOpenNI(this);
		
		if (context.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
		    exit();
		    return;
		} else {

			//TODO DO NOT WORK
			//context.setMirror(true);
			
			context.enableDepth();
			context.enableUser();
			
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
		
		}
		  
		//hint(DISABLE_DEPTH_MASK);

	}
	public void draw(){	
		
		App.secondApplet.redraw();
				
		context.update();
		
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
	@SuppressWarnings("unused")
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
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			frameRate(sl_frameRate);
			App.oldSceneId = sceneId;
					
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},                
	                {"strokeWeight", 1, 100, 1, App.colors[6]}, //4 before
	                {"rotateX", -360, 360, 45, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"rawData", 0, 10, 2, App.colors[6]}, //when zero freeze depthvalues
	                
	                {"fttAmpli", 1, 150, 100, App.colors[4]},
	                {"texFftStart", 0, 10, 0, App.colors[6]}, // 0 100 8
	                {"texFftEnd", 0, 10, 0, App.colors[0]}, // 1 5 3
	                {"depthTS", -200, 200, -74, App.colors[6]},
	                {"xSpace", 4, 100, 10, App.colors[4]}, //4 --> 150
	                {"ySpace", 4, 100, 10, App.colors[5]}, //4 --> 150
	                {"depth", -1000, 1000, 100, App.colors[6]}, //create space between points z axis 100=10
	                {"amplitude", 1, 3000, 300, App.colors[4]}, //before 390 max 500
	                
	                {"damper", 0, 10, 9, App.colors[6]},
	                {"maxDist", 1, 1500, 500, App.colors[7]},
	                {"borderXSize", 0, 200, 0, App.colors[4]}, //in relation with xSpace
	                //TODO place below ySpace
	                {"borderYSize", 0, 200, 15, App.colors[5]}, //borderYSize = ySpace*2 
	                //TODO make it FLOAT beetween 0 and 1 
	                {"colorTS", 0, 254, 0, App.colors[3]}, //offset colors of tex1
	                //TODO make it FLOAT beetween 0 and 1 
	                {"fillAlpha", 0, 255, 25, App.colors[6]}, //only used with textures
					{"strokeAlpha", 0, 255, 45, App.colors[7]}}; //only used with textures
			
			drawLineScene = new DrawLineScene(this, objects, App.width, App.height);
			App.setActualScene(drawLineScene);
					
		}
		
		//-------------- draw ------------------//
		
		drawLineScene.update(context);
		
		pushMatrix();
		  
		translateAndRotateV2();
		  
		drawLineScene.display();
		  
		popMatrix();
						
	}
	private void scene1(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			frameRate(sl_frameRate);
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},                
	                {"strokeWeight", 1, 100, 4, App.colors[6]},
	                {"rotateX", -360, 360, 45, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"rawData", 1, 10, 5, App.colors[6]},
	                
	                {"fttAmpli", 1, 150, 50, App.colors[4]},
	                {"texFftStart", 0, 100, 0, App.colors[6]},
	                {"texFftEnd", 1, 5, 2, App.colors[0]},
	                {"depthTS", -200, 200, -74, App.colors[6]},
	                {"xSpace", 4, 150, 4, App.colors[4]},
	                {"ySpace", 4, 150, 4, App.colors[5]},
	                {"depth", -1000, 1000, 110, App.colors[6]},
	                {"amplitude", 1, 500, 390, App.colors[4]},
	                
	                {"maxDist", 1, 250, 250, App.colors[7]},
	                {"colorTS", 0, 254, 38, App.colors[3]}};
			
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
			
			frameRate(sl_frameRate);
			App.oldSceneId = sceneId;
			
			shader(App.defaultShader);
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, 0},
	                {"zTrans", -2500, 2500, 0},                
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"xSpace", 4, 150, 4, App.colors[4]},
	                {"ySpace", 4, 150, 4, App.colors[5]},
	                
	                {"area", 1, 1000, 260, App.colors[0]},
	                {"speed", 0, 1000, 50, App.colors[1]}};
			
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
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, 0},
	                {"zTrans", -2500, 2500, 0},
	                {"alpha", 0, 255, 0, App.colors[6]},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"frameRate", 1, 30, sl_frameRate, App.colors[6]},
	                
	                {"iterations", 1, 20, 10, App.colors[4]},
	                {"blurRadius", 1, 30, 2, App.colors[5]},
	                {"distMin", 10, 200, 10, App.colors[3]},
	                {"edgeMinNumber", 3, 400, 100, App.colors[7]},
	                {"amplitude", 1, 1000, 10, App.colors[5]},
	                {"strokeWeight", 1, 100, 1, App.colors[5]} };
			
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
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, 0},
	                {"zTrans", -2500, 2500, 0},
	                {"alpha", 0, 200, 0, App.colors[6]},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"iterations", 1, 20, 10, App.colors[4]},
	                
	                {"amplitude", 1, 1000, 300, App.colors[5]},
	                {"strokeWeight", 1, 100, 3, App.colors[5]} };
			
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
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, 0},
	                {"zTrans", -2500, 2500, 0},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]} };
			
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
				
		if (key=='l') {
			App.toggleValue();
		} else if (keyCode==UP) {
			App.setSelectedValue(+50);
		} else if (keyCode==DOWN) {
			App.setSelectedValue(-50);
		} else if(key=='n'){
			App.nextScene();
		} else if(key =='p'){
			App.prevScene();
		} else if (key=='c') {
			App.toogleColors();
		} else if (key=='r') {
			App.editUVPos();
		} else if (key == 's'){ //------- save things -------//
		    saveScreenPicture();			
		} else if(App.getSceneId() == 0){ //------- scenes -------//
			DrawLineScene.keyPressed(key);
		} else if(App.getSceneId() == 1){
			DrawPointScene.keyPressed(key);
		} else if(App.getSceneId() == 3){
			ShapeScene.keyPressed(key);
		} else if(App.getSceneId() == 4){
			ChunkyScene.keyPressed(key);
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

	public void onNewUser(SimpleOpenNI curContext, int userId)
	{
	  println("onNewUser - userId: " + userId);
	  println("\tstart tracking skeleton");
	  
	  curContext.startTrackingSkeleton(userId);
	}

	public void onLostUser(SimpleOpenNI curContext, int userId)
	{
	  println("onLostUser - userId: " + userId);
	}

	public void onVisibleUser(SimpleOpenNI curContext, int userId)
	{
	  //println("onVisibleUser - userId: " + userId);
	}
}
