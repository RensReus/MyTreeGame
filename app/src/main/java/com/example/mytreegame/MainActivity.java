package com.example.mytreegame;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {
    public int ErrorColor = Color.parseColor("#d40000");
    private final Integer[] colors = {
            Color.parseColor("#940085"),
            Color.parseColor("#00a108"),
            Color.parseColor("#00579e"),
            Color.parseColor("#d1b32c"),
            Color.parseColor("#919191")};
    private int size = 5;
    FieldState fieldState;
    private GridLayout layout;
    private int delayCount = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BuildField();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void BuildField() {
        // plaats N trees
//        doe wat random dingen
//        als je in een regio beperkingen nodig heb kan je dan 1 van de velden aan een andere regio geven zonder dat die meer opties krijgt
        // Denken met beperkingen niet mogelijkheden

// https://www.sporcle.com/games/Katie_Wandering/double-trees-logic-puzzle-27?playlist=katie_wanderings-trees-puzzle-playlist-4&creator=Katie_Wandering&pid=1d74a0631C
// had interessante tactiek met linker 3 kolommen
//        int[][] regionSetup = {{ 0, 0, 1, 1, 1},
//                {0, 1, 1, 2, 2},
//                {3, 3, 3, 2, 2},
//                {4, 3, 4, 2, 2},
//                {4, 4, 4, 2, 2}};
//        TestFieldState test = new TestFieldState(this, regionSetup, size);
        // fieldState = new TestFieldState(this, regionSetup, size);
//        ConstructField(regionSetup);
//        int tries = 0;
//        while(!fieldState.IsSolvable(regionSetup) && tries <10) {
//            // build new field of maak aanpassingen
//            regionSetup = GenerateRegionSetupType1();
//            tries++;
//            ConstructField(regionSetup);
//        }
        boolean solvable = false;
        int count = 0;
        int[][] regionSetup = GenerateRegionSetupType1();
        ConstructBasics();
        TextView solvableText = findViewById(R.id.solvableInfo);
        while(!solvable) {
            regionSetup = GenerateRegionSetupType1();
            TestFieldState test = new TestFieldState(regionSetup, size);
            solvable = test.IsSolvable(regionSetup);
            count++;
            solvableText.setText(String.format("Solvable %b tries %d", solvable, count));
        }
        ConstructField(regionSetup);
    }

    private void ConstructBasics() {
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.GridLayout1);
        Button buttonOne = (Button) findViewById(R.id.Rebuild);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                BuildField();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int[][] GenerateRegionSetupType1() {
        // initialize field
        int[][] regionSetup = new int[size][size];
        List<Coordinate> coordinates = new ArrayList<Coordinate>() { };

        for (int[] row: regionSetup)
            Arrays.fill(row, -1);

        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
                coordinates.add(new Coordinate(row, col));
            }
        }

        Integer[] starters = Helper.ValidShuffle(size);
        // Fill starters
        for (int i = 0; i < size; i++) {
            regionSetup[i][starters[i]] = i;
            int finalI = i;
            coordinates.removeIf(c -> c.row == finalI && c.col == starters[finalI]);
        }

        //build single line region
        int singleLineRow = (int)(Math.random() * size); // todo ook col als optie (maar dat is gewoon hele veld roteren)
        int len = 3; // todo based on size and random
        int singleLineColStart = starters[singleLineRow];
        int[] toFill;
        if (singleLineColStart == 0 || singleLineColStart == 1) {
            regionSetup[singleLineRow][0] = singleLineRow;
            regionSetup[singleLineRow][1] = singleLineRow;
            regionSetup[singleLineRow][2] = singleLineRow;
            toFill = new int[] {3, 4}; // todo dynamic based on size
        } else if (singleLineColStart == size - 1 || singleLineColStart == size - 2) {
            regionSetup[singleLineRow][size - 3] = singleLineRow;
            regionSetup[singleLineRow][size - 2] = singleLineRow;
            regionSetup[singleLineRow][size - 1] = singleLineRow;
            toFill = new int[] {0, 1}; // todo dynamic based on size
        } else {
            regionSetup[singleLineRow][singleLineColStart + 1] = singleLineRow;
            regionSetup[singleLineRow][singleLineColStart - 1] = singleLineRow;
            toFill = new int[] {0, size - 1};  // todo dynamic based on size
        }
        coordinates.removeIf(c -> c.row == singleLineRow);

        // make other regions grow to fill singleLineCol
        // determine closest field
        // make list of tofill coordinates(wss dit in voorgaande stap doen), make coordinate class?
        // for each coordinate calc manhattan distance to starter fields, make distance func in coordinate class, misschien met this als dat kan in java
        for (int tf : toFill) {
            int closestRow = -1;
            int closestCol = -1;
            int closestDist = 100;
            for (int row = 0; row < size; row++){
                if (row == singleLineRow || closestDist == 1)
                    continue;
                for (int col = 0; col < size; col++){
                    if (regionSetup[row][col] == -1)
                        continue;
                    int dist = Math.abs(col - tf) + Math.abs(row - singleLineRow);
                    if (dist < closestDist) {
                        closestRow = row;
                        closestCol = col;
                        closestDist = dist;
                    }
                }
            }
            regionSetup[singleLineRow][tf] = closestRow;
            for (int col = 0; col < size; col++) {
                if (col <= Math.max(closestCol, tf) && col >= Math.min(closestCol, tf)) {
                    regionSetup[closestRow][col] = closestRow;
                    int finalClosestRow = closestRow;
                    int finalCol = col;
                    coordinates.removeIf(c -> c.row == finalClosestRow && c.col == finalCol);
                }
            }
            for (int row = 0; row < size; row++) {
                if (row <= Math.max(closestRow, singleLineRow) && row >= Math.min(closestRow, singleLineRow)) {
                    regionSetup[row][tf] = closestRow;
                    int finalRow = row;
                    coordinates.removeIf(c -> c.row == finalRow && c.col == tf);
                }
            }
        }

        Collections.shuffle(coordinates);
        while(coordinates.size() > 0){
            Coordinate coord = coordinates.remove(0);
            ArrayList neighborTypes = GetNeighborTypes(coord, regionSetup);
            if (neighborTypes.size() == 0) {
                coordinates.add(coord);
                continue;
            }
            regionSetup[coord.row][coord.col] = getRandomElement(neighborTypes);
        }

        return regionSetup;
    }

    private ArrayList GetNeighborTypes(Coordinate coord, int[][] field){
        ArrayList resp = new ArrayList<Integer>();
        for (int row : new int[]{coord.row - 1, coord.row + 1 } ){
            if (row >= 0 && row < size && field[row][coord.col] != -1){
                resp.add(field[row][coord.col]);
            }
        }
        for (int col : new int[]{coord.col - 1, coord.col + 1 } ){
            if (col >= 0 && col < size && field[coord.row][col] != -1){
                resp.add(field[coord.row][col]);
            }
        }
        return resp;
    }

    public int getRandomElement(List<Integer> list)
    {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    private void ConstructField(int[][] regionSetup) {
        layout.setColumnCount(size);
        layout.setRowCount(size);
        fieldState = new FieldState(regionSetup, size);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenSize = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int fieldSize = (screenSize - 64)/size;
        float textSize = (float)(fieldSize / 4);

        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
//                Cell btn = fieldState.GetCell(row, col);
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

    private void CheckIfSolved() { // update ook error states van field, betere naam nodig of opsplitsen
        if(fieldState.Solved())
        {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Yeah opgelost");
            dlgAlert.setTitle("App Title");
//            dlgAlert.setPositiveButton("New Game", null);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton("New Game",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BuildField();
                }
            });
            dlgAlert.create().show();
        }
    }

