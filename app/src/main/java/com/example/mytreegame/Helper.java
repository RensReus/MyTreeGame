package com.example.mytreegame;

import android.os.Build;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

import androidx.annotation.RequiresApi;

public class Helper {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Integer[] ValidShuffle(int dimension) {
        Integer[] array = new Integer[dimension];
        Arrays.setAll(array, new IntFunction<Integer>() {
            @Override
            public Integer apply(int x) {
                return x;
            }
        });
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
