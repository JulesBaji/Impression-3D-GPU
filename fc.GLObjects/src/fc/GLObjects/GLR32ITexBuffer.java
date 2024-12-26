// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL30;

public class GLR32ITexBuffer extends GLTexBuffer
{
	public GLR32ITexBuffer(long size)
	{
		super(size, GL30.GL_R32I);
	}
	
	@Override
	public String getGLSLDeclaration(String uniformName)
	{
		String str = "coherent uniform " + getInternalFormatGLSLLayout() + " " + uniformName + ";\n";
		
		str += "ivec4 LOADIV(int a) { a*=4; return ivec4(imageLoad(" + uniformName + ", a).x, imageLoad(" + uniformName + ", (a)+1).x, imageLoad(" + uniformName + ", (a)+2).x, imageLoad(" + uniformName + ", (a)+3).x); }\n";
		str += "int LOADIVC(int a, int c) { a*=4; return imageLoad(" + uniformName + ", (a)+(c)).x; }\n";
		str += "void STOREIV(int a, ivec4 d) { a*=4; imageStore(" + uniformName + ", a, ivec4((d).x)); imageStore(" + uniformName + ", (a)+1, ivec4((d).y)); imageStore(" + uniformName + ", (a)+2, ivec4((d).z)); imageStore(" + uniformName + ", (a)+3, ivec4((d).w)); }\n";
		str += "void STOREIVC(int a, int c, int d) { a*=4; imageStore(" + uniformName + ", (a)+(c), ivec4(d)); }\n"; 
		str += "#define ATOMICIVCADD(a, c, d) imageAtomicAdd(" + uniformName + ", (a)*4+(c), d)\n";
		str += "#define ATOMICIVCCAS(a, c, d, e) imageAtomicCompSwap(" + uniformName + ", (a)*4+(c), d, e)\n";
		
		return str;
	}
}
