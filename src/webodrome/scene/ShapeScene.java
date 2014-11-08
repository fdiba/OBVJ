package webodrome.scene;

import java.util.ArrayList;

import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import webodrome.App;
import webodrome.Ramp;

public class ShapeScene extends Scene {
	
	public static PImage blobImg;
	private BlobDetection blobDetection;
	private PImage img;
	
	public static boolean useStroke = true;
	public static boolean userIsPresent;
	
	private ArrayList<ArrayList<ArrayList<PVector>>> megaContours;
	private ArrayList<Integer> colors;
	
	private Ramp ramp;
	private int rampId;
	private int rampValue;
	
	private ArrayList<PVector> centerOfMasses;
	
	public ShapeScene(PApplet _pApplet, Object[][] objects, int _width, int _height) {
		
		super(_pApplet, objects, _width, _height);

		
		img = pApplet.createImage(imgWidth, imgHeight, PApplet.RGB);
		
		blobImg = new PImage(imgWidth/2, imgHeight/2);
		blobDetection = new BlobDetection(blobImg.width, blobImg.height);
		blobDetection.setPosDiscrimination(true); //find bright areas
		blobDetection.setThreshold(0.2f); //between 0.0f and 1.0f
		
		megaContours = new ArrayList<ArrayList<ArrayList<PVector>>>();
		colors = new ArrayList<Integer>();
		ramp = new Ramp(true, false);
		rampId = 0;
		rampValue = 5;
		
	}
	public void update(SimpleOpenNI context){
		
		super.update(context);
		
		params = App.getActualScene().params;
		pApplet.frameRate(params.get("frameRate"));
				
		depthValues = context.depthMap();
		
		detectUsers(context);
		
		img.loadPixels();
		
		int threshold = params.get("alpha");
		
		drawDepthImg(context, depthValues, imgWidth, imgHeight, threshold);
		img.updatePixels();
		
		blobImg.copy(img, 0, 0, imgWidth, imgHeight, 0, 0, blobImg.width, blobImg.height);
		
		fastblur(blobImg, params.get("blurRadius"));
		
		createBlackBorders();
		
		blobDetection.computeBlobs(blobImg.pixels);
		
		ArrayList<ArrayList<PVector>> contours = createContours();
		
		addContoursToMContours(contours);
		
	}
	private void detectUsers(SimpleOpenNI context){
		
		 centerOfMasses = new ArrayList<PVector>();
		
		int[] userList = context.getUsers();
		
		if(userList.length>0){
			
			useStroke = true;
			userIsPresent = true;
			
			for(int i=0;i<userList.length;i++) {
				
				if(context.isTrackingSkeleton(userList[i])) {

					//drawSkeleton(userList[i]);
					
			    }
				
				PVector com = new PVector();
				  
				if(context.getCoM(userList[i],com)) {
					
					PVector com2d = new PVector();
					context.convertRealWorldToProjective(com,com2d);
					
					centerOfMasses.add(com2d);
				}
			
			PApplet.println(userList.length);
			
			}
			
		} else {
			useStroke = false;
			userIsPresent = false;
		}
		
	}
	private void displayCenterOfMasses(){
		
		for(PVector com : centerOfMasses){
			
			pApplet.fill(pApplet.color(255, 0, 255));
			pApplet.rect(com.x*xRatio, com.y*yRatio, 10, 10);
			
		}
		
	}
	public void display(){
			
		float addValue = 255/params.get("contours");
		int alpha = 0;
			    
	    for(int m=0; m<megaContours.size(); m++){			
	
	    	alpha += addValue;
	    	
	    	ArrayList<ArrayList<PVector>> actualContours = megaContours.get(m);
	    	
	    	int c;
	    	
	    	if(App.useColors){
	        	c = colors.get(m); 
	        } else {
	        	c = pApplet.color(255);
	        }
	    
			for(int i=0; i<actualContours.size(); i++){
			    
			    ArrayList<PVector> contour = actualContours.get(i);

			    PShape shape;  // The PShape object
			    
			    shape = pApplet.createShape();
			    shape.beginShape();
			    
			    if(useStroke){
			    	shape.noFill();
			    	shape.strokeWeight(1);
			    	shape.stroke(c, alpha);
			    } else {
			    	shape.fill(c, alpha);
				    shape.noStroke();
			    }
			    			    
			    for(int j=0; j<contour.size(); j++){
			      PVector v = contour.get(j);
			      shape.vertex(v.x*xRatio, v.y*yRatio, m);
			      	
			    }
			    
			    shape.endShape(PApplet.CLOSE);
			    
			    pApplet.shape(shape);
			}
		
		}
	    
	    displayCenterOfMasses();
		
	}
	private void addContoursToMContours(ArrayList<ArrayList<PVector>> contours){
		
		megaContours.add(contours);
		
		
		int c = ramp.colors[rampId]; 
		colors.add(c);
		rampId+= rampValue;
		
		if(rampId>=ramp.colors.length || rampId<=0 ){
			rampValue *= -1;
			rampId+= rampValue;
		}
		
		while(megaContours.size() > params.get("contours")){
			megaContours.remove(0);
			colors.remove(0);
		}
		
	}
	private ArrayList<ArrayList<PVector>> createContours() {
		
		Blob blob;
		EdgeVertex eA, eB;
	  
		ArrayList<ArrayList<PVector>> contours = new ArrayList<ArrayList<PVector>>();
	  
	  
		for(int n=0; n<blobDetection.getBlobNb(); n++){
		  
			blob = blobDetection.getBlob(n);
	    
			if(blob != null && blob.getEdgeNb() > params.get("edgeMinNumber")){
	      
				ArrayList<PVector> contour = new ArrayList<PVector>();

				for(int i=0; i<blob.getEdgeNb(); i++){
				  
					eA = blob.getEdgeVertexA(i);
					eB = blob.getEdgeVertexB(i);
			        
					if(i==0){
			    	  
						contour.add(new PVector(eA.x*imgWidth, eA.y*imgHeight));
			    	  
					} else {
			          
						PVector v = contour.get(contour.size()-1);
						float distance = PApplet.dist(eB.x*imgWidth, eB.y*imgHeight, v.x, v.y);
						if(distance > params.get("distMin"))contour.add(new PVector(eB.x*imgWidth, eB.y*imgHeight));
			    
					}
			    
				}
	      
				if(contour.size()>2) contours.add(contour);
	    
			}
	    
		}
		
		return contours;
		
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
