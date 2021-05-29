package com.github.adituv.runedoku;

import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;

public class RunedokuUtil {

    public static int[][] getPuzzleFromBoard(Widget board) {
        int[][] result;
        int size = 0;

        if(board.getChildren().length == 16) {
            result = new int[4][4];
            size = 4;
        }
        else if(board.getChildren().length == 81) {
            result = new int[9][9];
            size = 9;
        }
        else {
            return null;
        }

        for(int j = 0; j < size; j++) { // Rows
            for (int i = 0; i < size; i++) { // Columns
                Widget w = board.getChild(j * size + i);

                // Only consider pieces with a white outline - i.e. the preplaced pieces
                if (w.getBorderType() == 2) {
                    result[j][i] = itemIdToSudokuNumber(w.getItemId());
                }
            }
        }

        return result;
    }

    public static int itemIdToSudokuNumber(int itemId) {
        switch(itemId) {
            case ItemID.WATER_RUNE:
                return 1;
            case ItemID.FIRE_RUNE:
                return 2;
            case ItemID.EARTH_RUNE:
                return 3;
            case ItemID.AIR_RUNE:
                return 4;
            case ItemID.MIND_RUNE:
                return 5;
            case ItemID.BODY_RUNE:
                return 6;
            case ItemID.LAW_RUNE:
                return 7;
            case ItemID.CHAOS_RUNE:
                return 8;
            case ItemID.DEATH_RUNE:
                return 9;
            default:
                return 0;
        }
    }

    public static int sudokuNumberToItemId(int sudokuNumber) {
        switch(sudokuNumber) {
            case 1:
                return ItemID.WATER_RUNE;
            case 2:
                return ItemID.FIRE_RUNE;
            case 3:
                return ItemID.EARTH_RUNE;
            case 4:
                return ItemID.AIR_RUNE;
            case 5:
                return ItemID.MIND_RUNE;
            case 6:
                return ItemID.BODY_RUNE;
            case 7:
                return ItemID.LAW_RUNE;
            case 8:
                return ItemID.CHAOS_RUNE;
            case 9:
                return ItemID.DEATH_RUNE;
            default:
                return -1;
        }
    }

}
