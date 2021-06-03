package com.github.adituv.runedokuplugin;

import net.runelite.api.widgets.WidgetInfo;

public final class RunedokuConstants {
    public static final String RUNEDOKU_CONFIG_GROUP = "runedoku";

    public static final int RUNEDOKU_WIDGET_GROUP_ID = 292;
    public static final int RUNEDOKU_CONTAINER_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID, 0);
    public static final int RUNEDOKU_BOARD_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID,13);
    public static final int RUNEDOKU_RUNE_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID,9);
    public static final int RUNEDOKU_BUTTON_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID,10);
    public static final int RUNEDOKU_EXIT_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID, 15);
    public static final int RUNEDOKU_BOARD_CLICK_WIDGET_ID = WidgetInfo.PACK(RUNEDOKU_WIDGET_GROUP_ID, 14);
}
