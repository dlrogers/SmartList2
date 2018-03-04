package com.symdesign.smartlist;

/**
 * Created by dennis on 9/13/16.
 *
 * Represents the flags associated with each list item
 *
 * flags =     flags: bit0 ? (in shopping list) : 1 (in suggest list)   inList
 *                    bit1 ? (to be deleted) : (normal)                 deleted
 *                    bit2 ? (changed since last sync) : (normal)       changed
 *                    bit3 ? has cloud sync id : no cloud sync id       synced
 */

public class Flags {

    int bits;

    public Flags(int flags){
        bits = flags;
    }

    public Flags() {
        bits = 0;
    }

    boolean inList() {
        return (bits&1)>0;
    }
    boolean deleted() {
        return (bits&2)>0;
    }
    boolean changed() {
        return (bits&4)>0;
    }
    boolean synced()    { return (bits&8)>0; }


    void setList() {bits = bits | 1 ;}
    void setDeleted() {
        bits = bits | 2 ;
    }
    void setChg() {bits = bits | 4 ; }
    void setSync() {
        bits = bits | 8 ;
    }

    void clrList() {
        bits = bits & 1;
    }
    void clrDeleted() {
        bits = bits & 2;
    }
    void clrChg() {
        bits = bits & 4;
    }
    void clrSync() {
        bits = bits & 8;
    }
    void clearAll() {
        bits = 0;
    }
}
