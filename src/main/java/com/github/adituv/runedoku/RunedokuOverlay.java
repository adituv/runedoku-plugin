package com.github.adituv.runedoku;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.AsyncBufferedImage;

import java.awt.*;
import java.awt.image.RescaleOp;

@Slf4j
public class RunedokuOverlay extends Overlay {
    private final Client client;
    private final RunedokuConfig config;
    private final ItemManager itemManager;
    private final AsyncBufferedImage[] runeImages;

    private int[][] solution;

    @Override
    public Dimension render(Graphics2D graphics) {
        final Widget boardWidget = client.getWidget(RunedokuConstants.RUNEDOKU_BOARD_WIDGET);
        final Widget runesWidget = client.getWidget(RunedokuConstants.RUNEDOKU_RUNE_WIDGET);

        if(boardWidget == null || boardWidget.isHidden() == true || solution == null) {
            return null;
        }

        final Widget[] boardSquares = boardWidget.getChildren();

        final Color tintColor = config.fadeTint();
        final float[] tintChannels = new float[]
                { tintColor.getRed()/255f, tintColor.getGreen()/255f,
                  tintColor.getBlue()/255f, tintColor.getAlpha()/255f
                };
        final RescaleOp darkenImageOp = new RescaleOp(tintChannels, new float[]{0f,0f,0f,0f}, null);
        final int puzzleSize = solution.length;

        int selectedRune = 0;
        // Find which rune is selected
        for(int i = 0; i < runesWidget.getChildren().length; i++) {
            if(runesWidget.getChild(i).getBorderType() == 2) {
                selectedRune = i;
                break;
            }
        }

        boolean anyRuneIsWrongOrEmpty = false;

        for(int j = 0; j < puzzleSize; j++) {
            for(int i = 0; i < puzzleSize; i++) {
                int linearIndex = j*puzzleSize + i;
                int tileRune = solution[j][i];
                Rectangle bounds = boardSquares[linearIndex].getBounds();
                int currentValue = RunedokuUtil.itemIdToSudokuNumber(boardSquares[linearIndex].getItemId());

                if(currentValue == 0 && boardSquares[linearIndex].getBorderType() != 2) {
                    graphics.drawImage(runeImages[tileRune], darkenImageOp, bounds.x, bounds.y);
                    anyRuneIsWrongOrEmpty = true;

                    if(tileRune == selectedRune) {
                        graphics.setColor(config.successColor());
                        graphics.setStroke(new BasicStroke(2.0f));
                        graphics.drawRect(bounds.x,bounds.y,bounds.width,bounds.height);
                    }

                } else if(currentValue != tileRune) {
                    graphics.setColor(config.errorColor());
                    graphics.setStroke(new BasicStroke(3.0f));
                    graphics.drawRect(bounds.x,bounds.y,bounds.width,bounds.height);
                    anyRuneIsWrongOrEmpty = true;
                }
            }
        }

        if(!anyRuneIsWrongOrEmpty) {
            // Puzzle finished, highlight button
            final Widget button = client.getWidget(RunedokuConstants.RUNEDOKU_BUTTON_WIDGET);
            final Rectangle bounds = button.getBounds();

            graphics.setColor(config.successColor());
            graphics.setStroke(new BasicStroke(3.0f));
            graphics.drawRect(bounds.x,bounds.y,bounds.width,bounds.height);
        }

        return null;
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    @Inject
    public RunedokuOverlay(final RunedokuPlugin plugin, final RunedokuConfig config, final Client client, final ItemManager itemManager) {
        super(plugin);

        this.config = config;
        this.client = client;
        this.itemManager = itemManager;
        this.runeImages = new AsyncBufferedImage[10];
        for(int i = 1; i < 10; i++) {
            this.runeImages[i] = itemManager.getImage(RunedokuUtil.sudokuNumberToItemId(i));
        }

        setPosition(OverlayPosition.DETACHED);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.MED);
    }
}
