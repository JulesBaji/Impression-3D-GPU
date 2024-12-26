// Quaternion.glsl

struct Quaternion
{
	vec4 quat;
};

float Quaternion_length2(Quaternion q)
{
	return q.quat.x*q.quat.x + q.quat.y*q.quat.y + q.quat.z*q.quat.z + q.quat.w*q.quat.w;
}

Quaternion Quaternion_createRotationFromTo(vec3 from, vec3 to)
{
	// This routine takes any vector as argument but normalized
	// vectors are necessary, if only for computing the dot product.
	// Too bad the API is that generic, it leads to performance loss.
	// Even in the case the 2 vectors are not normalized but same length,
	// the sqrt could be shared, but we have no way to know beforehand
	// at this point, while the caller may know.
	// So, we have to test... in the hope of saving at least a sqrt
	vec3 sourceVector = from;
	vec3 targetVector = to;
	
	float fromLen2 = length(from)*length(from);
	float fromLen;
	
	float eps = 1e-7f;
	
	// normalize only when necessary, epsilon test
	if ((fromLen2 < (1.0f-eps)) || (fromLen2 > (1.0f+eps)))
	{
		fromLen = sqrt(fromLen2);
		sourceVector = sourceVector / fromLen;
	}
	else
		fromLen = 1.0f;

	float toLen2 = length(to)*length(to);
	// normalize only when necessary, epsilon test
	if ((toLen2 < (1.0f-eps)) || (toLen2 > (1.0f+eps)))
	{
		float toLen;
		// re-use fromLen for case of mapping 2 vectors of the same length
		if ((toLen2 > (fromLen2-eps)) && (toLen2 < (fromLen2+eps)))
		{
			toLen = fromLen;
		}
		else
			toLen = sqrt(toLen2);
		
		targetVector = targetVector / toLen;
	}

	// Now let's get into the real stuff
	// Use "dot product plus one" as test as it can be re-used later on
	float dotProdPlus1 = 1.0f + dot(sourceVector, targetVector);

	// Check for degenerate case of full u-turn. Use epsilon for detection
	if (dotProdPlus1 < eps)
	{
		// Get an orthogonal vector of the given vector
		// in a plane with maximum vector coordinates.
		// Then use it as quaternion axis with pi angle
		// Trick is to realize one value at least is >0.6 for a normalized vector.
		if (abs(sourceVector.x) < 0.6f)
		{
			float norm = sqrt(1.0f - sourceVector.x * sourceVector.x);
			vec4 vv = vec4(0.0f, sourceVector.z / norm, -sourceVector.y / norm, 0.0f);
			return Quaternion(vv);
		}
		else if (abs(sourceVector.y) < 0.6f)
		{
			float norm = sqrt(1.0f - sourceVector.y * sourceVector.y);
			vec4 vv = vec4(-sourceVector.z / norm, 0.0f, sourceVector.x / norm, 0.0f);
			return Quaternion(vv);
		}
		else
		{
			float norm = sqrt(1.0f - sourceVector.z * sourceVector.z);
			vec4 vv = vec4(sourceVector.y / norm, -sourceVector.x / norm, 0.0f, 0.0f);
			return Quaternion(vv);
		}
	}
	else
	{
		// Find the shortest angle quaternion that transforms normalized vectors
		// into one other. Formula is still valid when vectors are colinear
		float s = sqrt(0.5f * dotProdPlus1);
		//Vec3d tmp = sourceVector ^ targetVector / (2.0*s);
		vec3 tmp = cross(sourceVector, targetVector / (2.0f*s)); // in this order ('/' operator has precedence, over '^')
		vec4 vv = vec4(tmp.x, tmp.y, tmp.z, s);
		return Quaternion(vv);
	}
}
