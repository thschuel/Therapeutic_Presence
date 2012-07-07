package visualisations;

import therapeuticskeleton.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractSkeletonVisualisation extends AbstractVisualisation {
	protected Skeleton skeleton = null;
	
	public AbstractSkeletonVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		super(_mainApplet);
		skeleton = _skeleton;
	}
}
