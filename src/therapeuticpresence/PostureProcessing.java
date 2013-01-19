/*
Copyright (c) 2012, Thomas Schueler, http://www.thomasschueler.de
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the author nor the
      names of the contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THOMAS SCHUELER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package therapeuticpresence;

import scenes.*;
import therapeuticskeleton.*;
import visualisations.*;

public class PostureProcessing {
	private Skeleton skeleton;
	private TherapeuticPresence mainApplet;
	private AbstractScene scene;
	private AbstractVisualisation visualisation;
	private TherapyTaskManager taskManager = null;

	public static final float timeHoldShapeToTrigger = 1.7f;
	public static final float timeBlockTrigger = 2.5f;
	public static short activePosture = SkeletonPosture.NO_POSE;
	private boolean postureChange = false;
	public static short currentGesture = SkeletonGesture.NO_GESTURE;
	private boolean newGestureRecognized = false;
	// using counter for now
	private float[] shapeActiveCounters = new float[SkeletonPosture.NUMBER_OF_POSES]; // seconds Shape occured in a row
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
			if (activePosture == SkeletonPosture.HANDS_FORWARD_DOWN_POSE) {
				scene.shapeActiveAlert(shapeActiveCounters[activePosture]);
			}
			if (currentGesture == SkeletonGesture.PUSH_GESTURE) {
				mainApplet.toggleVisualisations();
				timeSinceLastAction = 0f;
			}
			if (shapeActiveCounters[SkeletonPosture.HANDS_FORWARD_DOWN_POSE] > timeHoldShapeToTrigger) {
				if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.GENERATIVE_TREE_VISUALISATION) {
					((GenerativeTreeVisualisation)visualisation).shakeTree();
					timeSinceLastAction = 0f;
				}
			}
			
		}
	}
	
	public float timeActiveShape () {
		return shapeActiveCounters[activePosture];
	}
	
}
