package com.github.adituv.runedokuplugin;

// Naive backtracking sudoku solver
public class SudokuSolver {
	private final int size;

	// The initial puzzle grid
	private final int[][] puzzle;

	// The working copy of the puzzle
	private final int[][] board;

	private boolean additionIsValid(int x, int y, int n) {
		for(int i = 0; i < size; i++) {
			if(board[y][i] == n) {
				// Number already present in row
				return false;
			}
		}

		for(int j = 0; j < size; j++) {
			if(board[j][x] == n) {
				// Number already present in column
				return false;
			}
		}

		for(int k = 0; k < size; k++) {
			int i,j;

			if(size == 4) {
				i = (x/2)*2 + k%2;
				j = (y/2)*2 + k/2;
			}
			else {
				i = (x/3)*3 + k%3;
				j = (y/3)*3 + k/3;
			}

			if(board[j][i] == n) {
				// Number already present in box
				return false;
			}
		}

		return true;
	}

	public int[][] getSolution() {
		return board;
	}

	public boolean solve() {
		for(int j = 0; j < size; j++) {
			for(int i = 0; i < size; i++) {
				if(board[j][i] == 0) {
					for(int n = 1; n <= size; n++) {
						if(additionIsValid(i,j,n)) {
							board[j][i] = n;

							if(solve()) {
								return true;
							} else {
								board[j][i] = 0;
							}
						}
					}

					// We have tried every number for this cell and none are valid
					return false;
				}
			}
		}

		return true;
	}

	public SudokuSolver(int[][] puzzle) {
		this.size = puzzle.length;
		this.puzzle = puzzle;
		this.board = new int[size][size];

		for(int j = 0; j < size; j++) {
			for(int i = 0; i < size; i++) {
				this.board[j][i] = puzzle[j][i];
			}
		}
	}
}