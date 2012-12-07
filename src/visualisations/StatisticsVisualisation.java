package visualisations;

import processing.core.*;
import SimpleOpenNI.*;
import therapeuticpresence.*;
import therapeuticskeleton.SkeletonStatistics;

public class StatisticsVisualisation extends AbstractVisualisation {

	private int strokeColor = 0;
	private int jointColor = 0;
	
	private SkeletonStatistics statistics = null;
	private GuiHud guiHud = null;
	
	private PVector skeletonPosition = new PVector();
	private PVector lastLeftShoulderLCS = new PVector();
	private PVector lastRightShoulderLCS = new PVector();
	private PVector lastTorsoLCS = new PVector();
	private PVector leftHandAtMaxAngle = new PVector();
	private PVector leftElbowAtMaxAngle = new PVector();
	private PVector rightHandAtMaxAngle = new PVector();
	private PVector rightElbowAtMaxAngle = new PVector();
	
	public StatisticsVisualisation (TherapeuticPresence _mainApplet, SkeletonStatistics _statistics, GuiHud _guiHud) {
		super(_mainApplet);
		statistics = _statistics;
		guiHud = _guiHud;
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(0,255,255);
		jointColor = mainApplet.color(0,0,255);
		mainApplet.colorMode(PConstants.HSB,360,100,100,100);
		
		skeletonPosition = statistics.getLastSkeletonPosition();
		lastLeftShoulderLCS = statistics.getLastLeftShoulderLCS();
		lastRightShoulderLCS = statistics.getLastRightShoulderLCS();
		lastTorsoLCS = statistics.getLastTorsoLCS();
		leftHandAtMaxAngle = statistics.getLeftHandAtMaxAngle();
		leftElbowAtMaxAngle = statistics.getLeftElbowAtMaxAngle();
		rightHandAtMaxAngle = statistics.getRightHandAtMaxAngle();
		rightElbowAtMaxAngle = statistics.getRightElbowAtMaxAngle();

		guiHud.setFinalStatistics(statistics.getDistanceLeftHand(), statistics.getDistanceLeftElbow(), statistics.getDistanceRightHand(), statistics.getDistanceRightElbow());
	}

	public void draw() {
		
		// draw Skeleton with max angles
		mainApplet.pushMatrix();
		mainApplet.translate(0f,0f,3f*TherapeuticPresence.maxDistanceToKinect/4f);
		mainApplet.rotateY(PConstants.PI); // skeleton coordinate system faces towards scene coordinate system on z axis.
		
		drawLineBetweenJoints(new PVector(),lastLeftShoulderLCS);
		drawLineBetweenJoints(new PVector(),lastRightShoulderLCS);
		drawLineBetweenJoints(lastTorsoLCS,lastLeftShoulderLCS);
		drawLineBetweenJoints(lastTorsoLCS,lastRightShoulderLCS);

		drawLineBetweenJoints(lastLeftShoulderLCS,leftElbowAtMaxAngle);
		drawLineBetweenJoints(lastRightShoulderLCS,rightElbowAtMaxAngle);
		drawLineBetweenJoints(leftElbowAtMaxAngle,leftHandAtMaxAngle);
		drawLineBetweenJoints(rightElbowAtMaxAngle,rightHandAtMaxAngle);
		
		drawJoint(new PVector());
		drawJoint(lastLeftShoulderLCS);
		drawJoint(lastRightShoulderLCS);
		drawJoint(lastTorsoLCS);
		
		drawJoint(leftElbowAtMaxAngle);
		drawJoint(rightElbowAtMaxAngle);
		drawJoint(leftHandAtMaxAngle);
		drawJoint(rightHandAtMaxAngle);
		
		mainApplet.popMatrix();
	}
	
	private void drawLineBetweenJoints (PVector joint1, PVector joint2) {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.stroke(strokeColor,255);
		mainApplet.strokeWeight(2f);
		mainApplet.line(joint1.x,joint1.y,joint1.z,joint2.x,joint2.y,joint2.z);
	}
	
	private void drawJoint (PVector joint) {
		mainApplet.pushMatrix();
		mainApplet.translate(joint.x,joint.y,joint.z);
		mainApplet.noStroke();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.fill(jointColor,255);
		mainApplet.sphere(10f);
		mainApplet.popMatrix();
	}

	public short getVisualisationType() {
		return TherapeuticPresence.STATISTICS_VISUALISATION;
	}

}
