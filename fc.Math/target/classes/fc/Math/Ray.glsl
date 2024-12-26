// Ray.glsl

Ray Ray_new(vec3 start, vec3 dir)
{
	int id = ALLOC(3); // id is negative here
	STOREIV(id, Ray_class);
	STOREIV(id+1, ivec4(floatBitsToInt(start), 0));
	STOREIV(id+2, ivec4(floatBitsToInt(dir), 0));
	return id;
}

vec3 Ray_getStart(Ray that)
{
	ivec3 i = LOADIV(that + 1).xyz;
	return intBitsToFloat(i);
}

vec3 Ray_getDir(Ray that)
{
	ivec3 i = LOADIV(that + 2).xyz;
	return intBitsToFloat(i);
}
