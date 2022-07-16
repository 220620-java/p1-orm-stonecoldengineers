package com.revature.berhanu.proj1.Generator;

public class Column {

    public String column_name, data_type, ordinal_position, is_nullable;
    public boolean isPrimaryKey, isAutoIncremented, isForeignKey ;
    

    public String getAdjOrdinalPos(String ordinalPos){
        return  String.valueOf(Integer.parseInt(ordinal_position)- 1);
    }
}
