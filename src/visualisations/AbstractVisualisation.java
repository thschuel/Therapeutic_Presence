package visualisations;

import processing.core.PApplet;

public abstract class AbstractVisualisation {
	protected PApplet mainApplet = null;
	
	public AbstractVisualisation (PApplet _mainApplet) {
		mainApplet = _mainApplet;
	}
	public abstract void setup ();
	public abstract void draw ();
}
