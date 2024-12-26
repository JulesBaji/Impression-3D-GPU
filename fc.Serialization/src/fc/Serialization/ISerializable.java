// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Serialization;

public interface ISerializable
{
	void serialize(ISerializer buffer);
	void deserialize(ISerializer buffer);
}
