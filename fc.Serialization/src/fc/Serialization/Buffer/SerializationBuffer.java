package fc.Serialization.Buffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import fc.Serialization.ISerializable;
import fc.Serialization.ISerializer;

public abstract class SerializationBuffer implements ISerializer
{
	private static int NULL_REFERENCE = 0xFFFFDEAD;
	
	private LinkedHashMap<Class, UUID> m_UUIDs = new LinkedHashMap<Class, UUID>();
	protected Map<Object, BufferLocation> m_ObjectLocations;
	//private ArrayList<Object> m_SubObjects = new ArrayList<>();
	private Set<Object> m_SubObjects = Collections.newSetFromMap( new IdentityHashMap<>() );
	private Set<Object> m_AlreadySaved = Collections.newSetFromMap( new IdentityHashMap<>() );
	private boolean m_ShallowSerialization;
	public Object m_Object;
	
	public SerializationBuffer(Object object)
	{
		// Also see: https://stackoverflow.com/questions/33907109/is-there-an-identityhashmap-implementation-that-maintains-insert-order
		m_ObjectLocations = new IdentityHashMap<Object, BufferLocation>();

		m_Object = object;
		createBuffer();
		setObject(object);
	}
	
	protected abstract void createBuffer();
	
	protected abstract long getBufferSizeInBytes();
	
	protected abstract long getBufferPosition(); // in byte units
	
	protected abstract void setBufferPosition(long position); // in byte units
	
	protected abstract int getInt();
	
	protected abstract void putInt(int value);
	
	public BufferLocation getObjectLocation(Object obj)
	{
		return m_ObjectLocations.get(obj);
	}
	
	private void setObject(Object object)
	{
		m_ObjectLocations.clear();
		mapForWriting(0L, getBufferSizeInBytes());
		BufferLocation location = new BufferLocation();
		location.m_StartPosition = -1;
		location.m_EndPosition = -1;
		m_ObjectLocations.put(object, location);
		m_ShallowSerialization = false;
		m_SubObjects.add(object);
		while (!m_SubObjects.isEmpty())
		{
			Object o = m_SubObjects.iterator().next();
			m_SubObjects.remove(o);
			m_AlreadySaved.add(o);
			serialize(o);
		}
		for (Map.Entry<Object, BufferLocation> entry : m_ObjectLocations.entrySet())
		{
			BufferLocation loc = entry.getValue();
			for (long refer : loc.m_References)
			{
				setBufferPosition(refer);
				if ((loc.m_StartPosition % 16L) != 0L)
					throw new NullPointerException("Cannot write out an offset that is not 128-bit aligned");
				putInt((int)(loc.m_StartPosition / 16L));
			}
		}
		unmap(true);
	}
	
	// ISerializer
	@Override
	public UUID getUUIDOfClass(Class c)
	{
		UUID uuid = m_UUIDs.get(c);
		if (uuid == null)
		{
			uuid = UUID.randomUUID();
			m_UUIDs.put(c, uuid);
		}
		return uuid;
	}
	
	//
	// Gets the UUIDs of all actually serialized classes.
	// Note: the map returned MAY be modified by the caller to add some more UUIDs.
	// This is expected and wanted.
	//
	public LinkedHashMap<Class, UUID> getUUIDOfClasses()
	{
		return m_UUIDs;
	}
	
	//
	// This method is now unused.
	//
	public Class[] getClassesOfSerializedObjects()
	{
		HashSet<Class> classes = new HashSet<>();
		for (Entry<Object, BufferLocation> entry : m_ObjectLocations.entrySet())
		{
			classes.add(entry.getKey().getClass());
		}
		
		return classes.toArray(new Class[classes.size()]);
	}
	
	protected void mapForReading(long startPositionInBytes, long endPositionInBytes)
	{
		map(startPositionInBytes, endPositionInBytes, true, false);
	}
	
	protected void mapForWriting(long startPositionInBytes, long endPositionInBytes)
	{
		map(startPositionInBytes, endPositionInBytes, false, true);
	}
	
	protected abstract void map(long startPositionInBytes, long endPositionInBytes, boolean mapForReading, boolean mapForWriting);
	
	private static String toHexString(int val)
	{
		String str = Integer.toHexString(val);
		while (str.length() < 8)
			str = "0" + str;
		return str.toUpperCase();
	}
	
	private static String toAddress(int val)
	{
		String str = "" + val;
		while (str.length() < 10)
			str = "0" + str;
		return str.toUpperCase();
	}
	
