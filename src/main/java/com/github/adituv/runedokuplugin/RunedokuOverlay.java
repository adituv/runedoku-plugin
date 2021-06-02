package com.github.adituv.runedokuplugin;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import static com.github.adituv.runedokuplugin.RunedokuConstants.*;

public class RunedokuOverlay extends Overlay {
    private final Client client;

    private boolean isActive;
    private boolean shouldDrawNumbers;
    private Color foregroundColor;

    @Override
    public Dimension render(Graphics2D graphics) {
        if(this.isActive) {
            Widget containerWidget = client.getWidget(RUNEDOKU_CONTAINER_WIDGET_ID);
            if(containerWidget == null || containerWidget.isHidden()) {
                return null;
            }

            Widget boardWidget = client.getWidget(RUNEDOKU_BOARD_WIDGET_ID);
            Widget runeWidget = client.getWidget(RUNEDOKU_RUNE_WIDGET_ID);

            setupGraphics(graphics);

            if(shouldDrawNumbers) {
                drawNumbers(graphics, boardWidget, runeWidget);
            }
        }

        return null;
    }

    private void setupGraphics(Graphics2D graphics) {
        graphics.setColor(this.foregroundColor);
        Font font = FontManager.getDefaultBoldFont().deriveFont(50.0f);
        graphics.setFont(font);
    }

    private void drawNumbers(Graphics2D graphics, Widget boardWidget, Widget runeWidget) {
        final float BIG_FONT_SIZE = 24.0f;
        final float BIG_FONT_X_OFFSET = -2.0f;
        final float BIG_FONT_Y_OFFSET = -7.0f;

        Font bigFont = FontManager.getDefaultBoldFont().deriveFont(BIG_FONT_SIZE);
        FontMetrics fontMetrics = graphics.getFontMetrics(bigFont);
        int fontHeight = fontMetrics.getAscent();

        TextComponent text = new TextComponent();
        text.setOutline(true);
        text.setColor(foregroundColor);
        text.setFont(bigFont);

        for(Widget w : boardWidget.getChildren()) {
            RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

            if(rune != null) {
                String numberAsText = String.format("%d",rune.getSudokuNumber());

                Rectangle2D stringBounds = fontMetrics.getStringBounds(numberAsText,graphics);
                double x = w.getBounds().getCenterX() - stringBounds.getWidth()/2.0 + BIG_FONT_X_OFFSET;
                double y = w.getBounds().getCenterY() + stringBounds.getHeight()/2.0 + BIG_FONT_Y_OFFSET;
                Point location = new Point((int)x,(int)y);

                text.setText(numberAsText);
                text.setPosition(location);
                text.render(graphics);
            }
        }

        for(Widget w : runeWidget.getChildren()) {
            RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

            if(rune != null) {
                String numberAsText = String.format("%d",rune.getSudokuNumber());

                Rectangle2D stringBounds = fontMetrics.getStringBounds(numberAsText,graphics);
                double x = w.getBounds().getCenterX() - stringBounds.getWidth()/2.0 + BIG_FONT_X_OFFSET;
                double y = w.getBounds().getCenterY() + stringBounds.getHeight()/2.0 + BIG_FONT_Y_OFFSET;
                Point location = new Point((int)x,(int)y);

                text.setText(numberAsText);
                text.setPosition(location);
                text.render(graphics);
            }
        }
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setShouldDrawNumbers(boolean drawNumbers) {
        this.shouldDrawNumbers = drawNumbers;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Inject
    public RunedokuOverlay(final RunedokuPlugin plugin, Client client) {
        super(plugin);
        this.client = client;

        setPosition(OverlayPosition.DETACHED);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.MED);
    }
}
