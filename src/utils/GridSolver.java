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

package utils;

import processing.core.*;

//Velocity: How fast each pixel is moving up or down
//Density: How much "fluid" is in each pixel.

//*note* 
//Density isn't conserved as far as I know. 
//Changing the velocity ends up changing the density too.

public class GridSolver implements Runnable {
	private int cellSize;

	// Use 2 dimensional arrays to store velocity and density for each pixel.
	// To access, use this: grid[x/cellSize][y/cellSize]
	private float [][] velocity;
	private float [][] density;
	private float [][] oldVelocity;
	private float [][] oldDensity;
	
	private boolean isUpdated=false;
	private boolean hasBeenReturned=false;
	  
	private PApplet mainApplet;
	private PGraphics canvas;
	private Thread t;
	  
	private float friction = 0.58f;
	private float speed = 20;

	// Variables for the timeStep
	private long previousTime;
	private long currentTime;
	private float timeScale = 1; // Play with this to slow down or speed up the fluid (the higher, the faster)
	private final int fixedDeltaTime = (int)(10 / timeScale);
	private float fixedDeltaTimeSeconds = (float)fixedDeltaTime / 1000;
	private float leftOverDeltaTime = 0;

	/* Constructor */
	public GridSolver (int sizeOfCells, PApplet _mainApplet) {
		mainApplet = _mainApplet;
		mainApplet.registerDispose(this);
	    canvas = mainApplet.createGraphics(mainApplet.width,mainApplet.height,PConstants.P2D);
		cellSize = sizeOfCells;
		velocity = new float[canvas.width/cellSize][canvas.height/cellSize];
		density = new float[canvas.width/cellSize][canvas.height/cellSize];
	}

	  
	public void start() {
	    t = new Thread(this);
	    t.start();
	}

	public void run() {
	    /******** Physics ********/
	    // time related stuff
	    while(true) {
	    	isUpdated=false;
		    // Calculate amount of time since last frame (Delta means "change in")
		    currentTime = mainApplet.millis();
		    long deltaTimeMS = (long)((currentTime - previousTime));
		    previousTime = currentTime; // reset previousTime
		      
		    // timeStepAmt will be how many of our fixedDeltaTimes we need to make up for the passed time since last frame. 
		    int timeStepAmt = (int)(((float)deltaTimeMS + leftOverDeltaTime) / (float)(fixedDeltaTime));
		      
		    // If we have any left over time left, add it to the leftOverDeltaTime.
		    leftOverDeltaTime += deltaTimeMS - (timeStepAmt * (float)fixedDeltaTime); 
	      
		    if (timeStepAmt > 15) {
		    	timeStepAmt = 15; // too much accumulation can freeze the program!
		    }
	      
		    // Update physics
		    for (int iteration = 1; iteration <= 1; iteration++) {
		    	this.solve(fixedDeltaTimeSeconds * timeScale);
		    }
	      
		    this.draw();
		    hasBeenReturned = false;
		    isUpdated = true;
		    try {
		    	this.wait(100);
		    } catch (Exception e) { PApplet.println("interrupted"); }
	    }
	}

	public void stop() {
	    t = null;
	}

	// this will magically be called by the parent once the user hits stop
	// this functionality hasn't been tested heavily so if it doesn't work, file a bug
	public void dispose() {
	    stop();
	}

	/* Drawing */
	private void draw () {
	    canvas.beginDraw();
	    canvas.colorMode(PConstants.HSB, 255);
	    canvas.noStroke();
	    for (int x = 0; x < velocity.length; x++) {
	    	for (int y = 0; y < velocity[x].length; y++) {
	    		/* Sine probably isn't needed, but oh well. It's pretty and looks more organic. */
	    		canvas.fill(127+ 127 * PApplet.sin(density[x][y]*0.0004f), 255, 127 + 127 * PApplet.sin(velocity[x][y]*0.01f));
	    		canvas.rect(x*cellSize, y*cellSize, cellSize, cellSize);
	    	}
	    }
	    canvas.endDraw();
	 }
	  
	public PImage getFluid () {
	    if (!hasBeenReturned) {
	    	hasBeenReturned = true;
	    	return canvas.get();
	    } else {
	    	return null;
	    }
	}

	/* "Fluid" Solving
	Based on http://www.cs.ubc.ca/~rbridson/fluidsimulation/GameFluids2007.pdf
	To help understand this better, imagine each pixel as a spring.
	  Every spring pulls on springs adjacent to it as it moves up or down (The speed of the pull is the Velocity)
	  This pull flows throughout the window, and eventually deteriates due to friction
	*/
	private void solve (float timeStep) {
		// Reset oldDensity and oldVelocity
		oldDensity = (float[][])density.clone();  
		oldVelocity = (float[][])velocity.clone();
		 
		for (int x = 0; x < velocity.length; x++) {
			for (int y = 0; y < velocity[x].length; y++) {
		    /* Equation for each cell:
		        Velocity = oldVelocity + (sum_Of_Adjacent_Old_Densities - oldDensity_Of_Cell * 4) * timeStep * speed)
		        Density = oldDensity + Velocity
		        Scientists and engineers: Please don't use this to model tsunamis, I'm pretty sure it's not *that* accurate
		    */
		    velocity[x][y] = friction * oldVelocity[x][y] + ((getAdjacentDensitySum(x,y) - density[x][y] * 4) * timeStep * speed);
		    density[x][y] = oldDensity[x][y] + velocity[x][y];
		   }
		}
	}
	
	public void setVelocity (int x, int y, float force) {
		velocity[x][y] += force;
	}

	private float getAdjacentDensitySum (int x, int y) {
		// If the x or y is at the boundary, use the closest available cell
		float sum = 0;
		if (x-1 > 0)
			sum += oldDensity[x-1][y];
		else
			sum += oldDensity[0][y];
	   
		if (x+1 <= oldDensity.length-1)
			sum += (oldDensity[x+1][y]);
		else
			sum += (oldDensity[oldDensity.length-1][y]);
	   
		if (y-1 > 0)
			sum += (oldDensity[x][y-1]);
		else
			sum += (oldDensity[x][0]);
	   
		if (y+1 <= oldDensity[x].length-1)
			sum += (oldDensity[x][y+1]);
		else
			sum += (oldDensity[x][oldDensity[x].length-1]);
	   
		return sum;
	}
}
