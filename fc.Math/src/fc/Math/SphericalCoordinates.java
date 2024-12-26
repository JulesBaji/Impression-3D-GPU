// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

public class SphericalCoordinates
{
	public static Vec3f getCoordinates(float theta, float phi)
	{
		Vec3f wi = new Vec3f(
				(float)(Math.sin(theta)*Math.cos(phi)),
				(float)(Math.sin(theta)*Math.sin(phi)),
				(float)(Math.cos(theta)));
		
		return wi;
	}
	
	//
	// https://en.wikipedia.org/wiki/Spherical_coordinate_system#Cartesian_coordinates
	//
	public static Vec3f getSphericalCoordinates(/*in*/ Vec3f v)
	{
		float r = v.length();
		float theta = (float)Math.acos(v.z / r);
		float phi = (float)Math.atan2(v.y, v.x);
		
		return new Vec3f(theta, phi, r);
	}
}
