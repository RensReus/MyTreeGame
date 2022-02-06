import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestFile{

     public static void main(String []args){
        // initialize field
        int size = 5;
        int[][] field = new int[size][size];
        for (int[] row: field)
            Arrays.fill(row, -1);
//        Arrays.fill(field, -1);
        
        Integer[] starters = ValidShuffle(size);
        // Fill starters
        for (int i = 0; i < size; i++) {
            field[i][starters[i]] = i;
        }

        //build single line region
        int singleLineRow = 1;//(int)(Math.random() * size); // todo ook col als optie (maar dat is gewoon hele veld roteren)
        int len = 3; // todo based on size and random
        int singleLineColStart = starters[singleLineRow];
        int[] toFill;
        if (singleLineColStart == 0 || singleLineColStart == 1) {
            field[singleLineRow][0] = singleLineRow;
            field[singleLineRow][1] = singleLineRow;
            field[singleLineRow][2] = singleLineRow;
            toFill = new int[] {3, 4}; // todo dynamic based on size
        } else if (singleLineColStart == size - 1 || singleLineColStart == size - 2) {
            field[singleLineRow][size - 3] = singleLineRow;
            field[singleLineRow][size - 2] = singleLineRow;
            field[singleLineRow][size - 1] = singleLineRow;
            toFill = new int[] {0, 1}; // todo dynamic based on size
        } else {
            field[singleLineRow][singleLineColStart + 1] = singleLineRow;
            field[singleLineRow][singleLineColStart - 1] = singleLineRow;            
            toFill = new int[] {0, size - 1};  // todo dynamic based on size
        }

        // make other regions grow to fill singleLineCol
        // determine closest field
        // make list of tofill coordinates(wss dit in voorgaande stap doen), make coordinate class?
        // for each coordinate calc manhattan distance to starter fields, make distance func in coordinate class, misschien met this als dat kan in java
        for (int[] arr : field)
            System.out.println(Arrays.toString(arr));

        System.out.println(Arrays.toString(toFill));
        for (int tf : toFill) {
            int closest = -1;
            int closestDist = 100;
            for (int row = 0; row < size; row++){
                if (row == singleLineRow || closestDist == 1)
                    continue;
                for (int col = 0; col < size; col++){
                    if (field[row][col] == -1)
                        continue;
                    int dist = Math.abs(col - tf) + Math.abs(row - singleLineRow);
                    // System.out.printf("INSIDE tf %s Field %s, row, col %s %s, dist %s currclosest %s \n", tf, field[row][col], row, col, dist, closestDist);
                    // System.out.println(dist < closestDist);

                    if (dist < closestDist) {
                        closest = row;
                        closestDist = dist;
                    }
                }
            }
            System.out.printf("tf %s cl %s, CLdist %s\n", tf, closest, closestDist);
            
            field[singleLineRow][tf] = closest;
        }
        for (int[] arr : field)
            System.out.println(Arrays.toString(arr));
        // route bepalen? oef
        // invullen
        // dit 2x

        // en daarna een beetje random velden laten groeien en kijken of er iets afgesloten wordt, CHECK NOTES


     }

     public static Integer[] ValidShuffle(int dimension) {
        Integer[] array = new Integer[dimension];
        Arrays.setAll(array, x -> x);
		List<Integer> intList = Arrays.asList(array);
        while(!IsValid(intList)){
            Collections.shuffle(intList);
        }

		return intList.toArray(array);
    }

    public static boolean IsValid(List<Integer> list) {
        boolean valid = true;
        int dimension = list.size();
        for (int j = 0; j < dimension - 1; j++) {
            valid = Math.abs(list.get(j) - list.get(j + 1)) > 1;
            if (!valid){
                break;
            }
        }
        return valid;
    }
}