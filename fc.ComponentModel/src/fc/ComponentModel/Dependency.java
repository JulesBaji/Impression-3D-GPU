// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.ComponentModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //on class level
public @interface Dependency
{
	Class[] classes();
}
