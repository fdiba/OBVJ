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
	
	private float yPos;
	
	public Menu(Scene _scene, PVector _loc, Object[][] objects){
		
		location = _loc;
		scene = _scene;
		sliders = new Slider[objects.length];
		
		int numSliders = BehringerBCF.numSliders;
		
		int color;
				
		for(int i=0; i<objects.length; i++){

			String param = (String) objects[i][0];
			float lowValue = (int) objects[i][1];
			float maxValue = (int) objects[i][2];
			
			if(objects[i].length > 4){
				color = (int) objects[i][4];
			} else {
				color = (int) App.colors[i];
			}
			
			int row = i/numSliders;
			int sliderId = i%numSliders;
			int gapBetweenSliders = 15*row;
			
			yPos = (float) (location.y + 15*i + gapBetweenSliders);
			
			sliders[i] = new Slider(scene, new PVector(location.x, yPos), param, lowValue, maxValue, color);
					
			if(App.BCF2000) sliders[i].setbehSlider(row, sliderId);
			
			int value = (int) objects[i][3];
			sliders[i].initValueAndPos(value);
			
			scene.params.put(param, value);
			
		}
		
	}
	public void reveal(){
		showTime = SHOWTIME_MAX;    
	}
	public void display(SecondApplet p){
		
		float mx, my, mwidth, mheight;
	    mx = location.x-10;
	    my = location.y-10;
	    mwidth = 300;
	    mheight =  yPos;
		
		drawBorders(p, mx, my, mwidth, mheight);
		for (Slider s: sliders) s.display(p);
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
		p.stroke(0xFFFFFFFF, 127);
		p.rect(mx, my, mwidth, mheight);  
	}
	public void reinitSlidersValueAndPos(){
		for (Slider s: sliders) s.reinitValueAndPos();
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
