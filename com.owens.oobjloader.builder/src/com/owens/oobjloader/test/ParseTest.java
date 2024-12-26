package com.owens.oobjloader.test;

import java.io.File;

import com.owens.oobjloader.builder.Build;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import com.owens.oobjloader.parser.Parse;

public class ParseTest
{
    public static void main(String[] argv)
    {

        System.out.println("STARTING PARSING TEST - NOTHING WILL BE DISPLAYED - SEE com.owens.oobjloader.lwjgl.Test if you want to see things displayed.");
        for (String filename : argv) {

        	System.out.println("LOADING FILE " + filename);
            try {
                Build builder = new Build();
                Parse obj = new Parse(builder, new File(filename).toURL());
            } catch (java.io.FileNotFoundException e) {
            	System.out.println("FileNotFoundException loading file "+filename+", e=" + e);
                e.printStackTrace();
            } catch (java.io.IOException e) {
            	System.out.println("IOException loading file "+filename+", e=" + e);
                e.printStackTrace();
            }
            System.out.println("DONE LOADING FILE " + filename);
        }
    }
}