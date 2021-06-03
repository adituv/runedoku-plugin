package com.github.adituv.runedokuplugin;

import com.google.inject.Inject;
import com.jogamp.graph.geom.Outline;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static com.github.adituv.runedokuplugin.RunedokuConstants.*;

@Setter
@Slf4j
public class RunedokuOverlay extends Overlay {
    private final Client client;

    @Setter(AccessLevel.NONE)
    private boolean isActive;

    @Setter(AccessLevel.NONE)
    private RunedokuBoard board;

    private boolean shouldDrawNumbers;
    private Color foregroundColor;
    private Color outlineColor;
    private Color errorColor;

    private final Font bigFont;
    private final Font markFont;

    private final int BOX_X_OFFSET = -2;
    private final int BOX_Y_OFFSET = 0;
    private final int BOX_WIDTH_OFFSET = -1;
    private final int BOX_HEIGHT_OFFSET = -1;

    private final float BIG_FONT_SIZE = 24.0f;
    private final float BIG_FONT_X_OFFSET = -2.5f;

    @Override
    public Dimension render(Graphics2D graphics) {
        if(this.isActive) {
            final Widget containerWidget = client.getWidget(RUNEDOKU_CONTAINER_WIDGET_ID);
            if(containerWidget == null || containerWidget.isHidden()) {
                return null;
            }

            final int boardWidth = this.board.getWidth();

            if(!(boardWidth == 4 || boardWidth == 9)) {
                log.error(String.format("render: Invalid board width %d", boardWidth));
            }

            final Widget boardWidget = client.getWidget(RUNEDOKU_BOARD_WIDGET_ID);
            final Widget runeWidget = client.getWidget(RUNEDOKU_RUNE_WIDGET_ID);

            final FontMetrics bigFontMetrics = graphics.getFontMetrics(bigFont);
            final FontMetrics markFontMetrics = graphics.getFontMetrics(markFont);

            if(boardWidget == null) {
                log.error("render: boardWidget is null");
                return null;
            }
            if(runeWidget == null) {
                log.error("render: runeWidget is null");
                return null;
            }

            final OutlineTextComponent bigNumberText = new OutlineTextComponent();
            bigNumberText.setOutline(2,outlineColor);
            bigNumberText.setColor(foregroundColor);
            bigNumberText.setFont(bigFont);

            final OutlineTextComponent markNumberText = new OutlineTextComponent();
            markNumberText.setOutline(1,outlineColor);
            markNumberText.setColor(foregroundColor);
            markNumberText.setFont(markFont);

            for(RunedokuCell c : board.getCells()) {
                if(c.getSudokuNumber() > 0) {
                    if(board.cellHasClash(c)) {
                        // This rune clashes with another rune.  Highlight it with an error box
                        drawErrorBox(graphics, c.getWidget());
                    }

                    if(shouldDrawNumbers) {
                        final Widget w = c.getWidget();
                        final String numberAsText = String.format("%d", c.getSudokuNumber());

                        Rectangle2D stringBounds = bigFontMetrics.getStringBounds(numberAsText,graphics);
                        double x = w.getBounds().getCenterX() - stringBounds.getCenterX() + BIG_FONT_X_OFFSET;
                        double y = w.getBounds().getCenterY() - stringBounds.getCenterY();
                        Point location = new Point((int)x,(int)y);

                        bigNumberText.setText(numberAsText);
                        bigNumberText.setPosition(location);
                        bigNumberText.render(graphics);
                    }
                } else {
                    // If there isn't a number in the cell, draw pencil marks
                    Rectangle cellBounds = c.getWidget().getBounds();
                    int xPos = cellBounds.x + BOX_X_OFFSET;
                    int yPos = cellBounds.y + BOX_Y_OFFSET + markFontMetrics.getHeight();
                    cellBounds.x += BOX_X_OFFSET + 1;
                    cellBounds.y += BOX_Y_OFFSET + 1;
                    cellBounds.width += BOX_WIDTH_OFFSET - 1;
                    cellBounds.height += BOX_HEIGHT_OFFSET - 1;

                    StringBuffer marks = new StringBuffer("");

                    for(int i = 0; i < 9; i++) {
                        if(c.getMarks()[i]) {
                            marks.append(i+1);
                            if(marks.length() == 5) {
                                // Render first line and start again
                                markNumberText.setText(marks.toString());
                                markNumberText.setPosition(new Point(xPos, yPos));
                                Dimension textBounds = markNumberText.render(graphics);

                                yPos += textBounds.height;

                                marks = new StringBuffer("");
                            }
                        }
                    }

                    markNumberText.setText(marks.toString());
                    markNumberText.setPosition(new Point(xPos, yPos));
                    markNumberText.render(graphics);
                }
            }

            for (Widget w : runeWidget.getChildren()) {
                if(shouldDrawNumbers) {
                    RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

                    if(rune != null) {
                        String numberAsText = String.format("%d",rune.getSudokuNumber());

                        Rectangle2D stringBounds = bigFontMetrics.getStringBounds(numberAsText,graphics);
                        double x = w.getBounds().getCenterX() - stringBounds.getCenterX() + BIG_FONT_X_OFFSET;
                        double y = w.getBounds().getCenterY() - stringBounds.getCenterY();
                        Point location = new Point((int)x,(int)y);

                        bigNumberText.setText(numberAsText);
                        bigNumberText.setPosition(location);
                        bigNumberText.render(graphics);
                    }
                }
            }
        }

        return null;
    }

