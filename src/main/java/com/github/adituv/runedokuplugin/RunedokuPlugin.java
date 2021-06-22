package com.github.adituv.runedokuplugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
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
	private ClientThread clientThread;

	@Inject
	private RunedokuConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RunedokuOverlay overlay;

	private RunedokuRune selectedRune = null;
	private String selectedRuneText = "<col=ff9040>Water rune</col>";
	private RunedokuBoard board = null;

	private boolean markOnShift;

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
			} else if(configChanged.getKey().equals("markOnShift")) {
				this.markOnShift = config.markOnShift();
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if(widgetLoaded.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			log.debug("Runedoku widget loaded");
			this.selectedRune = RunedokuRune.WATER_RUNE;

			clientThread.invokeLater(() -> {
				Widget boardWidget = client.getWidget(RUNEDOKU_BOARD_WIDGET_ID);
				this.board = new RunedokuBoard(boardWidget);
				this.board.updateCells();
				this.overlay.activate(board);
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed) {
		if(widgetClosed.getGroupId() == RUNEDOKU_WIDGET_GROUP_ID) {
			board = null;
			overlay.deactivate();
		}
	}

	@Subscribe
	public void onCanvasSizeChanged(CanvasSizeChanged canvasSizeChanged) {
		if(board != null)
		{
			board.updateCells();
		}
	}

	// Must run before Inventory Tags
	@Subscribe(priority = -1.0f)
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		if(menuOptionClicked.getWidgetId() == RUNEDOKU_RUNE_WIDGET_ID) {
			clientThread.invokeLater(() -> {
				// Update the currently selected rune
				Widget w = client.getWidget(menuOptionClicked.getWidgetId());

				for(Widget cw : w.getChildren()) {
					if(cw.getBorderType() == 2) {
						this.selectedRune = RunedokuRune.getByItemId(cw.getItemId());
						String name = cw.getName();
						if(name.isEmpty()) {
							name="<col=ff9040>None</col>";
						}
						this.selectedRuneText = name;
						break;
					}
				}
			});
		} else if(menuOptionClicked.getWidgetId() == RUNEDOKU_BOARD_CLICK_WIDGET_ID) {
			if(menuOptionClicked.getMenuOption().equals("Mark")) {
				menuOptionClicked.consume();

				int cellId = menuOptionClicked.getActionParam();
				int sudokuIndex = this.selectedRune.getSudokuNumber()-1;

				log.debug(String.format("Marking cell=%d num=%d", cellId, sudokuIndex+1));

				if(sudokuIndex >= 0) {
					RunedokuCell c = board.getCell(cellId);
					boolean[] marks = c.getMarks();
					marks[sudokuIndex] = !marks[sudokuIndex];
					c.setMarks(marks);
				}
			} else {
				clientThread.invokeLater(() -> board.updateCells());
			}
		}
	}

	// Manually create an extra menu entry for pencil marks when the right-click menu is opened
	@Subscribe
	public void onMenuOpened(MenuOpened menuOpened) {
		int widgetId = menuOpened.getFirstEntry().getParam1();
		int widgetIndex = menuOpened.getFirstEntry().getParam0();

		if(widgetId == RUNEDOKU_BOARD_CLICK_WIDGET_ID && selectedRune != null) {
			Widget cell = client.getWidget(RUNEDOKU_BOARD_WIDGET_ID).getChild(widgetIndex);
			if(cell.getBorderType() == 2) {
				// If it's a fixed rune, don't show the mark option
				return;
			}

			MenuEntry markEntry = makeMarkMenuEntry(widgetId, widgetIndex);

			MenuEntry[] newEntries = insertMarkMenuEntry(markEntry);

			client.setMenuEntries(newEntries);
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded) {
		if(menuEntryAdded.getActionParam1() != RUNEDOKU_BOARD_CLICK_WIDGET_ID || client.isMenuOpen()) {
			return;
		}

		if(markOnShift && client.isKeyPressed(KeyCode.KC_SHIFT)) {
			if(menuEntryAdded.getOption().startsWith("Place")) {
				MenuEntry[] menuEntries = client.getMenuEntries();

				for(MenuEntry entry : menuEntries) {
					if(entry.getOption() == menuEntryAdded.getOption()) {

						// Our onMenuOptionClicked code consumes the click event for the menu option "Mark"
						// so we can just change the option and target to get the desired functionality
						entry.setOption("Mark");
						entry.setTarget(this.selectedRuneText);
					}
				}

				client.setMenuEntries(menuEntries);
			}
		}
	}

	private MenuEntry makeMarkMenuEntry(int widgetId, int widgetIndex) {
		MenuEntry markEntry = new MenuEntry();
		markEntry.setParam0(widgetIndex);
		markEntry.setParam1(widgetId);
		markEntry.setOption("Mark");
		markEntry.setTarget(selectedRuneText);
		markEntry.setType(MenuAction.CC_OP.getId());
		return markEntry;
	}

	private MenuEntry[] insertMarkMenuEntry(MenuEntry markEntry) {
		MenuEntry[] oldEntries = client.getMenuEntries();
		MenuEntry[] newEntries = new MenuEntry[oldEntries.length + 1];

		for(int i = 0; i < oldEntries.length; i++) {
			newEntries[i] = oldEntries[i];
		}

		if(markOnShift && client.isKeyPressed(KeyCode.KC_SHIFT)) {
			newEntries[newEntries.length-1] = markEntry;
		} else {
			newEntries[newEntries.length-2] = markEntry;
			newEntries[newEntries.length-1] = oldEntries[oldEntries.length-1];
		}

		return newEntries;
	}

	protected void loadConfig() {
		overlay.setShouldDrawNumbers(config.useNumbers());
		overlay.setForegroundColor(config.foregroundColor());
		overlay.setOutlineColor(config.outlineColor());
		overlay.setErrorColor(config.errorColor());
		this.markOnShift = config.markOnShift();
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
