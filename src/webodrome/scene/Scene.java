package webodrome.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.ctrl.Menu;

public class Scene {
	
	protected PApplet pApplet;
	protected int width, height;
	
	public Map<String, Integer> params;
	public Menu menu;
	
	protected int w, h;
	protected int[] depthValues;
	
	protected int imgWidth;
	protected int imgHeight;
	
	protected float xRatio;
	protected float yRatio;
	
	protected ArrayList<FloatList> buffers;
	
	public Scene(PApplet _pApplet, Object[][] objects, int _w, int _h){
		
		pApplet = _pApplet;
		params = new HashMap<String, Integer>();
		
		w = _w;
		h = _h;
		
		imgWidth = 640;
		imgHeight = 480;
			
		xRatio = (float) w/imgWidth;
		yRatio = (float) h/imgHeight;
		
		createMenu(objects);
	}
	//TODO UPDATE
	protected void updateBuffers(){
		
		int bSize = buffers.size();
		FloatList bufferValues = new FloatList();
		
		if(!App.useLiveMusic){
			
			for(int i = 0; i < App.player.bufferSize(); i++) {

				float value = App.player.left.get(i);
				value *= params.get("amplitude");
				bufferValues.append(value);
			
			}
		
		} else {
			
			for(int i = 0; i < App.in.bufferSize(); i++) {
				
				float value = App.in.left.get(i);
				value *= params.get("amplitude");
				bufferValues.append(value);
			
			}
		}
	   
		if(bSize > 0) buffers.remove(0);
		buffers.add(bufferValues);
		
	}
	//TODO UPDATE FOR EACH SCENE
	protected void addUpdateAndEraseBuffers(){
		
		int bSize = buffers.size();
		FloatList bufferValues = new FloatList();
		
		if(!App.useLiveMusic){
			
			for(int i = 0; i < App.player.bufferSize(); i++) {

				float value = App.player.left.get(i);
				//value *= params.get("amplitude");
				bufferValues.append(value);
			
			}
		
		} else {
			
			for(int i = 0; i < App.in.bufferSize(); i++) {
				
				float value = App.in.left.get(i);
				//value *= params.get("amplitude");
				bufferValues.append(value);
			
			}
		}
	   
		if(bSize > 0) buffers.remove(0);
		buffers.add(bufferValues);
		
	}
	protected ArrayList<ArrayList<PVector>> editLastArrayList(ArrayList<ArrayList<PVector>> arrayLists, int amplitude){

		ArrayList<ArrayList<PVector>> editedSkulls = new ArrayList<ArrayList<PVector>>();
		
		for(int i=0; i<arrayLists.size(); i++) {

			ArrayList<PVector> arrayList = arrayLists.get(i);
			
			if(App.useLiveMusic){
				
				arrayList = editVerticesPosBasedOnSound(arrayList, amplitude);
				editedSkulls.add(arrayList);				
			}
		}
		return editedSkulls;	
	}
	protected ArrayList<PVector> editVerticesPosBasedOnSound(ArrayList<PVector> arrayList, int amplitude){
		
		ArrayList<PVector> editedSkull = new ArrayList<PVector>();
		PVector centroid = calculateCentroid(arrayList);
		  
		for(int i=0; i<arrayList.size(); i++){
		    
			int id = i;
		    
		    PVector v = arrayList.get(id);
		  
		    PVector addon = PVector.sub(centroid, v);
		    addon.normalize();
		    
		    id = (int) PApplet.map(id, 0, arrayList.size()-1, 0, App.in.bufferSize()-1);
		    
		    float bufferValue = App.in.left.get(id); //-1 to 1
		    
		    addon.mult(amplitude*bufferValue);
		    
		    v.add(addon);
		    editedSkull.add(v);
		    
		  }
		
		return editedSkull;
		
	}
	protected PVector calculateCentroid(ArrayList<PVector> arrayList){
		PVector centroid = new PVector();
		for (PVector v : arrayList) centroid.add(v);
		centroid.div(arrayList.size());
		return centroid;
	}
	public Scene(PApplet _pApplet){
		pApplet = _pApplet;
		menu = null;
	}
	protected void createMenu(Object[][] objects){	
		menu = new Menu(this, new PVector(40, 30), objects);
	}
	public void update(){
		if(menu!=null)menu.update(App.secondApplet);
	}
	public void update(SimpleOpenNI context){
		if(menu!=null)menu.update(App.secondApplet);
		
		depthValues = context.depthMap();
	}
	public void displayMenu(){
		menu.display(App.secondApplet);
	}
}
