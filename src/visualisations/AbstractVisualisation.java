package visualisations;

import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractVisualisation {
	protected TherapeuticPresence mainApplet = null;
	
	public AbstractVisualisation (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
	}
	public abstract void setup ();
	public abstract void draw ();
	public abstract short getVisualisationType ();
	
	/// should be overwritten if fadeIn or fadeOut is needed
	public boolean fadeIn () { return true; }
	public boolean fadeOut () { return true; }
}
