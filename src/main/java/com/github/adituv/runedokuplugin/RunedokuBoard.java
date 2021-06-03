package com.github.adituv.runedokuplugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

@Getter
@Slf4j
public class RunedokuBoard {
    private final Widget containerWidget;
    private final RunedokuCell[] cells;

    private final int width;

    private final int[][] rowNumbers;
    private final int[][] colNumbers;
    private final int[][] boxNumbers;

    public boolean cellHasClash(RunedokuCell cell) {
        int sudokuIndex = cell.getSudokuNumber() - 1;
        return ( rowNumbers[cell.getRowNumber()][sudokuIndex] > 1
                || colNumbers[cell.getColumnNumber()][sudokuIndex] > 1
                || boxNumbers[cell.getBoxNumber()][sudokuIndex] > 1);
    }

    public void updateCells() {
        Widget[] children = containerWidget.getChildren();
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < width; j++) {
                rowNumbers[i][j] = 0;
                colNumbers[i][j] = 0;
                boxNumbers[i][j] = 0;
            }
        }

        for(int i = 0; i < children.length; i++) {
            cells[i].updateFromWidget(children[i]);
            int sudokuIndex = cells[i].getSudokuNumber() - 1;

            if(sudokuIndex >= 0) {
                this.rowNumbers[cells[i].getRowNumber()][sudokuIndex]++;
                this.colNumbers[cells[i].getColumnNumber()][sudokuIndex]++;
                this.boxNumbers[cells[i].getBoxNumber()][sudokuIndex]++;
            }
        }
    }

    public RunedokuCell getCell(int index) {
        if(index > 0 && index < cells.length) {
            return cells[index];
        } else {
            return null;
        }
    }

    public RunedokuBoard(Widget containerWidget) {
        this.containerWidget = containerWidget;

        Widget[] children = containerWidget.getChildren();

        if(children.length == 81) {
            width = 9;
            cells = new RunedokuCell[81];

            for(int i = 0; i < children.length; i++) {
                cells[i] = new RunedokuCell(width, i);
            }

            rowNumbers = new int[9][9];
            colNumbers = new int[9][9];
            boxNumbers = new int[9][9];
        } else if(children.length == 16) {
            width = 4;
            cells = new RunedokuCell[16];

            for(int i = 0; i < children.length; i++) {
                cells[i] = new RunedokuCell(width,i);
            }

            rowNumbers = new int[4][4];
            colNumbers = new int[4][4];
            boxNumbers = new int[4][4];
        } else {
            log.error(String.format("constructor: invalid board size %d", children.length));
            width = 0;
            cells = null;
            rowNumbers = null;
            colNumbers = null;
            boxNumbers = null;
        }
    }
}
