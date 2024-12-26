// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL42;

public class GLTexBuffer extends GLBuffer
{
	protected int m_IdTex;
	protected int m_InternalFormat;
	
	public GLTexBuffer(long size, int internalFormat)
	{
		super(size, GL31.GL_TEXTURE_BUFFER);
		
		m_IdTex = GL11.glGenTextures();
		m_InternalFormat = internalFormat;
		
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, m_IdTex);
		GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, internalFormat, m_Id);
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0);
	}
	
	public void bind(GLResourceContext rc)
	{
		int imageUnit = rc.reserveImageUnit(this);
		GL20.glUniform1i(GL20.glGetUniformLocation(rc.getShader().m_ProgramId, "ssbo_Scene"), imageUnit);
		GL42.glBindImageTexture(imageUnit, m_IdTex, 0, false, 0, GL15.GL_READ_WRITE, m_InternalFormat);
	}
	
	public void unbind(GLResourceContext rc)
	{
		int imageUnit = rc.freeImageUnit(this);
		GL42.glBindImageTexture(imageUnit, 0, 0, false, 0, GL15.GL_READ_WRITE, m_InternalFormat);
	}
	
	public String getGLSLDeclaration(String uniformName)
	{
		String str = "restrict coherent uniform " + getInternalFormatGLSLLayout() + " " + uniformName + ";\n";
		
		str += "#define LOADIV(a) imageLoad(" + uniformName + ", a)\n";
		str += "#define STOREIV(a, d) imageStore(" + uniformName + ", a, d)\n";
		str += "#define ATOMICIVCADD(a, c, d) atomicAdd(ssbo_Scene.data[a][c], d)\n";
		str += "#define ATOMICIVCCAS(a, c, d, e) atomicCompSwap(ssbo_Scene.data[a][c], d, e)\n";
		
		return str;
	}
	
	protected String getInternalFormatGLSLLayout()
	{
		switch (m_InternalFormat)
		{
		case GL30.GL_RGBA32I:
			return "layout(rgba32i) iimageBuffer";
		case GL30.GL_RGBA32UI:
			return "layout(rgba32ui) uimageBuffer";
		case GL30.GL_R32I:
			return "layout(r32i) iimageBuffer";
		case GL30.GL_R32UI:
			return "layout(r32ui) uimageBuffer";
		default:
			throw new NullPointerException();
		}
	}
	
	public int getTexId()
	{
		return m_IdTex;
	}
	
	public int getSizeOfTexel()
	{
		switch (m_InternalFormat)
		{
		case GL30.GL_RGBA32I:
		case GL30.GL_RGBA32UI:
			return 4*4;
		case GL30.GL_R32I:
		case GL30.GL_R32UI:
			return 4;
		default:
			throw new NullPointerException();
		}
	}
	
	public int getSizeInTexels()
	{
		return (int)getSizeInBytes() / getSizeOfTexel();
	}
	
	@Override
	public void dispose()
	{
		GL11.glDeleteTextures(m_IdTex);
		m_IdTex = -1;
		super.dispose();
	}
}
