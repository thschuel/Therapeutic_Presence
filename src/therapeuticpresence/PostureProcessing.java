package therapeuticpresence;

import scenes.*;
import therapeuticskeleton.Skeleton;
import visualisations.*;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	private AbstractScene scene;
	private AbstractVisualisation visualisation;
	private TherapyTaskManager taskManager = null;

	public static final float timeHoldShapeToTrigger = 1.7f;
	public static final float timeBlockTrigger = 2.5f;
	public static short activePosture = Skeleton.NO_POSE;
	private boolean postureChange = false;
	public static short currentGesture = Skeleton.NO_GESTURE;
	private boolean newGestureRecognized = false;
	// using counter for now
	private float[] shapeActiveCounters = new float[Skeleton.NUMBER_OF_POSES]; // seconds Shape occured in a row
	private float timeSinceLastAction = 0f; // seconds since last switch
	
	public PostureProcessing (TherapeuticPresence _mainApplet, Skeleton _skeleton, AbstractScene _scene, AbstractVisualisation _visualisation) {
		mainApplet = _mainApplet;
		skeleton = _skeleton;
		scene = _scene;
		visualisation = _visualisation;
		for (int i=0; i<shapeActiveCounters.length; i++) {
			shapeActiveCounters[i] = 0f;
		}
	}
	
	public void setScene (AbstractScene _scene) {
		scene = _scene;
	}
	
	public void setVisualisation (AbstractVisualisation _visualisation) {
		visualisation = _visualisation;
	}
	
	public void setTaskManager (TherapyTaskManager _taskManager) {
		taskManager = _taskManager;
	}
	
	public void removeTaskManager () {
		taskManager = null;
	}
	
	public void updatePosture () {
		postureChange = false;
		newGestureRecognized=false;
		timeSinceLastAction += 1f/mainApplet.frameRate;
		
		short activePostureTemp = skeleton.getCurrentUpperBodyPosture();
		if (activePostureTemp != activePosture)
			postureChange = true;
		activePosture=activePostureTemp;
		
		short currentGestureTemp = skeleton.getLastUpperBodyGesture(10);
		if (currentGestureTemp != currentGesture) 
			newGestureRecognized = true;
		currentGesture = currentGestureTemp;
		
		for (int i=0; i<shapeActiveCounters.length; i++) {
			if (i==activePosture) {
				shapeActiveCounters[i] += 1f/mainApplet.frameRate;
			} else {
				shapeActiveCounters[i] = 0f;
			}
		}
		if (postureChange) { // react to change in posture, for now this is used for structuring the therapy
			if (taskManager != null) {
				taskManager.notifyPostureChange(activePosture);
			}
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > timeBlockTrigger) {
			if (activePosture == Skeleton.HANDS_FORWARD_DOWN_POSE) {
				scene.shapeActiveAlert(shapeActiveCounters[activePosture]);
			}
			if (currentGesture == Skeleton.PUSH_GESTURE) {
				mainApplet.toggleVisualisations();
				timeSinceLastAction = 0f;
			}
			if (shapeActiveCounters[Skeleton.HANDS_FORWARD_DOWN_POSE] > timeHoldShapeToTrigger) {
				if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION) {
					((GenerativeTree3DVisualisation)visualisation).shakeTree();
					timeSinceLastAction = 0f;
				}
			}
			
		}
	}
	
	public float timeActiveShape () {
		return shapeActiveCounters[activePosture];
	}
	
}
