package therapeuticpresence;

import scenes.*;
import therapeuticskeleton.Skeleton;
import visualisations.*;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	private AbstractScene scene;
	private AbstractVisualisation visualisation;

	public static final float timeHoldShapeToTrigger = 1.7f;
	public static final float timeBlockTrigger = 5.5f;
	public static short activePosture = Skeleton.NO_POSE;
	public static short currentGesture = Skeleton.NO_GESTURE;
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
	
	public void updatePosture () {
		timeSinceLastAction += 1f/mainApplet.frameRate;
		activePosture = skeleton.getCurrentUpperBodyPosture();
		currentGesture = skeleton.getLastUpperBodyGesture(100);
		for (int i=0; i<shapeActiveCounters.length; i++) {
			if (i==activePosture) {
				shapeActiveCounters[i] += 1f/mainApplet.frameRate;
			} else {
				shapeActiveCounters[i] = 0f;
			}
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > timeBlockTrigger) {
			if (activePosture == Skeleton.HANDS_FORWARD_DOWN_POSE) {
				scene.shapeActiveAlert(shapeActiveCounters[activePosture]);
			}
			if (shapeActiveCounters[Skeleton.HANDS_FORWARD_DOWN_POSE] > timeHoldShapeToTrigger) {
				if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_3D_VISUALISATION);
				} else if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GEOMETRY_3D_VISUALISATION) {
//					mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
//					mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
//				}  else if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.STICKFIGURE_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION);
				}
				timeSinceLastAction = 0f;
			}
			if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION) {
				if (currentGesture == Skeleton.PUSH_GESTURE) {
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
