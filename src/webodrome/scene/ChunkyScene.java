package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;

public class ChunkyScene extends Scene {
	
	public static PImage userImg;
	public static boolean userIsPresent;
	public static boolean isTrackingSkeleton;
	public static boolean displayPoints;
	public static boolean displayCross = true;
	
	private ArrayList<PVector> centerOfMasses;
	private ArrayList<PVector> lastCenterOfMasses;
	
	private ArrayList<ArrayList<PVector>> skulls;
	private ArrayList<ArrayList<ArrayList<PVector>>> megaSkulls;
	
	public static boolean isDrawingSkeleton = true;
	public static int displayMode = 2;
	
	private int lastNumberOfSkullInLastSkulls;
	
	public ChunkyScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		if(App.useLiveMusic) buffers = new ArrayList<FloatList>();
		
		userImg = new PImage(imgWidth/2, imgHeight/2);
		
		lastNumberOfSkullInLastSkulls = 0;
		
		megaSkulls = new ArrayList<ArrayList<ArrayList<PVector>>>();
		
		lastCenterOfMasses = new ArrayList<PVector>();

	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		while(megaSkulls.size() > params.get("iterations")){
			megaSkulls.remove(0);
		}
		
		//add skulls
		detectUsers(context);
		
		centerOfMasses = updateCOM(centerOfMasses);
		lastCenterOfMasses = copyList(centerOfMasses);
		
		userImg.copy(context.userImage(), 0, 0, imgWidth, imgHeight, 0, 0, userImg.width, userImg.height);
		
		int amplitude = params.get("amplitude");
		
