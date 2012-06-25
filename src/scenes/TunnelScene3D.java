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
	protected int offsetTunnelEffect = 0;
	protected int offsetTunnelEffectMax = 0;
	protected final int horizontalLines = 3;
	// background colors are controlled by audio stream
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 255;
	protected int backgroundTintHue = 120;
	protected float audioReactionDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;
	
	public TunnelScene3D (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor);
		audioManager = _audioManager;
		textureWallsVer = mainApplet.loadImage("../data/textureTunnel.png");
		textureWallsHor = mainApplet.loadImage("../data/textureTunnelCrossline.png");
		textureWallsImg = new PImage(textureWallsVer.width,textureWallsVer.height);
		textureWalls = mainApplet.createGraphics(textureWallsVer.width,textureWallsVer.height,PConstants.P2D);
		offsetTunnelEffectMax = (textureWalls.height+textureWallsHor.height)/horizontalLines;
		tunnelTube = new Tube(mainApplet,8,60);
		tunnelTube.setSize(2000f,2000f,200f,200f,5000f);
		tunnelTube.rotateBy(PConstants.PI/2,0,0);
		tunnelTube.z(2499f);
		tunnelTube.visible(false,Tube.BOTH_CAP);
	}
	
	public void reset () {
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,1,audioManager.maxFFT,1);
		backgroundTintColor = mainApplet.color(backgroundTintHue,1,fftDCValueDelayed,1);
		backgroundColor = backgroundTintColor+0x00111111;
		// update texture and draw background
		super.reset();
		updateTexture();
		tunnelTube.setTexture(textureWalls.get(),10,1);
		tunnelTube.drawMode(Shape3D.TEXTURE);
		tunnelTube.draw();
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
