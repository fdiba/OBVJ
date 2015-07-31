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
		noLoop();
	}
	private void drawVariablesStatus(){
		String str = DrawLineScene.variablesStatus();		
		text(str, 360, 100);
	}
	public void draw(){
		
		background(0xFF333333);
		//background(255,0,0);
		
		if(App.getActualScene() != null){
			
			if(App.getSceneId() == 0) { 
				
				if(App.lineSoundImage != null && DrawLineScene.multipleBuffers == false){
					//TODO resize it
					image(App.lineSoundImage, 360, 20);
				} else if(App.basicSoundImage != null && DrawLineScene.multipleBuffers){
					//TODO DISPLAY DO NOT WORK
					image(App.basicSoundImage, 360, 20);
				}
				drawVariablesStatus();
				
			} else if(App.getSceneId() == 3) { //scene 3
				
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
				
		App.keyPressed(key);
		
		if (keyCode==UP) {
			App.setSelectedValue(+50);
		} else if (keyCode==DOWN) {
			App.setSelectedValue(-50);
		} else if(key=='s'){  //------- save things -------//
			saveMenuPicture();
		}
	}
	public void saveMenuPicture() {
		
		Date date = new Date();
		String name = "data/images/objv-"+date.getTime()+".png";
		save(name);
		
		if(App.getSceneId() == 0){
			
			String imgName;
			
			if(DrawLineScene.multipleBuffers){
				imgName = "data/images/basicSoundImage-"+date.getTime()+".png";
				App.basicSoundImage.save(imgName);
			} else {
				imgName = "data/images/lineSoundImage-"+date.getTime()+".png";
				App.lineSoundImage.save(imgName);				
			}
		}
		
	}
	//--------------- mouse ---------------------//
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
}
