// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Math;

import fc.ComponentModel.Dependency;

@Dependency(classes = { Quaternion.class })
public class Matrix
{
	private float _mat[][];
	
	public Matrix()
	{
		_mat = new float[4][4];
		
		for (int c=0; c < 4; c++)
		{
			for (int r=0; r < 4; r++)
			{
				_mat[r][c] = (r == c ? 1.0f : 0.0f);
			}
		}
	}
	
	public Matrix(Matrix rhs)
	{
		_mat = new float[4][4];
		
		for (int c=0; c < 4; c++)
		{
			for (int r=0; r < 4; r++)
			{
				_mat[r][c] = rhs.at(r, c);
			}
		}
	}
	
	public Matrix(float[][] val)
	{
		_mat = val;
	}
	
	// See toColumnMajorArray - this must match!
	public Matrix(float[] columnMajorValues)
	{
		_mat = new float[4][4];
		
		int index=0;
		for (int c=0; c < 4; c++)
		{
			for (int r=0; r < 4; r++)
			{
				_mat[r][c] = columnMajorValues[index];
				index++;
			}
		}
	}
	
	public static Matrix createTranslation(Vec3f trans)
	{
		Matrix m = new Matrix();
		m.set(0,3, trans.x);
		m.set(1,3, trans.y);
		m.set(2,3, trans.z);
		return m;
	}
	
	public Vec3f getTranslation()
	{
		return new Vec3f(at(0,3), at(1,3), at(2,3));
	}
	
	public Matrix transpose()
	{
		Matrix m = new Matrix();
		for (int c=0; c < 4; c++)
		{
			for (int r=0; r < 4; r++)
				m.set(c,r, at(r,c));
		}
		return m;
	}
	
	public float[] toColumnMajorArray()
	{
		// Returns a column-major array of values
		return new float[] {
			at(0,0), at(1,0), at(2,0), at(3,0), // 1st column
			at(0,1), at(1,1), at(2,1), at(3,1), // 2nd column  
			at(0,2), at(1,2), at(2,2), at(3,2), // 3rd column
			at(0,3), at(1,3), at(2,3), at(3,3)  // 4th column
		};
	}
	
	public float[] toRowMajorArray()
	{
		// Returns a row-major array of values
		return new float[] {
			at(0,0), at(0,1), at(0,2), at(0,3),
			at(1,0), at(1,1), at(1,2), at(1,3),  
			at(2,0), at(2,1), at(2,2), at(2,3),
			at(3,0), at(3,1), at(3,2), at(3,3)
		};
	}
	
	public float at(int r, int c)
	{
		return _mat[r][c];
	}
	
	public void set(int r, int c, float value)
	{
		_mat[r][c] = value;
	}
	
	public static Matrix fromQuaternion(Quaternion q)
	{
	    float length2 = q.length2();

    	float rlength2;
    	// normalize quat if required.
    	// We can avoid the expensive sqrt in this case since all 'coefficients' below are products of two q components.
    	// That is a square of a square root, so it is possible to avoid that
    	if (length2 != 1.0f)
    	{
    		rlength2 = 2.0f/length2;
    	}
    	else
    	{
    		rlength2 = 2.0f;
    	}

    	// Source: Gamasutra, Rotating Objects Using Quaternions
    	//
    	//http://www.gamasutra.com/features/19980703/quaternions_01.htm

    	float wx, wy, wz, xx, yy, yz, xy, xz, zz, x2, y2, z2;
    	
    	float QX = q.x;
    	float QY = q.y;
    	float QZ = q.z;
    	float QW = q.w;
    			
    	// calculate coefficients
    	x2 = rlength2*QX;
    	y2 = rlength2*QY;
    	z2 = rlength2*QZ;

    	xx = QX * x2;
    	xy = QX * y2;
    	xz = QX * z2;

    	yy = QY * y2;
    	yz = QY * z2;
    	zz = QZ * z2;

    	wx = QW * x2;
    	wy = QW * y2;
    	wz = QW * z2;

    	// Note.  Gamasutra gets the matrix assignments inverted, resulting
    	// in left-handed rotations, which is contrary to OpenGL and OSG's
    	// methodology.  The matrix assignment has been altered in the next
    	// few lines of code to do the right thing.
    	// Don Burns - Oct 13, 2001
    	
    	Matrix m = new Matrix();
    	
    	m.set(0,0, 1.0f - (yy + zz));
    	m.set(0,1, xy - wz);
    	m.set(0,2, xz + wy);

    	m.set(1,0, xy + wz);
    	m.set(1,1, 1.0f - (xx + zz));
    	m.set(1,2, yz - wx);

    	m.set(2,0, xz - wy);
    	m.set(2,1, yz + wx);
    	m.set(2,2, 1.0f - (xx + yy));
    	
    	return m;
	}
	
