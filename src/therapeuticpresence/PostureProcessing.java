package therapeuticpresence;

import processing.core.PConstants;
import scenes.AbstractScene;
import therapeuticskeleton.Skeleton;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	private AbstractScene scene;

	public static final float timeHoldShapeToTrigger = 1.7f;
	public static final float timeBlockTrigger = 2.5f;
	public static short activeShape = Skeleton.NO_SHAPE;
	// using counter for now
	private float[] shapeActiveCounters = new float[9]; // seconds Shape occured in a row
	private float timeSinceLastAction = 0f; // seconds since last switch
	
	public PostureProcessing (TherapeuticPresence _mainApplet, Skeleton _skeleton, AbstractScene _scene) {
		mainApplet = _mainApplet;
		skeleton = _skeleton;
		scene = _scene;
		for (int i=0; i<shapeActiveCounters.length; i++) {
			shapeActiveCounters[i] = 0f;
		}
	}
	
	public void setScene (AbstractScene _scene) {
		scene = _scene;
	}
	
	public void updatePosture () {
		timeSinceLastAction += 1f/mainApplet.frameRate;
		activeShape = skeleton.evaluateUpperJointPosture();
		for (int i=0; i<shapeActiveCounters.length; i++) {
			if (i==activeShape) {
				shapeActiveCounters[i] += 1f/mainApplet.frameRate;
			} else {
				shapeActiveCounters[i] = 0f;
			}
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > timeBlockTrigger) {
			if (shapeActiveCounters[Skeleton.U_SHAPE] > timeHoldShapeToTrigger) {
				if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_3D_VISUALISATION);
				} else if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GEOMETRY_3D_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
				}  else if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.STICKFIGURE_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION);
				}
				timeSinceLastAction = 0f;
			}
		}
		if (activeShape != Skeleton.NO_SHAPE) {
			scene.shapeActiveAlert(shapeActiveCounters[activeShape]);
		}
	}
	
	public float timeActiveShape () {
		return shapeActiveCounters[activeShape];
	}
	
}
