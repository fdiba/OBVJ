package webodrome.ctrl;

import java.util.Date;

import processing.core.PApplet;
import webodrome.App;
import webodrome.scene.ChunkyScene;
import webodrome.scene.DrawPointsAndLinesScene;
import webodrome.scene.ShapeScene;

@SuppressWarnings("serial")
public class SecondApplet extends PApplet {
	
	public SecondApplet() {
	}
	
	public void setup(){
		
		frameRate(12);
		
	}
	public void draw(){
		
		background(0x000000);

		if(App.getActualScene() != null){
			
			App.getActualScene().displayMenu2();
			
			if(App.getSceneId() == 2) {
				displayMiniature();
				
				if(ShapeScene.userIsPresent){
					
					displayCube();
					
				}
				if(ShapeScene.isTrackingSkeleton){
					displayCube2();
				}
				
				
			} else if (App.getSceneId() == 3){
				
				if(ChunkyScene.userIsPresent){
					
					displayCube();
					
				}
				if(ChunkyScene.isTrackingSkeleton){
					displayCube2();
				}
				
				displayUserImage();
			}
		
		}
		
	}
	private void displayCube(){
		
		fill(255, 0, 255);
		rect(180, 10, 10, 10);
		
	}
	private void displayCube2(){
		
		fill(0, 255, 0);
		rect(180, 20, 10, 10);
		
	}
	private void displayUserImage(){
		image(ChunkyScene.userImg, 200, 0);
	}
	private void displayMiniature(){
		
		image(ShapeScene.blobImg, 200, 0);
		
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
		} else if(App.getSceneId() == 0 || App.getSceneId() == 1){
		
			
			if (key == 'v') {
				DrawPointsAndLinesScene.linesVisibility = !DrawPointsAndLinesScene.linesVisibility;
			} else if (key == 'b') {
				DrawPointsAndLinesScene.multipleBuffers = !DrawPointsAndLinesScene.multipleBuffers;
			}  else if (key == '0'){
			
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
									   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, -100, -200, 6, 45, 0, 0, 50, 10, 112, 45, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
			
			} else if (key == '1'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, 0, 20, 4, 0, 0, 0, 50, 12, 120, 45, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '2'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, 0, 20, 14, 0, 0, 0, 150, 17, 172, 250, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = true;
			} else if (key == '3'){
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
						   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, 0, 20, 10, 0, 0, 0, 350, 4, 60, 1, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '4') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, -50, 20, 16, -290, 0, 90, 70, 60, 60, 45, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '5') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, -50, 20, 16, -290, 0, 90, 115, 10, 60, 45, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '6') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, -50, 50, 50, -100, 0, 90, 450, 10, -72, 1, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '7') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, 0, 374, 100, 0, 0, 315, 220, 44, 48, 10, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = true;
				
			} else if (key == '8') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, -100, -50, 4, -300, -180, 0, 100, 10, -200, 40, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = false;
				
			} else if (key == '9') {
				
				String[] parameters = {"xTrans", "yTrans", "zTrans", "strokeWeight", "rotateX", "rotateY", "rotateZ",
			   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
				int[] values = {0, 0, 50, 8, -360, -180, -360, 500, 10, -200, 250, 255};
				editParams(0, parameters, values);
				
				App.useColors = false;
				DrawPointsAndLinesScene.multipleBuffers = true;
				
			}
			
		} else if(App.getSceneId() == 2){
			
			if (key == 'f'){		
				ShapeScene.useStroke = !ShapeScene.useStroke;
			} else if (key == 'x'){		
				ShapeScene.displayCross = !ShapeScene.displayCross;	
			}
			
		} else if(App.getSceneId() == 3){
			
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
		if(id>4)id=0;
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
				App.actualMenu.sliders[i].initValue(values[i]);
			}

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
			App.lowestValue = constrain(App.lowestValue, 0, App.highestValue-10);
			println(App.lowestValue);
		} else {
			App.highestValue += value;
			App.highestValue = constrain(App.highestValue, App.lowestValue+10, 15000);
			println(App.highestValue);
		}
	}
	//--------------- mouse ---------------------//
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
}
