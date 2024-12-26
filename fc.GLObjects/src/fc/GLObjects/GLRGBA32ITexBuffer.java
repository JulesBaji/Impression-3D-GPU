// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL30;

public class GLRGBA32ITexBuffer extends GLTexBuffer
{
	public GLRGBA32ITexBuffer(long size)
	{
		super(size, GL30.GL_RGBA32I);
	}
	
	@Override
	public String getGLSLDeclaration(String uniformName)
	{
		String str = "coherent uniform " + getInternalFormatGLSLLayout() + " " + uniformName + ";\n";
		
		str += "#define LOADIV(a) imageLoad(" + uniformName + ", a)\n";
		str += "#define LOADIVC(a, c) imageLoad(" + uniformName + ", a)[c]\n";
		str += "#define STOREIV(a, d) imageStore(" + uniformName + ", a, d)\n";
		// Note: atomic operations are NOT supported with RGBA* image types
		// See:
		// http://stackoverflow.com/questions/37143129/imageatomic-glsl-functions-and-rgba-image-format
		
		return str;
	}
}
