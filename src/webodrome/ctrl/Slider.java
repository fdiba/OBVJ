package webodrome.ctrl;

import processing.core.PApplet;
import processing.core.PVector;
import webodrome.App;
import webodrome.scene.Scene;

public class Slider {
	
	private final int WIDTH = 100;
	
	private PVector location;
	private String param;
	private float lowValue;
    private float maxValue;    
    private float lowXPos;
    private float maxYPos;
    private int color;
    private int row;
    private int id;
    private SliderController sliderCtrl;
    private boolean dragging;
    private Scene scene;

	public Slider(Scene _scene, PVector _loc, String _param, float _lowValue, float _maxValue, int _color) {

		scene = _scene;
		
		location = _loc;
		param = _param;
		
		sliderCtrl = new SliderController(new PVector(location.x + WIDTH/2, location.y));
		
		lowValue = _lowValue;
	    maxValue = _maxValue;
	    
	    lowXPos = location.x;
	    maxYPos = location.x + WIDTH;
	    
	    color = _color;
		
	}
	protected void initValue(float val){
	    
		float value = PApplet.map(val, lowValue, maxValue, location.x, location.x+WIDTH);
		sliderCtrl.setXLocation(value);
		    
		if(App.BCF2000){     
			int behValue = (int) PApplet.map(val, lowValue, maxValue, 0, 127);
			App.behringer.setSliderPosition(row, id, behValue);
		}
    
	}
	public void editValWithBeh(int value){
		float xPos = PApplet.map(value, 0, 127, lowXPos, maxYPos);
	    sliderCtrl.location.x = xPos;
	    editValue();
	}
	protected void editBehSliderPos(){
		int behValue = (int) PApplet.map(sliderCtrl.location.x, lowXPos, maxYPos, 0, 127);
	    App.behringer.setSliderPosition(row, id, behValue);
	}
	protected void setbehSlider(int _row, int _id){
	    row = _row;
	    id = _id;
	}
	protected void update(PVector mousePosition){
	    
		if(mousePosition.x > sliderCtrl.location.x - sliderCtrl.WIDTH/2 && mousePosition.x < sliderCtrl.location.x + sliderCtrl.WIDTH/2 &&
	       mousePosition.y > sliderCtrl.location.y - sliderCtrl.WIDTH/2 && mousePosition.y <sliderCtrl. location.y + sliderCtrl.WIDTH/2){
	      
			dragging = true;
	  
	    } 
	}
	protected void reset(){
		dragging = false;
	}
	protected void followMouse(PApplet p){
		
	    if(dragging) {	      
	    	
	    	sliderCtrl.location.x = p.mouseX;
	    	
	    	if(sliderCtrl.location.x <= lowXPos) {
	    		sliderCtrl.location.x = lowXPos;
	    	} else if (sliderCtrl.location.x >= maxYPos) {	        
	    		sliderCtrl.location.x = maxYPos;
	    	}
	      
	    	editValue();

	    }
	  
	}
	private void editValue(){
	    
	    float value = PApplet.map(sliderCtrl.location.x, lowXPos, maxYPos, lowValue, maxValue);
	    
	    scene.params.put(param, (int) value);
	    
	    PApplet.println(param + ": " + value);
	      
	}
	public void display(PApplet p) {
		p.rectMode(PApplet.CORNER);
		p.noStroke();
		p.fill(color);
		p.rect(location.x, location.y, WIDTH, 10);
	    sliderCtrl.display(p);
		
	}
	
}
