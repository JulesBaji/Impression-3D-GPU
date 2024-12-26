// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public class GLTimer
{
	private int m_Handle;
	
	public GLTimer()
	{
		m_Handle = GL15.glGenQueries();
	}
	
	public void dispose()
	{
		GL15.glDeleteQueries(m_Handle);
	}
	
	public void start()
	{
		GL15.glBeginQuery(GL33.GL_TIME_ELAPSED, m_Handle);
	}
	
	public void stop()
	{
		GL15.glEndQuery(GL33.GL_TIME_ELAPSED);
	}
	
	public long getResult()
	{
		int available = GL15.glGetQueryObjecti(m_Handle, GL15.GL_QUERY_RESULT_AVAILABLE);
		if (available != 0)
		{
			long result = GL33.glGetQueryObjecti64(m_Handle, GL15.GL_QUERY_RESULT);
			return result;
		}
		else
			return -1;
	}
	
	public float waitResult()
	{
		while (true)
		{
			long t = getResult();
			if (t >= 0L)
				return (float)t / 16.0f;
		}
	}
}
