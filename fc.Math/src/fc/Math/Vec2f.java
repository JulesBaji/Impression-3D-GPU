// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Vec2f
{
	public float x;
	public float y;
	
	public Vec2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vec2f mul(float d)
	{
		return new Vec2f(x*d, y*d);
	}
	
	public Vec2f add(Vec2f rhs)
	{
		return new Vec2f(x + rhs.x, y + rhs.y);
	}
	
	public Vec2f sub(Vec2f rhs)
	{
		return new Vec2f(x - rhs.x, y - rhs.y);
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y);
	}
	
	public float lengthSquared()
	{
		return (x*x + y*y);
	}
	
	public float dot(Vec2f rhs)
	{
		return x*rhs.x + y*rhs.y;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Vec2f))
			return false;
		Vec2f rhs = (Vec2f)o;
		return rhs.x == x && rhs.y == y;
	}
}
