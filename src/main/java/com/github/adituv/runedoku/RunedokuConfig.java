package com.github.adituv.runedoku;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("runedoku")
public interface RunedokuConfig extends Config
{
	@ConfigItem(
		keyName = "successColor",
		name = "Success color",
		description = "The border color for the button on puzzle complete"
	)
	default Color successColor() { return Color.GREEN; }

	@ConfigItem(
        keyName = "errorColor",
		name = "Error color",
		description = "The border color for cells with an incorrect value"
	)
	default Color errorColor() { return Color.RED; }

	@ConfigItem(
			keyName = "fadeTint",
			name = "Fade tint",
			description = "The color to tint solution preview runes with"
	)
	default Color fadeTint() { return new Color(128,128,153,255); }

}
