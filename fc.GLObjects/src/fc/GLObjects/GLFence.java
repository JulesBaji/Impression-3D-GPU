package fc.GLObjects;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;

public class GLFence
{
	// https://stackoverflow.com/questions/63988104/opengl-mapped-memory-not-updating
	public static void insertAndWait()
	{
		GL42.glMemoryBarrier(GL42.GL_ALL_BARRIER_BITS);
		GLError.check("glMemoryBarrier");
		long fence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
		if (fence == 0L)
			GLError.check("glFenceSync");
		int state = GL32.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 4000000000L); //Timeout after 4 second
		GLError.check("glClientWaitSync");
		if (state != GL32.GL_ALREADY_SIGNALED && state != GL32.GL_CONDITION_SATISFIED )
			throw new IllegalStateException("glClientWaitSync failed");
		GL32.glDeleteSync(fence); //Delete the fence object
		GLError.check("glDeleteSync");
	}
}
