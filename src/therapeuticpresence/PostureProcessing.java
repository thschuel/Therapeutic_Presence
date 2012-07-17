package therapeuticpresence;

import processing.core.PConstants;
import scenes.AbstractScene;
import therapeuticskeleton.Skeleton;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	private AbstractScene scene;
	public static short activeShape = Skeleton.NO_SHAPE;
	// using counter for now
	private float oShapeCounter = 0f; // seconds Shape occured in a row
	private float iShapeCounter = 0f;
	private float vShapeCounter = 0f;
	private float aShapeCounter = 0f;
	private float nShapeCounter = 0f;
	private float uShapeCounter = 0f;
	private float timeSinceLastAction = 0f; // seconds since last switch
	
	public PostureProcessing (TherapeuticPresence _mainApplet, Skeleton _skeleton, AbstractScene _scene) {
		mainApplet = _mainApplet;
		skeleton = _skeleton;
		scene = _scene;
	}
	
	public void updatePosture () {
		timeSinceLastAction += 1f/mainApplet.frameRate;
		
		switch (skeleton.evaluateUpperJointPosture()) {
			case Skeleton.O_SHAPE:
				oShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.O_SHAPE) {
					iShapeCounter = 0f;
					vShapeCounter = 0f;
					aShapeCounter = 0f;
					nShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.O_SHAPE;
				}
				break;
			case Skeleton.I_SHAPE:
				iShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.I_SHAPE) {
					oShapeCounter = 0f;
					vShapeCounter = 0f;
					aShapeCounter = 0f;
					nShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.I_SHAPE;
				}
				break;
			case Skeleton.V_SHAPE:
				vShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.V_SHAPE) {
					oShapeCounter = 0f;
					iShapeCounter = 0f;
					aShapeCounter = 0f;
					nShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.V_SHAPE;
				}
				break;
			case Skeleton.A_SHAPE:
				aShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.A_SHAPE) {
					oShapeCounter = 0f;
					iShapeCounter = 0f;
					vShapeCounter = 0f;
					nShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.A_SHAPE;
				}
				break;
			case Skeleton.N_SHAPE:
				nShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.N_SHAPE) {
					oShapeCounter = 0f;
					iShapeCounter = 0f;
					vShapeCounter = 0f;
					aShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.N_SHAPE;
				}
				break;
			case Skeleton.U_SHAPE:
				uShapeCounter += 1f/mainApplet.frameRate;
				if (activeShape != Skeleton.U_SHAPE) {
					oShapeCounter = 0f;
					iShapeCounter = 0f;
					vShapeCounter = 0f;
					aShapeCounter = 0f;
					nShapeCounter = 0f;
					activeShape = Skeleton.U_SHAPE;
				}
				break;
			default:
				if (activeShape != Skeleton.NO_SHAPE) {
					oShapeCounter = 0f;
					iShapeCounter = 0f;
					vShapeCounter = 0f;
					aShapeCounter = 0f;
					nShapeCounter = 0f;
					uShapeCounter = 0f;
					activeShape = Skeleton.NO_SHAPE;
				}
				break;
		}
	}
	
	public void triggerAction () {
		if (timeSinceLastAction > 1.5f) {
			if (activeShape != Skeleton.NO_SHAPE) {
				// tint background color of scene for visual signal
				mainApplet.colorMode(PConstants.RGB,255,255,255,100);
				scene.setBackgroundColor(mainApplet.color(200,10,10));
			}
			if (uShapeCounter > 0.7f) {
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
			case Skeleton.O_SHAPE:
				return oShapeCounter;
			case Skeleton.I_SHAPE:
				return iShapeCounter;
			case Skeleton.V_SHAPE:
				return vShapeCounter;
			case Skeleton.A_SHAPE:
				return aShapeCounter;
			case Skeleton.N_SHAPE:
				return nShapeCounter;
			case Skeleton.U_SHAPE:
				return uShapeCounter;
			default:
				return 0f;
		}
	}
	
}
