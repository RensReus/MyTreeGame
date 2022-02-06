package com.example.mytreegame.MyClasses;

import android.view.View;

import com.example.mytreegame.R;

public class FieldState {
    Region[] regions;
    int[] columnTreeCount;
    int[] rowTreeCount;
    int[] columnDashCount;
    int[] rowDashCount;
    int treeCount = 0;
    int size;
    Cell[][] cells;
    
    public FieldState(int[][] regionSetup, int size) {
        this.size = size;
        regions = new Region[size];
        for (int i = 0; i < size; i++){
            regions[i] = new Region();
        }
        columnTreeCount = new int[size];
        rowTreeCount = new int[size];
        columnDashCount = new int[size];
        rowDashCount = new int[size];
        cells = new Cell[size][size];
        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
                int regionId = regionSetup[row][col];
                regions[regionId].size++;
                Cell cell = new Cell(row, col, regionId);
                cells[row][col] = cell;
            }
        }
    }

    public FieldState() { }

    public void UpdateCellState(View v, CellState newState) {
        int row = (int)v.getTag(R.id.row);
        int col = (int)v.getTag(R.id.col);
        UpdateCellState(row, col, newState);
    }

    public void UpdateCellState(int row, int col, CellState newState) {
        CellState oldState = cells[row][col].state;
        cells[row][col].state = newState;

        Region region = regions[cells[row][col].region];
        switch (oldState) {
            case Tree:
                region.treeCount--;
                rowTreeCount[row]--;
                columnTreeCount[col]--;
                treeCount--;
                break;
            case Dash:
                region.dashCount--;
                rowDashCount[row]--;
                columnDashCount[col]--;
                break;
        }
        switch (newState) {
            case Tree:
                region.treeCount++;
                rowTreeCount[row]++;
                columnTreeCount[col]++;
                treeCount++;
                break;
            case Dash:
                region.dashCount++;
                rowDashCount[row]++;
                columnDashCount[col]++;
                break;
        }

        for (int row2 = 0; row2 < size; row2++){
            String line = "";
            for (int col2 = 0; col2 < size; col2++){
                line += cells[row][col].state.toString() + ", ";
            }
        }
    }

    public boolean Solved() {
        return NoErrors() && treeCount == size;
    }

    public boolean NoErrors() {
        Integer errorCount = 0;
        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++) {
                cells[row][col].SetError(false);
                if(CellHasError(row, col)){
                    cells[row][col].SetError(true);
                    errorCount++;
                }
            }
        }
        return errorCount == 0;
    }

    private boolean CellHasError(int row, int col) {
        Region region = regions[cells[row][col].region];
        return (cells[row][col].state == CellState.Tree && CellHasNeighborTree(row, col)) ||
                rowDashCount[row] == size ||
                columnDashCount[col] == size ||
                rowTreeCount[row] > 1 ||
                columnTreeCount[col] > 1 ||
                region.treeCount > 1 ||
                region.dashCount == region.size;
    }

    public boolean CellHasNeighborTree(int row, int col) {
        for (int x = row - 1; x < row + 2; x++){
            if (x < 0 || x >= size)
            {
                continue;
            }
            for (int y = col - 1; y < col + 2; y++) {
                if (y < 0 || y >= size || (x == row && y == col))
                {
                    continue;
                }
                if(cells[x][y].state == CellState.Tree) {
                    return true;
                }
            }
        }
        return false;
    }

    public Cell GetCell(View btn) {
        int row = (int)btn.getTag(R.id.row);
        int col = (int)btn.getTag(R.id.col);
        return cells[row][col];
    }

    public void ForAllCells() { // pass function as argument?

    }
}
