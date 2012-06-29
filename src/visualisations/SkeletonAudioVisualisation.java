package visualisations;

import therapeuticpresence.AudioManager;
import therapeuticpresence.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public abstract class SkeletonAudioVisualisation extends SkeletonVisualisation {
	protected AudioManager audioManager = null;
	
	public SkeletonAudioVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}

}
