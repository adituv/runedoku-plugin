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
    private int solutionNumber;
    private Widget widget;

    private final int columnNumber;
    private final int rowNumber;
    private final int boxNumber;

    @Setter
    private boolean[] marks = new boolean[9];

    /**
     * Data structure containing a rune, mark, or solution at a position
     * @param boardWidth size of the board 9 or 4
     * @param cellId index of cell, translates to row and column
     */
    public RunedokuCell(int boardWidth, int cellId) {
        this.columnNumber = cellId % boardWidth;
        this.rowNumber = cellId / boardWidth;

        if (boardWidth == 9) {
            this.boxNumber = 3*(rowNumber/3) + (columnNumber/3);
        } else if(boardWidth == 4) {
            this.boxNumber = 2*(rowNumber/2) + (columnNumber/2);
        } else {
            log.error(String.format("constructor: invalid boardWidth %d", boardWidth));
            this.boxNumber = 0;
        }
    }

    /**
     * Update cell from UI widget
     * @param w Widget of a specific cell in the board
     */
    public void updateFromWidget(Widget w) {
        this.widget = w;
        this.rune = RunedokuRune.getByItemId(w.getItemId());
        this.sudokuNumber = this.rune != null ? rune.getSudokuNumber() : 0;
    }

    /**
     * Literally a setter
     * Set the value of this cell that would lead to a solved board state
     * @param solution integer value of solution to cell
     */
    public void updateSolution(int solution) {
        solutionNumber = solution;
    }
}