    private Point getMarkLocation(int i, Graphics2D graphics, Rectangle cellBounds) {
        final FontMetrics fontMetrics = graphics.getFontMetrics(markFont);
        double x = -1.0;
        double y = -1.0;
        int boardWidth = board.getWidth();
        Rectangle2D stringBounds = fontMetrics.getStringBounds(String.format("%d", i+1), graphics);

        if(boardWidth == 4) {
            if(i % 2 == 0) {
                x = cellBounds.x;
            } else {
                x = cellBounds.getMaxX() - stringBounds.getMaxX();
            }

            if(i / 2 == 0) {
                y = cellBounds.y + stringBounds.getHeight();
            } else {
                y = cellBounds.getMaxY() + fontMetrics.getDescent();
            }
        } else if(boardWidth == 9) {
            if(i % 3 == 0) {
                x = cellBounds.x;
            } else if(i % 3 == 1) {
                x = cellBounds.getCenterX() - stringBounds.getCenterX();
            } else {
                x = cellBounds.getMaxX() - stringBounds.getMaxX();
            }

            if(i / 3 == 0) {
                y = cellBounds.y + stringBounds.getHeight();
            } else if(i / 3 == 1) {
                y = cellBounds.getCenterY() - stringBounds.getCenterY() + fontMetrics.getDescent()/2.0;
            } else {
                y = cellBounds.getMaxY() + fontMetrics.getDescent();
            }
        }

        return new Point((int)x,(int)y);
    }

    public void activate(RunedokuBoard board) {
        this.board = board;
        this.isActive = true;
    }

    public void deactivate() {
        this.board = null;
        this.isActive = false;
    }

    private void drawErrorBox(Graphics2D graphics, Widget w) {

        graphics.setColor(ColorUtil.colorWithAlpha(errorColor, 0xFF));
        graphics.setStroke(new BasicStroke(2.0f));

        Rectangle bounds = w.getBounds();
        graphics.drawRect(bounds.x + BOX_X_OFFSET,
                bounds.y + BOX_Y_OFFSET,
                bounds.width + BOX_WIDTH_OFFSET,
                bounds.height + BOX_HEIGHT_OFFSET
        );
    }

    @Inject
    public RunedokuOverlay(final RunedokuPlugin plugin, Client client) {
        super(plugin);
        this.client = client;
        this.bigFont = FontManager.getDefaultBoldFont().deriveFont(BIG_FONT_SIZE);
        this.markFont = FontManager.getRunescapeSmallFont();

        setPosition(OverlayPosition.DETACHED);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.MED);
    }
}
