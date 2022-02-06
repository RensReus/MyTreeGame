package com.example.mytreegame.MyClasses;

import android.content.Context;
import android.graphics.Color;

public class Cell {
    public int r= 0;
    public int c=0;
    public int region;
    public CellState state = CellState.Empty;
    public boolean HasError = false;

    public Cell(int r, int c, int region) {
        this.r = r;
        this.c = c;
        this.region = region;
    }

    public Cell clone()
    {
        Cell cloned = new Cell(r, c, region);
        cloned.state = state;
        return cloned;
    }

//    public void UpdateText() {
//        switch (state) {
//            case Tree: setText("T"); break;
//            case Dash: setText("-"); break;
//            case Empty: setText(""); break;
//        }
//    }
//
    public void SetError(boolean b) {
        HasError = b;
    }
}
