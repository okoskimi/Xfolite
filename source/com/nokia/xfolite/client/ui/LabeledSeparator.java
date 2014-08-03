/*
 * This file is part of: Xfolite (J2ME XForms client)
 *
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
 *
 * Contact: Oskari Koskimies <oskari.koskimies@nokia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.nokia.xfolite.client.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.*;

/**
 * Reusable separator custom item that contains a text.
 */

public class LabeledSeparator
//#if polish.LibraryBuild
     extends FakeCustomItem {
//#else
//#  extends Item {
//#endif  

    /**
     * The entries to be displayed.
     */
    protected String displayString;

    /**
     * The font.
     */
    protected Font font;

    /**
     * The font color.
     */
    protected int fontColor;
    
    /**
     * The line padding.
     */
    protected int linePadding = 2;

    /**
     * Constructor
     * 
     * @param label -
     *            the label for this item
     * @param icon -
     *            icon for this entry.
     * @param displayText -
     *            the text to be displayed.
     * @param display - the display instance.           
     * @param id -
     *            the identifier for this instance.
     */
    public LabeledSeparator(String displayText) {
        super(null);
        this.displayString = displayText;
        setAppearanceMode(PLAIN);
    }

    /**
     * Constructor
     * 
     * @param label -
     *            the label for this item
     * @param icon -
     *            icon for this entry.
     * @param displayText -
     *            the text to be displayed.
     * @param id -
     *            the identifier for this instance.
     * @param Style -
     *            the style to be applied for this instance.
     */
    public LabeledSeparator(String displayText, Style style) {
        //#ifdef polish.usePolishGui
        //# super(style);
        //#else
        //# super(); //$NON-NLS-1$
        //#endif

        this.displayString = displayText;
        setAppearanceMode(PLAIN);        
    }

    /**
     * Get the String to be displayed on the button.
     * 
     * @return the String to be displayed on the button.
     */
    public String getDisplayString() {
        return displayString;
    }

    protected String createCssSelector() {
        return "separator";
    }
    
    /**
     * Initialize the graphic elements needed for this item.
     */
    protected void initContent(int firstLineWidth, int availWidth, int availHeight){
        if (this.font == null)
        {
            this.font = Font.getDefaultFont();
            this.fontColor = 0x000000;
        }
        contentHeight = font.getHeight() + this.linePadding;
        contentWidth = availWidth;
    }


    /**
     * Calcuate the minimum height needed for this instance.
     * 
     * @param forWidth
     * @return
     */
    protected int calculateHeight(final int forWidth) {
        return this.font.getHeight();
    }

    public String getText() {
        return displayString;
    }
    
    public void setText(String text) {
        displayString = text;
        requestInit();
    }
    
    /**
     * @see de.enough.polish.ui.CustomItem#paint(javax.microedition.lcdui.Graphics, int, int)
     */
    // protected void paint(Graphics g, int w, int h) {
    protected void paintContent( int x, int y, int leftBorder, int rightBorder, Graphics g ) {
        final int c = y + contentHeight / 2;

        g.setColor(0x949494);
        g.setFont(this.font);
        
        if (displayString == null || displayString.length() == 0) {
            g.drawLine(leftBorder, c, rightBorder, c);
            return;
        }
        
       

        int totalStringWidth = this.font.stringWidth(displayString) + this.linePadding;
        if (this.isLayoutCenter) {
            totalStringWidth += this.linePadding;
        }
        
        int lineLen = rightBorder - leftBorder - totalStringWidth;
        if (this.isLayoutCenter) {
            lineLen /= 2;
        }

        if (this.isLayoutCenter || this.isLayoutRight) {
            g.drawLine(leftBorder, c, leftBorder + lineLen, c);
        } else {
            g.drawLine(leftBorder + totalStringWidth, c, leftBorder + totalStringWidth + lineLen, c);
        }
        
        g.setColor(this.fontColor);
        if (this.isLayoutCenter) {
            g.drawString(displayString, leftBorder + lineLen + this.linePadding, y, Graphics.LEFT | Graphics.TOP);
        } else if (this.isLayoutRight) {
            g.drawString(displayString, rightBorder, y, Graphics.RIGHT | Graphics.TOP);
        } else {
            g.drawString(displayString, leftBorder, y, Graphics.LEFT | Graphics.TOP);
        }
        if (this.isLayoutCenter) {
            g.setColor(0x949494);        
            g.drawLine(leftBorder + lineLen + totalStringWidth, c, rightBorder, c);
        }
    }

    //#ifdef polish.usePolishGui
    public void setStyle(Style style) {
        super.setStyle(style);
        this.font = style.getFont();
        this.linePadding = style.getPaddingHorizontal(10);
        this.fontColor = style.getFontColor();
        requestInit();
    }
    //#endif
}
