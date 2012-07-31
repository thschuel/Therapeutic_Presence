package utils;

import processing.core.*;
import scenes.TunnelScene3D;
import shapes3d.Shape3D;
import shapes3d.Tube;
import therapeuticpresence.AudioManager;
import therapeuticpresence.TherapeuticPresence;

public class Ellipsoid3D {
	public static final float MAX_STEPS = 36f;
	public static final float MAX_TRANSPARENCY = 255f;

	private PVector center = new PVector();
	private PVector pos = new PVector();
	private float zOffset = 0f;
	private float radius;
	private float height;
	private float orientation;
	private float speed;
	private float FADE_OUT_SECONDS = 0.6f;
	private int color;
	public float transparency = MAX_TRANSPARENCY;
	private int framesAlive = 0;
	private float fadeOutFrames;
	private boolean fadeOut;
	
	public Ellipsoid3D(PVector _center, PVector _pos, float _radius, float _height, float _orientation, int _color, float _speed, boolean _fadeOut) {
		center=_center;
		pos=_pos;
		color=_color;
		radius=_radius;
		height=_height;
		orientation=_orientation;
		speed=_speed;
		fadeOut=_fadeOut;
		if (!fadeOut) {
			transparency*=0.5f;
		}
	}
	public void draw (PApplet _mainApplet) {
		if (transparency > 0) {
			fadeOutFrames = _mainApplet.frameRate*FADE_OUT_SECONDS;
			_mainApplet.pushStyle();
			_mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,Ellipsoid3D.MAX_TRANSPARENCY);
			_mainApplet.fill(color,transparency);
			_mainApplet.noStroke();
			_mainApplet.pushMatrix();
			_mainApplet.translate(center.x,center.y,center.z);
			_mainApplet.translate(pos.x,pos.y,-pos.z);
			_mainApplet.rotateX(orientation);
			_mainApplet.pushMatrix();
			_mainApplet.translate(0,zOffset,0);
			makeEllipsoid(_mainApplet);
			_mainApplet.popMatrix();
			if (zOffset != 0) {
				_mainApplet.pushMatrix();
				_mainApplet.translate(0,-zOffset,0);
				makeEllipsoid(_mainApplet);
				_mainApplet.popMatrix();
			}
			_mainApplet.popMatrix();
			_mainApplet.popStyle();
			
			if (fadeOut) {
				transparency -= regression(fadeOutFrames);
				if (++framesAlive >= fadeOutFrames) transparency = 0f;
				zOffset += 250;//TunnelScene3D.tunnelLength/_mainApplet.frameRate*FADE_OUT_SECONDS;
			} else {
				transparency=0f;
			}
		}
	}
	
	private void makeEllipsoid(PApplet _mainApplet) {
		_mainApplet.beginShape(PConstants.QUAD_STRIP);
		for (float angle=0, steps=0; steps<=MAX_STEPS; steps++, angle+=360f/MAX_STEPS ) {
			float x = PApplet.cos(PApplet.radians(angle))*radius/2f;
			float z = PApplet.sin(PApplet.radians(angle))*radius/2f;
			_mainApplet.vertex(x,-height,z);
			_mainApplet.vertex(x,height,z);
		}
		_mainApplet.endShape();
	}
	
	private float regression (float frameRate) {
		return PApplet.pow(transparency/fadeOutFrames,2f);
	}
}
