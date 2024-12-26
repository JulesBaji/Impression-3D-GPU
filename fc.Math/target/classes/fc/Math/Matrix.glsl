// Matrix.glsl

// include "fc.Math.Quaternion"

struct Matrix
{
	vec4[4] col; // ne marche pas avec mat4. Le compilateur de nVidia ne supporte pas mat4 en inout dans les arguments de fonction
};

mat4 Matrix_getMat4(Matrix m)
{
	return mat4(m.col[0], m.col[1], m.col[2], m.col[3]);
}

// define Matrix_set(m, r, c, val) m.col[r][c] = val
// Attention, ne marche pas!:
void Matrix_set(inout Matrix m, int r, int c, float val)
{
	m.col[r][c] = val;
}

Matrix Matrix_fromQuaternion(Quaternion q)
{
	float length2 = Quaternion_length2(q);

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
	
	float QX = q.quat.x;
	float QY = q.quat.y;
	float QZ = q.quat.z;
	float QW = q.quat.w;
			
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
	
	Matrix m = Matrix(vec4[](vec4(1,0,0,0), vec4(0,1,0,0), vec4(0,0,1,0), vec4(0,0,0,1))); // set to identity
/*
	Matrix_set(m, 0,0, 1.0f - (yy + zz));
	Matrix_set(m, 0,1, xy - wz);
	Matrix_set(m, 0,2, xz + wy);

	Matrix_set(m, 1,0, xy + wz);
	Matrix_set(m, 1,1, 1.0f - (xx + zz));
	Matrix_set(m, 1,2, yz - wx);

	Matrix_set(m, 2,0, xz - wy);
	Matrix_set(m, 2,1, yz + wx);
	Matrix_set(m, 2,2, 1.0f - (xx + yy));
*/

	Matrix_set(m, 0,0, 1.0f - (yy + zz));
	Matrix_set(m, 1,0, xy - wz);
	Matrix_set(m, 2,0, xz + wy);

	Matrix_set(m, 0,1, xy + wz);
	Matrix_set(m, 1,1, 1.0f - (xx + zz));
	Matrix_set(m, 2,1, yz - wx);

	Matrix_set(m, 0,2, xz - wy);
	Matrix_set(m, 1,2, yz + wx);
	Matrix_set(m, 2,2, 1.0f - (xx + yy));

	return m;
}