	public void dump(Object key)
	{
		BufferLocation firstLoc = m_ObjectLocations.get(key); // should match the Scene
		mapForReading(firstLoc.m_StartPosition, firstLoc.m_EndPosition);
		long count = (firstLoc.m_EndPosition - firstLoc.m_StartPosition) / 4L;
		for (int i=0; i < count; i++)
		{
			if ((i%8) == 0)
			{
				if (i != 0)
					System.out.println("");
				int address = i*4;
				System.out.print(toAddress(address) + ": ");
			}
			int val = getInt();
			System.out.print(toHexString(val) + " ");
		}
		unmap(false);
	}
	
	public void dumpAll()
	{
		long m_MaxEndPosition = -1L;
		for (Object key : m_ObjectLocations.keySet())
		{
			m_MaxEndPosition = Math.max(m_MaxEndPosition, m_ObjectLocations.get(key).m_EndPosition);
		}
		
		mapForReading(0, m_MaxEndPosition);
		long count = m_MaxEndPosition / 4L;
		for (int i=0; i < count; i++)
		{
			if ((i%8) == 0)
			{
				if (i != 0)
					System.out.println("");
				int address = i*4;
				System.out.print(toAddress(address) + ": ");
			}
			int val = getInt();
			System.out.print(toHexString(val) + " ");
		}
		unmap(false);
	}
	
	protected abstract void unmap(boolean forWriting);
	
	@Override
	public void serialize(Object obj)
	{
		if (!m_ShallowSerialization)
		{
			BufferLocation location = m_ObjectLocations.get(obj);
			location.m_StartPosition = getBufferPosition();
			
			m_ShallowSerialization = true;
			serializeRaw(obj);
			location.m_EndPosition = getBufferPosition();
			m_ShallowSerialization = false;
		}
		else
		{
			if (obj == null)
			{
				putInt(NULL_REFERENCE); // this indicates a null reference
			}
			else
			{
				BufferLocation location = m_ObjectLocations.get(obj);
				if (location == null)
				{
					location = new BufferLocation();
					location.m_StartPosition = -1;
					location.m_EndPosition = -1;
					m_ObjectLocations.put(obj, location);
				}
				location.addReference(getBufferPosition());
				if (!m_AlreadySaved.contains(obj))
					m_SubObjects.add(obj);
				
				//
				// At this point, location.m_StartPosition might be equal to -1.
				// This may happen during initial serialization.
				// This is not a problem, as -1 references will be updated later on.
				//
				if (location.m_StartPosition != -1)
				{
					// If not equal to -1, just check if it is a multiple of 128 bits
					if ((location.m_StartPosition % 16L) != 0L)
						throw new NullPointerException("Cannot write out an offset which is not 128-bit aligned");
				}
				putInt((int)(location.m_StartPosition / 16L)); // TODO: any value can be set here, as this object reference will be updated later on
			}
		}
	}
	
	@Override
	public Object deserializeObject()
	{
		int intPosition = deserializeInt();
		
		if (intPosition == NULL_REFERENCE)
			return null;
		
		long position = (long)intPosition*16L; // vec4 units -> byte units
		
		for (Entry<Object, BufferLocation> entry : m_ObjectLocations.entrySet())
		{
			Object obj = entry.getKey();
			BufferLocation loc = entry.getValue();
			if (position == loc.m_StartPosition) // m_StartPosition is in byte units
				return obj;
		}
		
		throw new NullPointerException("Cannot resolve object reference");
	}
	
	public static int[] UUIDTo4Integers(UUID uuid)
	{
		long msb = uuid.getMostSignificantBits();
		int aMsb = (int)(msb >> 32);
		int bMsb = (int)msb;

		long lsb = uuid.getLeastSignificantBits();
		int aLsb = (int)(lsb >> 32);
		int bLsb = (int)lsb;
		
		return new int[]{aMsb, bMsb, aLsb, bLsb};
	}
	
	private void serializeRaw(Object obj)
	{
		long posi = getBufferPosition();
		if ((posi%16L) != 0L)
		{
			// If we reach here, it means that the last serialized object did not respect the 128-bit alignment.
			throw new NullPointerException("Object needs to be serialized with respect to a 128-bit alignment");
			// Uncomment the line of code below (println("Serializing....")...) - this will tell you which object class
			// gets serialized before the exception is raised, and thus which class serializer violates the 128-bit alignment.
		}
		
		// Uncomment this line to debug 128-bit alignment problems
		//System.out.println("Serializing " + obj.getClass().getSimpleName());
		
		int[] components = UUIDTo4Integers(getUUIDOfClass(obj.getClass()));
		for (int i=0; i < components.length; i++)
			serialize(components[i]);

		if (!(obj instanceof ISerializable))
			throw new NullPointerException("Object of class " + obj.getClass().getSimpleName() + " is not serializable");
		
		((ISerializable)obj).serialize(this); 
	}
	
