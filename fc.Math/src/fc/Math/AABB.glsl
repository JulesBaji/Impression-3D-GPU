// AABB.glsl

vec3 AABB_getMin(AABB that)
{
	ivec3 v = LOADIV(that + 1).xyz;
	return intBitsToFloat(v);
}

vec3 AABB_getMax(AABB that)
{
	ivec3 v = LOADIV(that + 2).xyz;
	return intBitsToFloat(v);
}

bool AABB_intersectRay(AABB that, Ray ray, out vec2 tminmax)
{
	vec3 m_Min = AABB_getMin(that);
	vec3 m_Max = AABB_getMax(that);

	vec3 rstart = Ray_getStart(ray);
	vec3 rdir = Ray_getDir(ray);

	float tmin = (m_Min.x - rstart.x) / rdir.x;
	float tmax = (m_Max.x - rstart.x) / rdir.x;

	float temp;

	if (tmin > tmax)
	{
		// Swap tmin and tmax
		temp = tmin;
		tmin = tmax;
		tmax = temp;
	}

	float tymin = (m_Min.y - rstart.y) / rdir.y;
	float tymax = (m_Max.y - rstart.y) / rdir.y;

	if (tymin > tymax)
	{
		// Swap tymin and tymax
		temp = tymin;
		tymin = tymax;
		tymax = temp;
	}

	if ((tmin > tymax) || (tymin > tmax))
		return false;

	if (tymin > tmin)
		tmin = tymin;

	if (tymax < tmax)
		tmax = tymax;

	float tzmin = (m_Min.z - rstart.z) / rdir.z;
	float tzmax = (m_Max.z - rstart.z) / rdir.z;

	if (tzmin > tzmax)
	{
		// Swap tzmin and tzmax
		temp = tzmin;
		tzmin = tzmax;
		tzmax = temp;
	}

	if ((tmin > tzmax) || (tzmin > tmax))
		return false;

	if (tzmin > tmin)
		tmin = tzmin;

	if (tzmax < tmax)
		tmax = tzmax;

	tminmax.x = tmin;
	tminmax.y = tmax;

	return true;
}
