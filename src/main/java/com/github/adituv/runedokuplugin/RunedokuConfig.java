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
		keyName = "markOnShift",
		name = "Shift-Mark",
		description = "Hold shift to make mark the left click menu option"
	)
	default boolean markOnShift() { return true;}

	@ConfigItem(
		keyName = "foregroundColor",
		name = "Foreground color",
		description = "The default foreground color for the overlay"
	)
	default Color foregroundColor() { return Color.YELLOW; }

	@ConfigItem(
		keyName = "errorColor",
		name = "Error color",
		description = "The color used to highlight errors in the overlay"
	)
	default Color errorColor() { return Color.RED; }

	@ConfigItem(
		keyName = "outlineColor",
		name = "Outline color",
		description = "The outline color for text in the overlay"
	)
	default Color outlineColor() { return Color.BLACK; }
}
