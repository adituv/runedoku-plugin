package com.github.adituv.runedokuplugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

/**
 * A data structure to store the current state of the board
 */
@Getter
@Slf4j
public class RunedokuBoard {
    private final Widget containerWidget;
    private final RunedokuCell[] cells;

    private final int width;

    private final int[][] rowNumbers;
    private final int[][] colNumbers;
    private final int[][] boxNumbers;
    private SudokuSolver solver;

    /**
     Check cell to verify it does not exist in an invalid location
     @param cell Runedoku cell to check
     */
    public boolean cellHasClash(RunedokuCell cell) {
        int sudokuIndex = cell.getSudokuNumber() - 1;
        return ( rowNumbers[cell.getRowNumber()][sudokuIndex] > 1
                || colNumbers[cell.getColumnNumber()][sudokuIndex] > 1
                || boxNumbers[cell.getBoxNumber()][sudokuIndex] > 1);
    }

    /**
     Check that cell has a solution. If showSolution is off, this will always return false
     @param cell Runedoku cell to check
     */
    public boolean cellHasSolution(RunedokuCell cell) {
        return cell.getSolutionNumber() != 0;
    }

    /**
     * Grab widget and update each RunedokuCell within the board with new values
     */
    public void updateCells() {
        Widget[] children = containerWidget.getChildren();
        if (children == null) {
            log.error("error: children null");
            return;
        }

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

    /**
     * Generate solution to current board state, if possible
     * Updates each cell to contain its the solution value
     * Accessible at cell.solutionNumber
     *
     * This function only runs once at the creation of the board, as the solution algorithm is computationally expensive
     * especially if someone creates an unsolvable board
     */
    public void solveBoard() {
        // try to solve the board
        if (solver.solve()) {

            // grab the solved board state
            int[][] solutionGrid = solver.getSolution();

            for(int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    // Update each cell with its solution value
                    cells[(i * width) + j].updateSolution(solutionGrid[i][j]);
                }
            }
        } else {
            // Hopefully we never get here, as runescape should always generate a solvable board.
            log.error("Unsolvable board state");
        }
    }

    /**
     * Get cell at a specific offset
     * @param   index   offset
     * @return  RunedokuCell at offset
     */
    public RunedokuCell getCell(int index) {
        if(index >= 0 && index < cells.length) {
            return cells[index];
        } else {
            return null;
        }
    }

    /**
     * Create a new Runedoku board based on the given widget
     * @param containerWidget A runedoku UI widget
     */
    public RunedokuBoard(Widget containerWidget, boolean solve) {
        this.containerWidget = containerWidget;

        Widget[] children = containerWidget.getChildren();

        if(children != null) {
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
            if (solve) {
                if (cells != null) {
                    int[][] solutionBuilder = new int[9][9];

                    for(int i = 0; i < children.length; i++) {
                        cells[i].updateFromWidget(children[i]);
                        int sudokuIndex = cells[i].getSudokuNumber() - 1;

                        solutionBuilder[cells[i].getRowNumber()][cells[i].getColumnNumber()] = sudokuIndex + 1;
                    }
                    solver = new SudokuSolver(solutionBuilder);

                    // this should never error because runescape should always generate a solvable board
                    solveBoard();
                }
            }
        } else {
            log.error("render: containerWidget children array is null");
            width = 0;
            cells = null;
            rowNumbers = null;
            colNumbers = null;
            boxNumbers = null;
        }
    }
}
