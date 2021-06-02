package com.github.adituv.runedokuplugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunedokuPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RunedokuPlugin.class);
		RuneLite.main(args);
	}
}