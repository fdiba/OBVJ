package webodrome.scene;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import webodrome.App;

public class RadarScene extends Scene {
	
	private int xSpace;
	private int ySpace;
	private int area;
	private int speed;
	
	float actualDepthValue;
	
	public RadarScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		actualDepthValue = App.lowestValue;
				
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		ySpace = params.get("ySpace");
		xSpace = params.get("xSpace");
		area = params.get("area");
		speed = params.get("speed");
	}
	public void display(){

		pApplet.noStroke();
		pApplet.fill(0xFFFFFFFF);
		float weight = 2;
		
		for (int i=0; i<imgHeight; i+= ySpace){
			
			for(int j=0; j<imgWidth; j+=xSpace){
				
				float depthValue = depthValues[j+i*imgWidth];
				
				if(depthValue>App.lowestValue && depthValue>=actualDepthValue && depthValue<actualDepthValue+area){
				
					PVector v = App.pvectors[j+i*imgWidth];
			    	pApplet.ellipse(v.x, v.y, weight, weight);		
			    	
				}
			
			}
				
		}
		
		actualDepthValue +=speed;
		if (actualDepthValue>App.highestValue+area*2)actualDepthValue=App.lowestValue-area;

	}
}
