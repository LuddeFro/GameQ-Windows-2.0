package io.gameq.gameqwindows;

import java.util.HashMap;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class PacketMap extends HashMap<Integer,Integer> {

    public PacketMap(int [] keys) {
        for (int key: keys){
            put(key,0);
        }
    }


}
