package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import therapeuticpresence.*;
import shapes3d.*;

public class TunnelScene3D extends BasicScene3D {
	protected AudioManager audioManager = null;
	
	protected Tube tunnelTube = null;
	protected PGraphics textureWalls = null;
	protected PImage textureWallsImg = null;
	protected PImage textureWallsVer = null;
	protected PImage textureWallsHor = null;
	public static float tunnelWidth = 4000f;
	public static float tunnelHeight = 3000f;
	public static float tunnelLength = 5000f;
	public static float tunnelShrink = 10f;
	public static float tunnelEntryZ = tunnelLength;
	
	// animation of texture, background colors are controlled by audio stream
	protected int offsetTunnelEffect = 0;
	protected int offsetTunnelEffectMax = 0;
	protected final int horizontalLines = 3;
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 255;
	protected int backgroundTintHue = 120;
	protected int defaultBackgroundHighlightColor = 0x10ffffff;
	protected float audioReactionDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;
	
	public TunnelScene3D (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor);
		audioManager = _audioManager;
		// rotate and set up for third person view
		rotY = PApplet.radians(180);
		translateZ = -tunnelLength; // negative translation, because translation will be applied after rotation around Y
		tunnelTube = new Tube(mainApplet,8,60);
		// set radii of tube
		tunnelTube.setSize(tunnelWidth/2f,tunnelHeight/2f,(tunnelWidth/2f)/tunnelShrink,(tunnelWidth/2f)/tunnelShrink,tunnelLength);
		tunnelTube.rotateBy(-PConstants.PI/2,0,0);
		tunnelTube.z(tunnelLength/2f-1f);
		tunnelTube.visible(false,Tube.BOTH_CAP);
		// basic components for texture animation
		textureWallsVer = mainApplet.loadImage("../data/textureTunnel.png");
		textureWallsHor = mainApplet.loadImage("../data/textureTunnelCrossline.png");
		textureWallsImg = new PImage(textureWallsVer.width,textureWallsVer.height);
		textureWalls = mainApplet.createGraphics(textureWallsVer.width,textureWallsVer.height,PConstants.P2D);
		offsetTunnelEffectMax = (textureWalls.height+textureWallsHor.height)/horizontalLines; 
	}
	
	public void reset () {
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,1,audioManager.getMaxFFT(),100);
		backgroundTintColor = mainApplet.color(backgroundTintHue,1,fftDCValueDelayed,100);
		defaultBackgroundColor = PApplet.blendColor(backgroundTintColor,defaultBackgroundHighlightColor,PConstants.BLEND);
		backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
		super.reset();
		// update texture and draw background
		updateTexture();
		tunnelTube.setTexture(textureWalls.get(),10,1);
		tunnelTube.drawMode(Shape3D.TEXTURE);
		tunnelTube.draw();
	}
	
	public static float getTunnelWidthAt (float _z) {
		if (tunnelEntryZ-_z < 0 || tunnelEntryZ-_z > tunnelLength) {
			return 0;
		} 
		return tunnelWidth/tunnelShrink + _z/tunnelLength * (tunnelWidth-tunnelWidth/tunnelShrink);
	}
	
	public static float getTunnelHeightAt (float _z) {
		if (tunnelEntryZ-_z < 0 || tunnelEntryZ-_z > tunnelLength) {
			return 0;
		} 
		return tunnelHeight/tunnelShrink + _z/tunnelLength * (tunnelHeight-tunnelHeight/tunnelShrink);
	}
	
	private void updateTexture () {
		// animation
		if (offsetTunnelEffect>0) offsetTunnelEffect--;
		else offsetTunnelEffect = offsetTunnelEffectMax;
		
		// assemble texture
		textureWallsImg.copy(textureWallsVer,0,0,textureWallsVer.width,textureWallsVer.height,0,0,textureWallsVer.width,textureWallsVer.height);
		for (int i=0;i<horizontalLines; i++) {
			textureWallsImg.copy(textureWallsHor,0,0,textureWallsHor.width,textureWallsHor.height,0,i*offsetTunnelEffectMax+offsetTunnelEffect,textureWallsHor.width,textureWallsHor.height);
		}
		
		// tint according to audio stream
		textureWalls.beginDraw();
		textureWalls.tint(backgroundTintColor);
		textureWalls.image(textureWallsImg,0,0,textureWallsVer.width,textureWallsVer.height,0,0,textureWallsVer.width,textureWallsVer.height);
		textureWalls.endDraw();
	}
	
}
