package visualisations;

import java.util.ArrayList;

import processing.core.*;
import SimpleOpenNI.*;
import therapeuticpresence.*;
import therapeuticskeleton.SkeletonStatistics;

public class StatisticsVisualisation extends AbstractVisualisation {

	private int strokeColor = 0;
	private int jointColor = 0;
	
	private SkeletonStatistics statistics = null;
	private GuiHud guiHud = null;
	
	public StatisticsVisualisation (TherapeuticPresence _mainApplet, SkeletonStatistics _statistics, GuiHud _guiHud) {
		super(_mainApplet);
		statistics = _statistics;
		guiHud = _guiHud;
	}
	
	public void setup() {
		guiHud.setFinalStatistics(statistics.getDistanceLeftHand(), statistics.getDistanceLeftElbow(), statistics.getDistanceRightHand(), statistics.getDistanceRightElbow());
	}

	public void draw() {
		
		// draw upper limb joints history
		ArrayList<PVector> leftHand = statistics.getHistoryLeftHand();
		ArrayList<PVector> rightHand = statistics.getHistoryRightHand();
		ArrayList<PVector> leftElbow = statistics.getHistoryLeftElbow();
		ArrayList<PVector> rightElbow = statistics.getHistoryRightElbow();
		
		for (int i=0; i<leftHand.size()-1; i++) {
			mainApplet.colorMode(PConstants.HSB,4,100,100,100);
			strokeColor = mainApplet.color(1,100,100,100);
			drawLineBetweenJoints(leftHand.get(i),leftHand.get(i+1));
			strokeColor = mainApplet.color(2,100,100,100);
			drawLineBetweenJoints(rightHand.get(i),rightHand.get(i+1));
			strokeColor = mainApplet.color(3,100,100,100);
			drawLineBetweenJoints(leftElbow.get(i),leftElbow.get(i+1));
			strokeColor = mainApplet.color(4,100,100,100);
			drawLineBetweenJoints(rightElbow.get(i),rightElbow.get(i+1));
		}
	}
	
	private void drawLineBetweenJoints (PVector joint1, PVector joint2) {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.stroke(strokeColor,255);
		mainApplet.strokeWeight(1f);
		mainApplet.line(joint1.x,joint1.y,joint1.z,joint2.x,joint2.y,joint2.z);
	}

	public short getVisualisationType() {
		return TherapeuticPresence.STATISTICS_VISUALISATION;
	}

}
