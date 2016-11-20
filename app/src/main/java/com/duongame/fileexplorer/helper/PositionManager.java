package com.duongame.fileexplorer.helper;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-20.
 */

public class PositionManager {
    private static HashMap<String, Integer> positionMap = new HashMap<String, Integer>();

    public static void setPosition(String path, int position) {
        positionMap.put(path, position);
    }

    public static int getPosition(String path) {
        if(positionMap.containsKey(path)) {
            return positionMap.get(path);
        }
        return 0;
    }
}
