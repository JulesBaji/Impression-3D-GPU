// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class Vec2i
{
	public int x;
	public int y;
	
	public Vec2i(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vec2i mul(int d)
	{
		return new Vec2i(x*d, y*d);
	}
	
	public Vec2i add(Vec2i rhs)
	{
		return new Vec2i(x + rhs.x, y + rhs.y);
	}
	
	public Vec2i sub(Vec2i rhs)
	{
		return new Vec2i(x - rhs.x, y - rhs.y);
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y);
	}
	
	public int dot(Vec2i rhs)
	{
		return x*rhs.x + y*rhs.y;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Vec2i))
			return false;
		Vec2i rhs = (Vec2i)o;
		return rhs.x == x && rhs.y == y;
	}
}
