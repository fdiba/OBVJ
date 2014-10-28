package webodrome.scene;

import java.util.ArrayList;

import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.FloatList;
import webodrome.App;
import webodrome.Ramp;

public class DrawPointsAndLinesScene extends Scene {
	
	private PVector[] pvectors;	
	private ArrayList<FloatList> buffers;
	
	private int lineNumber;
	private int[] depthValues;
	
	public static boolean linesVisibility = true;
	public static boolean multipleBuffers = false;
	public static boolean useColors;
	
	private int w;
	private int h;
	
	private float xRatio;
	private float yRatio; 
	
	private Ramp ramp;
	
	//-----------//
	private PVector oldVector, actualVector;
	private float oldBufferValue, actualBufferValue;
	private float oldDepthValue, actualDepthValue;
	
	//-----------//
	public static PImage blobImg;
	private BlobDetection blobDetection;
	private ArrayList<ArrayList<PVector>> contours;
	private PImage img;
	
	public DrawPointsAndLinesScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);
		
		w = 640;
		h = 480;
			
		xRatio = (float) _width/w;
		yRatio = (float) _height/h;
		
		PApplet.println(xRatio+" "+yRatio);
		
		setVectors();
		
		ramp = new Ramp();
		
		buffers = new ArrayList<FloatList>();
		//lineNumber = 0;
		
		setBuffers(params.get("ySpace"));		
		
		img = pApplet.createImage(App.width, App.height, PApplet.RGB);
		
		blobImg = new PImage(App.width/2, App.height/2);
		blobDetection = new BlobDetection(blobImg.width, blobImg.height);
		blobDetection.setPosDiscrimination(true); //find bright areas
		blobDetection.setThreshold(0.2f); //between 0.0f and 1.0f
		  		  
		PApplet.println("----------------------------------" + "\n" +
		                "depth limits: press l + UP OR DOWN" + "\n" +
		                "dark lines visibility: press v" + "\n" +
		                "use multiple buffers: press b" + "\n" +
		          		"use colors: press c");
				
	}
	private void setVectors(){
		pvectors = new PVector[w*h]; 
		for (int i=0; i<h; i++){			
			for(int j=0; j<w; j++){
				pvectors[j+i*w] = new PVector(j, i, 0);
		    }
		} 
	}
	public void update(SimpleOpenNI context){
		
		super.update();
		
		lineNumber = 0;
		
		depthValues = context.depthMap();
		
		addAndEraseBuffers();
		
	}
	public void update2(SimpleOpenNI context){
		
		super.update();
		
		lineNumber = 0;
		
		depthValues = context.depthMap();
		
		addAndEraseBuffers();
		
		int mapWidth = context.depthWidth();
		int mapHeight = context.depthHeight();
		
		img.loadPixels();
		
		//TODO PARAMS
		int threshold = 255 - params.get("alpha");
		if(threshold>255)threshold=255;
		
		drawDepthImg(context, depthValues, mapWidth, mapHeight, threshold);
		img.updatePixels();
		
		blobImg.copy(img, 0, 0, mapWidth, mapHeight, 0, 0, blobImg.width, blobImg.height);
		
		//TODO PARAMS
		//fastblur(blobImg, params.get("blurRadius"));
		fastblur(blobImg, 2);
		
		createBlackBorders();
		
		blobDetection.computeBlobs(blobImg.pixels);
		
		createContours();
		
	}
	private void createBlackBorders(){
		  
	
		blobImg.loadPixels();
	  
		//TODO PARAMS 
		//int offset = params.get("borderOffset");
		int offset = 1;
		
		int color = (0 << 16) | (0 << 8) | 0;
	  
		//top border
		for(int j=0; j<offset; j++){
			for(int i=0; i<blobImg.width; i++){
				blobImg.pixels[i+j*blobImg.width] = color;    
			}
		}
	
		//right border
		for(int i=0; i<offset; i++){
			for(int j=0; j<blobImg.height; j++){
				blobImg.pixels[i+j*blobImg.width] = color;    
			}
		}
	  
		//bottom border
		for(int j=blobImg.height-offset; j<blobImg.height; j++){
			for(int i=0; i<blobImg.width; i++){
				blobImg.pixels[i+j*blobImg.width] = color;    
			}
		}
	
		//left border
		for(int i=blobImg.width-offset; i<blobImg.width; i++){
			for(int j=0; j<blobImg.height; j++){
				blobImg.pixels[i+j*blobImg.width] = color;    
			}
		}
	  
		blobImg.updatePixels();
	  
	}
	private void createContours() {
		
		Blob blob;
		EdgeVertex eA, eB;
	  
		contours = new ArrayList<ArrayList<PVector>>();
	  
	  
		for(int n=0; n<blobDetection.getBlobNb(); n++){
		  
			blob = blobDetection.getBlob(n);
	    
			//TODO PARAMS
			//if(blob != null && blob.getEdgeNb() > params.get("edgeMinNumber")){
			if(blob != null && blob.getEdgeNb() > 100){
	      
				ArrayList<PVector> contour = new ArrayList<PVector>();

				for(int i=0; i<blob.getEdgeNb(); i++){
				  
					eA = blob.getEdgeVertexA(i);
					eB = blob.getEdgeVertexB(i);
			        
					if(i==0){
			    	  
						contour.add(new PVector(eA.x*w, eA.y*h));
			    	  
					} else {
			          
						PVector v = contour.get(contour.size()-1);
						float distance = PApplet.dist(eB.x*w, eB.y*h, v.x, v.y);
						//TODO PARAMS
						if(distance > 10)contour.add(new PVector(eB.x*w, eB.y*h));
						//if(distance> params.get("distMin"))contour.add(new PVector(eB.x*width, eB.y*height));
			    
					}
			    
				}
	      
				if(contour.size()>2) contours.add(contour);
	    
			}
	    
		}
		
	}
	private void drawDepthImg(SimpleOpenNI context, int[] _depthValues, int mapWidth, int mapHeight, int threshold){
		
		int cValue;
		int lValue = App.lowestValue;
		int hValue = App.highestValue;
		
		for (int x = 0; x < mapWidth; x++) {
			//UPDATE
			int pixId = x - mapWidth;
			
			for (int y = 0; y < mapHeight; y++) {
				
				//int pixId = x + y * mapWidth;
				pixId += mapWidth;
				
				int currentDepthValue = depthValues[pixId];
 
				if(currentDepthValue >= lValue && currentDepthValue <= hValue){

					cValue = (int) PApplet.map(currentDepthValue, lValue, hValue, 255, threshold);
					img.pixels[pixId] = (255 << 24) | (cValue << 16) | (cValue << 8) | cValue;
				      
				} else {

					cValue = 0;
					img.pixels[pixId] = (0 << 24) | (cValue << 16) | (cValue << 8) | cValue;
	    
				}
				
		    }
		}
		
	}
	public void display(){
		
		int ySpace = params.get("ySpace");
		
		
		for (int i=10; i<h; i+= ySpace){
		    
			oldVector = null;
			oldDepthValue = 0;
			oldBufferValue = 0;
	    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(lineNumber);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) {
		    	
		    	editPointsPosition(i, actualBufferValues, lineNumber);
		    
		    }
		    
		    lineNumber++;    
		  
		}
		
	}
	public void display1(){
		
		int ySpace = params.get("ySpace");
		
		
		for (int i=10; i<h; i+= ySpace){
		    
			oldVector = null;
			oldDepthValue = 0;
			oldBufferValue = 0;
	    
			FloatList actualBufferValues;
	    
		    if(multipleBuffers){ //display different lines
		    	actualBufferValues = buffers.get(lineNumber);
		    } else { //display the same line
		    	actualBufferValues = buffers.get(buffers.size()-1); 
		    }
		    
		    if(actualBufferValues.size() > 0) editPointsPosition1(i, actualBufferValues, lineNumber);
		    
		    lineNumber++;    
		  
		}
		
	}
	public void display2(){
		
		int c;
		
		if(useColors){
        	c = pApplet.color(0, 255, 0);  
        } else {
        	c = pApplet.color(255);
        }
		
		pApplet.strokeWeight(2);
	    pApplet.stroke(c);
	    pApplet.noFill();
		
		for(int i=0; i<contours.size(); i++){
		    
		    ArrayList<PVector> contour = contours.get(i);
		    
		    pApplet.beginShape();
		        
		    for(int j=0; j<contour.size(); j++){
		      
		      PVector v = contour.get(j);
		      pApplet.vertex(v.x*xRatio, v.y*yRatio);
		      	
		    }
		    
		    pApplet.endShape(PApplet.CLOSE);
		}
		
	}
	private void editPointsPosition1(int i, FloatList actualBufferValues, int lineNumber){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<w; j+=10){
      		    
			actualVector = pvectors[j+i*w];
		    actualBufferValue = actualBufferValues.get(j);
		    actualDepthValue = depthValues[j+i*w];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){ //foreground
		    		
		    		drawPoint(lVal, hVal, true, lineNumber);
		    	
		    	} else { //background
	                
			        if(actualDepthValue < lVal) {
			        	//depthValue = lVal;
			        	actualDepthValue = hVal;
			        } else if(actualDepthValue > hVal){
			        	actualDepthValue = hVal;
			        }
	
			        drawPoint(lVal, hVal, false, lineNumber);
			        
		    	}
		    	
		    }
		    
		    oldVector = actualVector;
		    oldDepthValue = actualDepthValue;
		    oldBufferValue = actualBufferValue;
		
		}  

	}
	private void drawPoint(int lVal, int hVal, boolean isInFront, int lineNumber){
		
		int c;
		int blackAndWhiteColor;
		
		if(isInFront){
			blackAndWhiteColor = 255;
		} else {
			blackAndWhiteColor = 75;
		}
        
        if(useColors){
        	c = ramp.pickColor(actualDepthValue, lVal, hVal);  
        } else {
        	c = pApplet.color(blackAndWhiteColor);
        }
        
        
        float weight = (float) PApplet.map(actualDepthValue, lVal, hVal, 4, 1);
		//weight *= xRatio;
        actualDepthValue = PApplet.map(actualDepthValue, lVal, hVal, -1, 1);
      
        pApplet.stroke(c);
		pApplet.strokeWeight(weight);

		float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
        float avz = actualVector.z - actualDepthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
        
        float distance = PApplet.abs(ovz-avz);
             
        if(	(isInFront && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){
        	
        	float yPos = params.get("ySpace") * lineNumber;
        	float alpha = PApplet.map(yPos, 0, h, params.get("alpha"), 255);
        				
			
        	pApplet.stroke(c, alpha);
    		pApplet.strokeWeight(weight);
        	pApplet.point(actualVector.x*xRatio, actualVector.y*yRatio, avz);
			
		
		}

	}
	private void editPointsPosition(int i, FloatList actualBufferValues, int lineNumber){
		
		int hVal = App.highestValue;
		int lVal = App.lowestValue;
	    
		for(int j=0; j<w; j+=10){
      		    
			actualVector = pvectors[j+i*w];
		    actualBufferValue = actualBufferValues.get(j);
		    actualDepthValue = depthValues[j+i*w];
	    	    
		    if(oldVector != null){
	            
		    	if(actualDepthValue >= lVal && actualDepthValue <= hVal){ //foreground
		    		
		    		drawLine(lVal, hVal, true, lineNumber);
		    	
		    	} else { //background
	                
			        if(actualDepthValue < lVal) {
			        	//depthValue = lVal;
			        	actualDepthValue = hVal;
			        } else if(actualDepthValue > hVal){
			        	actualDepthValue = hVal;
			        }
	
			        drawLine(lVal, hVal, false, lineNumber);
			        
		    	}
		    	
		    }
		    
		    oldVector = actualVector;
		    oldDepthValue = actualDepthValue;
		    oldBufferValue = actualBufferValue;
		
		}  

	}
	
	private void drawLine(int lVal, int hVal, boolean isInFront, int lineNumber){
		
		int c;
		int blackAndWhiteColor;
		
		if(isInFront){
			blackAndWhiteColor = 255;
		} else {
			blackAndWhiteColor = 75;
		}
        
        if(useColors){
        	c = ramp.pickColor(actualDepthValue, lVal, hVal);  
        } else {
        	c = pApplet.color(blackAndWhiteColor);
        }
        
        
        float weight = (float) PApplet.map(actualDepthValue, lVal, hVal, 4, 1);
		//weight *= xRatio;
        actualDepthValue = PApplet.map(actualDepthValue, lVal, hVal, -1, 1);
      
        pApplet.stroke(c);
		pApplet.strokeWeight(weight);

		float ovz = oldVector.z - oldDepthValue*params.get("depth") - oldBufferValue*params.get("amplitude");
        float avz = actualVector.z - actualDepthValue*params.get("depth") - actualBufferValue*params.get("amplitude");
        
        float distance = PApplet.abs(ovz-avz);
             
        if(	(isInFront && distance < params.get("maxDist")) || (distance < params.get("maxDist") && linesVisibility) ){
        	
        	float yPos = params.get("ySpace") * lineNumber;

        	float alpha = PApplet.map(yPos, 0, h, params.get("alpha"), 255);     	
        	
        	//PApplet.println(params.get("ySpace")+ " " + lineNumber + " "+ test+" "+alpha);
        	//int alpha = lineNumber;
        	
        	//alpha = (int) PApplet.map(alpha, 0, h, params.get("alpha"), 255);
        		
        	/*pApplet.stroke(c, (float) (alpha-75*3));
    		pApplet.strokeWeight((float) (weight + .5*3));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
        	
        	pApplet.stroke(c, (float) (alpha-75*2));
    		pApplet.strokeWeight((float) (weight + .5*2));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);*/
        	
        	float tmp_alpha  = alpha-75;
        	tmp_alpha = PApplet.constrain(tmp_alpha, 0, 255);
        	pApplet.stroke(c, (float) (tmp_alpha));
    		pApplet.strokeWeight((float) (weight + .5));
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);
        	
        	pApplet.stroke(c, alpha);
    		pApplet.strokeWeight(weight);
			pApplet.line(oldVector.x*xRatio, oldVector.y*yRatio, ovz, actualVector.x*xRatio, actualVector.y*yRatio, avz);		
		
		}

	}
	private void addAndEraseBuffers(){
		
		FloatList bufferValues = new FloatList();
		  
		for(int i = 0; i < App.player.bufferSize(); i++) {
			bufferValues.append(App.player.left.get(i));
		}
	   
		if(buffers.size() > 0) buffers.remove(0);
		buffers.add(bufferValues);
		
	}
	private void setBuffers(int _ySpace){
		
		for (int i=10; i<h; i+= _ySpace){
			FloatList bufferValues = new FloatList();
			buffers.add(bufferValues);
		}
  
	}
