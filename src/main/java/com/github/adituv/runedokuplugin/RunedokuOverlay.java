package com.github.adituv.runedokuplugin;

import com.google.inject.Inject;
import com.jogamp.graph.geom.Outline;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.RescaleOp;

import static com.github.adituv.runedokuplugin.RunedokuConstants.*;

@Setter
@Slf4j
public class RunedokuOverlay extends Overlay {
    private final Client client;

    @Setter(AccessLevel.NONE)
    private boolean isActive;

    @Setter(AccessLevel.NONE)
    private RunedokuBoard board;

    @Setter(AccessLevel.NONE)
    private RunedokuRune selectedRune;

    private boolean shouldDrawNumbers;
    private boolean shouldShowSolution;
    private Color foregroundColor;
    private Color outlineColor;
    private Color errorColor;
    private Color solutionColor;
    private final AsyncBufferedImage[] runeImages;

    private final Font bigFont;
    private final Font markFont;

    private final int BOX_X_OFFSET = -2;
    private final int BOX_Y_OFFSET = 0;
    private final int BOX_WIDTH_OFFSET = -1;
    private final int BOX_HEIGHT_OFFSET = -1;

    private final float BIG_FONT_SIZE = 24.0f;
    private final float BIG_FONT_X_OFFSET = -2.5f;

    /**
     * 'main' class to determine what to draw on screen for Runedoku.
     * @param graphics 2D graphics object handler for runelite
     * @return a Dimension. I think this is like a layer, but I am not sure
     */
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

            // innocent until proven guilty
            boolean solved = true;
            for(RunedokuCell c : board.getCells()) {
                if(c.getSudokuNumber() > 0) {
                    // Show errors in sudoku gameplay
                    if(board.cellHasClash(c)) {
                        // This rune clashes with another rune.  Highlight it with an error box
                        drawErrorBox(graphics, c.getWidget());
                    }
                    // Show numbers over runes for clarity
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

                    StringBuilder marks = new StringBuilder("");

                    for(int i = 0; i < 9; i++) {
                        if(c.getMarks()[i]) {
                            marks.append(i+1);
                            if(marks.length() == 5) {
                                // Render first line and start again
                                markNumberText.setText(marks.toString());
                                markNumberText.setPosition(new Point(xPos, yPos));
                                Dimension textBounds = markNumberText.render(graphics);

                                yPos += textBounds.height;

                                marks = new StringBuilder("");
                            }

                            markNumberText.setText(marks.toString());
                            markNumberText.setPosition(new Point(xPos, yPos));
                            markNumberText.render(graphics);
                        }
                    }

                    // Show the solution values and highlight current rune solutions
                    if (shouldShowSolution) {
                        if (c.getRune() != null) {
                            if (c.getRune().getSudokuNumber() != c.getSolutionNumber()) {
                                solved = false;
                            }
                        } else {
                            solved = false;
                        }
                        final Color tintColor = new Color(128,128,153,255);
                        final float[] tintChannels = new float[]
                                { tintColor.getRed()/255f, tintColor.getGreen()/255f,
                                        tintColor.getBlue()/255f, tintColor.getAlpha()/255f
                                };
                        final RescaleOp darkenImageOp = new RescaleOp(tintChannels, new float[]{0f,0f,0f,0f}, null);
                        Rectangle bounds = c.getWidget().getBounds();
                        graphics.drawImage(runeImages[c.getSolutionNumber()], darkenImageOp, bounds.x, bounds.y);

                        if (this.selectedRune != null) {
                            if (board.cellHasSolution(c) && c.getSolutionNumber() == this.selectedRune.getSudokuNumber()) {
                                drawSolutionBox(graphics, c.getWidget());
                            }
                        }

                        if(shouldDrawNumbers) {
                            final Widget w = c.getWidget();
                            final String numberAsText = String.format("%d", c.getSolutionNumber());

                            Rectangle2D stringBounds = bigFontMetrics.getStringBounds(numberAsText, graphics);
                            double x = w.getBounds().getCenterX() - stringBounds.getCenterX() + BIG_FONT_X_OFFSET;
                            double y = w.getBounds().getCenterY() - stringBounds.getCenterY();
                            Point location = new Point((int) x, (int) y);

                            bigNumberText.setText(numberAsText);
                            bigNumberText.setPosition(location);
                            bigNumberText.render(graphics);
                        }
                    } else {
                        solved = false;
                    }
                }
            }

            if (solved) {
                drawComplete(graphics, client.getWidget(RUNEDOKU_BUTTON_WIDGET_ID));
            }

            Widget[] children = runeWidget.getChildren();

            if(children == null) {
                log.error("render: runeWidget children array is null");
                return null;
            }

            for (Widget w : children) {
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

    private void drawSolutionBox(Graphics2D graphics, Widget w) {
        graphics.setColor(ColorUtil.colorWithAlpha(solutionColor, 0xFF));
        graphics.setStroke(new BasicStroke(2.0f));

        Rectangle bounds = w.getBounds();
        graphics.drawRect(bounds.x + BOX_X_OFFSET,
                bounds.y + BOX_Y_OFFSET,
                bounds.width + BOX_WIDTH_OFFSET,
                bounds.height + BOX_HEIGHT_OFFSET
        );
    }

    private void drawComplete(Graphics2D graphics, Widget w) {
        graphics.setColor(ColorUtil.colorWithAlpha(solutionColor, 0xFF));
        graphics.setStroke(new BasicStroke(2.0f));

        Rectangle bounds = w.getBounds();
        graphics.drawRect(bounds.x + BOX_X_OFFSET,
                bounds.y + BOX_Y_OFFSET,
                bounds.width + BOX_WIDTH_OFFSET,
                bounds.height + BOX_HEIGHT_OFFSET
        );
    }

    @Inject
    public RunedokuOverlay(final RunedokuPlugin plugin, Client client, final ItemManager itemManager) {
        super(plugin);
        this.client = client;
        this.bigFont = FontManager.getDefaultBoldFont().deriveFont(BIG_FONT_SIZE);
        this.markFont = FontManager.getRunescapeSmallFont();

        setPosition(OverlayPosition.DETACHED);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.MED);

        // Preload images for runes
        this.runeImages = new AsyncBufferedImage[10];
        for(int i = 1; i < 10; i++) {
            this.runeImages[i] = itemManager.getImage(RunedokuRune.getBySudokuNumber(i).getItemId());
        }
    }

    /**
     * Setter for the currently selected rune
     * @param rune RunedokuRune
     */
    public void updateSelectedRune(RunedokuRune rune) {
        this.selectedRune = rune;
    }
}
