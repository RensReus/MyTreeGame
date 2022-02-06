package com.example.mytreegame.MyClasses;

public class TestFieldState extends FieldState {
    public TestFieldState(int[][] regionSetup, int size) {
        super(regionSetup, size);
    }

    public TestFieldState() {};

    public TestFieldState copy()
    {
            TestFieldState cloned = new TestFieldState();
            cloned.treeCount = treeCount;
            cloned.size = size;

            cloned.regions = new Region[size];
            for (int i = 0; i < size; i++) {
                cloned.regions[i] = regions[i].clone();
            }

            cloned.columnTreeCount = columnTreeCount.clone();
            cloned.rowTreeCount = rowTreeCount.clone();
            cloned.columnDashCount = columnDashCount.clone();
            cloned.rowDashCount = rowDashCount.clone();

            cloned.cells = new Cell[size][size];
            for (int row = 0; row < size; row++){
                for (int col = 0; col < size; col++){
                    cloned.cells[row][col] = cells[row][col].clone();
                }
            }
            return cloned;
    }

    public boolean IsSolvable(int[][] field) {
        boolean fieldChanged = true;
        while (fieldChanged) {
            fieldChanged = false;
            PlaceSimpleDashesAndTrees();
            if (Solved()) {
                return true;
            }
            for (int row = 0; row < size; row++){
                for (int col = 0; col < size; col++){
                    if (cells[row][col].state != CellState.Empty) {
                        continue;
                    }
                    if(PlacingXWillLeadToInvalidState(CellState.Tree, row, col)) {
                        UpdateCellState(row, col, CellState.Dash);
                        fieldChanged = true;
                        continue;
                    }
                    if(PlacingXWillLeadToInvalidState(CellState.Dash, row, col)) {
                        UpdateCellState(row, col, CellState.Tree);
                        AutoPlaceDashes();
                        fieldChanged = true;
                    }
                }
            }
        }
        return false;
    }

    public void PlaceSimpleDashesAndTrees() {
        boolean fieldChanged = true;
        while (fieldChanged) {
            fieldChanged = false;
            for (int row = 0; row < size; row++){
                for (int col = 0; col < size; col++){
                    if (cells[row][col].state != CellState.Empty) {
                        continue;
                    }
                    if(PlacingXLeadsToInvalidState(CellState.Tree, row, col)) {
                        UpdateCellState(row, col, CellState.Dash);
                        fieldChanged = true;
                        continue;
                    }
                    if(PlacingXLeadsToInvalidState(CellState.Dash, row, col)) {
                        UpdateCellState(row, col, CellState.Tree);
                        AutoPlaceDashes();
                        fieldChanged = true;
                    }
                }
            }
        }
        return;
    }

    public boolean PlacingXLeadsToInvalidState(CellState state, int row, int col) {
        TestFieldState cloned = this.copy();
        cloned.UpdateCellState(row, col, state);
        if (state == CellState.Tree) cloned.AutoPlaceDashes();
        return !cloned.NoErrors();
    }

    private void AutoPlaceDashes() {
        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
                if (cells[row][col].state == CellState.Empty && ByTree(row, col)) {
                    UpdateCellState(row, col, CellState.Dash);
                }
            }
        }
    }

    private boolean ByTree(int row, int col) {
        Region region = regions[cells[row][col].region];
        return CellHasNeighborTree(row, col) ||
                rowTreeCount[row] > 0 ||
                columnTreeCount[col] > 0 ||
                region.treeCount > 0;
    }

    private boolean PlacingXWillLeadToInvalidState(CellState newCellState, int rowChange, int colChange) {
        TestFieldState cloned = this.copy();
        cloned.PlaceSimpleDashesAndTrees();
        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
                if (cells[row][col].state != CellState.Empty) {
                    continue;
                }
                if(cloned.PlacingXLeadsToInvalidState(CellState.Tree, row, col)
                        && cloned.PlacingXLeadsToInvalidState(CellState.Dash, row, col)) {
                    return true;
                }
            }
        }
        return false;
    }
}
