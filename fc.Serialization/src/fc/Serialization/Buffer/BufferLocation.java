package fc.Serialization.Buffer;

import java.util.ArrayList;

public class BufferLocation
{
	public long m_StartPosition; // byte units
	public long m_EndPosition; // byte units
	public ArrayList<Long> m_References = new ArrayList<>();
	
	public void addReference(long position)
	{
		m_References.add(position);
	}
}