	public Matrix mul(Matrix rhs)
	{
		Matrix m = new Matrix();
		for (int col=0; col < 4; col++)
		{
			for (int row=0; row < 4; row++)
			{
				float v = 0.0f;
				for (int i=0; i < 4; i++)
					v += at(row,i)*rhs.at(i,col);
				m.set(row, col, v);
			}
		}
		return m;
	}
	
	/*public Vector mul(Vector v)
	{
	    double d = 1.0/(_mat[3][0]*v.x+_mat[3][1]*v.y+_mat[3][2]*v.z+_mat[3][3]);
	    return new Vector(
	    		(_mat[0][0]*v.x + _mat[0][1]*v.y + _mat[0][2]*v.z + _mat[0][3])*d,
	            (_mat[1][0]*v.x + _mat[1][1]*v.y + _mat[1][2]*v.z + _mat[1][3])*d,
	            (_mat[2][0]*v.x + _mat[2][1]*v.y + _mat[2][2]*v.z + _mat[2][3])*d) ;
	}*/
	 
    // http://stackoverflow.com/questions/13166135/how-does-glulookat-work
    public static Matrix gluLookAt(
    		Vec3f eye,
    		Vec3f center,
    		Vec3f up)
    {
    	Vec3f forward = center.sub(eye);

        forward = forward.norm();

        /* Side = forward x up */
        Vec3f side = forward.cross(up);
        side = side.norm();

        /* Recompute up as: up = side x forward */
        up = side.cross(forward);

        // Load identity
        Matrix m = new Matrix();
        if (true)
        {
	        m.set(0,0, side.x);
	        m.set(0,1, side.y);
	        m.set(0,2, side.z);
	        m.set(1,0, up.x);
	        m.set(1,1, up.y);
	        m.set(1,2, up.z);
	        m.set(2,0, -forward.x);
	        m.set(2,1, -forward.y);
	        m.set(2,2, -forward.z);
        }
        else
        {
	        m.set(0,0, side.x);
	        m.set(1,0, side.y);
	        m.set(2,0, side.z);
	        m.set(0,1, up.x);
	        m.set(1,1, up.y);
	        m.set(2,1, up.z);
	        m.set(0,2, -forward.x);
	        m.set(1,2, -forward.y);
	        m.set(2,2, -forward.z);
        }
    	//m.glhTranslate2f(-eye.x, -eye.y, -eye.z);
        for (int i = 0; i < 3; ++i)
        {
            float tmp = eye.mul(-1).getCoord(i);
            //if (tmp == 0)
            //    continue;
            if (true)
            {
	            m.set(0, 3, m.at(0, 3) + tmp*m.at(0, i));
	            m.set(1, 3, m.at(1, 3) + tmp*m.at(1, i));
	            m.set(2, 3, m.at(2, 3) + tmp*m.at(2, i));
	            m.set(3, 3, m.at(3, 3) + tmp*m.at(3, i));
            }
            else
            {
	            m.set(3, 0, m.at(3, 0) + tmp*m.at(i, 0));
	            m.set(3, 1, m.at(3, 1) + tmp*m.at(i, 1));
	            m.set(3, 2, m.at(3, 2) + tmp*m.at(i, 2));
	            m.set(3, 3, m.at(3, 3) + tmp*m.at(i, 3));
            }
        }
        return m;
    }
    
    public void glhTranslate2f(float x, float y, float z)
    {
    	/*matrix[12]=matrix[0]*x+matrix[4]*y+matrix[8]*z+matrix[12];
    	matrix[13]=matrix[1]*x+matrix[5]*y+matrix[9]*z+matrix[13];
    	matrix[14]=matrix[2]*x+matrix[6]*y+matrix[10]*z+matrix[14];
    	matrix[15]=matrix[3]*x+matrix[7]*y+matrix[11]*z+matrix[15];*/
    }
	
