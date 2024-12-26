// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Vec3f
{
	public float x;
	public float y;
	public float z;
	
	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f(float[] xyz)
	{
		this.x = xyz[0];
		this.y = xyz[1];
		this.z = xyz[2];
	}
	
	public Vec3f(Vec3f v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public void set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f mul(float k)
	{
		return new Vec3f(k*x, k*y, k*z);
	}
	
	public Vec3f neg()
	{
		return new Vec3f(-x, -y, -z);
	}
	
	public Vec3f mul(Vec3f v)
	{
		return new Vec3f(v.x*x, v.y*y, v.z*z);
	}
	
	public Vec3f div(Vec3f v)
	{
		return new Vec3f(x/v.x, y/v.y, z/v.z);
	}
	
	public Vec3f div(float k)
	{
		return new Vec3f(x/k, y/k, z/k);
	}
	
	public Vec3f add(Vec3f v2)
	{
		return new Vec3f(x + v2.x, y + v2.y, z + v2.z);
	}
	
	public Vec3f sub(Vec3f v2)
	{
		return new Vec3f(x - v2.x, y - v2.y, z - v2.z);
	}
	
	public void subtract(Vec3f v2)
	{
		x = x - v2.x;
		y = y - v2.y;
		z = z - v2.z;
	}
	
	public void sub(Vec3f v1, Vec3f v2)
	{
		set(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
	}
	
	public float dot(Vec3f v2)
	{
		return x*v2.x + y*v2.y + z*v2.z;
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vec3f norm()
	{
		float length = length();
		if (length != 0.0)
			return this.div(length); 
		else
			return this;
	}
	
	public Vec3f cross(Vec3f v2)
	{
		return new Vec3f(
				y * v2.z - z * v2.y,
				z * v2.x - x * v2.z,
				x * v2.y - y * v2.x);
	}
	
	public Vec3f clamp(float min, float max)
	{
		Vec3f v = new Vec3f(this);
		
		for (int i=0; i < 3; i++)
		{
			float val = v.getCoord(i);
			if (val < min)
				val = min;
			else if (val > max)
				val = max;
			v.setCoord(i, val);
		}
		
		return v;
	}
	
	public float getCoord(int n)
	{
		if (n == 0)
			return x;
		else if (n == 1)
			return y;
		else if (n == 2)
			return z;
		else
			throw new NullPointerException("Invalid Vec3f coordinate number " + n);
	}
	
	public void setCoord(int n, float val)
	{
		if (n == 0)
			x = val;
		else if (n == 1)
			y = val;
		else if (n == 2)
			z = val;
		else
			throw new NullPointerException("Invalid Vec3f coordinate number " + n);
	}
	
	public int getMaxDim()
	{
		float maxVal = Float.NEGATIVE_INFINITY;
		int maxValCoord = -1;
		
		for (int i=0; i < 3; i++)
		{
			float c = getCoord(i);
			c = Math.abs(c);
			if (c > maxVal)
			{
				maxVal = c;
				maxValCoord = i;
			}
		}
		
		return maxValCoord;
	}
	
	public float[] toArray()
	{
		return new float[] { x, y, z };
	}
	
	@Override
	public int hashCode()
	{
		return (int)x;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Vec3f))
			return false;
		Vec3f rhs = (Vec3f)o;
		return rhs.x == x && rhs.y == y && rhs.z == z;
	}
}
