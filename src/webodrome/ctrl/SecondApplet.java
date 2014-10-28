package webodrome.ctrl;

import processing.core.PApplet;
import webodrome.App;
import webodrome.scene.DrawPointsAndLinesScene;

@SuppressWarnings("serial")
public class SecondApplet extends PApplet {
	
	//create main ?
	
	public SecondApplet() {
		// TODO Auto-generated constructor stub
	}
	
	public void setup(){
		
	}
	public void draw(){
		
		background(0x000000);

		if(App.getActualScene() != null){
			App.getActualScene().displayMenu2();
		}
		
	}
	//--------------- keys ---------------------//
	public void keyPressed() {
		
		if (key == 'l') {
			toggleValue();
		} else if (keyCode == UP) {
			setSelectedValue(+50);
		} else if (keyCode == DOWN) {
			setSelectedValue(-50);
		} else if(key == 'n'){
			nextScene();
		} else if (key == 'v') {
			DrawPointsAndLinesScene.linesVisibility = !DrawPointsAndLinesScene.linesVisibility;
		} else if (key == 'b') {
			DrawPointsAndLinesScene.multipleBuffers = !DrawPointsAndLinesScene.multipleBuffers;
		} else if (key == 'c') {
			DrawPointsAndLinesScene.useColors = !DrawPointsAndLinesScene.useColors;
		} else if (key == '0'){
		
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
								   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, -100, -200, 45, 0, 0, 25, 10, 112, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = false;
			DrawPointsAndLinesScene.multipleBuffers = false;
		
		} else if (key == '1'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, 0, 20, 0, 0, 0, 25, 10, 120, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = false;
			
		} else if (key == '2'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, 0, 20, 0, 0, 0, 60, 10, 172, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = true;
		} else if (key == '3'){
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
					   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, 0, 20, 0, 0, 0, 140, 10, 60, 1, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = false;
			
		} else if (key == '4') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, -50, 20, -290, 0, 90, 70, 60, 60, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = false;
			
		} else if (key == '5') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, -50, 20, -290, 0, 90, 70, 10, 60, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = false;
			
		} else if (key == '6') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, -50, 50, -100, 0, 90, 88, 10, -72, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = true;
			
		} else if (key == '7') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, 0, 374, 0, 0, 315, 20, 10, 48, 250, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = true;
			
		} else if (key == '8') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, -200, -50, -300, -180, 0, 60, 10, -200, 45, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = false;
			
		} else if (key == '9') {
			
			String[] parameters = {"xTrans", "yTrans", "zTrans", "rotateX", "rotateY", "rotateZ",
		   			   "amplitude", "ySpace", "depth", "maxDist", "alpha"};
			int[] values = {0, 0, 50, -360, -180, -360, 200, 10, -200, 120, 255};
			editParams(0, parameters, values);
			
			DrawPointsAndLinesScene.useColors = true;
			DrawPointsAndLinesScene.multipleBuffers = true;
			
		}
			
	}
	private void nextScene(){
		
		int id = App.getSceneId();
		id++;
		if(id>2)id=0;
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
			App.lowestValue = constrain(App.lowestValue, 0, App.highestValue-100);
			println(App.lowestValue);
		} else {
			App.highestValue += value;
			App.highestValue = constrain(App.highestValue, App.lowestValue+100, 7000);
			println(App.highestValue);
		}
	}
	//--------------- mouse ---------------------//
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
}
