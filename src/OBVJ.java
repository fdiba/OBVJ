

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Date;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import ddf.minim.Minim;
import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import themidibus.MidiBus;
import webodrome.App;
import webodrome.ctrl.BehringerBCF;
import webodrome.ctrl.PFrame;
import webodrome.scene.ChunkyScene;
import webodrome.scene.DrawPointsAndLinesScene;
import webodrome.scene.MonitorScene;
import webodrome.scene.ShapeScene;

@SuppressWarnings("serial")
public class OBVJ extends PApplet {
	
	private static Rectangle monitor;
	private SimpleOpenNI context;
	
	private int w = 1024;
	private int h = 768;
	
	/*private int w = 640;
	private int h = 480;*/
	
	@SuppressWarnings("unused")
	private int timeToTakeASnapShot;
		
	//-------- scenes -----------//
	
	private DrawPointsAndLinesScene drawPointsAndLinesScene; //scene 0 et 1
	private ShapeScene shapeScene; //scene 2
	private ChunkyScene chunkyScene; //scene 3
	private MonitorScene monitorScene; //scene 4
	
	//---------------------------//

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GraphicsEnvironment gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = gEnvironment.getScreenDevices();
		
		if(graphicsDevices.length>1){
			GraphicsDevice graphicsDevice = graphicsDevices[1];
			GraphicsConfiguration[] gConfigurations = graphicsDevice.getConfigurations();
			monitor = gConfigurations[0].getBounds();
			PApplet.main( new String[] { "--display=1", OBVJ.class.getSimpleName() });
			//PApplet.main( new String[] { "--present", KPrez.class.getSimpleName() });
		} else {
			GraphicsDevice graphicsDevice = graphicsDevices[0];
			GraphicsConfiguration[] gConfigurations = graphicsDevice.getConfigurations();
			monitor = gConfigurations[0].getBounds();
			PApplet.main(OBVJ.class.getSimpleName());
		}

	}
	public void init() {
		frame.removeNotify();
		frame.setUndecorated(true);
		super.init();
	}
	public void setup(){		
		
		if(App.fscreen){
			w = monitor.width;
			h = monitor.height;
		}		
		
		size(w, h, OPENGL);
		//size(w, h, P3D);
		smooth(8);
		frameRate(15); //---------------------------------- param -------//
		
		//noCursor();
		
		PFrame pFrame = new PFrame(360+640/2, 340);
		pFrame.setTitle("ctrl board");
				
		context = new SimpleOpenNI(this);
		
		if (context.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
		    exit();
		    return;
		} else {

			//context.setMirror(true);
			
			context.enableDepth();
			context.enableUser();
			
			App.minim = new Minim(this);
			
			if(!App.useLiveMusic){
				App.player = App.minim.loadFile("DwaMillioneMSTRDrev11644.wav");
				App.player.play();	
				//App.player.loop();			
				App.player.mute();
			} else {			
				App.in = App.minim.getLineIn(Minim.MONO);
			}
			
			//--- behringer -----------//		  
			if(App.BCF2000){
				MidiBus.list();
				App.midiBus = new MidiBus(this, "BCF2000", "BCF2000");
				App.behringer = new BehringerBCF(App.midiBus);
			}
			//-------------------------//
		
		}

	}
	public void draw(){	
				
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
		default:
			scene0();
			break;
		}

		//timeToTakeASnapShot--;
		//if(timeToTakeASnapShot == 0 || timeToTakeASnapShot == 24 || timeToTakeASnapShot == 24) savePicture();
		
		//drawCuePoints();		
	
	}
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
			
			App.oldSceneId = sceneId;
		
			frameRate(15);
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},                
	                {"strokeWeight", 4, 100, 4, App.colors[6]},
	                {"rotateX", -360, 360, 45, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"ySpace", 4, 150, 4, App.colors[5]},
	                
	                {"depth", -200, 200, 112, App.colors[6]},
	                {"amplitude", 1, 500, 50, App.colors[4]},
	                {"maxDist", 1, 250, 140, App.colors[7]},
	                {"alpha", -255, 255, 255, App.colors[3]} };
			
			drawPointsAndLinesScene = new DrawPointsAndLinesScene(this, objects, w, h);
			App.setActualScene(drawPointsAndLinesScene);
					
		}
		
		//-------------- draw ------------------//
		
		drawPointsAndLinesScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		drawPointsAndLinesScene.display();
		  
		popMatrix();
						
	}
	private void scene1(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			frameRate(15);
		
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},
	                {"strokeWeight", 4, 100, 4, App.colors[5]},
	                {"rotateX", -360, 360, 45, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"amplitude", 1, 200, 25, App.colors[4]},
	                
	                {"ySpace", 4, 150, 4, App.colors[5]},
	                {"depth", -200, 200, 112, App.colors[6]},
	                {"maxDist", 1, 250, 45, App.colors[7]},
	                {"alpha", -255, 255, 255, App.colors[3]} };
			
			drawPointsAndLinesScene = new DrawPointsAndLinesScene(this, objects, w, h);
			App.setActualScene(drawPointsAndLinesScene);
					
		}
		
		//-------------- draw ------------------//

		drawPointsAndLinesScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		drawPointsAndLinesScene.display1();
		  
		popMatrix();
		
	}
	private void scene2(){
		
		background(0);
				
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},
	                {"alpha", 0, 255, 0, App.colors[6]},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"frameRate", 1, 30, 25, App.colors[6]},
	                
	                {"iterations", 1, 20, 10, App.colors[4]},
	                {"blurRadius", 1, 30, 2, App.colors[5]},
	                {"distMin", 10, 200, 10, App.colors[3]},
	                {"edgeMinNumber", 3, 400, 100, App.colors[7]},
	                {"amplitude", 1, 1000, 10, App.colors[5]},
	                {"strokeWeight", 1, 100, 1, App.colors[5]} };
			
			shapeScene = new ShapeScene(this, objects, w, h);
			App.setActualScene(shapeScene);
						
		}
		
		//-------------- draw ------------------//

		shapeScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		shapeScene.display();
		  
		popMatrix();
		
	}
	private void scene3(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},
	                {"alpha", 0, 200, 0, App.colors[6]},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]},
	                {"iterations", 1, 20, 10, App.colors[4]},
	                
	                {"amplitude", 1, 1000, 300, App.colors[5]},
	                {"strokeWeight", 1, 100, 3, App.colors[5]} };
			
			chunkyScene = new ChunkyScene(this, objects, w, h);
			App.setActualScene(chunkyScene);
						
		}
		
		//-------------- draw ------------------//
		chunkyScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		chunkyScene.display();
		  
		popMatrix();
	
	}
	private void scene4(){
		
		background(0);
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			
			App.oldSceneId = sceneId;
			
			Object[][] objects = { {"xTrans", -2500, 2500, 0},
	                {"yTrans", -2500, 2500, -100},
	                {"zTrans", -2500, 2500, -200},
	                {"rotateX", -360, 360, 0, App.colors[0]},
	                {"rotateY", -360, 360, 0, App.colors[1]},
	                {"rotateZ", -360, 360, 0, App.colors[2]} };
			
			monitorScene = new MonitorScene(this, objects, w, h);
			App.setActualScene(monitorScene);
						
		}
		
		//-------------- draw ------------------//

		monitorScene.update(context);
		
		pushMatrix();
		  
		translateAndRotate();
		  
		monitorScene.display();
		  
		popMatrix();
		
		
	}
	private void translateAndRotate(){
		
		Map<String, Integer> params = App.getActualScene().params;
		  
		translate(w/2 + params.get("xTrans"), h/2 + params.get("yTrans"), params.get("zTrans"));
  
		rotateX(radians(params.get("rotateX")));
		rotateY(radians(params.get("rotateY")));
		rotateZ(radians(params.get("rotateZ")));
  
		translate(-w/2, -h/2, 0);
 
	}
	//--------------- keys ---------------------//
	public void keyPressed() {
				
	
		if (key == 's'){
			//TO DO move it
		    savePicture();			
		} 
	}
	
	public void savePicture() {
		Date date = new Date();
		String name = "data/images/objv-"+date.getTime()+".png";
		save(name);	
	}
	//------------- MIDI ------------------//
	public void midiMessage(MidiMessage message, long timestamp, String bus_name) {
	  
	   int channel = message.getMessage()[0] & 0xFF;
	   int number = message.getMessage()[1] & 0xFF;
	   int value = message.getMessage()[2] & 0xFF;
	   
	   //PApplet.println("bus " + bus_name + " | channel " + channel + " | num " + number + " | val " + value);
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
