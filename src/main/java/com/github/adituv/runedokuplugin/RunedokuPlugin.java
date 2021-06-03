package com.github.adituv.runedokuplugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import static com.github.adituv.runedokuplugin.RunedokuConstants.*;

@Slf4j
@PluginDescriptor(
	name = "Runedoku Helper"
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
	private RunedokuOverlay overlay;

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if(configChanged.getGroup().equals(RUNEDOKU_CONFIG_GROUP)) {
			if(configChanged.getKey().equals("useNumbers")) {
				overlay.setShouldDrawNumbers(config.useNumbers());
			} else if(configChanged.getKey().equals("foregroundColor")) {
				overlay.setForegroundColor(config.foregroundColor());
			} else if(configChanged.getKey().equals("outlineColor")) {
				overlay.setOutlineColor(config.outlineColor());
			} else if(configChanged.getKey().equals("errorColor")) {
				overlay.setErrorColor(config.errorColor());
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if(widgetLoaded.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			int boardSize = client.getWidget(RUNEDOKU_RUNE_WIDGET_ID).getChildren().length - 1;
			overlay.setBoardSize(boardSize);
			overlay.setActive(true);
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed) {
		if(widgetClosed.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			overlay.setActive(false);
		}
	}

	protected void loadConfig() {
		overlay.setShouldDrawNumbers(config.useNumbers());
		overlay.setForegroundColor(config.foregroundColor());
		overlay.setOutlineColor(config.outlineColor());
		overlay.setErrorColor(config.errorColor());
	}

	@Override
	protected void startUp() throws Exception {
		loadConfig();

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Provides
	RunedokuConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RunedokuConfig.class);
	}
}
