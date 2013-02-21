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

import java.util.ArrayList;

import processing.core.PApplet;

import therapeuticskeleton.*;

public class ProgressionManager {

	private TherapeuticPresence mainApplet;
	
	public static final short MANUAL_PROGRESSION_MODE = 0;
	public static final short TIME_PROGRESSION_MODE = 1;
	public static final short POSTURE_PROGRESSION_MODE = 2;
	
	public static short progressionMode = MANUAL_PROGRESSION_MODE;
	
	// this defines the therapy in time_progression_mode
	public static float secondsToProgress = 100f;
	private float secondsSinceLastChange = 0f;
	private int lastFrameCount = -9999;
	
	// this defines the therapy in posture_progression_mode
	private ArrayList<Short> alternatingPostures = new ArrayList<Short>();
	private int alternationsIndex=0;
	private int roundsToComplete=5;
	private int roundCounter=0;
	
	private boolean isActive = false;
	
	public ProgressionManager (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
		alternatingPostures.add(SkeletonPosture.A_SHAPE);
		alternatingPostures.add(SkeletonPosture.V_SHAPE);
		reset();
	}
	
	public void reset() {
		switch (progressionMode) {
			case TIME_PROGRESSION_MODE:
				secondsSinceLastChange = 0f;
				lastFrameCount = -9999;
				mainApplet.updateScore(PApplet.round(secondsToProgress-secondsSinceLastChange));
				isActive = true;
				break;
				
			case POSTURE_PROGRESSION_MODE:
				mainApplet.updateTask(alternatingPostures.get(alternationsIndex));
				mainApplet.updateScore(roundCounter);
				isActive = true;
				break;
				
			default:
				isActive = false;
				break;
		}
	}
	
	public void setActive(boolean _active) {
		if (isActive != _active) {
			alternationsIndex = 0;
			roundCounter = 0;
			secondsSinceLastChange = 0f;
			lastFrameCount = -9999;
		}
		isActive = _active;	
	}
	
	public boolean isActive () {
		return isActive;
	}
	
	public void updateTime(int _frameCount, float _frameRate) {
		if (isActive) {
			if (lastFrameCount != -9999) {
				secondsSinceLastChange += (_frameCount-lastFrameCount)/_frameRate;
				
			}
			if (secondsSinceLastChange >= secondsToProgress) {
				mainApplet.toggleVisualisations();
				secondsSinceLastChange = 0f;
			}
			mainApplet.updateTime(secondsSinceLastChange);
			lastFrameCount = _frameCount;
		}
	}
	
	public void notifyPostureChange(short _posture) {
		if (isActive) {
			if (alternatingPostures.size() >= 2) {
				if (_posture == alternatingPostures.get(alternationsIndex)) {
					// correct posture detected, switch to next in array
					if (++alternationsIndex >= alternatingPostures.size()) { 
						mainApplet.updateScore(++roundCounter);
						alternationsIndex=0;
					}
					mainApplet.updateTask(alternatingPostures.get(alternationsIndex));
				}
			}
			if (roundCounter == roundsToComplete) {
				mainApplet.toggleVisualisations();
				roundCounter=0;
			}
		}
	}
}
