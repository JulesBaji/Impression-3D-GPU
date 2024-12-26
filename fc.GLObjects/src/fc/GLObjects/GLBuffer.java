// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;

public class GLBuffer 
{
	protected int m_BufferTarget;
	
	protected int m_Id;
	protected long m_Size;
	
	public GLBuffer(long size, int bufferTarget)
	{
		m_BufferTarget = bufferTarget;
		m_Id = GL15.glGenBuffers();
		GLError.check("Could not generate buffer ID");
		resize(size);
	}
	
	public void resize(long size)
	{
		m_Size = size;
		GL15.glBindBuffer(m_BufferTarget, m_Id);
		GLError.check("glBindBuffer");
		//
		// 2021-04-02: GL_DYNAMIC_READ makes glMapBufferRange(WRITE_BIT|INVALIDATE_RANGE)  fast on dual-processor setups.
		// GL_DYNAMIC_DRAW is incredibly slow (15 seconds for a 1GB buffer).
		// -> sticking with GL_DYNAMIC_READ
		// See emails from Piers Daniell from nVidia
		// 2021-08-09: GL_DYNAMIC_READ should not be used for shader storage buffers, as hinted by the driver in a debug message callback (nVidia)
		// See GLShaderStorageBufferTests.java for details.
		// The solution for the 2021-04-02 problem described above is to NOT use INVALIDATE_RANGE, that's all.
		//
		GL15.glBufferData(m_BufferTarget, size, GL15.GL_STATIC_DRAW); // STATIC_DRAW uses a LOT less memory than DYNAMIC_DRAW {FC:2018-01-18}
		GLError.check("glBufferData");
		GL15.glBindBuffer(m_BufferTarget, 0);
		GLError.check("glBindBuffer(0)");
	}
	
	public int getId()
	{
		return m_Id;
	}
	
	public long getSizeInBytes()
	{
		return m_Size;
	}
	
	public void dispose()
	{
		GL15.glBindBuffer(m_BufferTarget, 0);
		GL15.glDeleteBuffers(m_Id);
		m_Id = -1;
	}
}
