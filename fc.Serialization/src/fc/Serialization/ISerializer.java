// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Serialization;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.UUID;

public interface ISerializer
{
	void serializeFloatBuffer(ByteBuffer buffer);
	void deserializeFloatBuffer(/*out*/ ByteBuffer buffer, int numFloats); // num floats to deserialize
	void serialize(Object o);
	Object deserializeObject();
	void serialize(int n);
	int deserializeInt();
	void serialize(float f);
	float deserializeFloat();
	void serialize(int[] n);
	int[] deserializeIntArray(int size);
	void deserializeIntArray(int[] data);
	void serialize(float[] f);
	float[] deserializeFloatArray(int size);
	void deserializeFloatArray(float[] data);
	void skip(int n); // in vec4 (128 bit) units
	UUID getUUIDOfClass(Class c);
}
