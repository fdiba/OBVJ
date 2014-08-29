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
	
	public Scene(PApplet _pApplet, Object[][] objects){
		pApplet = _pApplet;
		params = new HashMap<String, Integer>();
		createMenu(objects);
	}
	public Scene(PApplet _pApplet){
		pApplet = _pApplet;
		menu = null;
	}
	protected void createMenu(Object[][] objects){	
		menu = new Menu(this, new PVector(450, 50), objects);
	}
	public void update(){
		if(menu!=null)menu.update(pApplet);	
	}
	public void displayMenu(){
		menu.display(pApplet);
	}
}