	private void deserializeRaw(Object obj)
	{
		if ((getBufferPosition()%16L) != 0L)
		{
			// If we reach here, it means that the last serialized object did not respect the 128-bit alignment.
			throw new NullPointerException("Object needs to be serialized with respect to a 128-bit alignment");
			// Uncomment the line of code below (println("Serializing....")...) - this will tell you which object class
			// gets serialized before the exception is raised, and thus which class serializer violates the 128-bit alignment.
		}
		
		// Uncomment this line to debug 128-bit alignment problems
		//System.out.println("Serializing " + obj.getClass().getSimpleName());
		
		int[] components = UUIDTo4Integers(getUUIDOfClass(obj.getClass()));
		for (int i=0; i < components.length; i++)
			deserializeInt();

		if (!(obj instanceof ISerializable))
			throw new NullPointerException("Object of class " + obj.getClass().getSimpleName() + " is not serializable");
		
		((ISerializable)obj).deserialize(this); 
	}
	
	@Override
	public void serializeFloatBuffer(ByteBuffer buffer)
	{
		while (buffer.hasRemaining())
		{
			int value = buffer.getInt();
			serialize(Float.floatToIntBits(value));
		}
	}
	
	@Override
	public void deserializeFloatBuffer(ByteBuffer buffer, int numFloats)
	{
		for (int i=0; i < numFloats; i++)
		{
			float value = deserializeFloat();
			buffer.putInt(Float.floatToIntBits(value));
		}
	}
	
	@Override
	public void serialize(int value)
	{
		putInt(value);
	}
	
	@Override
	public int deserializeInt()
	{
		return getInt();
	}
	
	@Override
	public void serialize(int[] value)
	{
		for (int i=0; i < value.length; i++)
			serialize(value[i]);
	}
	
	@Override
	public final int[] deserializeIntArray(int size)
	{
		int[] n = new int[size];
		deserializeIntArray(n);
		return n;
	}
	
	@Override
	public void deserializeIntArray(int[] v)
	{
		int size = v.length;
		for (int i=0; i < size; i++)
			v[i] = deserializeInt();
	}
	
	@Override
	public void serialize(float[] value)
	{
		for (int i=0; i < value.length; i++)
			serialize(value[i]);
	}
	
	@Override
	public final float[] deserializeFloatArray(int size)
	{
		float[] f = new float[size];
		deserializeFloatArray(f);
		return f;
	}
	
	@Override
	public void deserializeFloatArray(float[] f)
	{
		int size = f.length;
		for (int i=0; i < size; i++)
			f[i] = deserializeFloat();
	}
	
	@Override
	public void skip(int numberOfVec4ToSkip)
	{
		throw new NullPointerException("Not implemented. Please implement in derived class.");
	}
	
	@Override
	public void serialize(float value)
	{
		serialize(Float.floatToIntBits(value));
	}
	
	@Override
	public float deserializeFloat()
	{
		return Float.intBitsToFloat(deserializeInt());
	}
	
	//
	// TODO:
	// We should have:
	// Session sess = beginSceneObjectChangedSession();
	// session.markAsChanged(obj1);
	// session.markAsChanged(obj2);
	// endSceneObjectChangedSession(); // -> appellerait mapForReadWrite, puis unmap
	// Facile a implementer.
	//
	public void onObjectChanged(Object obj)
	{
		BufferLocation location = m_ObjectLocations.get(obj);
		if (location == null)
			throw new NullPointerException("Cannot find object in database");

		mapForWriting(location.m_StartPosition, location.m_EndPosition);
		m_ShallowSerialization = true;
		serializeRaw(obj);
		m_ShallowSerialization = false;
		unmap(true);
	}
	
	public void readBack(Object obj)
	{
		BufferLocation location = m_ObjectLocations.get(obj);
		if (location == null)
			throw new NullPointerException("Cannot find object in database");
		
		mapForReading(location.m_StartPosition, location.m_EndPosition);
		deserializeRaw(obj);
		unmap(false);
	}
}
