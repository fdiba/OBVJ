package webodrome.scene;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.data.FloatList;

public class MonitorScene extends Scene {
	
	public MonitorScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		//lineNumber = 0;
		
		buffers = new ArrayList<FloatList>();
		
		addAndEraseBuffers();
		
	}
	public void display(){

		pApplet.noStroke();
		pApplet.fill(255, 0, 0);
		pApplet.rect(0, 0, w, h);

		pApplet.stroke(255);
		pApplet.strokeWeight(2);
		FloatList actualBufferValues = buffers.get(buffers.size()-1);
		
		for(int i = 0; i < actualBufferValues.size(); i++) {
		   
			pApplet.line( i, h/2 + actualBufferValues.get(i)*h/2, i+1, h/2 + actualBufferValues.get(i+1)*h/2 );
		 
		}
		
		
	}
}