private void fastblur(PImage img, int radius){
		
		if (radius<1) return;
	
		int w = img.width;
		int h = img.height;
	  
		int wm = w-1;
		int hm = h-1;
		int wh = w*h;
	  
		int div = radius+radius+1;
		
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		
		int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
		
		int vmin[] = new int[PApplet.max(w,h)];
		int vmax[] = new int[PApplet.max(w,h)];
		
		int[] pix = img.pixels;
		
		int dv[] = new int[256*div]; //?!
	  
		for (i=0; i < 256*div; i++) dv[i]=(i/div);

		yw = yi = 0;

		for (y=0; y < h; y++){
	    
			rsum = gsum = bsum = 0;
	    
			for(i = -radius; i <= radius; i++){
	      
				p = pix[yi + PApplet.min(wm, PApplet.max(i,0))];
				rsum += (p & 0xff0000) >> 16;
				gsum += (p & 0x00ff00) >> 8;
      			bsum += p & 0x0000ff;
			}
	    
			for (x=0; x < w; x++){

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				if(y == 0){
					
					vmin[x] = PApplet.min(x + radius + 1, wm);
	        		vmax[x] = PApplet.max(x - radius, 0);
				}
	      
				p1=pix[yw+vmin[x]];
				p2=pix[yw+vmax[x]];
	
				rsum += ((p1 & 0xff0000)-(p2 & 0xff0000)) >> 16;
		      	gsum += ((p1 & 0x00ff00)-(p2 & 0x00ff00)) >> 8;
		      	bsum += (p1 & 0x0000ff)-(p2 & 0x0000ff);
		      	yi++;
			}
			
			yw += w;
		}

		for (x=0; x < w; x++){
	    
			rsum = gsum = bsum = 0;
			yp = -radius*w;
	    
			for(i = -radius; i <= radius; i++){
				
				yi = PApplet.max(0,yp)+x;
			    rsum += r[yi];
			    gsum += g[yi];
			    bsum += b[yi];
			    yp += w;
			}
	    
			yi = x;
	    
			for (y=0; y < h; y++){
	      
				pix[yi] = 0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
	      
				if(x == 0){	
					vmin[y] = PApplet.min(y + radius + 1,hm)*w;
					vmax[y] = PApplet.max(y - radius, 0)*w;
				}
	      
				p1 = x + vmin[y];
				p2 = x + vmax[y];

			    rsum += r[p1] - r[p2];
			    gsum += g[p1] - g[p2];
			    bsum += b[p1] - b[p2];

			    yi+=w;
			}
		}
	}
}
