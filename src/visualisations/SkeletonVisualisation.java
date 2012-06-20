package visualisations;

import processing.core.PApplet;
import therapeuticpresence.Skeleton;

public abstract class SkeletonVisualisation extends AbstractVisualisation {
	protected Skeleton skeleton = null;
	
	public SkeletonVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
		super(_mainApplet);
		skeleton = _skeleton;
	}
}
