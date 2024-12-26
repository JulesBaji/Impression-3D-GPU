// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.ComponentModel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class DependencyWalker
{
	public static Class[] getDependencies(Class c)
	{
		ArrayList<Class> dependentClasses = new ArrayList<>();
		Annotation[] annotations = c.getAnnotations();
		for (Annotation a : annotations)
		{
			if (a instanceof Dependency)
			{
				Dependency d = (Dependency)a;
				Class[] classes = d.classes();
				for (Class sc : classes)
					dependentClasses.add(sc);
			}
		}
		return dependentClasses.toArray(new Class[dependentClasses.size()]);
	}
}
