package therapeuticpresence;

import processing.core.PApplet;

public abstract class SkeletonVisualisation extends Visualisation {
	protected Skeleton skeleton = null;
	
	public SkeletonVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
		super(_mainApplet);
		skeleton = _skeleton;
	}
}
