package com.kuncheff.colorart;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorArt {

  public static final float COLOR_THRESHOLD_MIN_PERCENTAGE = 0.01f;
  public final Color backgroundColor;
  public final Color primaryColor;
  public final Color secondaryColor;
  public final Color detailColor;

  public ColorArt(final BufferedImage image) {
    final List<CountedColor> imageColors = ColorArt.getAllColors(image);
    final List<CountedColor> leftEdgeColors = ColorArt.getLeftEdgeColors(image);
    final CountedColor backgroundColor = ColorArt.findEdgeColor(leftEdgeColors);
    final Color[] colorPSD = ColorArt.findComplimentingColors(imageColors, backgroundColor);

    this.backgroundColor = backgroundColor.color;
    this.primaryColor = colorPSD[0];
    this.secondaryColor = colorPSD[1];
    this.detailColor = colorPSD[2];
  }

  public static boolean isDarkColor(final Color color) {
    return new CountedColor(color, -1).isDarkColor();
  }

  private static void putIncrement(final Map<Color, Integer> map, final Color color) {
    final Integer count = map.get(color);
    if (count == null) {
      map.put(color, 1);
    }
    else {
      map.put(color, count + 1);
    }
  }

  /**
   * Iterates through all the pixels of an image, and returns a list of all the unique colors and their count.
   */
  private static List<CountedColor> getAllColors(final BufferedImage image) {
    final Map<Color, Integer> imageColors = new HashMap<Color, Integer>(image.getWidth() * image.getHeight());

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        putIncrement(imageColors, new Color(image.getRGB(x, y)));
      }
    }

    return countMapToListOfCountedColors(imageColors);
  }

  /**
   * Iterates through all the pixels on the left edge of an image, and returns a list of all the unique colors and
   * their count.
   */
  private static List<CountedColor> getLeftEdgeColors(final BufferedImage image) {
    final Map<Color, Integer> leftEdgeColors = new HashMap<Color, Integer>(image.getHeight());

    for (int y = 0; y < image.getHeight(); y++) {
      putIncrement(leftEdgeColors, new Color(image.getRGB(0, y)));
    }

    return countMapToListOfCountedColors(leftEdgeColors);
  }

  /**
   * Uses a list of edge colors, and finds the most prominent non-white or non-black color, if possible.
   */
  private static CountedColor findEdgeColor(final List<CountedColor> leftEdgeColors) {
    final List<CountedColor> sortedColors = new ArrayList<CountedColor>(leftEdgeColors.size());

    for (final CountedColor color : leftEdgeColors) {
      // Prevent the usage of random colors.
      final float randomColorsThreshold = leftEdgeColors.size() * COLOR_THRESHOLD_MIN_PERCENTAGE;
      if (color.count > randomColorsThreshold) {
        sortedColors.add(color);
      }
    }

    Collections.sort(sortedColors);

    CountedColor proposedEdgeColor = new CountedColor(Color.WHITE, -1);

    if (sortedColors.size() > 0) {
      proposedEdgeColor = sortedColors.get(0);
      // Attempt to find a color other than black or white.
      if (proposedEdgeColor.isBlackOrWhite()) {
        for (int i = 1; i < sortedColors.size(); i++) {
          final CountedColor nextProposedColor = sortedColors.get(i);
          // 2nd color should show up at least 30% as often as first color.
          if (((double) nextProposedColor.count / (double) proposedEdgeColor.count) > 0.3) {
            if (!nextProposedColor.isBlackOrWhite()) {
              proposedEdgeColor = nextProposedColor;
              break;
            }
          }
          else {
            break;
          }
        }
      }
    }

    return proposedEdgeColor;
  }

  /**
   * Finds other prominent colors that work well with the background color found.
   */
  private static Color[] findComplimentingColors(final List<CountedColor> colors, final CountedColor backgroundColor) {
    final List<CountedColor> sortedColors = new ArrayList<CountedColor>(colors.size());
    final boolean findDarkTextColor = !backgroundColor.isDarkColor();

    for (final CountedColor color : colors) {
      final CountedColor curColor = color.colorWithMinSaturation(0.15f);

      if (curColor.isDarkColor() == findDarkTextColor) {
        sortedColors.add(color);
      }
    }

    Collections.sort(sortedColors);

    CountedColor primaryColor = null;
    CountedColor secondaryColor = null;
    CountedColor detailColor = null;

    for (final CountedColor color : sortedColors) {
      if (primaryColor == null) {
        if (color.isContrastingColor(backgroundColor.color)) {
          primaryColor = color;
        }
      }
      else if (secondaryColor == null) {
        if (primaryColor.isDistinct(color.color) && color.isContrastingColor(backgroundColor.color)) {
          secondaryColor = color;
        }
      }
      else if (detailColor == null) {
        if (secondaryColor.isDistinct(color.color) &&
            primaryColor.isDistinct(color.color) &&
            color.isContrastingColor(backgroundColor.color)) {
          detailColor = color;
        }
      }
    }

    // Default the colors to black or white based on the background color before setting the found colors.
    final Color[] detectedColors = new Color[] {
        backgroundColor.isDarkColor() ? Color.WHITE : Color.BLACK,
        backgroundColor.isDarkColor() ? Color.WHITE : Color.BLACK,
        backgroundColor.isDarkColor() ? Color.WHITE : Color.BLACK
    };
    if (primaryColor != null) {
      detectedColors[0] = primaryColor.color;
    }
    if (secondaryColor != null) {
      detectedColors[1] = secondaryColor.color;
    }
    if (detailColor != null) {
      detectedColors[2] = detailColor.color;
    }
    return detectedColors;
  }

  /**
   * Takes our initial map of colors to their counts, and creates CountedColors so that they are easier to work with.
   */
  private static List<CountedColor> countMapToListOfCountedColors(final Map<Color, Integer> colors) {
    final List<CountedColor> countedColors = new ArrayList<CountedColor>();

    for (final Color color : colors.keySet()) {
      countedColors.add(new CountedColor(color, colors.get(color)));
    }
    return countedColors;
  }
}
