package com.github.adituv.runedokuplugin;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

@Getter
@Slf4j
public class RunedokuCell {
    private RunedokuRune rune;
    private int sudokuNumber;
    private Widget widget;

    private int columnNumber;
    private int rowNumber;
    private int boxNumber;

    @Setter
    private boolean[] marks = new boolean[9];

    public RunedokuCell(int boardWidth, int cellId) {
        this.columnNumber = cellId % boardWidth;
        this.rowNumber = cellId / boardWidth;

        if (boardWidth == 9) {
            this.boxNumber = 3*(rowNumber/3) + (columnNumber/3);
        } else if(boardWidth == 4) {
            this.boxNumber = 2*(rowNumber/2) + (columnNumber/2);
        } else {
            log.error(String.format("constructor: invalid boardWidth %d", boardWidth));
        }
    }

    public void updateFromWidget(Widget w) {
        this.widget = w;
        this.rune = RunedokuRune.getByItemId(w.getItemId());
        this.sudokuNumber = this.rune != null ? rune.getSudokuNumber() : 0;
    }
}
