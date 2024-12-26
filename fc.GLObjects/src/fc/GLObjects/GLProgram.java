// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

public class GLProgram
{
	protected int m_ProgramId;
	
	public void init(GLShader vsCode, GLShader fsCode)
	{
		int vsShader = -1, fsShader = -1;
		
		m_ProgramId = GL20.glCreateProgram();
		GLError.check("glCreateProgram");
		
		if (vsCode != null)
		{
			vsShader = loadGLSLProgram(vsCode, GL20.GL_VERTEX_SHADER);
			GL20.glAttachShader(m_ProgramId, vsShader);
			GLError.check("glAttachShader");
		}
		
		if (fsCode != null)
		{
			fsShader = loadGLSLProgram(fsCode, GL20.GL_FRAGMENT_SHADER);
			GL20.glAttachShader(m_ProgramId, fsShader);
			GLError.check("glAttachShader");
		}
		
		preLinkStep();
		
		GL20.glLinkProgram(m_ProgramId);
		GLError.check("glLinkProgram");
		int linked = GL20.glGetProgrami(m_ProgramId, GL20.GL_LINK_STATUS);
		if (linked == GL11.GL_FALSE)
		{
			//int maxLength = GL20.glGetProgrami(m_ProgramId, GL20.GL_INFO_LOG_LENGTH);
			String infoLog = GL20.glGetProgramInfoLog(m_ProgramId);
			throw new GLSLLinkException(new String[] { vsCode.getCode(), fsCode.getCode() }, infoLog);
		}
		
		if (vsShader != -1)
			GL20.glDeleteShader(vsShader);
		
		if (fsShader != -1)
			GL20.glDeleteShader(fsShader);

		GL20.glUseProgram(0);
	}
	
	public int getId()
	{
		return m_ProgramId;
	}
	
	protected void preLinkStep()
	{
		// Override in derived class
		// Things you may want to do:
		// glBindAttribLocation(m_Shader, ..., "attrib_position");
		// glBindAttribLocation(m_Shader, ..., "attrib_vertex");
		// glBindAttribLocation(m_Shader, ..., "attrib_normal");
	}
	
	public void begin()
	{
		GL20.glUseProgram(m_ProgramId);
		GLError.check("glUseProgram");
	}
	
	public void end()
	{
		GL20.glUseProgram(0);
		GLError.check("glUseProgram(0)");
	}

	private String readFile(String fileName) throws IOException
	{
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	
	protected int loadGLSLProgram(GLShader shader, int type)
	{
		String shaderSource = shader.getCode();
		
		int id = GL20.glCreateShader(type);
		
		if (!shaderSource.startsWith("#version"))
		{
			String glslVersion = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
			if (glslVersion == null)
				throw new IllegalStateException("glGetString returned null. Probable reason: no context is currently active, or OpenGL is not initialized properly.");
			glslVersion = glslVersion.split(" ")[0].replaceAll("\\.", "");
			
			shaderSource = "#version " + glslVersion + System.lineSeparator() + shaderSource;
		}
		
		/*
		// Debug : read shader source code from text file.
		// Directly edit shaderSource_read.txt using notepad and trace the code below to see if the code compiles correctly
		try
		{
			shaderSource = readFile("C:\\Users\\claux\\Documents\\shaderSource_read.txt");
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
		finally
		{
		}*/
		
		GL20.glShaderSource(id, shaderSource);
		GL20.glCompileShader(id);
		int isCompiled = GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS);
		if (isCompiled == GL11.GL_FALSE)
		{
			String infoLog = GL20.glGetShaderInfoLog(id);
			
			String[] lines = shaderSource.split("\\r?\\n");
			int lineNumber = 1;
			System.err.println("Shader class: " + shader.getClass().getCanonicalName());
			for (String line : lines)
			{
				System.err.println((lineNumber++) + ": " + line);
			}
			
			/*
			// Debug: output shader code
			{
				PrintWriter out = new PrintWriter( "C:\\Users\\claux\\Documents\\shaderSource.txt" );
			    out.println( shaderSource );
			    out.close();
			}
			catch (Exception ex)
			{
				System.out.print(ex.toString());
			}*/
			
			throw new GLSLCompileException(shaderSource, infoLog);
		}

		/*
		// Debug
		{
			String[] lines = shaderSource.split("\\r?\\n");
			int lineNumber = 1;
			for (String line : lines)
			{
				System.err.println((lineNumber++) + ": " + line);
			}
		}*/
		
		return id;
	}
	
	public void dispose()
	{
		GL20.glDeleteProgram(m_ProgramId);
		m_ProgramId = -1;
	}
}
