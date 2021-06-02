package com.github.adituv.runedokuplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

import static com.github.adituv.runedokuplugin.RunedokuConstants.RUNEDOKU_CONFIG_GROUP;

@ConfigGroup(RUNEDOKU_CONFIG_GROUP)
public interface RunedokuConfig extends Config
{
	@ConfigItem(
		keyName = "useNumbers",
		name = "Show numbers over runes",
		description = "Whether to overlay a number over each rune to help input into a sudoku solver"
	)
	default boolean useNumbers() { return false; }

	@ConfigItem(
		keyName = "foregroundColor",
		name = "Foreground color",
		description = "The default foreground color for the overlay"
	)
	default Color foregroundColor() { return Color.YELLOW; }
}
