package webodrome.ctrl;

import processing.core.PApplet;
import processing.core.PVector;
import webodrome.App;
import webodrome.scene.Scene;

public class Menu {
	
	public Slider[] sliders;
	
	private PVector location;
	private Scene scene;
	
	private int showTime;
	private final int SHOWTIME_MAX = 23;
	
	public Menu(Scene _scene, PVector _loc, Object[][] objects){
		
		location = _loc;
		scene = _scene;
		sliders = new Slider[objects.length];
		
		int numSliders = BehringerBCF.numSliders;
		
		for(int i=0; i<objects.length; i++){

			String param = (String) objects[i][0];
			float lowValue = (int) objects[i][1];
			float maxValue = (int) objects[i][2];
			int color = (int) objects[i][3];
			
			sliders[i] = new Slider(scene, new PVector(location.x, location.y + 15*i), param, lowValue, maxValue, color);
						
			int row = i/numSliders;
			int sliderId = i%numSliders;
			
			int value = (int) objects[i][4];	
			
			if(App.BCF2000) sliders[i].setbehSlider(row, sliderId);
			sliders[i].initValue(value);
			
			scene.params.put(param, value);
			
		}
		
	}
	public void reveal(){
		showTime = SHOWTIME_MAX;    
	}
	public void display2(SecondApplet p){
		
		float mx, my, mwidth, mheight;
	    mx = location.x-10;
	    my = location.y-10;
	    mwidth = 120;
	    mheight = 15*sliders.length - 35;
		
		drawBorders(p, mx, my, mwidth, mheight);
		for (Slider s: sliders) s.display(p);
	}
	public void display(PApplet p){

	    float mx, my, mwidth, mheight;
	    mx = location.x-10;
	    my = location.y-10;
	    mwidth = 120;
	    mheight = location.y + 15*sliders.length - 35;
	    
	    if(p.mouseX > mx && p.mouseX < mx+mwidth && p.mouseY > my && p.mouseY < my + mheight || showTime>0) {
	    	drawBorders(p, mx, my, mwidth, mheight);
	    	for (Slider s: sliders) s.display(p);
	    }
	}
	public int getSlidersLength(){
		return sliders.length;
	}
	public void resetBSliders(){	
		for (Slider s: sliders) s.editBehSliderPos();
	}
	private void drawBorders(PApplet p, float mx, float my, float mwidth, float mheight){
		p.noFill();
		p.rectMode(PApplet.CORNER);
		p.strokeWeight(1);
		p.stroke(App.colors[4], 127);
		p.rect(mx, my, mwidth,  15*(sliders.length+1));  
	}
	public void resetSliders(){
		for (Slider s: sliders) s.reset();
	}
	public void update(PApplet p) {
		
		if(showTime>0)showTime--;

		if(p.mousePressed){
			PVector mousePosition = new PVector(p.mouseX, p.mouseY);
			for (Slider s: sliders) s.update(mousePosition);
		}
		for (Slider s: sliders) s.followMouse(p);
	}
}
