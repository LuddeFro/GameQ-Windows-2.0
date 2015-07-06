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

    public void printMap(){
        for (int name: this.keySet()){
            String value = this.get(name).toString();
            System.out.print(name + " : " + value + ", ");
        }
        System.out.println();
    }
}
