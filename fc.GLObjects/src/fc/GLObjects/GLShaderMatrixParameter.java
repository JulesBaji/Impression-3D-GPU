// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import fc.Math.Matrix;

public class GLShaderMatrixParameter extends GLShaderParameter
{
	private FloatBuffer m_Buffer;
	
	public GLShaderMatrixParameter(String name)
	{
		super(name);
		
		m_Buffer = BufferUtils.createFloatBuffer(16);
	}
	
	public void set(Matrix matrix)
	{
		if (m_Location != -1)
			GL20.glUniformMatrix4fv(m_Location, false, (FloatBuffer)m_Buffer.put(matrix.toColumnMajorArray()).flip());
	}
}