	public static Matrix gluPerspective(
			float fovyInDegrees,
			float aspect,
			float znear, float zfar)
	{
		Matrix m = new Matrix();
		float ymax = znear * (float)Math.tan(fovyInDegrees * Math.PI / 360.0f);
		  float ymin = -ymax;
		  float xmax = ymax * aspect;
		  float xmin = ymin * aspect;

		  float width = xmax - xmin;
		  float height = ymax - ymin;

		  float depth = zfar - znear;
		  float q = -(zfar + znear) / depth;
		  float qn = -2 * (zfar * znear) / depth;

		  float w = 2 * znear / width;
		  w = w / aspect;
		  float h = 2 * znear / height;

		  m.set(0,0,w);
		  m.set(0,1,0);
		  m.set(0,2,0);
		  m.set(0,3,0);

		  m.set(1,0,0);
		  m.set(1,1,h);
		  m.set(1,2,0);
		  m.set(1,3,0);

		  m.set(2,0,0);
		  m.set(2,1,0);
		  m.set(2,2,q);
		  m.set(2,3,-1);

		  m.set(3,0,0);
		  m.set(3,1,0);
		  m.set(3,2,qn);
		  m.set(3,3,0);
		  
		  return  m;
	}

	// https://github.com/openscenegraph/OpenSceneGraph/blob/master/src/osg/Matrix_implementation.cpp
    public static Matrix createOrtho(
    		float l, float r,
    		float b, float t,
    		float zn, float zf)
    {
    	float tx = - (r+l) / (r-l);
    	float ty = - (t+b) / (t-b);
    	float tz = - (zf+zn) / (zf-zn);
    	
    	Matrix m = new Matrix(new float[][]{
    		{2.0f / (r-l),            0,             0,          tx},
    		{           0, 2.0f / (t-b),             0,          ty},
    		{           0,            0, -2.0f/(zf-zn),          tz},
    		{           0,            0,             0,           1}});
    	
    	return m;
    }
	
	public static Matrix glhPerspectivef2(
			float fovyInDegrees,
			float aspectRatio,
			float znear, float zfar)
	{
		Matrix matrix = new Matrix();
		float ymax, xmax;
		ymax = znear * (float)Math.tan(fovyInDegrees * (float)Math.PI / 360.0f);
		//ymin = -ymax;
		//xmin = -ymax * aspectRatio;
		xmax = ymax * aspectRatio;
		glhFrustumf2(matrix, -xmax, xmax, -ymax, ymax, znear, zfar);
		return matrix;
	}
	
	private static void glhFrustumf2(
			Matrix matrix,
			float left, float right,
			float bottom, float top,
			float znear, float zfar)
	{
		float temp, temp2, temp3, temp4;
		temp = 2.0f * znear;
		temp2 = right - left;
		temp3 = top - bottom;
		temp4 = zfar - znear;
		
		matrix.set(0,0, temp / temp2);
		matrix.set(1,0, 0.0f);
		matrix.set(2,0, 0.0f);
		matrix.set(3,0, 0.0f);
		matrix.set(0,1, 0.0f);
		matrix.set(1,1, temp / temp3);
		matrix.set(2,1, 0.0f);
		matrix.set(3,1, 0.0f);
		matrix.set(0,2, (right + left) / temp2);
		matrix.set(1,2, (top + bottom) / temp3);
		matrix.set(2,2, (-zfar - znear) / temp4);
		matrix.set(3,2, -1.0f);
		matrix.set(0,3, 0.0f);
		matrix.set(1,3, 0.0f);
		matrix.set(2,3, (-temp * zfar) / temp4);
		matrix.set(3,3, 0.0f);
	}
	
	public Vec4f mul(Vec4f v)
	{
		return new Vec4f(
				at(0, 0)*v.x + at(0, 1)*v.y + at(0, 2)*v.z + at(0, 3)*v.w,    
				at(1, 0)*v.x + at(1, 1)*v.y + at(1, 2)*v.z + at(1, 3)*v.w,    
				at(2, 0)*v.x + at(2, 1)*v.y + at(2, 2)*v.z + at(2, 3)*v.w,    
				at(3, 0)*v.x + at(3, 1)*v.y + at(3, 2)*v.z + at(3, 3)*v.w);    
	}

