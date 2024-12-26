// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Quaternion
{
	public float x;
	public float y;
	public float z;
	public float w;
	
	public Quaternion(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public float length2()
	{
		return x*x + y*y + z*z + w*w;
	}
	
	// Untested (taken from OSG)
	public static Quaternion createRotationFromAxisAngle(float angle, Vec3f v) // angle in radians, v must be normalized
	{
	    float length = (float)Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
	    float inversenorm  = 1.0f/length;
	    float coshalfangle = (float)Math.cos(0.5*angle);
	    float sinhalfangle = (float)Math.sin(0.5*angle);

	    return new Quaternion(
	    		v.x * sinhalfangle * inversenorm,
	    		v.y * sinhalfangle * inversenorm,
	    		v.z * sinhalfangle * inversenorm,
	    		coshalfangle);
	}
	
	// Untested (taken from OSG)
	public static Quaternion createRotationFromTo(Vec3f from, Vec3f to)
	{
		// This routine takes any vector as argument but normalized
		// vectors are necessary, if only for computing the dot product.
		// Too bad the API is that generic, it leads to performance loss.
		// Even in the case the 2 vectors are not normalized but same length,
		// the sqrt could be shared, but we have no way to know beforehand
		// at this point, while the caller may know.
		// So, we have to test... in the hope of saving at least a sqrt
		Vec3f sourceVector = from;
		Vec3f targetVector = to;
		
		float fromLen2 = from.length()*from.length();
		float fromLen;
		
		// normalize only when necessary, epsilon test
		if ((fromLen2 < 1.0-1e-7) || (fromLen2 > 1.0+1e-7))
		{
			fromLen = (float)Math.sqrt(fromLen2);
			sourceVector = sourceVector.div(fromLen);
		}
		else
			fromLen = 1.0f;
	
		float toLen2 = to.length()*to.length();
		// normalize only when necessary, epsilon test
		if ((toLen2 < 1.0-1e-7) || (toLen2 > 1.0+1e-7))
		{
			float toLen;
			// re-use fromLen for case of mapping 2 vectors of the same length
			if ((toLen2 > fromLen2-1e-7) && (toLen2 < fromLen2+1e-7))
			{
				toLen = fromLen;
			}
			else
				toLen = (float)Math.sqrt(toLen2);
			
			targetVector = targetVector.div(toLen);
		}
	
		// Now let's get into the real stuff
		// Use "dot product plus one" as test as it can be re-used later on
		float dotProdPlus1 = 1.0f + sourceVector.dot(targetVector);
	
		// Check for degenerate case of full u-turn. Use epsilon for detection
		if (dotProdPlus1 < 1e-7)
		{
			// Get an orthogonal vector of the given vector
			// in a plane with maximum vector coordinates.
			// Then use it as quaternion axis with pi angle
			// Trick is to realize one value at least is >0.6 for a normalized vector.
			if (Math.abs(sourceVector.x) < 0.6)
			{
				float norm = (float)Math.sqrt(1.0 - sourceVector.x * sourceVector.x);
				return new Quaternion(
						0.0f,
						sourceVector.z / norm,
						-sourceVector.y / norm,
						0.0f);
			}
			else if (Math.abs(sourceVector.y) < 0.6)
			{
				float norm = (float)Math.sqrt(1.0 - sourceVector.y * sourceVector.y);
				return new Quaternion(
						-sourceVector.z / norm,
						0.0f,
						sourceVector.x / norm,
						0.0f);
			}
			else
			{
				float norm = (float)Math.sqrt(1.0 - sourceVector.z * sourceVector.z);
				return new Quaternion(
						sourceVector.y / norm,
						-sourceVector.x / norm,
						0.0f,
						0.0f);
			}
		}
		else
		{
			// Find the shortest angle quaternion that transforms normalized vectors
			// into one other. Formula is still valid when vectors are colinear
			float s = (float)Math.sqrt(0.5 * dotProdPlus1);
			//Vec3d tmp = sourceVector ^ targetVector / (2.0*s);
			Vec3f tmp = sourceVector.cross(targetVector.div(2.0f*s)); // in this order ('/' operator has precedence, over '^')
			return new Quaternion(
					tmp.x,
					tmp.y,
					tmp.z,
					s);
		}
	}
	
	public Quaternion mul(Quaternion q)
	{
		float sp = w;
		Vec3f vp = new Vec3f( x, y, z );
		
		float sq = q.w;
		Vec3f vq = new Vec3f( q.x, q.y, q.z );
		 
		float sr = sp*sq - vp.dot(vq);
		
		Vec3f vr;
		Vec3f dp;
		dp = vp.cross(vq);
		vp = vp.mul(sq);
		vq = vq.mul(sp);
		vr = vp.add(vq).add(dp);
		
		return new Quaternion(vr.x, vr.y, vr.z, sr);
	}
	
	public Quaternion normalize()
	{
		float l = w*w + x*x + y*y + z*z;
	    if (l < 1e-20)
	        return new Quaternion(0,0,0,1);
	
	    l = (float)Math.sqrt(l);
	
	    return new Quaternion(
		    x / l,
		    y / l,
		    z / l,
		    w / l);
	}
	
	public void makeRotate(Vec3f from, Vec3f to)
	{
		// This routine takes any vector as argument but normalized
		// vectors are necessary, if only for computing the dot product.
		// Too bad the API is that generic, it leads to performance loss.
		// Even in the case the 2 vectors are not normalized but same length,
		// the sqrt could be shared, but we have no way to know beforehand
		// at this point, while the caller may know.
		// So, we have to test... in the hope of saving at least a sqrt
		Vec3f sourceVector = from;
		Vec3f targetVector = to;
		
		float fromLen2 = from.length()*from.length();
		float fromLen;
		// normalize only when necessary, epsilon test
		if ((fromLen2 < 1.0f-1e-7) || (fromLen2 > 1.0f+1e-7)) {
			fromLen = (float)Math.sqrt(fromLen2);
			sourceVector = sourceVector.div(fromLen);
		}
		else
			fromLen = 1.0f;

		float toLen2 = to.length()*to.length();
		// normalize only when necessary, epsilon test
		if ((toLen2 < 1.0f-1e-7) || (toLen2 > 1.0f+1e-7))
		{
			float toLen;
			// re-use fromLen for case of mapping 2 vectors of the same length
			if ((toLen2 > fromLen2-1e-7) && (toLen2 < fromLen2+1e-7)) {
				toLen = fromLen;
			}
			else
				toLen = (float)Math.sqrt(toLen2);
			targetVector = targetVector.div(toLen);
		}

		// Now let's get into the real stuff
		// Use "dot product plus one" as test as it can be re-used later on
		float dotProdPlus1 = 1.0f + sourceVector.dot(targetVector);

		// Check for degenerate case of full u-turn. Use epsilon for detection
		if (dotProdPlus1 < 1e-7) {

			// Get an orthogonal vector of the given vector
			// in a plane with maximum vector coordinates.
			// Then use it as quaternion axis with pi angle
			// Trick is to realize one value at least is >0.6 for a normalized vector.
			if (Math.abs(sourceVector.x) < 0.6f) {
				float norm = (float)Math.sqrt(1.0f - sourceVector.x*sourceVector.x);
				x = 0.0f;
				y = sourceVector.z / norm;
				z = -sourceVector.y / norm;
				w = 0.0f;
			} else if (Math.abs(sourceVector.y) < 0.6f) {
				float norm = (float)Math.sqrt(1.0 - sourceVector.y*sourceVector.y);
				x = -sourceVector.z / norm;
				y = 0.0f;
				z = sourceVector.x / norm;
				w = 0.0f;
			} else {
				float norm = (float)Math.sqrt(1.0 - sourceVector.z*sourceVector.z);
				x = sourceVector.y / norm;
				y = -sourceVector.x / norm;
				z = 0.0f;
				w = 0.0f;
			}
		}
		else {
			// Find the shortest angle quaternion that transforms normalized vectors
			// into one other. Formula is still valid when vectors are colinear
			float s = (float)Math.sqrt(0.5f * dotProdPlus1);
			Vec3f tmp = sourceVector.cross(targetVector).div(2.0f*s);
			x = tmp.x;
			y = tmp.y;
			z = tmp.z;
			w = s;
		}
	}
}
