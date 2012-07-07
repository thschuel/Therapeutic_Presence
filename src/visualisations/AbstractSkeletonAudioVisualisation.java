package visualisations;

import therapeuticpresence.AudioManager;
import therapeuticskeleton.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractSkeletonAudioVisualisation extends AbstractSkeletonVisualisation {
	protected AudioManager audioManager = null;
	
	public AbstractSkeletonAudioVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}

}
