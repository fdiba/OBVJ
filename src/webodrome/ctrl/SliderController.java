package webodrome.ctrl;

import processing.core.PApplet;
import processing.core.PVector;

public class SliderController {
	
	protected final int WIDTH = 10;
	protected PVector location;
	
	public SliderController(PVector _loc) {
		location = _loc.get();
	    location.y += WIDTH/2;
	}
	protected void setXLocation(float value){
		location.x = value;
	}
	public void display(PApplet p) {
		p.rectMode(PApplet.CENTER);
		p.noStroke();
		p.fill(255);
		p.rect(location.x, location.y, WIDTH, WIDTH);
	}

}
