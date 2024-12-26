// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

public class GLSLLinkException extends GLSLException
{
	protected String[] m_ShaderCode;
	
	public GLSLLinkException(String[] shaderCode, String message)
	{
		super(message);
		
		m_ShaderCode = shaderCode;
	}
	
	public String toString()
	{
		String s = "";
		for (String sc : m_ShaderCode)
		{
			String[] lines = sc.split("\n"); // System.getProperty("line.separator"));
			int lineNumber = 2; // We start at line 2, because the first line is the #version line and does not appear in the source code that is explictly written (#version is automatically added by the infrastructure code)
			for (String line : lines)
			{
				s += "" + lineNumber + ": " + line + "\n";
				lineNumber++;
			}
			s += "\n";
		}
		s += getMessage();
		return s;
	}
}
