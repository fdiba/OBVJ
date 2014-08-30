package webodrome.scene;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import webodrome.ctrl.Menu;


public class Scene {
	
	protected PApplet pApplet;
	protected int width, height;
	
	public Map<String, Integer> params;
	public Menu menu;
	
	private int w;
	private int h;
	
	public Scene(PApplet _pApplet, Object[][] objects, int _w, int _h){
		pApplet = _pApplet;
		params = new HashMap<String, Integer>();
		
		w = _w;
		h = _h;
		
		createMenu(objects);
	}
	public Scene(PApplet _pApplet){
		pApplet = _pApplet;
		menu = null;
	}
	protected void createMenu(Object[][] objects){	
		menu = new Menu(this, new PVector(w - 190, 50), objects);
	}
	public void update(){
		if(menu!=null)menu.update(pApplet);	
	}
	public void displayMenu(){
		menu.display(pApplet);
	}
}
