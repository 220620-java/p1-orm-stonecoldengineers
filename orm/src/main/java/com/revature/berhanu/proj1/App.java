package com.revature.berhanu.proj1;

import com.revature.berhanu.proj1.Generator.ClassCreator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ClassCreator objClass = new ClassCreator();
        for(int i =0; i<objClass.getTables().size(); i++)
        {
            System.out.println(objClass.getTables().get(i));
        } 
    }
}
