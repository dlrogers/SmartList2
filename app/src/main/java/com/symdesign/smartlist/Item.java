package com.symdesign.smartlist;

/**
 * Created by dennis on 6/22/16.
 */
public class Item implements Comparable<Item>  {
    long id;
    String name;
    long inList,last_time,last_avg;
    float ratio;
    public Item(long _id,String nm,long il,long lt,long la,float rat) {
        this.id = _id;
        this.name = nm;
        this.inList = il;
        this.last_time = lt;
        this.last_avg = la;
        this.ratio = rat;
    }
    @Override
    public int compareTo(Item compare_item) {
        return (this.ratio < compare_item.ratio) ? 1 : -1;
    }
}
