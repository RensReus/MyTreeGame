package com.example.mytreegame.MyClasses;
public class Region implements Cloneable
{
    public int size = 0;
    public int dashCount = 0;
    public int treeCount = 0;

    public Region(){}
    public Region clone()
    {
        try
        {
            return (Region) super.clone();
        }
        catch( CloneNotSupportedException e )
        {
            return null;
        }
    }
}