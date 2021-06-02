package com.github.adituv.runedokuplugin;

public enum RunedokuRune {
    WATER_RUNE(555,1),
    FIRE_RUNE(554,2),
    EARTH_RUNE(557,3),
    AIR_RUNE(556,4),
    MIND_RUNE(558,5),
    BODY_RUNE(559,6),
    LAW_RUNE(563,7),
    CHAOS_RUNE(562,8),
    DEATH_RUNE(560,9);

    private final int itemId;
    private final int sudokuNumber;

    RunedokuRune(int itemID, int sudokuNumber) {
        this.itemId = itemID;
        this.sudokuNumber = sudokuNumber;
    }

    int getItemId() {
        return this.itemId;
    }

    int getSudokuNumber() {
        return this.sudokuNumber;
    }

    public static RunedokuRune getByItemId(int itemId) {
        for(RunedokuRune rune : RunedokuRune.values()) {
            if(rune.getItemId() == itemId) {
                return rune;
            }
        }

        return null;
    }

}
