// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

import fc.ComponentModel.Dependency;
import fc.Serialization.ISerializable;
import fc.Serialization.ISerializer;

@Dependency(classes = { Ray.class })
public class AABB implements ISerializable
{
	private Vec3f m_Min;
	private Vec3f m_Max;
	
	public AABB()
	{
		m_Min = new Vec3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		m_Max = new Vec3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}
	
	public AABB(AABB rhs)
	{
		m_Min = new Vec3f(rhs.getMin());
		m_Max = new Vec3f(rhs.getMax());
	}
	
	public AABB(Vec3f min, Vec3f max)
	{
		m_Min = min;
		m_Max = max;
	}
	
	@Override
	public void serialize(ISerializer buffer)
	{
		buffer.serialize(m_Min.toArray());
		buffer.serialize(0);
		buffer.serialize(m_Max.toArray());
		buffer.serialize(0);
	}
	
	@Override
	public void deserialize(ISerializer buffer)
	{
		m_Min = new Vec3f(buffer.deserializeFloatArray(3));
		buffer.deserializeInt();
		m_Max = new Vec3f(buffer.deserializeFloatArray(3));
		buffer.deserializeInt();
	}
	
	public void setEmpty()
	{
		m_Min = new Vec3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		m_Max = new Vec3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}
	
	public Vec3f getMin()
	{
		return m_Min;
	}
	
	public Vec3f getMax()
	{
		return m_Max;
	}
	
	public void setMinMax(Vec3f min, Vec3f max)
	{
		m_Min = min;
		m_Max = max;
	}
	
	public void enlarge(Vec3f v)
	{
		m_Min.x = Math.min(m_Min.x, v.x);
		m_Min.y = Math.min(m_Min.y, v.y);
		m_Min.z = Math.min(m_Min.z, v.z);
		
		m_Max.x = Math.max(m_Max.x, v.x);
		m_Max.y = Math.max(m_Max.y, v.y);
		m_Max.z = Math.max(m_Max.z, v.z);
	}
	
	public void enlarge(AABB rhs)
	{
		enlarge(rhs.getMin());
		enlarge(rhs.getMax());
	}
	
	public void addMargin(Vec3f margin)
	{
		m_Min = m_Min.sub(margin);
		m_Max = m_Max.add(margin);
	}
	
	public AABB grow(AABB rhs)
	{
		//AABB box = new AABB(new Vec3f(getMin()), new Vec3f(getMax()));
		
		this.enlarge(rhs.getMin());
		this.enlarge(rhs.getMax());
		
		return this; //box;
	}
	
	public Vec3f getCenter()
	{
		return m_Max.add(m_Min).div(2.0f);
	}
	
	public Vec3f getBoxHalfSize()
	{
		return m_Max.sub(getCenter());
	}
	
	public float getSurfaceArea()
	{
		Vec3f s = getMax().sub(getMin());
		return (s.x*s.y + s.x*s.z + s.y*s.z) * 2.0f;
	}
	
	//
	// See:
	// http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection
	// Attention, le papier ci-dessous n'est PAS implemente dans le code ici.
	// Robust BVH Ray Traversal, Thiago Ize
	// http://jcgt.org/published/0002/02/02/paper.pdf
	// A noter que cette problematique est differente de celle concernant l'intersection rayon/triangle (voir classe Triangle):
	// Watertight Ray/Triangle Intersection, Woop et al. (2013)
	// http://jcgt.org/published/0002/01/05/paper.pdf
	//
	public boolean intersectRay(Ray r, float[] tminmax) // t : array of 2 floats containing min and max of Ray parameter value if intersection is found. Can be null.
	{
		float tmin = (m_Min.x - r.start.x) / r.dir.x; 
		float tmax = (m_Max.x - r.start.x) / r.dir.x;
		
		float temp;
		 
		if (tmin > tmax)
		{
			// Swap tmin and tmax
			temp = tmin;
			tmin = tmax;
			tmax = temp;
		}
		 
		float tymin = (m_Min.y - r.start.y) / r.dir.y; 
		float tymax = (m_Max.y - r.start.y) / r.dir.y; 
		 
		if (tymin > tymax)
		{
			// Swap tymin and tymax
			temp = tymin;
			tymin = tymax;
			tymax = temp;
		}
		 
		if ((tmin > tymax) || (tymin > tmax)) 
			return false; 
		 
		if (tymin > tmin) 
			tmin = tymin; 
		 
		if (tymax < tmax) 
			tmax = tymax; 
		 
		float tzmin = (m_Min.z - r.start.z) / r.dir.z; 
		float tzmax = (m_Max.z - r.start.z) / r.dir.z; 
		 
		if (tzmin > tzmax)
		{
			// Swap tzmin and tzmax
			temp = tzmin;
			tzmin = tzmax;
			tzmax = temp;
		}
		 
		if ((tmin > tzmax) || (tzmin > tmax)) 
			return false; 
		 
		if (tzmin > tmin) 
			tmin = tzmin; 
		 
		if (tzmax < tmax) 
			tmax = tzmax;
		
		if (tminmax != null)
		{
			tminmax[0] = tmin;
			tminmax[1] = tmax;
		}
		 
		return true; 
	}
	
	public boolean intersectRay_(Ray ray, float[] tminmax) //, float minDist, float maxDist)
	{
		Vec3f min = m_Min;
		Vec3f max = m_Max;
        Vec3f invDir = new Vec3f(1f / ray.dir.x, 1f / ray.dir.y, 1f / ray.dir.z);

        boolean signDirX = invDir.x < 0;
        boolean signDirY = invDir.y < 0;
        boolean signDirZ = invDir.z < 0;

        Vec3f bbox = signDirX ? max : min;
        double tmin = (bbox.x - ray.start.x) * invDir.x;
        bbox = signDirX ? min : max;
        double tmax = (bbox.x - ray.start.x) * invDir.x;
        bbox = signDirY ? max : min;
        double tymin = (bbox.y - ray.start.y) * invDir.y;
        bbox = signDirY ? min : max;
        double tymax = (bbox.y - ray.start.y) * invDir.y;

        if ((tmin > tymax) || (tymin > tmax)) {
            return false;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }

        bbox = signDirZ ? max : min;
        double tzmin = (bbox.z - ray.start.z) * invDir.z;
        bbox = signDirZ ? min : max;
        double tzmax = (bbox.z - ray.start.z) * invDir.z;

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return false;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        //if ((tmin < maxDist) && (tmax > minDist)) {
            return true; //ray.start.add(ray.dir.mul((float)tmin)); //.getPointAtDistance(tmin);
        //}
        //return null;
    }
	
	@Override
	public int hashCode()
	{
		return (int)m_Min.x;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof AABB))
			return false;
		AABB rhs = (AABB)o;
		return rhs.getMin().equals(m_Min) && rhs.getMax().equals(m_Max);
	}
}
