// RayDifferential.glsl

RayDifferential RayDifferential_new(vec3 start, vec3 dir, vec3 xStart, vec3 xDir, vec3 yStart, vec3 yDir)
{
	int id = ALLOC(7); // id is negative here
	STOREIV(id, RayDifferential_class);
	STOREIV(id+1, ivec4(floatBitsToInt(start), 0));
	STOREIV(id+2, ivec4(floatBitsToInt(dir), 0));
	STOREIV(id+3, ivec4(floatBitsToInt(xStart), 0));
	STOREIV(id+4, ivec4(floatBitsToInt(xDir), 0));
	STOREIV(id+5, ivec4(floatBitsToInt(yStart), 0));
	STOREIV(id+6, ivec4(floatBitsToInt(yDir), 0));
	return id;
}

bool RayDifferential_instanceOf(Ray that)
{
	ivec4 clazz = LOADIV(that).xyzw;
	return (clazz == RayDifferential_class);
}

vec3 RayDifferential_getXStart(RayDifferential that)
{
	ivec3 i = LOADIV(that + 3).xyz;
	return intBitsToFloat(i);
}

vec3 RayDifferential_getXDir(RayDifferential that)
{
	ivec3 i = LOADIV(that + 4).xyz;
	return intBitsToFloat(i);
}

vec3 RayDifferential_getYStart(RayDifferential that)
{
	ivec3 i = LOADIV(that + 5).xyz;
	return intBitsToFloat(i);
}

vec3 RayDifferential_getYDir(RayDifferential that)
{
	ivec3 i = LOADIV(that + 6).xyz;
	return intBitsToFloat(i);
}
