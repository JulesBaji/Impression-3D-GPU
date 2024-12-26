// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Vec4f
{
	public float x;
	public float y;
	public float z;
	public float w;
	
	public Vec4f(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4f(Vec3f v, float w)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.w = w;
	}
	
	public Vec4f sub(Vec4f v)
	{
		return new Vec4f(x - v.x, y - v.y, z - v.z, w - v.w);
	}
	
	public Vec3f toVec3f()
	{
		return new Vec3f(x,y,z);
	}
}
