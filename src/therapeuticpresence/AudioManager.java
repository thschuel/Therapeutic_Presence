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

import ddf.minim.*;
import ddf.minim.analysis.FFT;

public class AudioManager implements AudioListener{
	public static boolean calcFFT = true; 
	public static int bands = 8;
	
	protected TherapeuticPresence mainApplet;
	protected Minim minim;
	protected AudioPlayer audioPlayer;

	protected float[] leftChannelSamples = null;
	protected float[] rightChannelSamples = null;
	protected FFT fft; 
	public static float gain = 0.12f;
	protected float maxFFT;
	protected float[] leftFFT = null;
	protected float[] rightFFT = null;
	
	protected boolean isUpdated = false;

	public AudioManager (TherapeuticPresence _mainApplet) {
		mainApplet = _mainApplet;
		minim = new Minim(mainApplet);
	}
	
	public void setup (String file) {
		audioPlayer = minim.loadFile(file,1024);
		audioPlayer.addListener(this);
		
		if (file.equals("../data/moan.mp3")) {
			gain = 0.07f;
		}
		
	    fft = new FFT(audioPlayer.bufferSize(), audioPlayer.sampleRate());
	    maxFFT =  audioPlayer.sampleRate() / audioPlayer.bufferSize() * gain;
	    fft.window(FFT.HAMMING);
		leftFFT = new float[bands];
		rightFFT = new float[bands];
	    fft.linAverages(bands);
	}
	
	public void start () {
		audioPlayer.loop();
		update();
	}
	
	public void update () {
		isUpdated = false;
		if (calcFFT) {
		    fft.forward(leftChannelSamples);
		    for(int i = 0; i < bands; i++) leftFFT[i] = fft.getAvg(i);
		    fft.forward(rightChannelSamples);
		    for(int i = 0; i < bands; i++) rightFFT[i] = fft.getAvg(i);
		}
	    maxFFT =  audioPlayer.sampleRate() / audioPlayer.bufferSize() * gain;
		isUpdated = true;
	}
	
	public void pause() {
		audioPlayer.pause();
	}
	
	public void stop() {
		audioPlayer.close();
		minim.stop();
	}
	
	public float getLeftFFT (int _index) {
		if (isUpdated) return leftFFT[_index];
		else return 0f;
	}
	
	public float getRightFFT (int _index) {
		if (isUpdated) return rightFFT[_index];
		else return 0f;
	}
	
	public float getMeanFFT (int _index) {
		if (isUpdated) return (rightFFT[_index]+leftFFT[_index])/2f;
		else return 0f;
	}
	
	public float getLeftSampleAt (int _index) {
		if (isUpdated) return leftChannelSamples[_index];
		else return 0f;
	}
	
	public float getRightSampleAt (int _index) {
		if (isUpdated) return rightChannelSamples[_index];
		else return 0f;
	}
	
	public float getMeanSampleAt (int _index) {
		if (isUpdated) return (leftChannelSamples[_index]+rightChannelSamples[_index])/2;
		else return 0f;
	}

	public float getSampleRate () {
		return audioPlayer.sampleRate();
	}
	
	public float getBufferSize () {
		return audioPlayer.bufferSize();
	}
	
	public void samples(float[] samp) {
		leftChannelSamples = samp;
	}

	public void samples(float[] sampL, float[] sampR) {
		leftChannelSamples = sampL;
		rightChannelSamples = sampR;
	}
	

	public float getMaxFFT() {
		return maxFFT;
	}

	public boolean isUpdated() {
		return isUpdated;
	}
}
