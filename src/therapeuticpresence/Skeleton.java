package therapeuticpresence;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import processing.opengl.*;

public class Skeleton {

	// available skeleton joints
	public static final short HEAD = 0; 
	public static final short NECK = 1;
	public static final short LEFT_SHOULDER = 2;
	public static final short LEFT_ELBOW = 3;
	public static final short LEFT_HAND = 4;
	public static final short RIGHT_SHOULDER = 5;
	public static final short RIGHT_ELBOW = 6;
	public static final short RIGHT_HAND = 7;
	public static final short TORSO = 8;
	public static final short LEFT_HIP = 9;
	public static final short LEFT_KNEE = 10;
	public static final short LEFT_FOOT = 11;
	public static final short RIGHT_HIP = 12;
	public static final short RIGHT_KNEE = 13;
	public static final short RIGHT_FOOT = 14;
	
	// The interface to talk to kinect
	protected SimpleOpenNI kinect;
	
	// stores skeleton Points in 3d Space
	protected PVector[] skeletonPoints = new PVector[15]; 
	protected float[] confidenceSkeletonPoints = new float[15];
	
	// stores joint orientation
	protected PMatrix3D[] jointOrientations = new PMatrix3D[15];
	protected float[] confidenceJointOrientations = new float[15];
	
	// calculation of mirror plane
	private PVector[] bodyPoints = new PVector[7]; // stores body points of skeleton 
	private PVector	rMP = new PVector(); // MirrorPlane in HNF: r*n0-d=0
	private PVector	n0MP = new PVector();
	private float dMP = 0.0f;
	private PVector	rBFP = new PVector(); // Best Fitting Plane in HNF: r*n0-d=0
	private PVector	n0BFP = new PVector();
	private float dBFP = 0.0f;
	
	// controls state of skeleton
	public boolean isUpdated = false;
	public boolean mirrorPlaneCalculated = false;
	
	// skeleton of user
	public int userId;
	
	public Skeleton (SimpleOpenNI _kinect, int _userId) {
		kinect = _kinect;
		userId = _userId;
		for (int i=0; i<15; i++){
			skeletonPoints[i] = new PVector();
			jointOrientations[i] = new PMatrix3D();
		}
		for (int i=0; i<7; i++){
			bodyPoints[i] = new PVector();
		}
	}
	
