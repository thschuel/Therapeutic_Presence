package therapeuticpresence;

import processing.core.*;

public class TraceBlob {
	public float x,y,z;
	public int c;
	public int transparency;
	public int stepSize;
	public float blobSize;
	PApplet mainApplet;
	
	TraceBlob(float _x, float _y, float _z, int _c, float distanceToBFP, PApplet _mainApplet) {
		this.x = _x;
		this.y = _y;
		this.z = _z;
		this.mainApplet = _mainApplet;
		c = _c;
		transparency = 255;
		stepSize = 5;
		blobSize = distanceToBFP;
	}
	
	void run () {
		if (transparency >= stepSize) transparency -= stepSize;
		else transparency = 0;
		//mainApplet.noStroke();
		//mainApplet.fill(c,transparency);
		//mainApplet.translate(x,y,z);
		//mainApplet.sphere(blobSize);
	}
	
	public int getTransparency () {
		return transparency;
	}
	
}
