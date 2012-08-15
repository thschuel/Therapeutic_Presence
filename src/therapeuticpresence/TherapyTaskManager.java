package therapeuticpresence;

import java.util.ArrayList;

import therapeuticskeleton.Skeleton;

public class TherapyTaskManager {

	private TherapeuticPresence mainApplet;
	
	// this defines the therapy
	private ArrayList<Short> alternatingPostures = new ArrayList<Short>();
	private int alternationsIndex=0;
	
	private int roundsToComplete=5;
	private int roundCounter=0;
	
	public TherapyTaskManager (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
		addPostureToAlternations(Skeleton.V_SHAPE);
		addPostureToAlternations(Skeleton.A_SHAPE);
		mainApplet.updateTask(alternatingPostures.get(alternationsIndex));
		mainApplet.updateScore(roundCounter);
	}
	
	public void addPostureToAlternations (short _posture) {
		alternatingPostures.add(_posture);
	}
	public void resetAlternatingPostures () {
		alternatingPostures.clear();
	}
	
	public void notifyPostureChange(short _posture) {
		if (alternatingPostures.size() >= 2) {
			if (_posture == alternatingPostures.get(alternationsIndex)) {
				// correct posture detected, switch to next in array
				if (++alternationsIndex >= alternatingPostures.size()) { 
					mainApplet.updateScore(++roundCounter);
					alternationsIndex=0;
				}
				mainApplet.updateTask(alternatingPostures.get(alternationsIndex));
			}
		}
		if (roundCounter == roundsToComplete) {
			mainApplet.toggleVisualisations();
			roundCounter=0;
		}
	}
}
