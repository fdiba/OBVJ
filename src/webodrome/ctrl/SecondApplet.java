package webodrome.ctrl;

import java.util.Date;

import processing.core.PApplet;
import processing.core.PVector;
import webodrome.App;
import webodrome.scene.ChunkyScene;
import webodrome.scene.DrawLineScene;
import webodrome.scene.DrawPointScene;
import webodrome.scene.ShapeScene;

@SuppressWarnings("serial")
public class SecondApplet extends PApplet {
	
	private int[] colors;
	private PVector[] positions;
	private PVector vPos;
	
	public SecondApplet() {
		colors = new int[2];
		colors[0] = color(255, 0, 255);
		colors[1] = color(0, 255, 0);
		positions = new PVector[2];
		positions[0] = new PVector(180, 10);
		positions[1] = new PVector(190, 10);
		vPos = new PVector(340, 20);
	}
	
	public void setup(){
		
		frameRate(24);
		
	}
	public void draw(){
		
		background(0xFF333333);

		if(App.getActualScene() != null){
			
			if(App.getSceneId() == 3) { //scene 3
				
				if(ShapeScene.userIsPresent)displayCube(colors[0], positions[0]);
				if(ShapeScene.isTrackingSkeleton)displayCube(colors[1], positions[1]);
				displayMiniature();
				
			} else if (App.getSceneId() == 4){ //scene 4
				
				if(ChunkyScene.userIsPresent)displayCube(colors[0], positions[0]);
				if(ChunkyScene.isTrackingSkeleton)displayCube(colors[1], positions[1]);	
				displayUserImage();
			}
			
			App.getActualScene().displayMenu();
		
		}
		
	}
	private void displayCube(int c, PVector loc){
		fill(c);
		rect(loc.x, loc.y, 10, 10);
	}
	private void displayUserImage(){
		if(ChunkyScene.userImg != null)image(ChunkyScene.userImg, vPos.x, vPos.y);
	}
	private void displayMiniature(){
		if(ShapeScene.blobImg != null)image(ShapeScene.blobImg, vPos.x, vPos.y);
	}
	//--------------- keys ---------------------//
	public void keyPressed() {
		
		if (key=='l') {
			toggleValue();
		} else if (keyCode==UP) {
			setSelectedValue(+50);
		} else if (keyCode==DOWN) {
			setSelectedValue(-50);
		} else if(key=='m'){
			
			if(App.useLiveMusic){

				if(App.in.isMonitoring())App.in.disableMonitoring();
			    else App.in.enableMonitoring();
			
			}
			
		} else if(key=='n'){
			nextScene();
		} else if(key =='p'){
			prevScene();
		} else if (key=='c') {
			App.useColors = !App.useColors;
		} else if(key=='s'){
			savePicture();
		} else if(App.getSceneId() == 0){ //---------- scene 0 ---------------//
		
			if (key == 'b') {
				DrawLineScene.multipleBuffers = !DrawLineScene.multipleBuffers;
			} else if (key == 'd') {
				App.duplicateFFT = !App.duplicateFFT;
			} else if (key == 'f') {
				DrawLineScene.useFFT = !DrawLineScene.useFFT;
			} else if (key == 'h') {
				DrawLineScene.mode++;
				if(DrawLineScene.mode>2)DrawLineScene.mode=0;
			} else if(key == 'j'){ //unused
				App.strokeJoin++;
				if(App.strokeJoin>2)App.strokeJoin=0;
				System.out.println(App.strokeJoin+" "+PApplet.MITER+" "+PApplet.BEVEL+" "+PApplet.ROUND);
			} else if(key == 'k'){ //unused
				App.strokeCap++;
				if(App.strokeCap>2)App.strokeCap=0;
				System.out.println(App.strokeCap+" "+PApplet.ROUND+" "+PApplet.SQUARE+" "+PApplet.PROJECT);
			} else if (key == 'v') {
				DrawLineScene.linesVisibility = !DrawLineScene.linesVisibility;
			} else if (key == '0'){
			
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
									   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -100, -200, 6, 45, 0, 0, 50, 10, 112, 45, -90, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
			
			} else if (key == '1'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 4, 0, 0, 0, 50, 12, 120, 45, -100, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '2'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 14, 0, 0, 0, 150, 17, 172, 250, -150, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = true;
				DrawLineScene.useFFT = false;
				
			} else if (key == '3'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 10, 0, 0, 0, 350, 4, 60, 20, -50, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '4') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 20, 16, 70, 0, 90, 70, 60, 60, 45, -55, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '5') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 20, 16, 70, 0, 90, 115, 10, 60, 45, -55, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '6') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 50, 50, 260, 0, 90, 450, 10, -72, 20, 75, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '7') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 374, 100, 0, 0, 315, 220, 44, 48, 20, -50, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = true;
				DrawLineScene.useFFT = false;
				
			} else if (key == '8') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -100, -50, 4, 60, 180, 0, 100, 10, -200, 40, 100, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = false;
				DrawLineScene.useFFT = false;
				
			} else if (key == '9') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 50, 4, 0, 180, 0, 500, 10, -200, 250, 100, 10};
				editParams(0, parameters, values);
				
				DrawLineScene.multipleBuffers = true;
				DrawLineScene.useFFT = false;
				
			}
			
		} else if(App.getSceneId() == 1){
			
			if (key == 'b') {
				DrawPointScene.multipleBuffers = !DrawPointScene.multipleBuffers;
			} else if (key == 'd') {
				App.duplicateFFT = !App.duplicateFFT;
			} else if (key == 'f') {
				DrawPointScene.useFFT = !DrawPointScene.useFFT;
			} else if (key == 'v') {
				DrawPointScene.linesVisibility = !DrawPointScene.linesVisibility;
			} else if (key == '0'){
			
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
									   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -100, -200, 6, 45, 0, 0, 50, 10, 112, 45, -90, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '1'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 4, 0, 0, 0, 50, 12, 120, 45, -100, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '2'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 14, 0, 0, 0, 150, 17, 172, 250, -150, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '3'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 20, 10, 0, 0, 0, 350, 4, 60, 20, -50, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '4') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 20, 16, 70, 0, 90, 70, 60, 60, 45, -55, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '5') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 20, 16, 70, 0, 90, 115, 10, 60, 45, -55, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '6') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -50, 50, 50, 260, 0, 90, 450, 10, -72, 20, 75, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '7') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 374, 100, 0, 0, 315, 220, 44, 48, 20, -50, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '8') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, -100, -50, 4, 60, 180, 0, 100, 10, -200, 40, 100, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			} else if (key == '9') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "depthTS", "xSpace"};
				int[] values = {0, 0, 50, 8, 0, 180, 0, 500, 10, -200, 250, 100, 10};
				editParams(0, parameters, values);
				
				DrawPointScene.multipleBuffers = false;
				DrawPointScene.useFFT = false;
				
			}
			
		} else if(App.getSceneId() == 3){
			
			if (key == 'f'){		
				ShapeScene.useStroke = !ShapeScene.useStroke;
			} else if (key == 'x'){		
				ShapeScene.displayCross = !ShapeScene.displayCross;	
			}
			
		} else if(App.getSceneId() == 4){
			
			if (key == 'o'){		
				ChunkyScene.displayPoints = !ChunkyScene.displayPoints;
			} else if (key == 'k'){		
				ChunkyScene.isDrawingSkeleton = !ChunkyScene.isDrawingSkeleton;	
			} else if (key == 'x'){		
				ChunkyScene.displayCross = !ChunkyScene.displayCross;	
			} else if (key == '1'){		
				ChunkyScene.displayMode = 1;	
			} else if (key == '2'){		
				ChunkyScene.displayMode = 2;	
			}
		}
	}
	public void savePicture() {
		Date date = new Date();
		String name = "data/images/objv-"+date.getTime()+".png";
		save(name);	
	}
	private void nextScene(){	
		int id = App.getSceneId();
		id++;
		if(id>5)id=0;
		App.setSceneId(id);
	}
	private void prevScene(){
		int id = App.getSceneId();
		id--;
		if(id<0)id=4;
		App.setSceneId(id);
	}
	private void editParams(int key, String[] parameters, int[] values){
		
		switch (key) {
		case 0:
			
			for (int i=0; i<parameters.length; i++){
				App.getActualScene().params.put(parameters[i], values[i]);
			}
			
			App.getActualScene().menu.reinitSlidersValueAndPos();

			break;

		default:
			break;
		}
		
	}
	private void toggleValue() {
		  App.switchValue = !App.switchValue;
	}
	private void setSelectedValue(int value) {    

		if (App.switchValue) {
			App.lowestValue += value;
			App.lowestValue = constrain(App.lowestValue, 50, App.highestValue-10);
			println("low:"+ App.lowestValue + " | high:"+ App.highestValue);
		} else {
			App.highestValue += value;
			App.highestValue = constrain(App.highestValue, App.lowestValue+10, 15000);
			println("high:"+ App.highestValue + " | low:"+ App.lowestValue);
		}
	}
	//--------------- mouse ---------------------//
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
}
