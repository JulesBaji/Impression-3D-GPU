package fc.PrintingApplication.TP2;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import fc.Math.Vec2f;

public class GLVec2fTriangle
{
	public int m_VBO;
	public int m_IBO;
	private FloatBuffer m_AttributesBuffer;
	private IntBuffer m_IndicesBuffer;
	
	public GLVec2fTriangle(Vec2f[] v) // must be 3 vertices
	{
		m_VBO = glGenBuffers();
		m_IBO = glGenBuffers();

		m_AttributesBuffer = BufferUtils.createFloatBuffer(9);
		m_AttributesBuffer.put(v[0].x); m_AttributesBuffer.put(v[0].y); m_AttributesBuffer.put(0.0f);
		m_AttributesBuffer.put(v[1].x); m_AttributesBuffer.put(v[1].y); m_AttributesBuffer.put(0.0f);
		m_AttributesBuffer.put(v[2].x); m_AttributesBuffer.put(v[2].y); m_AttributesBuffer.put(0.0f);

		m_IndicesBuffer = BufferUtils.createIntBuffer(3);
		m_IndicesBuffer.put(0); m_IndicesBuffer.put(1); m_IndicesBuffer.put(2);
				
		glBindBuffer(GL_ARRAY_BUFFER, m_VBO);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)m_AttributesBuffer.flip(), GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_IBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer)m_IndicesBuffer.flip(), GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);
	}
	
	public void render()
	{
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		glBindBuffer(GL_ARRAY_BUFFER, m_VBO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_IBO);
		glDrawElements(GL11.GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void dispose()
	{
		GL15.glDeleteBuffers(m_IBO);
		GL15.glDeleteBuffers(m_VBO);
	}
}
