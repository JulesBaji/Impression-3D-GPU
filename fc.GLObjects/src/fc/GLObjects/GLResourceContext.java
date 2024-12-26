// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.GLObjects;

import java.util.HashMap;
import java.util.Map;

public class GLResourceContext
{
	private HashMap<Object, Integer> m_ImageUnitSlots = new HashMap<>();
	private GLProgram m_Shader;
	
	public GLResourceContext(GLProgram shader)
	{
		m_Shader = shader;
	}
	
	public GLProgram getShader()
	{
		return m_Shader;
	}
	
	public int reserveImageUnit(Object objectThatReservers)
	{
		boolean[] freeSlots = new boolean[8];
		for (int i=0; i < 8; i++)
			freeSlots[i] = true;
		
		for (Map.Entry<Object, Integer> entry : m_ImageUnitSlots.entrySet())
		{
			Object key = entry.getKey();
			Integer slot = entry.getValue();
		    if (key == objectThatReservers)
		    	return slot;
		    freeSlots[slot] = false;
		}
		
		Integer slot = m_ImageUnitSlots.get(objectThatReservers);
		if (slot != null)
			return slot;
		
		int size = m_ImageUnitSlots.size();
		if (size >= 8) 
			throw new NullPointerException("Cannot reserve more than 8 image units");
		
		m_ImageUnitSlots.put(objectThatReservers, size);
		
		return size;
	}
	
	public int freeImageUnit(Object objectThatReservers)
	{
		Integer slot = m_ImageUnitSlots.remove(objectThatReservers);
		if (slot == null)
			throw new NullPointerException();
		else
			return slot;
	}
}

