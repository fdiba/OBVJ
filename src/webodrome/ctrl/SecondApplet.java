package webodrome.ctrl;

import processing.core.PApplet;
import webodrome.App;

@SuppressWarnings("serial")
public class SecondApplet extends PApplet {
	
	//create main ?
	
	public SecondApplet() {
		// TODO Auto-generated constructor stub
	}
	
	public void setup(){
		
	}
	public void draw(){
		
		background(0x000000);

		if(App.getActualScene() != null){
			App.getActualScene().displayMenu2();
		}
		
	}
	//--------------- mouse ---------------------//
	public void mouseReleased(){
		App.getActualScene().menu.resetSliders();		
	}
}
