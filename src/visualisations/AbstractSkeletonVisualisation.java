package visualisations;

import therapeuticskeleton.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractSkeletonVisualisation extends AbstractVisualisation {
	protected Skeleton skeleton = null;
	public static float angleScale1 = 0.7f;
	public static float angleScale2 = 0.9f;
	public static float angleScale3 = 0.8f;
	
	public AbstractSkeletonVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		super(_mainApplet);
		skeleton = _skeleton;
	}
}
