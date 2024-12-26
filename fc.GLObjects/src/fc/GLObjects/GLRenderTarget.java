// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

public class GLRenderTarget
{
	protected int m_FboId;
	protected int m_IdTex;
	protected int m_IdDepth;
	protected int m_InternalFormat;
	protected int m_Format;
	protected int m_Type;
	
	// Example: new RenderTarget(100, 100, GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT); // Lum
	// //Example: new RenderTarget(100, 100, GL30.GL_R32UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_INT);
	public GLRenderTarget(int width, int height, int internalFormat, int format, int type)
	{
		m_InternalFormat = internalFormat;
		m_Format = format;
		m_Type = type;
		
		m_IdTex = GL11.glGenTextures();
		GLError.check("Could not generate texture ID");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, m_IdTex);
		GLError.check("Could not bind texture ID");
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, // level,
				internalFormat,
				width, height,
				0, // border, must always be 0
				format,
				type,
				0L);
		GLError.check("Could not create texture used as color buffer back-end for framebuffer");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GLError.check("Call to getTexParameteri failed");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GLError.check("Call to getTexParameteri failed");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GLError.check("Could not unbind texture");
		
		m_IdDepth = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, m_IdDepth);
		GLError.check("Could not bind texture ID");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLError.check("Call to getTexParameteri GL_TEXTURE_MIN_FILTER failed");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLError.check("Call to getTexParameteri GL_TEXTURE_MAG_FILTER failed");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GLError.check("Call to getTexParameteri GL_TEXTURE_WRAP_S failed");
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GLError.check("Call to getTexParameteri GL_TEXTURE_WRAP_T failed");
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, // level,
				GL14.GL_DEPTH_COMPONENT24,
				width, height,
				0, // border, must always be 0
				GL11.GL_DEPTH_COMPONENT,
				GL11.GL_UNSIGNED_INT,
				0L);
		GLError.check("Could not create texture used as depth buffer back-end for framebuffer");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GLError.check("Could not bind texture ID");
		
		// See:
		// https://sites.google.com/site/john87connor/advanced-opengl-tutorials/tutorial-04-2-framebuffer-size
		
		m_FboId = GL30.glGenFramebuffers();
		GLError.check("Could generate framebuffer ID");
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, m_FboId);
		GLError.check("Could not bind framebuffer");
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, m_IdTex, 0);
		GLError.check("Could not bind color attachment to framebuffer");
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, m_IdDepth, 0);
		GLError.check("Could not bind depth attachment to framebuffer");
		//GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
		
		int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (status != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("glCheckFramebufferStatus returned " + status);
		GLError.check("glCheckFramebufferStatus returned GL_FRAMEBUFFER_COMPLETE but OpenGL is now in error state");

		// https://stackoverflow.com/questions/27535727/opengl-create-a-depth-stencil-texture-for-reading
		//m_IdDepthView = GL11.glGenTextures();
		//GL44.glTextureView(m_IdDepthView, GL11.GL_TEXTURE_2D, m_IdDepthStencil, GL30.GL_DEPTH24_STENCIL8, 0, 1, 0, 1);
		//GLError.check("Could not create texture view");
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GLError.check("Could not unbind framebuffer");
	}
	
	public void bind()
	{
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, m_FboId);
		GLError.check("Could not bind framebuffer");
        GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
		GLError.check("Call to glDrawBuffers failed");
        // Rien a faire ici concernant le DEPTH/STENCIL
	}
	
	public void unbind()
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GLError.check("Could not unbind framebuffer");
	}
	
	public void dispose()
	{
		GL11.glDeleteTextures(m_IdDepth);
		GLError.check("Could not delete texture " + m_IdDepth);
		GL11.glDeleteTextures(m_IdTex);
		GLError.check("Could not delete texture " + m_IdTex);
		GL30.glDeleteFramebuffers(m_FboId);
		GLError.check("Could not delete framebuffer " + m_FboId);
	}
	
	// A appeler pour utiliser comme texture
	// glActiveTextureARB(GL_TEXTURE0);
	// glBindTexture(GL_TEXTURE_2D, rt.getTexId());
	// // on n'unbind pas la texture
	public int getTexId()
	{
		return m_IdTex;
	}
	
	public int getDepthTexId()
	{
		return m_IdDepth;
	}
	
	private int getSizeOfOneComponentsInBytes()
	{
		switch (m_InternalFormat)
		{
		case GL30.GL_R32F:
		case GL30.GL_R32I:
		case GL30.GL_R32UI:
		case GL30.GL_RGBA32I:
		case GL30.GL_RGBA32UI:
		case GL30.GL_RGBA32F:
			return 4;
		default:
			throw new IllegalStateException("Don't know how to calculate the size of internal OpenGL pixel format " + m_InternalFormat);
		}
	}
	
	private int getNumPixelFormatComponents()
	{
		switch (m_InternalFormat)
		{
		case GL30.GL_R32F:
		case GL30.GL_R32I:
		case GL30.GL_R32UI:
			return 1;
		case GL30.GL_RGBA32I:
		case GL30.GL_RGBA32UI:
		case GL30.GL_RGBA32F:
			return 4;
		default:
			throw new IllegalStateException("Don't know how to calculate the number of pixel format components for OpenGL pixel format " + m_InternalFormat);
		}
	}
	
	public float[][][] readBackAsFloat() // each [height][width][4] (4: R,G,B,A. Each value is between [0.0f,1.0f])
	{
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, m_IdTex);
		int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		int numComponents = getNumPixelFormatComponents();
		IntBuffer buffer = BufferUtils.createByteBuffer(width * height * getSizeOfOneComponentsInBytes() * numComponents).asIntBuffer();
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, m_Format, m_Type, buffer);
		float[][][] data = new float[height][width][numComponents];
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				// TODO: this only works when one component = 32 bits, as we have a IntBuffer
				int i = (x + (width * y)) * numComponents;
				for (int j=0; j < numComponents; j++)
				{
					int val = buffer.get(i + j);
					data[y][x][j] = Float.intBitsToFloat(val);
				}
			}
		}
		return data;
	}
	
	public int[][][] readBackAsInt(int sx, int sy, int width, int height) // each [height][width][4] (4: R,G,B,A. Each value is between [0.0f,1.0f])
	{
		// Il y a 2 manieres de lire les pixels d'un RenderTarget.
		// Soit en utilisant glGetTexImage, comme illustre ci-dessus (methode du dessus).
		// Soit en utilisant glReadPixels.
		// Le principal avantage de glReadPixels est qu'il permet de ne lire qu'une *partie* de la texture.
		// Cependant, il exige qu'un framebuffer soit attache a la texture, ce qui n'est pas le cas avec glGetTexImage.
		// Dans les faits, glReadPixels n'est pas si rapide que ça. Il y a fort a parier que le driver copie des bouts
		// de donnees pour satisfaire le layout lie a width/height.
		// glGetTexImage est rapide, mais il necessite de prendre la totalite de la texture, qui peut etre immense.
		
		bind();
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, m_IdTex);
		int rtWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int rtHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		int numComponents = getNumPixelFormatComponents();
		IntBuffer buffer = BufferUtils.createByteBuffer(width * height * getSizeOfOneComponentsInBytes() * numComponents).asIntBuffer();
		GL11.glReadPixels(sx, sy, width, height, m_Format, m_Type, buffer);
		GLError.check("glReadPixels");
		//GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, m_Format, m_Type, buffer);
		int[][][] data = new int[height][width][numComponents];
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				// TODO: this only works when one component = 32 bits, as we have a IntBuffer
				int i = (x + (width * y)) * numComponents;
				for (int j=0; j < numComponents; j++)
				{
					int val = buffer.get(i + j);
					data[y][x][j] = val;
				}
			}
		}
		unbind();
		return data;
	}
	
	public int getFBOId()
	{
		return m_FboId;
	}
}
