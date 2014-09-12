package webodrome.ctrl;

import javax.swing.JFrame;

import webodrome.App;

@SuppressWarnings("serial")
public class PFrame extends JFrame {
	
	public PFrame(int w, int h) {
		setBounds(100, 60, w, h);
		
		SecondApplet secondApplet = new SecondApplet();
		App.secondApplet = secondApplet;
		
		add(App.secondApplet);
		App.secondApplet.init();
		setVisible(true);
	}

}
