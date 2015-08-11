package webodrome.ctrl;

import java.awt.Color;

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
    private float value;
    private StringBuilder info;

	public Slider(Scene scene, PVector location, String param, float lowVal, float maxVal, Color color) {
		
		info = new StringBuilder();
		
		this.scene = scene;
		
		this.location = location;
		this.param = param;
		
		sliderCtrl = new SliderController(new PVector(location.x + WIDTH/2, location.y));
		
		this.lowValue = lowVal;
	    this.maxValue = maxVal;
	    
	    lowXPos = location.x;
	    maxYPos = location.x + WIDTH;
	    
	    this.color = color.getRGB();
		
	}
	public void initValueAndPos(float _value){
		value = _value;
		initPos();
		editInfo();
	}
	public void reinitValueAndPos(){
		value = App.getActualScene().params.get(param);
		
		//TODO REMOVE ANIMATION WHEN ACTIVE + BUG KEYPAD 8 et 9 WHEN COMMENTED
		//updateTransValues(param, (int) value);
		
		initPos();
		editInfo();
	}
	private void initPos(){
		
		float xPos = PApplet.map(value, lowValue, maxValue, location.x, location.x+WIDTH);
		sliderCtrl.setXLocation(xPos);
		    
		if(App.BCF2000){     
			int behValue = (int) PApplet.map(value, lowValue, maxValue, 0, 127);
			App.behringer.setSliderPosition(row, id, behValue);
		}
		
	}
	public void editValWithBeh(int _value){
		float xPos = PApplet.map(_value, 0, 127, lowXPos, maxYPos);
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
	private void editInfo(){
		info.replace(0, info.length(), param+" | "+(int)lowValue+ " | "+(int)maxValue+ " | "+(int)value);
	}
	protected void reset(){
		dragging = false;
	}
	protected void followMouse(PApplet p){
		
	    if(dragging) {	//TODO add mouse Y      
	    	
	    	sliderCtrl.location.x = p.mouseX;
	    	
	    	if(sliderCtrl.location.x <= lowXPos) sliderCtrl.location.x = lowXPos;
	    	else if (sliderCtrl.location.x >= maxYPos) sliderCtrl.location.x = maxYPos;
	      
	    	editValue();
	    	editInfo();
	    	
	    }
	  
	}
	private void editValue(){
	    
		value = PApplet.map(sliderCtrl.location.x, lowXPos, maxYPos, lowValue, maxValue);
	    
	    if(App.getActualScene().params.get(param) != (int) value){
	    	
	    	if(!App.psRunning){
	    		
	    		if(param.equals("xSpace") || param.equals("ySpace") ||
	    		   param.equals("borderYSize") || param.equals("borderXSize")
	    		   || param.equals("strokeWeight")){
	    		
	    			App.recreateShapeGrid = true;
	    		}
	    		
	    	}
	    	
	    	scene.params.put(param, (int) value);
	    	
	    	updateAnimatedValues(param, (int) value);
	    	
	    	PApplet.println(param + ": " + value); 
	    }
	    	    
	}
	private void updateAnimatedValues(String _param, int _val){
		
		switch (_param) {
		case "xTrans":
			App.transValues[0] = _val;
			break;
		case "yTrans":
			App.transValues[1] = _val;
			break;
		case "zTrans":
			App.transValues[2] = _val;
			break;
		case "rotateX":
			App.transValues[3] = _val;
			break;
		case "rotateY":
			App.transValues[4] = _val;
			break;
		case "rotateZ":
			App.transValues[5] = _val;
			break;
		default:
			break;
		}
		
	}
	public void display(PApplet p) {
		p.rectMode(PApplet.CORNER);
		p.noStroke();
		p.fill(color);
		p.rect(location.x, location.y, WIDTH, 10);
	    sliderCtrl.display(p);
	    p.text(info.toString(), location.x + WIDTH + 15, location.y + 10);
		
	}
}
