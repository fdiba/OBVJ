package webodrome.scene;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;

public class ChunkyScene extends Scene {
	
	public ChunkyScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
	}
	public void display(SimpleOpenNI context){
		
		pApplet.image(context.userImage(),0,0);
		
		pApplet.rect(0, 0, w, h);
		int nc = pApplet.color(pApplet.random(255), pApplet.random(255), pApplet.random(255));
		pApplet.fill(nc);
		
	}
}
