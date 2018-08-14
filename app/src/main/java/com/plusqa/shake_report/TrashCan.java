package com.plusqa.shake_report;

import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;

public class TrashCan {

    private FloatingActionButton trashCanFAB;

    private LinearLayout trashCanLayout;

    TrashCan(FloatingActionButton trashCanFAB, LinearLayout trashCanLayout) {
        this.trashCanFAB = trashCanFAB;
        this.trashCanLayout = trashCanLayout;
    }

    public Rect getTrashCanRect() {
        Rect r = new Rect();
        int[] location = new int[2];
        trashCanLayout.getDrawingRect(r);
        trashCanLayout.getLocationOnScreen(location);
        r.offset(location[0], location[1]);
        r.left -= 30; r.top -= 30; r.right += 30; r.bottom += 30;
        return r;
    }

    public FloatingActionButton getTrashCanFAB() {
        return trashCanFAB;
    }

    public LinearLayout getTrashCanLayout() {
        return trashCanLayout;
    }
}
