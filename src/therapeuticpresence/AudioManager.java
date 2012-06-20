package therapeuticpresence;

import processing.core.PApplet;
import ddf.minim.*;
import ddf.minim.analysis.FFT;

public class AudioManager implements AudioListener{
	public static boolean calcFFT = true; 
	public static int bands = 8;
	
	protected PApplet mainApplet;
	protected Minim minim;
	protected AudioPlayer audioPlayer;

	protected float[] leftChannelSamples = null;
	protected float[] rightChannelSamples = null;
	protected FFT fft; 
	protected float maxFFT;
	protected float[] leftFFT = null;
	protected float[] rightFFT = null;
	
	public boolean isUpdated = false;

	public AudioManager (PApplet _mainApplet) {
		mainApplet = _mainApplet;
		minim = new Minim(mainApplet);
	}
	
	public void setup () {
		audioPlayer = minim.loadFile("../data/moan.mp3",1024);
		audioPlayer.addListener(this);
		
		float gain = .125f;
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
	
	public void samples(float[] samp) {
		leftChannelSamples = samp;
	}

	public void samples(float[] sampL, float[] sampR) {
		leftChannelSamples = sampL;
		rightChannelSamples = sampR;
	}
}
