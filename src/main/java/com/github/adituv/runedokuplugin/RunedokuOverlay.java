package com.github.adituv.runedokuplugin;

import com.google.inject.Inject;
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

    // 4 or 9
    private int boardSize;

    private boolean isActive;
    private boolean shouldDrawNumbers;
    private Color foregroundColor;
    private Color outlineColor;
    private Color errorColor;

    private final float BIG_FONT_SIZE = 24.0f;
    private final float BIG_FONT_X_OFFSET = -2.5f;

    @Override
    public Dimension render(Graphics2D graphics) {
        if(this.isActive) {
            Widget containerWidget = client.getWidget(RUNEDOKU_CONTAINER_WIDGET_ID);
            if(containerWidget == null || containerWidget.isHidden()) {
                return null;
            }

            if(!(this.boardSize == 4 || this.boardSize == 9)) {
                log.error(String.format("render: Invalid board size %d", this.boardSize));
            }

            Widget boardWidget = client.getWidget(RUNEDOKU_BOARD_WIDGET_ID);
            Widget runeWidget = client.getWidget(RUNEDOKU_RUNE_WIDGET_ID);

            if(boardWidget == null) {
                log.error("render: boardWidget is null");
                return null;
            }
            if(runeWidget == null) {
                log.error("render: runeWidget is null");
                return null;
            }

            Font bigFont = FontManager.getDefaultBoldFont().deriveFont(BIG_FONT_SIZE);
            final FontMetrics fontMetrics = graphics.getFontMetrics(bigFont);

            OutlineTextComponent bigNumberText = new OutlineTextComponent();
            bigNumberText.setOutline(2,outlineColor);
            bigNumberText.setColor(foregroundColor);
            bigNumberText.setFont(bigFont);

            int[][] rowNumbers = new int[boardSize][boardSize];
            int[][] colNumbers = new int[boardSize][boardSize];
            int[][] boxNumbers = new int[boardSize][boardSize];

            Widget[] boardChildren = boardWidget.getChildren();

            // First pass: look for numbers that directly clash
            for(int i = 0; i < boardChildren.length; i++) {
                Widget w = boardChildren[i];
                int rowNum = i / boardSize;
                int colNum = i % boardSize;
                int boxNum = getBoxNum(rowNum,colNum);

                RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

                int sudokuNum = 0;
                if(rune != null) {
                    sudokuNum = rune.getSudokuNumber();
                }

                if(sudokuNum > 0) {
                    rowNumbers[rowNum][sudokuNum - 1]++;
                    colNumbers[colNum][sudokuNum - 1]++;
                    boxNumbers[boxNum][sudokuNum - 1]++;
                }
            }

            // Second pass: draw overlays
            for(int i = 0; i < boardChildren.length; i++) {
                Widget w = boardChildren[i];
                int rowNum = i / boardSize;
                int colNum = i % boardSize;
                int boxNum = getBoxNum(rowNum,colNum);
                RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

                int sudokuNum = 0;
                if(rune != null) {
                    sudokuNum = rune.getSudokuNumber();
                }


                if(sudokuNum > 0) {
                    if(rowNumbers[rowNum][sudokuNum-1] > 1 || colNumbers[colNum][sudokuNum-1] > 1 || boxNumbers[boxNum][sudokuNum-1] > 1) {
                        // This rune clashes with another rune.  Highlight it with an error box
                        drawErrorBox(graphics, w);
                    }
                }

                if(shouldDrawNumbers) {
                    drawRuneText(graphics, fontMetrics, bigNumberText, w);
                }
            }

            for (Widget w : runeWidget.getChildren()) {
                if(shouldDrawNumbers) {
                    drawRuneText(graphics, fontMetrics, bigNumberText, w);
                }
            }
        }

        return null;
    }

    private int getBoxNum(int rowNum, int colNum) {
        if(boardSize == 4) {
            return 2*(rowNum/2) + (colNum/2);
        } else if(boardSize == 9) {
            return 3*(rowNum/3) + (colNum/3);
        } else {
            log.error(String.format("getBoxNum: Invalid boardSize %d", boardSize));
            return -1;
        }
    }

    private void drawErrorBox(Graphics2D graphics, Widget w) {
        final int BOX_X_OFFSET = -2;
        final int BOX_Y_OFFSET = 0;
        final int BOX_WIDTH_OFFSET = -1;
        final int BOX_HEIGHT_OFFSET = -1;

        graphics.setColor(ColorUtil.colorWithAlpha(errorColor, 0xFF));
        graphics.setStroke(new BasicStroke(2.0f));

        Rectangle bounds = w.getBounds();
        graphics.drawRect(bounds.x + BOX_X_OFFSET,
                bounds.y + BOX_Y_OFFSET,
                bounds.width + BOX_WIDTH_OFFSET,
                bounds.height + BOX_HEIGHT_OFFSET
        );
    }

    private void drawRuneText(Graphics2D graphics, FontMetrics fontMetrics, OutlineTextComponent text, Widget w) {
        RunedokuRune rune = RunedokuRune.getByItemId(w.getItemId());

        if(rune != null) {
            String numberAsText = String.format("%d",rune.getSudokuNumber());

            Rectangle2D stringBounds = fontMetrics.getStringBounds(numberAsText,graphics);
            double x = w.getBounds().getCenterX() - stringBounds.getCenterX() + BIG_FONT_X_OFFSET;
            double y = w.getBounds().getCenterY() - stringBounds.getCenterY();
            Point location = new Point((int)x,(int)y);

            text.setText(numberAsText);
            text.setPosition(location);
            text.render(graphics);
        }
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