	public Matrix invert()
	{
	    int[] indxc = new int[4];
	    int[] indxr = new int[4];
	    int[] ipiv = new int[4];
	    int i,j,k,l,ll;
	    int icol = 0;
	    int irow = 0;
	    float temp, pivinv, dum, big;
	
	    Matrix inv = new Matrix();
	    // copy in place this may be unnecessary
		for (int c=0; c < 4; c++)
		{
			for (int r=0; r < 4; r++)
			{
				inv.set(r, c, at(r,c));
			}
		}
	
	    for (j=0; j<4; j++)
	    	ipiv[j]=0;
	
	    for(i=0;i<4;i++)
	    {
	       big=0.0f;
	       for (j=0; j<4; j++)
	          if (ipiv[j] != 1)
	             for (k=0; k<4; k++)
	             {
	                if (ipiv[k] == 0)
	                {
	                   if (Math.abs(at(j,k)) >= big)
	                   {
	                      big = Math.abs(at(j,k));
	                      irow=j;
	                      icol=k;
	                   }
	                }
	                else if (ipiv[k] > 1)
	                   return null;
	             }
	       (ipiv[icol])++;
	       if (irow != icol)
	          for (l=0; l<4; l++)
	          {
	        	  //SGL_SWAP(at(irow,l), at(icol,l), temp);
	        	  temp = inv.at(irow,l);
	        	  inv.set(irow,l, inv.at(icol,l));
	        	  inv.set(icol,l, temp);
	          }
	
	       indxr[i]=irow;
	       indxc[i]=icol;
	       if (inv.at(icol,icol) == 0)
	          return null;
	
	       pivinv = 1.0f/inv.at(icol,icol);
	       inv.set(icol,icol, 1);
	       for (l=0; l<4; l++) inv.set(icol,l, inv.at(icol,l) * pivinv);
	       for (ll=0; ll<4; ll++)
	          if (ll != icol)
	          {
	             dum=inv.at(ll,icol);
	             inv.set(ll,icol, 0.0f);
	             for (l=0; l<4; l++) inv.set(ll,l, inv.at(ll,l) - inv.at(icol,l)*dum);
	          }
	    }
	    for (int lx=4; lx>0; --lx)
	    {
	       if (indxr[lx-1] != indxc[lx-1])
	       {
	          for (k=0; k<4; k++)
	          {
	        	  //SGL_SWAP(at(k,indxr[lx-1]), at(k,indxc[lx-1]),temp);
	        	  temp = inv.at(k,indxr[lx-1]);
	        	  inv.set(k,indxr[lx-1], inv.at(k,indxc[lx-1]));
	        	  inv.set(k,indxc[lx-1], temp);
	          }
	       }
	    }
	
	    return inv;
	}
	
	// Returns left right bottom top zNear zFar
	// https://github.com/openscenegraph/OpenSceneGraph/blob/master/src/osg/Matrix_implementation.cpp
	public float[] getProjectionAsOrtho()
	{
	    if (at(0,3)!=0.0f || at(1,3)!=0.0f || at(2,3)!=0.0f || at(3,3)!=1.0f)
	    	return null;

	    float zNear = (at(3,2)+1.0f) / at(2,2);
	    float zFar = (at(3,2)-1.0f) / at(2,2);

	    float left = -(1.0f+at(3,0)) / at(0,0);
	    float right = (1.0f-at(3,0)) / at(0,0);

	    float bottom = -(1.0f+at(3,1)) / at(1,1);
	    float top = (1.0f-at(3,1)) / at(1,1);

	    return new float[]{left, right, bottom, top, zNear, zFar};
	}
	
	// Tested - fine
	public float[] getProjectionAsFrustrum()
	{
	    float temp_near = at(2,3) / (at(2,2)-1.0f);
	    float temp_far = at(2,3) / (1.0f+at(2,2));

	    float nLeft = temp_near * (at(0,2)-1.0f) / at(0,0);
	    float nRight = temp_near * (1.0f+at(0,2)) / at(0,0);
	    float nTop = temp_near * (1.0f+at(1,2)) / at(1,1);
	    float nBottom = temp_near * (at(1,2)-1.0f) / at(1,1);

	    float fLeft = temp_far * (at(0,2)-1.0f) / at(0,0);
	    float fRight = temp_far * (1.0f+at(0,2)) / at(0,0);
	    float fTop = temp_far * (1.0f+at(1,2)) / at(1,1);
	    float fBottom = temp_far * (at(1,2)-1.0f) / at(1,1);

	    float zNear = temp_near;
	    float zFar = temp_far;

	    return new float[]{nLeft, nRight, nBottom, nTop, fLeft, fRight, fBottom, fTop, zNear, zFar};
	}
	
	// See Matrix_implementation::getPerspective
	public float getFovY()
	{
		float[] f = getProjectionAsFrustrum();
		float bottom = f[2];
		float top = f[3];
		float znear = f[8];
		return (180.0f / (float)Math.PI) * ((float)Math.atan(top/znear) - (float)Math.atan(bottom/znear)); // radians to degrees
	}
	
	public float getAspectRatio()
	{
		float[] f = getProjectionAsFrustrum();
		float left = f[0];
		float right = f[1];
		float bottom = f[2];
		float top = f[3];
        return (right-left)/(top-bottom);
	}
}
