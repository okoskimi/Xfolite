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

import de.enough.polish.ui.Gauge;
import de.enough.polish.ui.Style;

public class Range extends Gauge
{
    public static final int INTEGER = 0;
    public static final int DECIMAL = 1;
    public static final int TIME = 2;

    // public static int SECONDS_PER_MINUTE = 60;
    // public static int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    // public static int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    
    double start;
    double end;
    double step;
    int type = DECIMAL;

  public Range(final String label, final double start, final double end, final double step,
               final double value, final int type)
  {
    this(label, start, end, step, value, type, null);
  }


  public Range(final String label, final double start, final double end, final double step,
               final double value, final int type,
               final Style style)
  {
    super(label, true, (int)((end-start)/step), (int)((value-start)/step), style);
    this.start = start;
    this.end = end;
    this.step = step;
    if (type < INTEGER || type > TIME) {
        throw new IllegalArgumentException("Unknown type for Range");
    }
    this.type = type;
    //updateValueString(); // Have to call it ourselves, when superclass runs it start/end/step are not yet set
  }

  public void setRangeValue(double value) {
      setValue((int)((value-start)/step));
  }
  
  public double getRangeValue() {
      return start + getValue() * step;
  }
  /*
  protected int getMaxValueWidth() {
      int maxVal = 0;
      int curVal = 0;
      switch (type) {
      case DECIMAL:
          for (int i = 0; i<= maxValue; i++)  {
              curVal = this.font.stringWidth( Double.toString(start + i * step));
              if (curVal > maxVal) {
                  maxVal = curVal;
              }
          }
          return maxVal;
      case INTEGER:
          for (int i = 0; i<= maxValue; i++)  {
              curVal = this.font.stringWidth(Long.toString(MathUtil.round(start + i * step)));
              if (curVal > maxVal) {
                  maxVal = curVal;
              }
          }
          return maxVal;
      case TIME:
          for (int i = 0; i<= maxValue; i++)  {
              long seconds = MathUtil.round(start + i * step);
              long days = seconds / SECONDS_PER_DAY;
              seconds %= SECONDS_PER_DAY;
              long hours = seconds / SECONDS_PER_HOUR;
              seconds %= SECONDS_PER_HOUR;
              long minutes = seconds / SECONDS_PER_MINUTE;
              seconds %= SECONDS_PER_MINUTE;
              String s = "";
              if (days > 0) {
                  s += Long.toString(days) + "d";
              }
              if (hours > 0) {
                  s += Long.toString(hours) + "h";
              }
              if (minutes > 0) {
                  s += Long.toString(minutes) + "m";
              }
              if (seconds > 0) {
                  s += Long.toString(seconds) + "s";
              }
              
              curVal = this.font.stringWidth(s);
    
              if (curVal > maxVal) {
                  maxVal = curVal;
              }
          }
          return maxVal;
      }
      throw new RuntimeException("Illegal range type");
  }
*/  
  /*
  protected void updateValueString() {
      double dval = getRangeValue();
      switch (type) {
      case DECIMAL:
          this.valueString = Double.toString(dval);
          break;
      case INTEGER:
          this.valueString = Long.toString(MathUtil.round(dval));
          break;
      case TIME:
          long seconds = MathUtil.round(dval);
          long days = seconds / SECONDS_PER_DAY;
          seconds %= SECONDS_PER_DAY;
          long hours = seconds / SECONDS_PER_HOUR;
          seconds %= SECONDS_PER_HOUR;
          long minutes = seconds / SECONDS_PER_MINUTE;
          seconds %= SECONDS_PER_MINUTE;
          this.valueString = "";
          if (days > 0) {
              this.valueString += Long.toString(days) + "d";
          }
          if (hours > 0) {
              this.valueString += Long.toString(hours) + "h";
          }
          if (minutes > 0) {
              this.valueString += Long.toString(minutes) + "m";
          }
          if (seconds > 0) {
              this.valueString += Long.toString(seconds) + "s";
          }
          break;
      }
  }
*/  
 
  
}