		//edit last skulls
		if(App.useLiveMusic && amplitude > 1){
			addAndEraseBuffers();
			if(megaSkulls.size()>0){
				ArrayList<ArrayList<PVector>> editedSkulls = editLastSkulls(megaSkulls.get(megaSkulls.size()-1), amplitude);
				megaSkulls.remove(megaSkulls.size()-1);
				megaSkulls.add(editedSkulls);
			}
		}

	}
	private ArrayList<PVector> copyList(ArrayList<PVector> newCenters){
		
		lastCenterOfMasses = new ArrayList<PVector>();
		
		for(PVector vector : newCenters){
			
			lastCenterOfMasses.add(vector);
			
		}
		
		return newCenters;
	}
	private ArrayList<PVector> updateCOM(ArrayList<PVector> newCenters){
				
		ArrayList<PVector> newCenterOfMasses = new ArrayList<PVector>();
		
		if(newCenters.size()>0){
			
			for(int i=0; i<newCenters.size(); i++){
				
				PVector v = newCenters.get(i);
				
				if(i < lastCenterOfMasses.size()){
					
					PVector lv = lastCenterOfMasses.get(i);
					
					if(lv.x != lv.x){ //NaN
						
						newCenterOfMasses.add(v);
						
					} else {
						
						//edit stuff
						PVector distance = PVector.sub(v, lv);
						distance.mult((float) 0.5);
						lv.add(distance);
						newCenterOfMasses.add(lv);
												
					}
					
					//PApplet.println(v.x, v.y, " ", lv.x, lv.y);
				
				} else {
					//not edited
					newCenterOfMasses.add(v);
					
				}
			
			}
			
			
		}
		
		return newCenterOfMasses;
		
	}
	private ArrayList<ArrayList<PVector>> editLastSkulls(ArrayList<ArrayList<PVector>> skulls, int amplitude){

		ArrayList<ArrayList<PVector>> editedSkulls = new ArrayList<ArrayList<PVector>>();
		
		for(int i=0; i<skulls.size(); i++) {

			ArrayList<PVector> skull = skulls.get(i);
			
			if(App.useLiveMusic){
				
				skull = editVerticesPosBasedOnSound(skull, amplitude);
				editedSkulls.add(skull);				
			}
		}
		return editedSkulls;	
	}
	private void detectUsers(SimpleOpenNI context){
		
		skulls = new ArrayList<ArrayList<PVector>>();
		
		centerOfMasses = new ArrayList<PVector>();
		int skeletonTracked = 0;
		
		int[] userList = context.getUsers();
		
		if(userList.length>0){
			
			userIsPresent = true;
			
			for(int i=0;i<userList.length;i++) {
				
				if(context.isTrackingSkeleton(userList[i])) {

					if(isDrawingSkeleton)drawSkeleton(context, userList[i]);
					skeletonTracked++;
			    }
				
				PVector com = new PVector();
				  
				if(displayCross){
					
					if(context.getCoM(userList[i],com)) {
						
						PVector com2d = new PVector();
						context.convertRealWorldToProjective(com,com2d);
						
						centerOfMasses.add(com2d);
					}
					
				}
			
			}
			
			megaSkulls.add(skulls);
			
		} else {
			userIsPresent = false;
			if(megaSkulls.size() > 0)megaSkulls.remove(0);
		}
		
		lastNumberOfSkullInLastSkulls = skulls.size();
		
		if(skeletonTracked>0){
			isTrackingSkeleton=true;
		} else {
			isTrackingSkeleton=false;
		}
		
	}
	private void drawSkeleton(SimpleOpenNI context, int userId) {

		if(megaSkulls.size()>0){
			
			//TODO do it better using userId instead of skull number in skulls ?
			if(skulls.size()<lastNumberOfSkullInLastSkulls){
				
				ArrayList<ArrayList<PVector>> lastSkulls = megaSkulls.get(megaSkulls.size()-1);
				int actualSkullId = skulls.size();
				
				if(actualSkullId <= lastSkulls.size()-1){
																
					ArrayList<PVector> targetedSkull = lastSkulls.get(actualSkullId);
					
					ArrayList<PVector> skull = new ArrayList<PVector>();
					
					PVector v = createNewJointBasedOnPrev(context, userId, 0, targetedSkull);
					skull.add(v);
					v = createNewJointBasedOnPrev(context, userId, 1, targetedSkull);
					skull.add(v);
					v = createNewJointBasedOnPrev(context, userId, 2, targetedSkull);
					skull.add(v);
					v = createNewJointBasedOnPrev(context, userId, 3, targetedSkull);
					skull.add(v);
					v = createNewJointBasedOnPrev(context, userId, 4, targetedSkull);
					skull.add(v);
					
				} else {
					createNewJoints(context, userId);
				}
				
			} else {				
				createNewJoints(context, userId);
			}
			
		} else {
			createNewJoints(context, userId);
		}
	}
	private PVector createNewJointBasedOnPrev(SimpleOpenNI context, int userId, int jointId, ArrayList<PVector> targetedSkull){
		
		PVector last_vector = targetedSkull.get(0);
		PVector new_vector = new PVector();
				
		switch (jointId) {
		case 0:
			new_vector = jointPos(context, userId, SimpleOpenNI.SKEL_HEAD);
			break;
		case 1:
			new_vector = jointPos(context, userId, SimpleOpenNI.SKEL_LEFT_HAND);
			break;
		case 2:
			new_vector = jointPos(context, userId, SimpleOpenNI.SKEL_LEFT_FOOT);
			break;
		case 3:
			new_vector = jointPos(context, userId, SimpleOpenNI.SKEL_RIGHT_FOOT);
			break;
		case 4:
			new_vector = jointPos(context, userId, SimpleOpenNI.SKEL_RIGHT_HAND);
			break;
		default:
			break;
		}
		
		PVector distance = PVector.sub(new_vector, last_vector);
		distance.mult((float) 0.5);
		new_vector.add(distance);
				
		return new_vector;
		
	}
	private void createNewJoints(SimpleOpenNI context, int userId){
		
		ArrayList<PVector> skull = new ArrayList<PVector>();
		
		skull.add(jointPos(context, userId, SimpleOpenNI.SKEL_HEAD));
		
		skull.add(jointPos(context, userId, SimpleOpenNI.SKEL_LEFT_HAND));
		skull.add(jointPos(context, userId, SimpleOpenNI.SKEL_LEFT_FOOT));
	
		skull.add(jointPos(context, userId, SimpleOpenNI.SKEL_RIGHT_FOOT));
		skull.add(jointPos(context, userId, SimpleOpenNI.SKEL_RIGHT_HAND));
	
		skulls.add(skull);
	}
	private PVector jointPos(SimpleOpenNI context, int userId, int jointID) {
		
		PVector joint = new PVector();
		
		float confidence = context.getJointPositionSkeleton(userId, jointID, joint);
		//if(confidence < 0.5) return;
		
		PVector convertedJoint = new PVector();
		context.convertRealWorldToProjective(joint, convertedJoint);
		
		convertedJoint.x *= xRatio;
		convertedJoint.y *= yRatio;
		
		return convertedJoint;

	}
	private void displaySkulls2(){
		
		float addValue = 255/params.get("iterations");
		int alpha = 0;
		int index = 0;

		for(ArrayList<ArrayList<PVector>> skulls : megaSkulls){
			
			alpha += addValue;
			index++;
			
			for(ArrayList<PVector> skull : skulls){				
			
				pApplet.pushMatrix();
				pApplet.translate(0, 0, index);
				pApplet.strokeWeight(2);
				pApplet.stroke(0xFFFFFFFF, alpha);
				pApplet.noFill();
				
				pApplet.beginShape();
				
				int maxId = skull.size()-1;
				
				PVector fv = new PVector();
				PVector lv = skull.get(maxId);
				
				for(int i=0; i<=maxId; i++){
					
					PVector v = skull.get(i);
					
					if(i==0){
						
						pApplet.curveVertex(lv.x, lv.y);
						pApplet.curveVertex(v.x, v.y);
						fv.set(v);
					
					} else if(i==maxId){
						pApplet.curveVertex(v.x, v.y);
						pApplet.curveVertex(fv.x, fv.y);
						
						PVector sv = skull.get(1);
						pApplet.curveVertex(sv.x,  sv.y);
						
					} else {
						pApplet.curveVertex(v.x, v.y);
					}
						
				}
				pApplet.endShape();
				pApplet.popMatrix();
			}
			
		}
		
	}
	private ArrayList<PVector> editVerticesPosBasedOnSound(ArrayList<PVector> skull, int amplitude){
		
		ArrayList<PVector> editedSkull = new ArrayList<PVector>();
		PVector centroid = calculateCentroid(skull);
		  
		for(int i=0; i<skull.size(); i++){
		    
			int id = i;
		    
		    PVector v = skull.get(id);
		  
		    PVector addon = PVector.sub(centroid, v);
		    addon.normalize();
		    
		    id = (int) PApplet.map(id, 0, skull.size()-1, 0, App.in.bufferSize()-1);
		    
		    float bufferValue = App.in.left.get(id); //-1 to 1
		    
		    addon.mult(amplitude*bufferValue);
		    
		    v.add(addon);
		    editedSkull.add(v);
		    
		  }
		
		return editedSkull;
		
	}
	private PVector calculateCentroid(ArrayList<PVector> skull){
		 
		PVector centroid = new PVector();
		
		for (PVector v : skull) {		
			centroid.add(v);  
		} 
  
		centroid.div(skull.size());
		return centroid;
	}
	private void displayPoint(PVector v, int index, int alpha){
		
		pApplet.pushMatrix();
		pApplet.translate(0, 0, index);
		pApplet.noStroke();
		pApplet.fill(0xFFFFFFFF, alpha);
		pApplet.ellipse(v.x, v.y, 10, 10);
		pApplet.popMatrix();
		
	}
	private void displayLine(PVector a, PVector b, int index, int alpha){
		pApplet.strokeWeight(2);
		pApplet.stroke(0xFFFFFFFF, alpha);
		pApplet.pushMatrix();
		pApplet.translate(0, 0, index);
		pApplet.line(a.x, a.y, b.x, b.y);
		pApplet.popMatrix();
	}
	private void displaySkulls(){
		
		float addValue = 255/params.get("iterations");
		int alpha = 0;
		int index = 0;
		
		for(ArrayList<ArrayList<PVector>> skulls : megaSkulls){
			
			alpha += addValue;
			index++;
			
			for(ArrayList<PVector> skull : skulls){
				
				PVector pv = new PVector();
				PVector fv = new PVector();
				
				for(int i=0; i<skull.size(); i++){
					
					PVector v = skull.get(i);
					
					if(i==0){
						fv.set(v);
						pv.set(v);
						
						//index++;
						if(displayPoints) displayPoint(v, index, alpha);
						
					} else if(i==skull.size()-1){
						
						displayLine(v, pv, index, alpha);
						displayLine(v, fv, index, alpha);
						
						//index++;
						if(displayPoints) displayPoint(v, index, alpha);
						
					} else if(i>0){
						
						displayLine(v, pv, index, alpha);
						pv.set(v);
						
						//index++;
						if(displayPoints) displayPoint(v, index, alpha);
					}
					
				}
				
			}
			
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
		
		switch (displayMode) {
		case 1:
			displaySkulls();
			break;
		case 2:
			displaySkulls2();
			break;
		default:
			break;
		}
		
	}
}
