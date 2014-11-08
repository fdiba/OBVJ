package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class ChunkyScene extends Scene {
	
	public static PImage userImg;
	public static boolean userIsPresent;
	public static boolean isTrackingSkeleton;
	
	private ArrayList<PVector> centerOfMasses;
	
	public ChunkyScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		userImg = new PImage(imgWidth/2, imgHeight/2);
		
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		detectUsers(context);
		
		userImg.copy(context.userImage(), 0, 0, imgWidth, imgHeight, 0, 0, userImg.width, userImg.height);
		
	}
	private void detectUsers(SimpleOpenNI context){
		
		 centerOfMasses = new ArrayList<PVector>();
		 int skeletonTracked = 0;
		
		int[] userList = context.getUsers();
		
		if(userList.length>0){
			
			userIsPresent = true;
			
			for(int i=0;i<userList.length;i++) {
				
				if(context.isTrackingSkeleton(userList[i])) {

					//drawSkeleton(userList[i]);
					skeletonTracked++;
			    }
				
				PVector com = new PVector();
				  
				if(context.getCoM(userList[i],com)) {
					
					PVector com2d = new PVector();
					context.convertRealWorldToProjective(com,com2d);
					
					centerOfMasses.add(com2d);
				}
			
			
				//PApplet.println(userList.length);
			
			}
			
		} else {
			userIsPresent = false;
		}
		
		if(skeletonTracked>0){
			isTrackingSkeleton=true;
		} else {
			isTrackingSkeleton=false;
		}
		
	}
	private void displayCenterOfMasses(){
		
		for(PVector com : centerOfMasses){
			
			int c = 0xFFFFFFFF;
			
			if(isTrackingSkeleton){
				drawLines(com);
				pApplet.noStroke();
				pApplet.fill(c);
				pApplet.rect(com.x*xRatio-5, com.y*yRatio-5, 10, 10);
			} else {
				drawLines(com);
				pApplet.fill(0xFF000000);
				pApplet.stroke(c);
				pApplet.rect(com.x*xRatio-20, com.y*yRatio-20, 40, 40);
			}

		}
		
	}
	private void drawLines(PVector com){
		pApplet.pushMatrix();
			pApplet.translate(0, 0, -1);
			pApplet.stroke(255);
			pApplet.strokeWeight(1);
			pApplet.line(0, com.y*yRatio, w, com.y*yRatio);
			pApplet.line(com.x*xRatio, 0, com.x*xRatio, h);
		pApplet.popMatrix();
	}
	public void display(){
		
		displayCenterOfMasses();
		
	}
}
