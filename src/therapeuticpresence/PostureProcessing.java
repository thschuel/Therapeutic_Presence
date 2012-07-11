package therapeuticpresence;

import therapeuticskeleton.Skeleton;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	public static short activeShape = Skeleton.NO_SHAPE;
	// using counter for now
	private float vShapeCounter = 0f; // seconds V Shape occured in a row
	private float oShapeCounter = 0f; // seconds O Shape occured in a row
	private float timeSinceLastAction = 0f; // seconds since last switch
	
	public PostureProcessing (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		mainApplet = _mainApplet;
		skeleton = _skeleton;
	}
	
	public void updatePosture () {
		timeSinceLastAction += 1f/mainApplet.frameRate;
		
		switch (skeleton.getUpperJointPosture()) {
			case Skeleton.V_SHAPE:
				vShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.V_SHAPE) {
					oShapeCounter = 0f;
					activeShape = Skeleton.V_SHAPE;
				}
				break;
			case Skeleton.O_SHAPE:
				oShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.O_SHAPE) {
					vShapeCounter = 0f;
					activeShape = Skeleton.O_SHAPE;
				}
				break;
			default:
				if (activeShape != Skeleton.NO_SHAPE) {
					oShapeCounter = 0f;
					vShapeCounter =0f;
					activeShape = Skeleton.NO_SHAPE;
				}
				break;
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > 2f) {
			if (oShapeCounter > 1.2f) {
				if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_3D_VISUALISATION);
				} else if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GEOMETRY_3D_VISUALISATION) {
					mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
					mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION);
				}
				timeSinceLastAction = 0f;
			}
		}
	}
	
	public float timeActiveShape () {
		switch (activeShape) {
			case Skeleton.V_SHAPE:
				return vShapeCounter;
			case Skeleton.O_SHAPE:
				return oShapeCounter;
			default:
				return 0f;
		}
	}
	
}
