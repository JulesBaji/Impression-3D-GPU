// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class SlopeCoordinates
{
	public static Vec3f toCartesian(/*in*/ Vec2f v)
	{
	    float norm = (float)Math.sqrt(v.x*v.x + v.y*v.y + 1.0f);
	    return new Vec3f(-v.x/norm, -v.y/norm, 1.0f/norm);
	}
	
	public static Vec2f fromCartesian(/*in*/ Vec3f v) // input vector MUST be normalized before calling the method
	{
		return new Vec2f(-v.x/v.z, -v.y/v.z);
	}
	
	public static Vec2f fromSpherical(Vec3f sphericalCoords) // input spherical coords MUST be normalized, eg. sphericalCoords.z == 1.0f
	{
		float wh_x = -(float)Math.tan(sphericalCoords.x)*(float)Math.cos(sphericalCoords.y);
		float wh_y = -(float)Math.tan(sphericalCoords.x)*(float)Math.sin(sphericalCoords.y);
		
		return new Vec2f(wh_x, wh_y);
	}
}
