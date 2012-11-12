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

import therapeuticskeleton.*;

public class TherapyTaskManager {

	private TherapeuticPresence mainApplet;
	
	// this defines the therapy
	private ArrayList<Short> alternatingPostures = new ArrayList<Short>();
	private int alternationsIndex=0;
	
	private int roundsToComplete=5;
	private int roundCounter=0;
	
	public TherapyTaskManager (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
		addPostureToAlternations(SkeletonPosture.V_SHAPE);
		addPostureToAlternations(SkeletonPosture.A_SHAPE);
		mainApplet.updateTask(alternatingPostures.get(alternationsIndex));
		mainApplet.updateScore(roundCounter);
	}
	
	public void addPostureToAlternations (short _posture) {
		alternatingPostures.add(_posture);
	}
	public void resetAlternatingPostures () {
		alternatingPostures.clear();
	}
	
	public void notifyPostureChange(short _posture) {
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
