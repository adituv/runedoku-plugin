package com.github.adituv.runedoku;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Runedoku"
)
public class RunedokuPlugin extends Plugin
{
	protected final int RUNEDOKU_WIDGET_GROUP_ID = 292;
	protected final int RUNEDOKU_BOARD_WIDGET = WidgetInfo.PACK(292,13);
	protected final int RUNEDOKU_RUNE_WIDGET = WidgetInfo.PACK(292, 9);

	@Inject
	private Client client;

	@Inject
	private RunedokuConfig config;

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if(widgetLoaded.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			Widget board = client.getWidget(RUNEDOKU_BOARD_WIDGET);
			int[][] puzzle = getPuzzleFromBoard(board);
			SudokuSolver solver = new SudokuSolver(puzzle);

			log.info("Runedoku interface opened.");
			log.info(String.format("Board:\n%s", Arrays.deepToString(puzzle)));

			if(!solver.solve()) {
				log.info("No solution found.");
			}
			else {
				int[][] solution = solver.getSolution();
				log.info(String.format("Solution:\n%s", Arrays.deepToString(solution)));
			}

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Runedoku", "Runedoku interface opened.", null);

		}
	}

	private int[][] getPuzzleFromBoard(Widget board) {
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

	private int itemIdToSudokuNumber(int itemId) {
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

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if(widgetClosed.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			log.info("Runedoku interface closed.");
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Runedoku", "Runedoku interface closed.", null);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		if(menuOptionClicked.getWidgetId() == RUNEDOKU_RUNE_WIDGET) {
			log.info(String.format("Clicked rune selection panel.  Item index: %d", menuOptionClicked.getSelectedItemIndex()));
		}
	}

	@Provides
	RunedokuConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RunedokuConfig.class);
	}
}
