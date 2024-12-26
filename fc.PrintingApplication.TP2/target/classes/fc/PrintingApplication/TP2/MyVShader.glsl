// version 420

uniform mat4 u_mvpMatrix;

in vec3 in_Position;

// (0, 1, 0, 1, -1, 1)
mat4 createOrtho(
	float l, float r,
	float b, float t,
	float zn, float zf)
{
	float tx = - (r+l) / (r-l);
	float ty = - (t+b) / (t-b);
	float tz = - (zf+zn) / (zf-zn);
	
	if (false)
		return mat4(
			vec4(2.0f / (r-l),            0,             0,          tx),
			vec4(           0, 2.0f / (t-b),             0,          ty),
			vec4(           0,            0, -2.0f/(zf-zn),          tz),
			vec4(           0,            0,             0,           1));
	else
		return mat4(
			vec4(2.0f / (r-l),            0,             0,           0),
			vec4(           0, 2.0f / (t-b),             0,           0),
			vec4(           0,            0, -2.0f/(zf-zn),           0),
			vec4(          tx,           ty,            tz,           1));
}

void main()
{
	//mat4 mvp = in_ModelViewProjectionMatrix;
	//mat4 mvp = createOrtho(0, 600, 0, 400, -1, 1);
	gl_Position = u_mvpMatrix * vec4(in_Position, 1);
}
