package fc.Serialization.Buffer;

import java.nio.ByteBuffer;

public class ByteCounterSerializationBuffer extends SerializationBuffer
{
	private long m_Position; // in byte units
	public long m_MaxPosition; // in byte units
	
	public ByteCounterSerializationBuffer(Object object)
	{
		super(object);
	}

	@Override
	protected void createBuffer()
	{
		m_Position = 0;
		m_MaxPosition = 0;
		// Nothing to do, we just count bytes in this implementation
	}
	
	@Override
	protected long getBufferSizeInBytes()
	{
		return 0;
	}
	
	@Override
	protected long getBufferPosition() // in byte units
	{
		return m_Position;
	}
	
	@Override
	protected void setBufferPosition(long position) // in byte units
	{
		m_Position = position;
		m_MaxPosition = Math.max(m_Position, m_MaxPosition);
	}
	
	@Override
	protected int getInt()
	{
		m_Position += 4L;
		m_MaxPosition = Math.max(m_Position, m_MaxPosition);
		return 0;
	}
	
	@Override
	protected void putInt(int value)
	{
		m_Position += 4L;
		m_MaxPosition = Math.max(m_Position, m_MaxPosition);
	}
	
	@Override
	public void serializeFloatBuffer(ByteBuffer buffer)
	{
		int numBytesUntilEnd = buffer.limit() - buffer.position();
		m_Position += (long)numBytesUntilEnd;
		m_MaxPosition = Math.max(m_Position, m_MaxPosition);
	}
	
	@Override
	public void deserializeFloatBuffer(ByteBuffer buffer, int numFloats)
	{
		m_Position -= (long)numFloats*4L;
	}

	@Override
	protected void map(long startPositionInBytes, long endPositionInBytes, boolean mapForReading, boolean mapForWriting)
	{
		// Nothing to do
	}
	
	@Override
	protected void unmap(boolean forWriting)
	{
		// Nothing to do
	}
	
	@Override
	public void skip(int numberOfVec4ToSkip)
	{
		m_Position += (long)numberOfVec4ToSkip*16L; // *16 because m_Buffer here is a BYTEBuffer, not IntBuffer
		m_MaxPosition = Math.max(m_Position, m_MaxPosition);
	}
}
