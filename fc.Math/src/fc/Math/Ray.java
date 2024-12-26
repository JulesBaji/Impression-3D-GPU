// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

import fc.Serialization.ISerializable;
import fc.Serialization.ISerializer;

public class Ray implements ISerializable
{
	public Vec3f start;
	public Vec3f dir;
	
	public Ray(Vec3f start, Vec3f dir)
	{
		this.start = start;
		this.dir = dir;
	}
	
	@Override
	public void serialize(ISerializer buffer)
	{
		buffer.serialize(start.toArray());
		buffer.serialize(0);
		buffer.serialize(dir.toArray());
		buffer.serialize(0);
	}
	
	@Override
	public void deserialize(ISerializer buffer)
	{
		start = new Vec3f(buffer.deserializeFloatArray(3));
		buffer.deserializeInt();
		dir = new Vec3f(buffer.deserializeFloatArray(3));
		buffer.deserializeInt();
	}
}
