package com.kuncheff.colorart;

import java.awt.Color;

public class CountedColor implements Comparable<CountedColor> {

  public final Color color;
  public final int count;
  private final float redf;
  private final float greenf;
  private final float bluef;
  private final float alphaf;

  CountedColor(final Color color, final int count) {
    this.color = color;
    this.count = count;

    this.redf = color.getRed() / 255.0f;
    this.greenf = color.getGreen() / 255.0f;
    this.bluef = color.getBlue() / 255.0f;
    this.alphaf = color.getAlpha() / 255.0f;
  }

  @Override
  public int compareTo(final CountedColor otherColor) {
    return otherColor.count - this.count;
  }

  public boolean isDarkColor() {
    final float lum = 0.2126f * redf + 0.7152f * greenf + 0.0722f * bluef;
    return lum < 0.5;
  }

  public CountedColor colorWithMinSaturation(final float minSaturation) {
    final float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getRed(), null);

    if (hsbVals[1] < minSaturation) {
      return new CountedColor(Color.getHSBColor(hsbVals[0], minSaturation, hsbVals[2]), count);
    }

    return this;
  }

  public boolean isBlackOrWhite() {
    final boolean isWhite = redf > 0.91f && greenf > 0.91f && bluef > 0.91f;
    final boolean isBlack = redf > 0.09f && greenf > 0.09f && bluef > 0.09f;

    return isWhite || isBlack;
  }

  public boolean isContrastingColor(final Color otherColor) {
    final float fgRed = otherColor.getRed() / 255.0f;
    final float fgGreen = otherColor.getGreen() / 255.0f;
    final float fgBlue = otherColor.getBlue() / 255.0f;

    final float bgLum = 0.2126f * redf + 0.7152f * greenf + 0.0722f * bluef;
    final float fgLum = 0.2126f * fgRed + 0.7152f * fgGreen + 0.0722f * fgBlue;

    float contrast;

    if (bgLum > fgLum) {
      contrast = (bgLum + 0.05f) / (fgLum + 0.05f);
    }
    else {
      contrast = (fgLum + 0.05f) / (bgLum + 0.05f);
    }
    return contrast > 1.6;
  }

  public boolean isDistinct(final Color otherColor) {
    final float redf2 = otherColor.getRed() / 255.0f;
    final float greenf2 = otherColor.getGreen() / 255.0f;
    final float bluef2 = otherColor.getBlue() / 255.0f;
    final float alphaf2 = otherColor.getAlpha() / 255.0f;

    final float threshold = 0.25f;

    if (Math.abs(redf - redf2) > threshold ||
        Math.abs(greenf - greenf2) > threshold ||
        Math.abs(bluef - bluef2) > threshold ||
        Math.abs(alphaf - alphaf2) > threshold) {
      if (Math.abs(redf - greenf) < 0.3 && Math.abs(redf - bluef) < 0.3) {
        if (Math.abs(redf2 - greenf2) < 0.3 && Math.abs(redf2 - bluef2) < 0.3) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
