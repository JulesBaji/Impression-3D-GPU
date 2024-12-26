// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

import fc.Serialization.ISerializer;

public class RayDifferential extends Ray
{
	public Vec3f xStart;
	public Vec3f xDir;
	public Vec3f yStart;
	public Vec3f yDir;
	
	public RayDifferential(Vec3f start, Vec3f dir, Vec3f xStart, Vec3f xDir, Vec3f yStart, Vec3f yDir)
	{
		super(start, dir);
		
		this.xStart = xStart;
		this.xDir = xDir;
		this.yStart = yStart;
		this.yDir = yDir;
	}
	
	@Override
	public void serialize(ISerializer buffer)
	{
		super.serialize(buffer);
		
		buffer.serialize(xStart.toArray()); buffer.serialize(0);
		buffer.serialize(xDir.toArray()); buffer.serialize(0);
		buffer.serialize(yStart.toArray()); buffer.serialize(0);
		buffer.serialize(yDir.toArray()); buffer.serialize(0);
	}
	
	@Override
	public void deserialize(ISerializer buffer)
	{
		super.deserialize(buffer);
		
		xStart = new Vec3f(buffer.deserializeFloatArray(3)); buffer.deserializeInt();
		xDir = new Vec3f(buffer.deserializeFloatArray(3)); buffer.deserializeInt();
		yStart = new Vec3f(buffer.deserializeFloatArray(3)); buffer.deserializeInt();
		yDir = new Vec3f(buffer.deserializeFloatArray(3)); buffer.deserializeInt();
	}
}
