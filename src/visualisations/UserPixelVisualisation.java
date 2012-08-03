package visualisations;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import therapeuticpresence.TherapeuticPresence;

public class UserPixelVisualisation extends AbstractVisualisation {

	protected SimpleOpenNI kinect;
	protected int userId;
	protected int color;
	
	public UserPixelVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect, int _userId) {
		super(_mainApplet);
		mainApplet.setMirrorKinect(true);
		kinect = _kinect;
		userId = _userId;
	}

	public void setup() {
		mainApplet.colorMode(PConstants.HSB,360,100,100,100);
		color=mainApplet.color(mainApplet.random(360),100,100,100);
	}
	
	public int getColor () {
		return color;
	}

	public void draw() {

		int[] userPixels=kinect.getUsersPixels(SimpleOpenNI.USERS_ALL);
		if(userPixels != null) {
			PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
			int h = kinect.depthHeight();
			int w = kinect.depthWidth();
			int stepSize = 3;  // to speed up the drawing, draw every third point
			int index;
			
			mainApplet.stroke(color); 
			for(int y=0; y<h; y+=stepSize) {
				for(int x=0; x<w; x+=stepSize) {
					index=x+y*w;
					if(userPixels[index] == userId) { 
						// draw the projected point
						mainApplet.point(realWorldDepthMap[index].x,realWorldDepthMap[index].y,realWorldDepthMap[index].z);
					}
				} 
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.USER_PIXEL_VISUALISATION;
	}
}
