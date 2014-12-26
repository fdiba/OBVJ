package webodrome.ctrl;

import themidibus.MidiBus;
import webodrome.App;

public class BehringerBCF {
	
	private int slidersList;
	private MidiBus midiBus;
	
	//last slider values
	private int chan185num98Value; //number 0 to 7
	private int chan185num6Value; //value 0 to 127
	
	public final static int numSliders = 8;
	
	public BehringerBCF(MidiBus _midiBus){
		
		midiBus = _midiBus;
	    slidersList = 0; //chan 154 note 0 or 1
		
	}
	public void setSliderPosition(int bGrp, int bId, int behValue){
		if(slidersList == bGrp){
			midiBus.sendMessage(185, 99, 0);
			midiBus.sendMessage(185, 98, bId);
			midiBus.sendMessage(185, 6, behValue); //target	      
			midiBus.sendMessage(185, 38, 0); //other one
		}
    
	}
	public void midiMessage(int channel, int number, int value){
			    
		if(channel == 154 && number != slidersList){
	    
			slidersList = number;
			App.actualMenu.resetBSliders();
	     
	    } else if(channel == 185){
	        
	    	if(number==98) chan185num98Value = value;
	    	if(number==6) chan185num6Value = value;
	      
	    	int id=999;	      
	      
	    	if(slidersList == 0) { //first sliders grp       
	    		id = chan185num98Value;
	    	} else if (slidersList == 1){ //second sliders grp
	    		id = chan185num98Value + numSliders;	    		
	    	}
	    		    	
	    	if(id >= App.actualMenu.getSlidersLength()) return;
	    	
	    	if(number==38) {
	    		App.actualMenu.sliders[id].editValWithBeh(chan185num6Value);
	    		App.actualMenu.reveal();
	    	}
	       
	    }
	}
}
