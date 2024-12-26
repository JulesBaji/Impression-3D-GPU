// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Vec3d
{
	public double x;
	public double y;
	public double z;
	
	public Vec3d(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3d mul(double k)
	{
		return new Vec3d(k*x, k*y, k*z);
	}
	
	public Vec3d div(double k)
	{
		return new Vec3d(x/k, y/k, z/k);
	}
	
	public Vec3d add(Vec3d v2)
	{
		return new Vec3d(x + v2.x, y + v2.y, z + v2.z);
	}
	
	public Vec3d sub(Vec3d v2)
	{
		return new Vec3d(x - v2.x, y - v2.y, z - v2.z);
	}
	
	public double dot(Vec3d v2)
	{
		return x*v2.x + y*v2.y + z*v2.z;
	}
	
	public double length()
	{
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vec3d norm()
	{
		double length = length();
		if (length != 0.0)
			return this.div(length); 
		else
			return this;
	}
	
	public Vec3d cross(Vec3d v2)
	{
		return new Vec3d(
				y * v2.z - z * v2.y,
				z * v2.x - x * v2.z,
				x * v2.y - y * v2.x);
	}
}
