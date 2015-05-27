package webodrome;

import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import themidibus.MidiBus;
import webodrome.ctrl.BehringerBCF;
import webodrome.ctrl.Menu;
import webodrome.ctrl.SecondApplet;
import webodrome.scene.Scene;

public class App {
	
	public static SecondApplet secondApplet;
	
	public static boolean useLiveMusic = true;
	
	//-------- KINECT CONST ----------//
	public static int KWIDTH = 640;
	public static int KHEIGHT = 480;
	
	//-------- depthMapControl ----------//
	public static int lowestValue = 550;
	public static int highestValue = 2350;
	public static boolean switchValue;
	
	//colors : red, green, blue green, orange, dark blue
	public final static int[] colorsPanel = {255 << 24 | 240 << 16 | 65 << 8 | 50,
											 255 << 24 | 135 << 16 | 205 << 8 | 137,
											 255 << 24 | 40 << 16 | 135 << 8 | 145,
											 255 << 24 | 252 << 16 | 177 << 8 | 135,
											 255 << 24 | 15 << 16 | 65 << 8 | 85};
	
	public final static int[] colors = {-8410437,-9998215,-1849945,-5517090,-4250587,-14178341,-5804972,-3498634};
		
	private static Scene actualScene;
	public static Menu actualMenu;
	
	public static Minim minim;
	public static AudioPlayer player;
	public static AudioInput in;
	public static FFT fft;
	
	public static boolean updateSound = true;
	public static boolean duplicateFFT = true;
	
	//TODO UPDATE/ERASE IT
	public static PVector[] pvectors;
	public static PShape mainGrid;
	
	public static boolean useColors;
	
	public static int strokeJoin, strokeCap = 0;
	
	//-------- behringer ----------//
	public final static boolean BCF2000 = true;
	public static MidiBus midiBus;
	public static BehringerBCF behringer;
	
	public static int[] transValues;
	
	//-----------------------//
	
	public static boolean fscreen = false;
	
	private static int sceneId = 0;
	public static int oldSceneId = 999;
	
	public App() {
		// TODO Auto-generated constructor stub
	}
	public static int getSceneId(){
		return sceneId;
	}
	public static void setSceneId(int _sceneId) {
		sceneId = _sceneId;
	}
	public static Scene getActualScene() {
		return actualScene;
	}
	public static void setActualScene(Scene actualScene) {
		App.actualScene = actualScene;
		App.actualMenu = actualScene.menu;
	}

}
