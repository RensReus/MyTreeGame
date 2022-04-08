package com.example.mytreegame.MyClasses;

public class Coordinate {
    public int row;
    public int col;
    public int region;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
        this.region = -1;
    }

    public Coordinate(int row, int col, int region) {
        this.row = row;
        this.col = col;
        this.region = region;
    }
}
