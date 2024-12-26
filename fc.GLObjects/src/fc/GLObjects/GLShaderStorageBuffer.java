// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

public class GLShaderStorageBuffer extends GLBuffer
{
	public GLShaderStorageBuffer(long size)
	{
		super(size, GL43.GL_SHADER_STORAGE_BUFFER);
		
		int[] maxSize = new int[1];
		GL11.glGetIntegerv(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE, maxSize);
		int[] maxTBSize = new int[1];
		GL11.glGetIntegerv(GL43.GL_MAX_TEXTURE_BUFFER_SIZE, maxTBSize);
		
		// Intel drivers probably do not report the correct size for SSBOs ('maxSize')
		// They report 128MB, which seems obviously too low.
		// Therefore, also take into account GL_MAX_TEXTURE_BUFFER_SIZE, multiplied by 16
		// (1 vec4 = 16 bytes)
		long maxEstimatedBufferSize = Math.max((long)maxSize[0], 16L*(long)maxTBSize[0]);
		
		//
		// For some reason, it is possible to allocate
		// GL_MAX_TEXTURE_BUFFER_SIZE-16 bytes on Intel, (2G-16 bytes)
		// but not GL_MAX_TEXTURE_BUFFER_SIZE bytes! (2G bytes)
		// On nVidia, as reported by the GLShaderStorageBufferStorageTests.java in the fc.Test project,
		// weirdly, it is possible to allocate and read a 3GB buffer (but not a 5GB buffer),
		// despite the fact that GL_MAX_SHADER_STORAGE_BLOCK_SIZE is 2GB as well.
		//
		maxEstimatedBufferSize -= 16L;
		
		if (size > maxEstimatedBufferSize)
			throw new IllegalStateException("Cannot allocate GPU buffer of requested size " + size + " bytes. Estimated maximum size: " + maxEstimatedBufferSize);
	}
	
	// Pour un binding dynamic voir:
	// http://www.geeks3d.com/20140704/tutorial-introduction-to-opengl-4-3-shader-storage-buffers-objects-ssbo-demo/
	public void bind(GLResourceContext rc)
	{
		// TODO: add reserveSSBOBinding and freeSSBOBinding
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, /* binding point TODO: change this to have something dynamic */ 0, m_Id);
		GLError.check("Could not bind shader storage buffer " + m_Id);
	}
	
	public void unbind(GLResourceContext rc)
	{
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
		GLError.check("Could not unbind shader storage buffer");
	}
	
	public static String getGLSLDeclaration(String name)
	{
		// TODO: use std140 instead? (packing considerations...)
		String str = "layout(std430, binding=0) coherent restrict buffer PixelBuffer { ivec4 data[]; } " + name + ";\n";
		
		str += "ivec4 LOADIV(int a) { return isLocal(a) ? TLS_LOADIV(a) : " + name + ".data[a]; }\n";
		str += "void STOREIV(int a, ivec4 d) { if (isLocal(a)) TLS_STOREIV(a, d); else " + name + ".data[a] = d; }\n";
		str += "void STOREIVC(int a, int c, int d) { if (isLocal(a)) TLS_STOREIVC(a, c, d); else " + name + ".data[a][c] = d; }\n";
		str += "int ATOMICIVCADD(int a, int c, int d) { return atomicAdd(" + name + ".data[a][c], d); }\n";
		//str += "#define ATOMICIVCADD(a, c, d) atomicAdd(" + name + ".data[a][c], d)\n";

		// Generates a compilation error on nVidia driver versions >= 465.89
		//str += "int ATOMICIVCCAS(int a, int c, int d, int e) { return atomicCompSwap(" + name + ".data[a][c%4], uint(d), uint(e)); }\n";
		str += "int ATOMICIVCCAS(int a, int c, int d, int e) {\n" +
				"	if (c == 0) return atomicCompSwap(" + name + ".data[a].x, d, e);\n" +
				"	if (c == 1) return atomicCompSwap(" + name + ".data[a].y, d, e);\n" +
				"	if (c == 2) return atomicCompSwap(" + name + ".data[a].z, d, e);\n" +
				"	return atomicCompSwap(" + name + ".data[a].w, d, e);\n" +
			   "}\n";

		//str += "#define ATOMICIVCCAS(a, c, d, e) atomicCompSwap(" + name + ".data[a][c], d, e)\n";
		
		return str;
	}
}
