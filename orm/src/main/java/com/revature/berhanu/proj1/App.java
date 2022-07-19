package com.revature.berhanu.proj1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.revature.berhanu.proj1.Generator.Column;
import com.revature.berhanu.proj1.Generator.ClassCreator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Creating Classes..." );
        ClassCreator objClass = new ClassCreator();
        objClass.createClass(objClass.getTables());
       
        System.out.println("Done !!!");
    }
}
