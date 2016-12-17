package com.duongame.explorer.bitmap;

import com.duongame.explorer.adapter.ExplorerFileItem;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class PageKey implements Cloneable {
    public String path;
    public ExplorerFileItem.Side side;

    public PageKey(String path, ExplorerFileItem.Side side) {
        this.path = path;
        this.side = side;
    }

    @Override
    public boolean equals(Object obj) {
        PageKey key = (PageKey)obj;
        if(key.path == this.path
                && key.side == this.side)
            return true;
        return false;
    }

    @Override
    public Object clone() {
        Object item;
        try {
            item = super.clone();
            return item;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
