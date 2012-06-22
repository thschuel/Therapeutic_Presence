package visualisations;

import therapeuticpresence.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public abstract class SkeletonVisualisation extends AbstractVisualisation {
	protected Skeleton skeleton = null;
	
	public SkeletonVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		super(_mainApplet);
		skeleton = _skeleton;
	}
}
