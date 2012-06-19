package Visuals;

import processing.core.PApplet;

public abstract class Visualisation {
	protected PApplet mainApplet = null;
	protected int backgroundColor = 0;
	
	// variables to move through the scene. used only by some visualisations
	public float translateX = 0.0f;
	public float translateY = 0.0f;
	public float translateZ = 0.0f;
	public float rotX = PApplet.radians(0); 
	public float rotY = PApplet.radians(0);
	public float rotZ = PApplet.radians(180);  // by default rotate the hole scene 180deg around the z-axis, the data from openni comes upside down
	
	public Visualisation (PApplet _mainApplet) {
		mainApplet = _mainApplet;
		backgroundColor = mainApplet.color(0,0,0);
	}
	public abstract void setup ();
	public abstract void reset ();
	public abstract void draw ();
}
