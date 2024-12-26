package fc.GLObjects;

import org.lwjgl.opengl.GL11;

public class GLError
{
	//
	// It is best to have a dedicated function for checking the GL error, since reading the error can be done only once:
	// int err = GL11.glGetError(); // may return a value != 0
	// int err2 = GL11.glGetError(); // may be 0 if called a second time!
	//
	public static void check(String message)
	{
		int glError = GL11.glGetError();
		if (glError != 0)
			throw new IllegalStateException("GL is in error state " + glError + " Error: " + message);
	}
}
