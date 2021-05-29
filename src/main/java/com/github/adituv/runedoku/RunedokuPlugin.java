package com.github.adituv.runedoku;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Runedoku"
)
public class RunedokuPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RunedokuConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RunedokuOverlay runedokuOverlay;

	private Widget runeWidget;
	private Widget boardWidget;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(runedokuOverlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(runedokuOverlay);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if(widgetLoaded.getGroupId() == RunedokuConstants.RUNEDOKU_WIDGET_GROUP_ID) {
			this.boardWidget = client.getWidget(RunedokuConstants.RUNEDOKU_BOARD_WIDGET);
			this.runeWidget = client.getWidget(RunedokuConstants.RUNEDOKU_RUNE_WIDGET);

			int[][] puzzle = RunedokuUtil.getPuzzleFromBoard(boardWidget);
			SudokuSolver solver = new SudokuSolver(puzzle);

			log.info("Runedoku interface opened.");
			log.info(String.format("Board:\n%s", Arrays.deepToString(puzzle)));

			if(!solver.solve()) {
				log.error("No solution found.");
			}
			else {
				int[][] solution = solver.getSolution();
				log.info(String.format("Solution:\n%s", Arrays.deepToString(solution)));
				runedokuOverlay.setSolution(solution);
			}
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if(widgetClosed.getGroupId() == RunedokuConstants.RUNEDOKU_WIDGET_GROUP_ID) {
			log.info("Runedoku interface closed.");
			runedokuOverlay.setSolution(null);
		}
	}

	@Provides
	RunedokuConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RunedokuConfig.class);
	}
}
