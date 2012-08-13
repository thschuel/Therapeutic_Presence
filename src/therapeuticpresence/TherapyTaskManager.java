package therapeuticpresence;

import therapeuticskeleton.Skeleton;

public class TherapyTaskManager {

	private TherapeuticPresence mainApplet;
	
	// this defines the therapy
	private short alternatingPostures1 = Skeleton.V_SHAPE;
	private short alternatingPostures2 = Skeleton.A_SHAPE;
	private int alternations=10;
	
	private short lastPosture = Skeleton.NO_POSE;
	private int alternationsCounter=0;
	
	public TherapyTaskManager (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
	}
	
	public void notifyPostureChange(short _posture) {
		if (_posture != lastPosture) {
			if (_posture == alternatingPostures1 || _posture == alternatingPostures2) {
				lastPosture = _posture;
				mainApplet.updateScore(++alternationsCounter);
			}
		}
		if (alternationsCounter == alternations) {
			mainApplet.toggleVisualisations();
			alternationsCounter=0;
			mainApplet.updateScore(alternationsCounter);
		}
	}
}
