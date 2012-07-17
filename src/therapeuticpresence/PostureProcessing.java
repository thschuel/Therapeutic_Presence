package therapeuticpresence;

import therapeuticskeleton.Skeleton;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	public static short activeShape = Skeleton.NO_SHAPE;
	// using counter for now
	private float vShapeCounter = 0f; // seconds V Shape occured in a row
	private float oShapeCounter = 0f; // seconds O Shape occured in a row
	private float nShapeCounter = 0f;
	private float namasteShapeCounter = 0f;
	private float timeSinceLastAction = 0f; // seconds since last switch
	
	public PostureProcessing (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		mainApplet = _mainApplet;
		skeleton = _skeleton;
	}
	
	public void updatePosture () {
		timeSinceLastAction += 1f/mainApplet.frameRate;
		
		switch (skeleton.evaluateUpperJointPosture()) {
			case Skeleton.V_SHAPE:
				vShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.V_SHAPE) {
					oShapeCounter = 0f;
					nShapeCounter = 0f;
					namasteShapeCounter = 0f;
					activeShape = Skeleton.V_SHAPE;
				}
				break;
			case Skeleton.N_SHAPE:
				nShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.N_SHAPE) {
					vShapeCounter = 0f;
					oShapeCounter = 0f;
					namasteShapeCounter = 0f;
					activeShape = Skeleton.N_SHAPE;
				}
				break;
			case Skeleton.O_SHAPE:
				oShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.O_SHAPE) {
					vShapeCounter = 0f;
					nShapeCounter = 0f;
					namasteShapeCounter = 0f;
					activeShape = Skeleton.O_SHAPE;
				}
				break;
			case Skeleton.NAMASTE_SHAPE:
				namasteShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.NAMASTE_SHAPE) {
					vShapeCounter = 0f;
					nShapeCounter = 0f;
					oShapeCounter = 0f;
					activeShape = Skeleton.NAMASTE_SHAPE;
				}
				break;
			default:
				if (activeShape != Skeleton.NO_SHAPE) {
					oShapeCounter = 0f;
					vShapeCounter = 0f;
					nShapeCounter = 0f;
					namasteShapeCounter = 0f;
					activeShape = Skeleton.NO_SHAPE;
				}
				break;
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > 1.5f) {
			if (oShapeCounter > 0.7f) {
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
			case Skeleton.N_SHAPE:
				return nShapeCounter;
			case Skeleton.NAMASTE_SHAPE:
				return namasteShapeCounter;
			default:
				return 0f;
		}
	}
	
}
