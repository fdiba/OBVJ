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
	
	public static int[] potValues;
	
	public BehringerBCF(MidiBus _midiBus){
		
		midiBus = _midiBus;
	    slidersList = 0; //chan 154 note 0 or 1
	    
	    potValues = new int[8];
	    for(int i=0; i<potValues.length; i++) {
	    	potValues[i]=0;
	    }
		
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
	     
	    } else if(channel==185){ //faders
	        
	    	if(number==98) chan185num98Value = value;
	    	if(number==6) chan185num6Value = value;
	      
	    	int id=999;
	    	id = chan185num98Value + numSliders*slidersList;
	    		    	
	    	if(id >= App.actualMenu.getSlidersLength()) return;
	    	
	    	if(number==38) {
	    		App.actualMenu.sliders[id].editValWithBeh(chan185num6Value);
	    		App.actualMenu.reveal();
	    	}
	       
	    } else if (channel==191) { //pot
	    	
	    	potValues[number] = value;
	    	
	    } else if (channel==153) { //pot clic
	    	
	    	potValues[number] = 0;
	    	resetPot(number);
	    	
	    }
	}
	private void resetPot(int number){

		midiBus.sendMessage(191, number, 0);
		
		//reset to 0 when is already still
		String actualParam;
		
		switch (number) {
		case 0:
			actualParam = "xTrans";
			break;
		case 1:
			actualParam = "yTrans";
			break;
		case 2:
			actualParam = "zTrans";
			break;
		case 3:
			actualParam = "rotateX";
			break;
		case 4:
			actualParam = "rotateY";
			break;
		case 5:
			actualParam = "rotateZ";
			break;
		default:
			actualParam ="";
			break;
		}
		
		if(App.transValues[number] == App.getActualScene().params.get(actualParam)){
			App.getActualScene().params.put(actualParam, 0);
		}
		
	}
}
