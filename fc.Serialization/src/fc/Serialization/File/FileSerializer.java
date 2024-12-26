// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Serialization.File;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fc.Serialization.ISerializable;
import fc.Serialization.ISerializer;

public class FileSerializer implements ISerializer
{
	private static int NULL_REFERENCE = -1;
	
	protected File m_File;
	protected DataInputStream m_InStream = null;
	protected int m_InStreamCurrentPosition = 0;
	protected DataOutputStream m_OutStream = null;
	protected Map<Object, Integer> m_SerializedObjectPositions = new HashMap<Object, Integer>(); // positions are in byte units in the stream
	
	public FileSerializer(File file, boolean forWriting)
	{
		m_File = file;
		try
		{
			if (forWriting)
				m_OutStream = new DataOutputStream(new FileOutputStream(m_File));
			else
				m_InStream = new DataInputStream(new FileInputStream(m_File));
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalStateException("Could not create output stream");
		}
	}
	
	public void dispose()
	{
		try
		{
			if (m_InStream != null)
				m_InStream.close();
			else if (m_OutStream != null)
				m_OutStream.close();
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Could not close data stream");
		}
	}
	
	@Override
	public void serialize(Object obj)
	{
		if (obj == null)
		{
			serialize(NULL_REFERENCE); // this indicates a null reference
		}
		else
		{
			Integer position = m_SerializedObjectPositions.get(obj);
			if (position != null)
			{
				serialize(position);
			}
			else
			{
				m_SerializedObjectPositions.put(obj, m_OutStream.size());
				String className = obj.getClass().getCanonicalName();
				int[] classInt = classNameToIntArray(className);
				serialize(classInt);
				serialize(NULL_REFERENCE);
				((ISerializable)obj).serialize(this);
			}
		}
	}
	
	private static Character fromNumericValue(int x)
	{
	    /*if ((x < 0) || (x > 35))
	        throw new IllegalArgumentException();
	    
	    return Character.toUpperCase(Character.forDigit(x, 36));*/
		return (char)x;
	}
	
	private static int toNumericValue(Character c)
	{
		return (int)c; // Character.getNumericValue(c);
	}
	
	private int[] classNameToIntArray(String className)
	{
		int[] values = new int[className.length()];
		
		for (int i=0; i < className.length(); i++)
		{
			char c = className.charAt(i);
			int val = toNumericValue(c);
			values[i] = val;
		}
		
		return values;
	}
	
	private String intArrayToClassName(Integer[] values)
	{
		char[] characters = new char[values.length];
		
		for (int i=0; i < values.length; i++)
		{
			characters[i] = fromNumericValue(values[i]);
		}
		
		return new String(characters);
	}
	
	@Override
	public Object deserializeObject()
	{
		ArrayList<Integer> values = new ArrayList<>();
		
		while (true)
		{
			int val = deserializeInt();
			if (val == NULL_REFERENCE)
				break;
			else
				values.add(val);
		}
		
		// Null reference
		if (values.size() == 0)
			return null;
		
		// Known object reference
		if (values.size() == 1)
		{
			Object obj = m_SerializedObjectPositions.get(m_InStreamCurrentPosition);
			if (obj == null)
				throw new IllegalStateException("Cannot deserialize object identified by pointer " + m_InStreamCurrentPosition);
			return obj;
		}
		
		// Object contents
		String className = intArrayToClassName(values.toArray(new Integer[0]));
		Object obj = null;
		try
		{
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor();
			obj = ctor.newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e.toString());
		}
		
		((ISerializable)obj).deserialize(this);
		
		return obj;
	}
	
	@Override
	public void serialize(int n)
	{
		try
		{
			m_OutStream.writeInt(n);
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Could not serialize int");
		}
	}
	
	@Override
	public int deserializeInt()
	{
		int val;
		try
		{
			val = m_InStream.readInt();
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Could not deserialize int");
		}
		m_InStreamCurrentPosition += 4;
		return val;
	}
	
	@Override
	public void serialize(float f)
	{
		serialize(Float.floatToRawIntBits(f));
	}
	
	@Override
	public float deserializeFloat()
	{
		return Float.intBitsToFloat(deserializeInt());
	}
	
	@Override
	public void serialize(int[] n)
	{
		for (int val : n)
			serialize(val);
	}
	
	@Override
	public final int[] deserializeIntArray(int size)
	{
		int[] values = new int[size];
		deserializeIntArray(values);
		return values;
	}
	
	@Override
	public void deserializeIntArray(int[] values)
	{
		int size = values.length;
		for (int i=0; i < size; i++)
			values[i] = deserializeInt();
	}
	
	@Override
	public void serialize(float[] f)
	{
		for (float val : f)
			serialize(val);
	}
	
	@Override
	public float[] deserializeFloatArray(int size)
	{
		float[] values = new float[size];
		deserializeFloatArray(values);
		return values;
	}
	
	@Override
	public void deserializeFloatArray(float[] values)
	{
		int size = values.length;
		for (int i=0; i < size; i++)
			values[i] = deserializeFloat();
	}
	
	@Override
	public void serializeFloatBuffer(ByteBuffer buffer)
	{
		while (buffer.hasRemaining())
		{
			float value = buffer.get();
			serialize(Float.floatToIntBits(value));
		}
	}
	
	@Override
	public void deserializeFloatBuffer(/*out*/ ByteBuffer buffer, int numFloats) // num floats to deserialize
	{
		for (int i=0; i < numFloats; i++)
		{
			float value = deserializeFloat();
			buffer.putInt(Float.floatToIntBits(value));
		}
	}
	
	@Override
	public void skip(int n) // in vec4 (128 bit) units
	{
		for (int i=0; i < n; i++)
		{
			// Skip 128 bits (4 x 32 bits)
			deserializeInt();
			deserializeInt();
			deserializeInt();
			deserializeInt();
		}
	}
	
	@Override
	public UUID getUUIDOfClass(Class c)
	{
		return null;
	}
}
