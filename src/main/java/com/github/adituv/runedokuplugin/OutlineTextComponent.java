/*
 * Copyright (c) 2021, Iris Ward <aditu.venyhandottir@gmail.com>
 *
 * This file is a modified version of net.runelite.client.ui.overlay.components.TextComponent
 * originally by Tomas Slusny.  The copyright notice for the original file is given below.
 */

/*
 * Copyright (c) 2017, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.adituv.runedokuplugin;

import lombok.Setter;
import net.runelite.client.ui.overlay.RenderableEntity;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.regex.Pattern;

@Setter
public class OutlineTextComponent implements RenderableEntity {
    private static final String COL_TAG_REGEX = "(<col=([0-9a-fA-F]){2,6}>)";
    private static final Pattern COL_TAG_PATTERN_W_LOOKAHEAD = Pattern.compile("(?=" + COL_TAG_REGEX + ")");

    private String text;
    private Point position = new Point();
    private Color color = Color.WHITE;

    // If there is no outline, draw a black drop-shadow if this is true, or draw nothing extra otherwise
    private boolean shadow = true;

    @Nullable
    private Font font;

    private int outlineWidth = 1;
    private Color outlineColor = Color.BLACK;

    @Override
    public Dimension render(Graphics2D graphics) {
        Font originalFont = graphics.getFont();

        if (font != null) {
            graphics.setFont(font);
        }

        final FontMetrics fontMetrics = graphics.getFontMetrics();

        if (COL_TAG_PATTERN_W_LOOKAHEAD.matcher(text).find())
        {
            final String[] parts = COL_TAG_PATTERN_W_LOOKAHEAD.split(text);
            int x = position.x;

            for (String textSplitOnCol : parts)
            {
                final String textWithoutCol = Text.removeTags(textSplitOnCol);
                final String colColor = textSplitOnCol.substring(textSplitOnCol.indexOf("=") + 1, textSplitOnCol.indexOf(">"));

                if (outlineWidth > 0)
                {
                    graphics.setColor(ColorUtil.colorWithAlpha(outlineColor, 0xFF));

                    graphics.drawString(textWithoutCol, x + outlineWidth, position.y + outlineWidth);
                    graphics.drawString(textWithoutCol, x, position.y + outlineWidth);
                    graphics.drawString(textWithoutCol, x - outlineWidth, position.y + outlineWidth);

                    graphics.drawString(textWithoutCol, x + outlineWidth, position.y);
                    graphics.drawString(textWithoutCol, x - outlineWidth, position.y);

                    graphics.drawString(textWithoutCol, x + outlineWidth, position.y - outlineWidth);
                    graphics.drawString(textWithoutCol, x, position.y - outlineWidth);
                    graphics.drawString(textWithoutCol, x - outlineWidth, position.y - outlineWidth);
                }
                else if(shadow)
                {
                    graphics.setColor(Color.BLACK);

                    graphics.drawString(textWithoutCol, x + 1, position.y + 1);
                }

                // actual text
                graphics.setColor(Color.decode("#" + colColor));
                graphics.drawString(textWithoutCol, x, position.y);

                x += fontMetrics.stringWidth(textWithoutCol);
            }
        }
        else
        {
            graphics.setColor(Color.BLACK);

            if (outlineWidth > 0)
            {
                graphics.setColor(ColorUtil.colorWithAlpha(outlineColor, 0xFF));

                graphics.drawString(text, position.x + outlineWidth, position.y + outlineWidth);
                graphics.drawString(text, position.x,position.y + outlineWidth);
                graphics.drawString(text, position.x - outlineWidth, position.y + outlineWidth);

                graphics.drawString(text, position.x + outlineWidth, position.y);
                graphics.drawString(text, position.x - outlineWidth, position.y);

                graphics.drawString(text, position.x + outlineWidth, position.y - outlineWidth);
                graphics.drawString(text, position.x, position.y - outlineWidth);
                graphics.drawString(text, position.x - outlineWidth, position.y - outlineWidth);
            }
            else if(shadow)
            {
                graphics.setColor(Color.BLACK);

                graphics.drawString(text, position.x + 1, position.y + 1);
            }

            // actual text
            graphics.setColor(ColorUtil.colorWithAlpha(color, 0xFF));
            graphics.drawString(text, position.x, position.y);
        }

        int width = fontMetrics.stringWidth(text);
        int height = fontMetrics.getHeight();

        if (originalFont != null)
        {
            graphics.setFont(originalFont);
        }

        return new Dimension(width, height);
    }

    // Convenience setter for all outline properties
    public void setOutline(int width, Color color) {
        this.setOutlineWidth(width);
        this.setOutlineColor(color);
    }
}