	public void updateSkeleton () {
		isUpdated = false;
		
		confidenceSkeletonPoints[Skeleton.HEAD] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_HEAD,skeletonPoints[Skeleton.HEAD]);
		confidenceJointOrientations[Skeleton.HEAD] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_HEAD,jointOrientations[Skeleton.HEAD]);
		confidenceSkeletonPoints[Skeleton.NECK] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_NECK,skeletonPoints[Skeleton.NECK]);
		confidenceJointOrientations[Skeleton.NECK] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_NECK,jointOrientations[Skeleton.NECK]);
		confidenceSkeletonPoints[Skeleton.LEFT_SHOULDER] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_SHOULDER,skeletonPoints[Skeleton.LEFT_SHOULDER]);
		confidenceJointOrientations[Skeleton.LEFT_SHOULDER] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_SHOULDER,jointOrientations[Skeleton.LEFT_SHOULDER]);
		confidenceSkeletonPoints[Skeleton.RIGHT_SHOULDER] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_SHOULDER,skeletonPoints[Skeleton.RIGHT_SHOULDER]);
		confidenceJointOrientations[Skeleton.RIGHT_SHOULDER] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_SHOULDER,jointOrientations[Skeleton.RIGHT_SHOULDER]);
		confidenceSkeletonPoints[Skeleton.TORSO] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_TORSO,skeletonPoints[Skeleton.TORSO]);
		confidenceJointOrientations[Skeleton.TORSO] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_TORSO,jointOrientations[Skeleton.TORSO]);
		if (TherapeuticPresence.fullBodyTracking) {
			confidenceSkeletonPoints[Skeleton.LEFT_HIP] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HIP,skeletonPoints[Skeleton.LEFT_HIP]);
			confidenceJointOrientations[Skeleton.LEFT_HIP] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HIP,jointOrientations[Skeleton.LEFT_HIP]);
			confidenceSkeletonPoints[Skeleton.LEFT_KNEE] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_KNEE,skeletonPoints[Skeleton.LEFT_KNEE]);
			confidenceJointOrientations[Skeleton.LEFT_KNEE] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_KNEE,jointOrientations[Skeleton.LEFT_KNEE]);
			confidenceSkeletonPoints[Skeleton.LEFT_FOOT] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_FOOT,skeletonPoints[Skeleton.LEFT_FOOT]);
			confidenceJointOrientations[Skeleton.LEFT_FOOT] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_FOOT,jointOrientations[Skeleton.LEFT_FOOT]);
			confidenceSkeletonPoints[Skeleton.RIGHT_HIP] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HIP,skeletonPoints[Skeleton.RIGHT_HIP]);
			confidenceJointOrientations[Skeleton.RIGHT_HIP] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HIP,jointOrientations[Skeleton.RIGHT_HIP]);
			confidenceSkeletonPoints[Skeleton.RIGHT_KNEE] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_KNEE,skeletonPoints[Skeleton.RIGHT_KNEE]);
			confidenceJointOrientations[Skeleton.RIGHT_KNEE] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_KNEE,jointOrientations[Skeleton.RIGHT_KNEE]);
			confidenceSkeletonPoints[Skeleton.RIGHT_FOOT] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_FOOT,skeletonPoints[Skeleton.RIGHT_FOOT]);
			confidenceJointOrientations[Skeleton.RIGHT_FOOT] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_FOOT,jointOrientations[Skeleton.RIGHT_FOOT]);
		}
			
		switch (TherapeuticPresence.mirrorTherapy) {
			case TherapeuticPresence.MIRROR_OFF:
				confidenceSkeletonPoints[Skeleton.LEFT_ELBOW] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_ELBOW,skeletonPoints[Skeleton.LEFT_ELBOW]);
				confidenceJointOrientations[Skeleton.LEFT_ELBOW] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_ELBOW,jointOrientations[Skeleton.LEFT_ELBOW]);
				confidenceSkeletonPoints[Skeleton.LEFT_HAND] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HAND,skeletonPoints[Skeleton.LEFT_HAND]);
				confidenceJointOrientations[Skeleton.LEFT_HAND] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HAND,jointOrientations[Skeleton.LEFT_HAND]);
				confidenceSkeletonPoints[Skeleton.RIGHT_ELBOW] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_ELBOW,skeletonPoints[Skeleton.RIGHT_ELBOW]);
				confidenceJointOrientations[Skeleton.RIGHT_ELBOW] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_ELBOW,jointOrientations[Skeleton.RIGHT_ELBOW]);
				confidenceSkeletonPoints[Skeleton.RIGHT_HAND] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HAND,skeletonPoints[Skeleton.RIGHT_HAND]);
				confidenceJointOrientations[Skeleton.RIGHT_HAND] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HAND,jointOrientations[Skeleton.RIGHT_HAND]);
				mirrorPlaneCalculated = false;
				break;
				
			case TherapeuticPresence.MIRROR_LEFT:
				// left body side will be mirrored to right body side
				confidenceSkeletonPoints[Skeleton.LEFT_ELBOW] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_ELBOW,skeletonPoints[Skeleton.LEFT_ELBOW]);
				confidenceJointOrientations[Skeleton.LEFT_ELBOW] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_ELBOW,jointOrientations[Skeleton.LEFT_ELBOW]);
				confidenceSkeletonPoints[Skeleton.LEFT_HAND] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HAND,skeletonPoints[Skeleton.LEFT_HAND]);
				confidenceJointOrientations[Skeleton.LEFT_HAND] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_LEFT_HAND,jointOrientations[Skeleton.LEFT_HAND]);
				
				// calculates and stores mirror plane in HNF for mirroring joints
				calculateMirrorPlane(); 
				
				// mirror orientation of left shoulder to right shoulder
				jointOrientations[Skeleton.RIGHT_SHOULDER].set(jointOrientations[Skeleton.LEFT_SHOULDER]);
				mirrorOrientationMatrix(jointOrientations[Skeleton.RIGHT_SHOULDER]);
				confidenceJointOrientations[Skeleton.RIGHT_SHOULDER] = confidenceJointOrientations[Skeleton.LEFT_SHOULDER];
				
				// mirror left elbow to right elbow
				float lDistanceToMP = PVector.dot(skeletonPoints[Skeleton.LEFT_ELBOW],n0MP) - dMP;
				skeletonPoints[Skeleton.RIGHT_ELBOW].set(PVector.add(skeletonPoints[Skeleton.LEFT_ELBOW],PVector.mult(n0MP,-2*lDistanceToMP)));
				confidenceSkeletonPoints[Skeleton.RIGHT_ELBOW] = confidenceSkeletonPoints[Skeleton.LEFT_ELBOW];
				// mirror joint orientations
				jointOrientations[Skeleton.RIGHT_ELBOW].set(jointOrientations[Skeleton.LEFT_ELBOW]);
				mirrorOrientationMatrix(jointOrientations[Skeleton.RIGHT_ELBOW]);
				confidenceJointOrientations[Skeleton.RIGHT_ELBOW] = confidenceJointOrientations[Skeleton.LEFT_ELBOW];
				
				// mirror left hand to right hand
				lDistanceToMP = PVector.dot(skeletonPoints[Skeleton.LEFT_HAND],n0MP) - dMP;
				skeletonPoints[Skeleton.RIGHT_HAND].set(PVector.add(skeletonPoints[Skeleton.LEFT_HAND],PVector.mult(n0MP,-2*lDistanceToMP)));
				confidenceSkeletonPoints[Skeleton.RIGHT_HAND] = confidenceSkeletonPoints[Skeleton.LEFT_HAND];
				// mirror joint orientations
				jointOrientations[Skeleton.RIGHT_HAND] = jointOrientations[Skeleton.LEFT_HAND];
				mirrorOrientationMatrix(jointOrientations[Skeleton.RIGHT_HAND]);
				confidenceJointOrientations[Skeleton.RIGHT_HAND] = confidenceJointOrientations[Skeleton.LEFT_HAND];
				break;
				
			case TherapeuticPresence.MIRROR_RIGHT:
				// right body side will be mirrored to left body side
				confidenceSkeletonPoints[Skeleton.RIGHT_ELBOW] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_ELBOW,skeletonPoints[Skeleton.RIGHT_ELBOW]);
				confidenceJointOrientations[Skeleton.RIGHT_ELBOW] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_ELBOW,jointOrientations[Skeleton.RIGHT_ELBOW]);
				confidenceSkeletonPoints[Skeleton.RIGHT_HAND] = kinect.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HAND,skeletonPoints[Skeleton.RIGHT_HAND]);
				confidenceJointOrientations[Skeleton.RIGHT_HAND] = kinect.getJointOrientationSkeleton(userId,SimpleOpenNI.SKEL_RIGHT_HAND,jointOrientations[Skeleton.RIGHT_HAND]);
				
				// calculates and stores mirror plane in HNF for mirroring joints
				calculateMirrorPlane();  
				
				// mirror orientation of left shoulder to right shoulder
				jointOrientations[Skeleton.LEFT_SHOULDER].set(jointOrientations[Skeleton.RIGHT_SHOULDER]);
				mirrorOrientationMatrix(jointOrientations[Skeleton.LEFT_SHOULDER]);
				confidenceJointOrientations[Skeleton.LEFT_SHOULDER] = confidenceJointOrientations[Skeleton.RIGHT_SHOULDER];
	
				// mirror right elbow to left elbow
				float rDistanceToMP = PVector.dot(skeletonPoints[Skeleton.RIGHT_ELBOW],n0MP) - dMP;
				skeletonPoints[Skeleton.LEFT_ELBOW].set(PVector.add(skeletonPoints[Skeleton.RIGHT_ELBOW],PVector.mult(n0MP,-2*rDistanceToMP)));
				confidenceSkeletonPoints[Skeleton.LEFT_ELBOW] = confidenceSkeletonPoints[Skeleton.RIGHT_ELBOW];
				// mirror joint orientations
				jointOrientations[Skeleton.LEFT_ELBOW] = jointOrientations[Skeleton.RIGHT_ELBOW]; 
				mirrorOrientationMatrix(jointOrientations[Skeleton.LEFT_ELBOW]);
				confidenceJointOrientations[Skeleton.LEFT_ELBOW] = confidenceJointOrientations[Skeleton.RIGHT_ELBOW];
				
				// mirror right hand to left hand
				rDistanceToMP = PVector.dot(skeletonPoints[Skeleton.RIGHT_HAND],n0MP) - dMP;
				skeletonPoints[Skeleton.LEFT_HAND].set(PVector.add(skeletonPoints[Skeleton.RIGHT_HAND],PVector.mult(n0MP,-2*rDistanceToMP)));
				confidenceSkeletonPoints[Skeleton.LEFT_HAND] = confidenceSkeletonPoints[Skeleton.RIGHT_HAND];
				// mirror joint orientations
				jointOrientations[Skeleton.LEFT_HAND] = jointOrientations[Skeleton.RIGHT_HAND];
				mirrorOrientationMatrix(jointOrientations[Skeleton.LEFT_HAND]);
				confidenceJointOrientations[Skeleton.LEFT_HAND] = confidenceJointOrientations[Skeleton.RIGHT_HAND];
				break;
		}	
		
		isUpdated = true;
	}
	

	// -----------------------------------------------------------------
	// methods to communicate
	public PVector getJoint (short jointType) {
		if (jointType >= 0 && jointType <= 14) 
			return skeletonPoints[jointType];
		else
			return new PVector();
	}
	
	public float getConfidenceJoint (short jointType) {
		if (jointType >= 0 && jointType <= 14) 
			return confidenceJointOrientations[jointType];
		else
			return 0f;
	}
	
	public PMatrix3D getJointOrientation (short jointType) {
		if (jointType >= 0 && jointType <= 14) 
			return jointOrientations[jointType];
		else
			return new PMatrix3D();
	}
	
	public float getConfidenceJointOrientation (short jointType) {
		if (jointType >= 0 && jointType <= 14) 
			return confidenceJointOrientations[jointType];
		else
			return 0f;
	}
	
	public PVector getRVectorMirrorPlane () {
		if (mirrorPlaneCalculated)
			return rMP;
		else
			return new PVector();
	}
	
	public PVector getN0VectorMirrorPlane () {
		if (mirrorPlaneCalculated)
			return n0MP;
		else
			return new PVector();
	}
	
	public float getDValueMirrorPlane () {
		if (mirrorPlaneCalculated)
			return dMP;
		else
			return 0f;
	}
	
	// -----------------------------------------------------------------
	// methods to calculate body posture

	// return angle between left arm and body axis in radians
	public float angleBetween (short joint11, short joint12, short joint21, short joint22) {
		PVector axis1 = PVector.sub(skeletonPoints[joint11],skeletonPoints[joint12]);
		PVector axis2 = PVector.sub(skeletonPoints[joint21],skeletonPoints[joint22]);
		
		return PVector.angleBetween(axis1,axis2);
	}
	
	// return angle between up vector body axis in radians
	public float angleToUpAxis (short joint11, short joint12) {
		PVector axis1 = PVector.sub(skeletonPoints[joint11],skeletonPoints[joint12]);
		
		return PVector.angleBetween(axis1,new PVector(0,1,0));
	}
	
	public float distanceToKinect () {
		return skeletonPoints[Skeleton.TORSO].mag();
	}

	// -----------------------------------------------------------------
	// maths for mirroring
	private void mirrorOrientationMatrix (PMatrix3D matrix) {
		
//		PApplet.println("x1:"+matrix.m00+" y1:"+matrix.m01+" z1:"+matrix.m02+" t1:"+matrix.m03);
//		PApplet.println("x2:"+matrix.m10+" y2:"+matrix.m11+" z2:"+matrix.m12+" t2:"+matrix.m13);
//		PApplet.println("x3:"+matrix.m20+" y3:"+matrix.m21+" z3:"+matrix.m22+" t3:"+matrix.m23);
//		PApplet.println("x4:"+matrix.m30+" y4:"+matrix.m31+" z4:"+matrix.m32+" t4:"+matrix.m33);
		
		PVector x = new PVector(matrix.m00,matrix.m10,matrix.m20);
		PVector y = new PVector(matrix.m01,matrix.m11,matrix.m21);
		PVector z = new PVector(matrix.m02,matrix.m12,matrix.m22);
		
		x.add(rMP);
		y.add(rMP);
		z.add(rMP);
		
		float distanceToMP = PVector.dot(x,n0MP) - dMP;
		x.set(PVector.add(x,PVector.mult(n0MP,-2*distanceToMP)));
		distanceToMP = PVector.dot(y,n0MP) - dMP;
		y.set(PVector.add(y,PVector.mult(n0MP,-2*distanceToMP)));
		distanceToMP = PVector.dot(z,n0MP) - dMP;
		z.set(PVector.add(z,PVector.mult(n0MP,-2*distanceToMP)));
		
		x.sub(rMP);
		y.sub(rMP);
		z.sub(rMP);
		matrix.set(-x.x      ,y.x       ,z.x       ,matrix.m03,
				   -x.y      ,y.y       ,z.y       ,matrix.m13,
				   -x.z      ,y.z       ,z.z       ,matrix.m23,
				   matrix.m30,matrix.m31,matrix.m32,matrix.m33);		
	}
	
	private void calculateMirrorPlane () {
		
		// calculate body plane of Shoulder and Torso points in HNF
		// HNF: r*n0-d = 0
		PVector r;
		PVector n0;
		
		// r is position vector of any point in the plane
		r = skeletonPoints[Skeleton.TORSO];
		
		// n0 is cross product of two vectors in the plane
		PVector temp1 = PVector.sub(skeletonPoints[Skeleton.LEFT_SHOULDER],r);
		PVector temp2 = PVector.sub(skeletonPoints[Skeleton.RIGHT_SHOULDER],r);
		n0 = temp1.cross(temp2);
		n0.normalize();
		
		// mirrorPlane is orthogonal to body plane and contains the line between torso and neck
		// calculate mirrorPlane in HNF: r*n0-d = 0
		rMP = r; // r is always set to position of the torso
		n0MP = n0.cross(PVector.sub(skeletonPoints[Skeleton.NECK],skeletonPoints[Skeleton.TORSO]));
		n0MP.normalize();
		dMP = PVector.dot(rMP,n0MP);
		mirrorPlaneCalculated = true;
		
	}

	private void calculateMirrorPlaneOld () {
	
		// store neck-, head-, torso-, shoulder-, hip-vectors for calculation of mirror plane
		bodyPoints[0] = skeletonPoints[Skeleton.HEAD];
		bodyPoints[1] = skeletonPoints[Skeleton.NECK];
		bodyPoints[2] = skeletonPoints[Skeleton.TORSO];
		bodyPoints[3] = skeletonPoints[Skeleton.LEFT_SHOULDER];
		bodyPoints[4] = skeletonPoints[Skeleton.RIGHT_SHOULDER];
		bodyPoints[5] = skeletonPoints[Skeleton.LEFT_HIP];
		bodyPoints[6] = skeletonPoints[Skeleton.RIGHT_HIP];
		
		// calculate all possible planes formed by bodyPoints in HNF (headVector not used for now)
		// HNF: r*n0-d = 0
		PVector[] r = new PVector[20];
		PVector[] n0 = new PVector[20];
		float[] d = new float[20];
		float[] quadDistances = new float[20];
		int index = 0;
		
		PVector temp1 = new PVector();
		PVector temp2 = new PVector();
		
		for (int i=1; i<7; i++) { // skip headVector for now
			for (int j=i+1; j<6; j++) {
				for (int k=j+1; k<7; k++) {
					temp1.set(PVector.sub(bodyPoints[j],bodyPoints[i]));
					temp2.set(PVector.sub(bodyPoints[k],bodyPoints[i]));
					
					n0[index] = temp1.cross(temp2);
					
					// skip if bodyPoints form a straight line
					if (n0[index].x == 0.0f && n0[index].y == 0.0f && n0[index].z == 0.0f) 
						continue;
					
					n0[index].normalize();
					r[index] = bodyPoints[i];
					d[index] = r[index].dot(n0[index]);	
					
					// sum quad distances of bodyPoints to plane
					quadDistances[index] = 0.0f;
					for (int l=1; l<7; l++) {
						quadDistances[index] += PApplet.sq(bodyPoints[l].dot(n0[index]) - d[index]);
					}
					
					index++;
				}
			}
		}
		
		if (index > 0) {
			// find best fitting plane (plane with minimal summed distances of bodyPoints)
			int indexBestFittingPlane = 0;
			for (int i=1; i<index; i++) {
				if (quadDistances[i] < quadDistances[indexBestFittingPlane]) {
					indexBestFittingPlane = i;
				}
			}
			// store bestFittingPlane
			n0BFP = n0[indexBestFittingPlane];
			rBFP = r[indexBestFittingPlane];
			dBFP = d[indexBestFittingPlane];
			// mirrorPlane is orthogonal to bestFittingPlane and contains the line between torso and neck
			// calculate mirrorPlane in HNF: r*n0-d = 0
			n0MP = n0BFP.cross(PVector.sub(bodyPoints[1],bodyPoints[2]));
			n0MP.normalize();
			rMP = bodyPoints[1]; // r is always set to position of the neck
			dMP = PVector.dot(rMP,n0MP);
		}
		
	}
	
}
	