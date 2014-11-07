package webodrome.scene;

import java.util.HashMap;
import java.util.Map;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
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
	public Scene(PApplet _pApplet){
		pApplet = _pApplet;
		menu = null;
	}
	protected void createMenu(Object[][] objects){	
		menu = new Menu(this, new PVector(40, 30), objects);
	}
	public void update(){
		//if(menu!=null)menu.update(pApplet);
		if(menu!=null)menu.update(App.secondApplet);
	}
	public void update(SimpleOpenNI context){
		//if(menu!=null)menu.update(pApplet);
		if(menu!=null)menu.update(App.secondApplet);
		
		depthValues = context.depthMap();
	}
	public void displayMenu(){
		menu.display(pApplet);
	}
	public void displayMenu2(){
		menu.display2(App.secondApplet);
	}
}
