
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
import webodrome.scene.DrawPointsAndLinesScene;

@SuppressWarnings("serial")
public class OBVJ extends PApplet {
	
	private static Rectangle monitor;
	private SimpleOpenNI context;
	private int w = 1024;
	private int h = 768;
		
	
	private int timeToTakeASnapShot;
	
	//-------- scenes -----------//
	
	private DrawPointsAndLinesScene drawPointsAndLinesScene; //scene 0

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
		smooth(8);
		frameRate(12); //---------------------------------- param -------//
				
		context = new SimpleOpenNI(this);
		
		if (context.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
		    exit();
		    return;
		} else {

			context.setMirror(true);			
			context.enableDepth();
			
			App.minim = new Minim(this);
			App.player = App.minim.loadFile("02-Hourglass.mp3");
			App.player.loop();			
			App.player.mute();
			
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
	
		background(0);
		//background(0, 77, 119);
		
		context.update();
		//image(context.depthImage(), 0, 0); //test
		
		int sceneId = App.getSceneId();
		
		switch (sceneId) {
		case 0:
			scene0(); //points and lines
			break;
		default:
			scene0();
			break;
		}

		timeToTakeASnapShot--;
		if(timeToTakeASnapShot == 0 || timeToTakeASnapShot == 24 || timeToTakeASnapShot == 24) savePicture();
		
	
	}
	private void scene0(){
		
		//-------------- init ------------------//
		
		int sceneId = App.getSceneId();
		if (sceneId != App.oldSceneId) {
			App.oldSceneId = sceneId;
		
			Object[][] objects = { {"xTrans", -2500, 2500, App.colors[0], 0, 0, 0},
	                {"yTrans", -2500, 2500, App.colors[1], 0, 1, 0},
	                {"zTrans", -2500, 2500, App.colors[2], 0, 2, -200},
	                {"rotateX", -360, 360, App.colors[0], 1, 0, 45},
	                {"rotateY", -360, 360, App.colors[1], 1, 1, 0},
	                {"rotateZ", -360, 360, App.colors[2], 1, 2, 0},
	                {"amplitude", 1, 200, App.colors[4], 1, 3, 25},
	                {"ySpace", 10, 150, App.colors[5], 1, 4, 10},
	                {"depth", -200, 200, App.colors[6], 1, 5, 60},
	                {"maxDist", 1, 250, App.colors[7], 1, 6, 45},
	                {"alpha", -255, 255, App.colors[3], 1, 7, 255} };
			
			drawPointsAndLinesScene = new DrawPointsAndLinesScene(this, objects, w, h);
			App.setActualScene(drawPointsAndLinesScene);
					
		}
		
		//-------------- draw ------------------//
		
		drawPointsAndLinesScene.update(context);
		
		
		
		pushMatrix();
		  
		translateAndRotate();
		  
		drawPointsAndLinesScene.display();
		  
		popMatrix();
		
		App.getActualScene().displayMenu();
				
	}
	void translateAndRotate(){
		
		Map<String, Integer> params = App.getActualScene().params;
		  
		translate(w/2 + params.get("xTrans"), h/2 + params.get("yTrans"), params.get("zTrans"));
  
		rotateX(radians(params.get("rotateX")));
		rotateY(radians(params.get("rotateY")));
		rotateZ(radians(params.get("rotateZ")));
  
		translate(-w/2, -h/2, 0);
 
	}
	//--------------- keys ---------------------//
	public void keyPressed() {
		if (key == 'l') {
			toggleValue();
		} else if (keyCode == UP) {
			setSelectedValue(+50);
		} else if (keyCode == DOWN) {
			setSelectedValue(-50);
		} else if (key == 'v') {
			DrawPointsAndLinesScene.linesVisibility = !DrawPointsAndLinesScene.linesVisibility;
		} else if (key == 'b') {
			DrawPointsAndLinesScene.multipleBuffers = !DrawPointsAndLinesScene.multipleBuffers;
		} else if (key == 'c') {
			DrawPointsAndLinesScene.useColors = !DrawPointsAndLinesScene.useColors;
		}
	}
	private void toggleValue() {
		  App.switchValue = !App.switchValue;
	}
	private void setSelectedValue(int value) {    

		if (App.switchValue) {
			App.lowestValue += value;
			App.lowestValue = constrain(App.lowestValue, 0, App.highestValue-100);
			println(App.lowestValue);
		} else {
			App.highestValue += value;
			App.highestValue = constrain(App.highestValue, App.lowestValue+100, 7000);
			println(App.highestValue);
		}
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
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
	public void mousePressed() {
	  //savePicture();
	  savePictureWithDelay();
	}
	private void savePictureWithDelay(){
		timeToTakeASnapShot = 24*4;
	}
	private void savePicture() {
	  Date date = new Date();
	  String name = "data/images/objv-"+date.getTime()+".png";
	  save(name);
	}

}
