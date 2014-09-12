package webodrome.scene;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import webodrome.App;
import webodrome.ctrl.Menu;

public class Scene {
	
	protected PApplet pApplet;
	protected int width, height;
	
	public Map<String, Integer> params;
	public Menu menu;
	
	@SuppressWarnings("unused")
	private int w;
	@SuppressWarnings("unused")
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
		menu = new Menu(this, new PVector(40, 30), objects);
	}
	public void update(){
		//if(menu!=null)menu.update(pApplet);
		if(menu!=null)menu.update(App.secondApplet);	
	}
	public void displayMenu(){
		menu.display(pApplet);
	}
	public void displayMenu2(){
		menu.display2(App.secondApplet);
	}
}