//    private boolean HasNeighborTree(int a, int b) {
//        Integer trees = 0;
//        for (int x = a - 1; x < a + 2; x++){
//            if (x < 0 || x >= size)
//            {
//                continue;
//            }
//            for (int y = b - 1; y < b + 2; y++) {
//                if (y < 0 || y >= size || (x == a && y == b))
//                {
//                    continue;
//                }
//                trees += btnWord[x][y].getText() == "T" ? 1 : 0;
//            }
//        }
//        return trees == 1;
//    }

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
                case Tree: newState = CellState.Dash; break;
                case Dash: newState = CellState.Empty; break;
                default: newState = CellState.Tree;
            }
        } else {
            switch (oldState) {
                case Tree: newState = CellState.Empty; break;
                case Dash: newState = CellState.Tree; break;
                default: newState = CellState.Dash;
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
                        if(delayCount == 0) {
                            UpdateNewDisplayErrors();
                        }
                    }
                },
                1000
        );
    }

    private void UpdateNewDisplayErrors() {
        for (int i = 0; i < size*size; i++) {
            Button btn = layout.findViewById(i);
            Cell cell = fieldState.GetCell(btn);
            if (cell.HasError) {
                btn.setTextColor(ErrorColor);
            }
        }
    }

    private void UpdateDisplayTextAndFixedErrors() {
        for (int i = 0; i < size*size; i++) {
            Button btn = layout.findViewById(i);
            Cell cell = fieldState.GetCell(btn);
            switch (cell.state) {
                case Tree: btn.setText("T"); break;
                case Dash: btn.setText("-"); break;
                case Empty: btn.setText(""); break;
            }
            if (!cell.HasError) {
                btn.setTextColor(Color.BLACK);
            }
        }
    }
}
