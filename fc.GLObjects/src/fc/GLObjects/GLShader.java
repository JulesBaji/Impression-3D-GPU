// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

public class GLShader
{
	public String getCode()
	{
		String code = loadTextFile(this.getClass(), false);
		
		return code;
	}
	
	private static URL getGLSLFileForClass(Class c)
	{
		//System.out.println("getGLSLFileForClass(name=" + c.getName() + ",canonicalName=" + c.getCanonicalName() + ")");
		
		//
		// If the class is a shader-derived class (not a serializable object with runtime GLSL), we skip anonymous classes
		//
		if (GLShader.class.isAssignableFrom(c))
		{
			while (GLShader.class.isAssignableFrom(c))
			{
				String canonicalName = c.getCanonicalName();
				if (canonicalName != null) // if canonicalName is null, this is likely an anonymous class. Skip it.
				{
					String resourceName = "/" + canonicalName.replaceAll("\\.", "/") + ".glsl";
					URL resourceURL = c.getResource(resourceName);
					if (resourceURL != null)
					{
						//System.out.println("returns " + resourceName);
						return resourceURL;
					}
				}
				c = c.getSuperclass(); // skip the anonymous class (this will occur only once)
			}
			
			return null;
		}
		else
		{
			String resourceName = "/" + c.getCanonicalName().replaceAll("\\.", "/") + ".glsl";
			URL resourceURL = c.getResource(resourceName);
			return resourceURL;
		}
	}
	
	protected static InputStream findResourceStream(Class c)
	{
		//
		// Here, we do not always want to get the canonical name of the 'c' class, itself.
		// Suppose we have this situation:
		// public class MyMeshVShader extends AutoIncludeShader { ... }
		// Calling code:
		// GLProgram program = ...;
		// program.init(
		//   new MyMeshVShader()
		//   { @Override
		//     public String getCode()
		//     { ... }
		//   }, ...
		// Here, we want to get the GLSL resource contents for MyMeshVShader, not the derived, anonymous class. Otherwise, the .glsl file will not be found.
		//
		URL resourceURL = getGLSLFileForClass(c);
		if (resourceURL == null)
			return null;
		
		try
		{
			return resourceURL.openStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected String resolveIncludes(String code)
	{
		try
		{
			StringBuffer contents = new StringBuffer();
			BufferedReader reader = new BufferedReader(new StringReader(code));
			String text = null;
			
			while ((text = reader.readLine()) != null)
			{
				if (text.startsWith("#include"))
				{
					/*String includedClass = text.substring("#include \"".length(), text.length() - 1);
					text = loadTextFile(Class.forName(includedClass), false);
					if (text == null)
					{
						reader.close();
						return null;
					}
					text = resolveIncludes(text);*/
					text = "//" + text;
				}
				
				contents.append(text).append(System.getProperty("line.separator"));
			}
			
			reader.close();
			
			return contents.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected static String loadTextFile(Class c, boolean filterOutIncludes)
	{
		try
		{
			StringBuffer contents = new StringBuffer();
			InputStream glslInputStream = findResourceStream(c);
			if (glslInputStream == null)
				return null; // throw new NullPointerException("Cannot find GLSL code file for class " + c.getCanonicalName());
			BufferedReader reader = new BufferedReader(new InputStreamReader(glslInputStream));
			String text = null;
			
			while ((text = reader.readLine()) != null)
			{
				if (filterOutIncludes && text.startsWith("#include"))
					continue;
				
				contents.append(text).append(System.getProperty("line.separator"));
			}
			
			reader.close();
			
			return contents.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
