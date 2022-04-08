package com.example.mytreegame;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.TextView;

import com.example.mytreegame.MyClasses.Cell;
import com.example.mytreegame.MyClasses.CellState;
import com.example.mytreegame.MyClasses.FieldState;
import com.example.mytreegame.MyClasses.Coordinate;
import com.example.mytreegame.MyClasses.TestFieldState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {
    public int ErrorColor = Color.parseColor("#d40000");
    private final Integer[] colors = {
            Color.parseColor("#940085"),
            Color.parseColor("#00a108"),
            Color.parseColor("#00579e"),
            Color.parseColor("#d1b32c"),
            Color.parseColor("#919191"),
            Color.GREEN,
            Color.BLUE };
    private int size = 5;
    FieldState fieldState;
    private GridLayout layout;
    private int delayCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConstructBasics();
        BuildField();
    }

    public void BuildField() {
        // plaats N trees
        // doe wat random dingen
        // als je in een regio beperkingen nodig heb kan je dan 1 van de velden aan een
        // andere regio geven zonder dat die meer opties krijgt
        // Denken met beperkingen niet mogelijkheden

        // https://www.sporcle.com/games/Katie_Wandering/double-trees-logic-puzzle-27?playlist=katie_wanderings-trees-puzzle-playlist-4&creator=Katie_Wandering&pid=1d74a0631C
        // had interessante tactiek met linker 3 kolommen
        // int[][] regionSetup = {{ 0, 0, 1, 1, 1},
        // {0, 1, 1, 2, 2},
        // {3, 3, 3, 2, 2},
        // {4, 3, 4, 2, 2},
        // {4, 4, 4, 2, 2}};
        // TestFieldState test = new TestFieldState(this, regionSetup, size);
        EditText simpleEditText = (EditText) findViewById(R.id.sizeInput);
        if (simpleEditText != null) {
            int newSize = tryParseNewSize(simpleEditText.getText().toString());
            if (newSize != size && NotForbiddenSizes(newSize)) {
                size = newSize;
                ConstructBasics();
            }
            simpleEditText.setText("" + size);
        }
        boolean solvable = false;
        int[][] regionSetup;
        int[][] validRegionSetup = new int[size][size];
        TextView solvableText = findViewById(R.id.solvableInfo);
        int solvableCount = 0;
        // get average time to valid solvable
        final long startTime = System.currentTimeMillis();
        while (!solvable || ExtraRequirements(true)) {
            regionSetup = GenerateRegionSetupType2();
            TestFieldState test = new TestFieldState(regionSetup, size);
            solvable = test.IsSolvable(regionSetup);
            if (solvable) {
                solvableCount++;
                validRegionSetup = regionSetup;
            }
        }

        final long endTime = System.currentTimeMillis();
        solvableText.setText(String.format("average %d", (endTime - startTime) / solvableCount));
        ConstructField(validRegionSetup);
    }

    private boolean ExtraRequirements(boolean apply) {
        return apply && (solvableCount < 100 && System.currentTimeMillis() - startTime < 10000);
    }

    private boolean NotForbiddenSizes(int newSize) {
        return !(newSize == 2 || newSize == 3 || newSize > 7 || newSize < 1);
    }

    public int tryParseNewSize(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return size;
        }
    }

    private void ConstructBasics() {
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.GridLayout1);
        EditText simpleEditText = (EditText) findViewById(R.id.sizeInput);
        simpleEditText.setText("" + size);
        simpleEditText.setOnKeyListener(rebuildOnEnter);
        Button buttonOne = (Button) findViewById(R.id.Rebuild);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                BuildField();
            }
        });
    }

    private int[][] GenerateRegionSetupType2() {
        // initialize field
        int[][] regionSetup = new int[size][size];
        List<Coordinate>[] regions = new ArrayList[size];
        List<Coordinate> coordinates = new ArrayList<Coordinate>() {
        };

        for (int[] row : regionSetup) {
            Arrays.fill(row, -9);
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                coordinates.add(new Coordinate(row, col));
            }
        }

        Integer[] starterCols = Helper.ValidShuffle(size);
        // Fill starters
        for (int i = 0; i < size; i++) {
            int col = starterCols[i];
            int row = i;
            regions[i] = new ArrayList<Coordinate>() {
            };
            regionSetup[i][col] = i;
            regions[i].add(new Coordinate(row, col));
            coordinates.removeIf(c -> c.row == row && c.col == col);
        }

        // each region try to place base shape at least size 3
        if (size > 1) {
            for (int row = 0; row < size; row++) {
                var shape = GetRandomDynamicShape(size);
                int col = starterCols[row];
                var cellsToAdd = FitShape(shape, regionSetup, row, col);
                // Coordinate toAdd = GetRandomManhattanNeighbour(row, col);
                if (regionSetup[toAdd.row][toAdd.col] < 0) {
                    regionSetup[toAdd.row][toAdd.col] = row;
                    regions[row].add(toAdd);
                    coordinates.removeIf(c -> c.row == toAdd.row && c.col == toAdd.col);
                }
            }
        }

        regionSetup = FillRandomly(coordinates, regionSetup, -1); // todo exclude single line

        return regionSetup;
    }

    // Return tuple<boolean fits, ListCoords>
    private static List<Coords> FitShape(Shape shapes, int[][] regionShape, int baseRow, int baseCol, int size) {
        for (List<Coords> shape : shapes) {
            for (Coordinate coord : shape) {
                var newRow = baseRow + coord.row;
                var newCol = baseCol + coord.col;
                if (newRow > size || newRow < 0 || newCol > size || newCol < 0 || regionShape[newRow][newCol] != -1) {
                    continue;
                }
                return shape;
            }
        }
    }

    private List<Shape> GetRandomShape(int size) {
        // From generated shapes file
        // filter by size
        // apply bonus?
        // pick random from filtered list
        // shuffle to prevent repetition
    }

    private int[][] FillRandomly(List<Coordinate> coordinates, int[][] regionSetup, int exclude) {
        Collections.shuffle(coordinates);
        while (coordinates.size() > 0) {
            Coordinate coord = coordinates.remove(0);
            int region = GetRandomNeighbourRegion(coord, regionSetup);
            if (region < 0 || region == exclude) {
                coordinates.add(coord);
                continue;
            }
            regionSetup[coord.row][coord.col] = region;
        }
        return regionSetup;
    }

    private Coordinate GetCoordinateToExtendLine(List<Coordinate>[] regions, int[][] regionSetup) {
        int region = (int) (Math.random() * size);
        List<Coordinate> extendOptions = new ArrayList<Coordinate>();
        int row0 = regions[region].get(0).row;
        int row1 = regions[region].get(1).row;
        int col0 = regions[region].get(0).col;
        int col1 = regions[region].get(1).col;
        if (col0 == col1) {
            extendOptions.add(new Coordinate(Math.min(row0, row1) - 1, col0, region));
            extendOptions.add(new Coordinate(Math.max(row0, row1) + 1, col0, region));
        } else {
            extendOptions.add(new Coordinate(row0, Math.min(col0, col1) - 1, region));
            extendOptions.add(new Coordinate(row0, Math.max(col0, col1) + 1, region));
        }
        extendOptions.removeIf(x -> NotInsideGrid(x));
        extendOptions.removeIf(x -> regionSetup[x.row][x.col] >= 0);
        if (extendOptions.size() == 0) {
            return GetCoordinateToExtendLine(regions, regionSetup);
        }
        return getRandomElement(extendOptions);
    }

    private boolean NotInsideGrid(Coordinate x) {
        return x.col < 0 || x.col >= size || x.row < 0 || x.row >= size;
    }

    private Coordinate GetRandomManhattanNeighbour(int row, int col) {
        List<Coordinate> neighbours = GetManhattanNeighbours(row, col);
        return getRandomElement(neighbours);
    }

    private int GetRandomNeighbourRegion(Coordinate coord, int[][] field) {
        List<Coordinate> neighbours = GetManhattanNeighbours(coord.row, coord.col);
        for (Coordinate c : neighbours) {
            c.region = field[c.row][c.col];
        }
        neighbours.removeIf(x -> field[x.row][x.col] < 0);
        if (neighbours.size() == 0) {
            return -1;
        }
        return getRandomElement(neighbours).region;
    }

    private List<Coordinate> GetManhattanNeighbours(int row, int col) {
        List<Coordinate> neighbours = new ArrayList<Coordinate>();
        neighbours.add(new Coordinate(row - 1, col));
        neighbours.add(new Coordinate(row + 1, col));
        neighbours.add(new Coordinate(row, col - 1));
        neighbours.add(new Coordinate(row, col + 1));
        neighbours.removeIf(x -> NotInsideGrid(x));
        return neighbours;
    }

    public <T> T getRandomElement(List<T> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    private void ConstructField(int[][] regionSetup) {
        layout.removeAllViews();
        layout.setColumnCount(size);
        layout.setRowCount(size);
        fieldState = new FieldState(regionSetup, size);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenSize = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int fieldSize = (screenSize - 64) / size;
        float textSize = (float) (fieldSize / 4);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Cell btn = fieldState.GetCell(row, col);
                Button btn = new Button(this);
                int regionId = regionSetup[row][col];
                btn.setTag(R.id.row, row);
                btn.setTag(R.id.col, col);
                btn.setId(row * size + col);
                btn.setTextSize(textSize);
                btn.setOnClickListener(btnShortClick);
                btn.setOnLongClickListener(btnLongClick);
                btn.setBackgroundColor(colors[regionId]);
                LayoutParams params = new LayoutParams();
                params.width = fieldSize;
                params.height = fieldSize;
                params.setMargins(2, 2, 2, 2);
                btn.setLayoutParams(params);
                layout.addView(btn);
            }
        }
    }

    private void CheckIfSolved() {
        if (fieldState.Solved()) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Yeah opgelost");
            dlgAlert.setTitle("App Title");
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BuildField();
                }
            });
            dlgAlert.create().show();
        }
    }

    // private boolean HasNeighborTree(int a, int b) {
    // Integer trees = 0;
    // for (int x = a - 1; x < a + 2; x++){
    // if (x < 0 || x >= size)
    // {
    // continue;
    // }
    // for (int y = b - 1; y < b + 2; y++) {
    // if (y < 0 || y >= size || (x == a && y == b))
    // {
    // continue;
    // }
    // trees += btnWord[x][y].getText() == "T" ? 1 : 0;
    // }
    // }
    // return trees == 1;
    // }

    View.OnClickListener btnShortClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HandleClick(v, false);
        }
    };

    View.OnLongClickListener btnLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            HandleClick(v, true);
            return true;
        }
    };

    private void HandleClick(View v, boolean longPress) {
        CellState oldState = fieldState.GetCell(v).state;
        CellState newState;
        if (longPress) {
            switch (oldState) {
                case Tree:
                    newState = CellState.Dash;
                    break;
                case Dash:
                    newState = CellState.Empty;
                    break;
                default:
                    newState = CellState.Tree;
            }
        } else {
            switch (oldState) {
                case Tree:
                    newState = CellState.Empty;
                    break;
                case Dash:
                    newState = CellState.Tree;
                    break;
                default:
                    newState = CellState.Dash;
            }
        }
        fieldState.UpdateCellState(v, newState);
        CheckIfSolved(); // updates error states
        UpdateDisplayTextAndFixedErrors();

        delayCount++;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        delayCount--;
                        if (delayCount == 0) {
                            UpdateNewDisplayErrors();
                        }
                    }
                },
                500);
    }

    private void UpdateNewDisplayErrors() {
        for (int i = 0; i < size * size; i++) {
            Button btn = layout.findViewById(i);
            Cell cell = fieldState.GetCell(btn);
            if (cell.HasError) {
                btn.setTextColor(ErrorColor);
            }
        }
    }

    private void UpdateDisplayTextAndFixedErrors() {
        for (int i = 0; i < size * size; i++) {
            Button btn = layout.findViewById(i);
            Cell cell = fieldState.GetCell(btn);
            switch (cell.state) {
                case Tree:
                    btn.setText("T");
                    break;
                case Dash:
                    btn.setText("-");
                    break;
                case Empty:
                    btn.setText("");
                    break;
            }
            if (!cell.HasError) {
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    View.OnKeyListener rebuildOnEnter = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    v.clearFocus();
                    BuildField();
                }
                return true;
            }
            return false;
        }
    };
}
