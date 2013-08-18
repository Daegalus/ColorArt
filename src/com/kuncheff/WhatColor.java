package com.kuncheff;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.kuncheff.colorart.ColorArt;

public class WhatColor extends JFrame {

  private JLabel Original;
  private JButton PickFile;
  private JLabel Primary;
  private JLabel Secondary;
  private JLabel Detail;
  private JPanel panel1;
  private int textColorsWidth = 50;
  private int textColorsHeight = 50;
  private int originalWidth = 300;
  private int originalHeight = 300;
  private int gap = 5;

  public WhatColor() throws Exception {
    final Dimension size = new Dimension(3 * gap + textColorsWidth + originalWidth, 3 * gap + originalHeight + 50);
    this.setPreferredSize(size);
    this.setMinimumSize(size);
    Insets frameInsets = this.getInsets();
    this.setSize(size.width + frameInsets.left + frameInsets.right, size.height + frameInsets.top + frameInsets.bottom);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    panel1 = new JPanel();
    panel1.setLayout(null);

    Primary = new JLabel();
    panel1.add(Primary);
    Secondary = new JLabel();
    panel1.add(Secondary);
    Detail = new JLabel();
    panel1.add(Detail);

    Original = new JLabel();
    panel1.add(Original);

    PickFile = new JButton();
    PickFile.setText("Browse");
    panel1.add(PickFile);

    Insets insets = panel1.getInsets();
    Primary.setBounds(gap + insets.left, gap + insets.top, textColorsWidth, textColorsHeight);
    Secondary.setBounds(gap + insets.left, textColorsHeight + gap + insets.top, textColorsWidth, textColorsHeight);
    Detail.setBounds(gap + insets.left, 2 * textColorsHeight + gap + insets.top, textColorsWidth, textColorsHeight);
    Original.setBounds(2 * gap + textColorsWidth + insets.left, gap + insets.top, originalWidth, originalHeight);
    PickFile.setBounds((size.width / 2 - PickFile.getPreferredSize().width / 2) + insets.left, gap + originalHeight + insets.top,
        PickFile.getPreferredSize().width, PickFile.getPreferredSize().height);

    PickFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // Use AWT file dialog for nicer and more power native dialogs compared to JFileChooser's lame one.
        final FileDialog fileDialog = new FileDialog(WhatColor.this, "Choose File", FileDialog.LOAD);
        fileDialog.setVisible(true);
        final String path = fileDialog.getDirectory();
        final String filename = fileDialog.getFile();

        if (path != null && filename != null) {
          final File file = new File(path + filename);
          getImage(file);
        }
      }
    });

    this.add(panel1);
    this.setVisible(true);
  }

  public static void main(String[] args) throws Exception {
    new WhatColor();
  }

  private void getImage(final File file) {
    final BufferedImage image;
    try {
      image = ImageIO.read(file);
    }
    catch (IOException e) {
      e.printStackTrace();
      return;
    }

    final ColorArt colorArt = new ColorArt(image);

    panel1.setBackground(colorArt.backgroundColor);

    final BufferedImage imageResized = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_ARGB);
    if (image.getWidth() > image.getHeight()) {
      final float scale = image.getWidth() / (float) originalWidth;
      final int offset = Math.abs(originalWidth - new Float(image.getHeight() / (image.getHeight() / (float) originalWidth)).intValue()) / 2;
      final Graphics2D graphics2D = imageResized.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics2D.drawImage(image, 0, offset, new Float(image.getWidth() / scale).intValue(), new Float(image.getHeight() / scale).intValue(), null);
      graphics2D.dispose();
    }
    else {
      final float scale = image.getHeight() / (float) originalHeight;
      final int offset = Math.abs(originalHeight - new Float(image.getWidth() / (image.getWidth() / (float) originalHeight)).intValue()) / 2;
      final Graphics2D graphics2D = imageResized.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics2D.drawImage(image, offset, 0, new Float(image.getWidth() / scale).intValue(), new Float(image.getHeight() / scale).intValue(), null);
      graphics2D.dispose();
    }

    Original.setIcon(new ImageIcon(imageResized));

    final BufferedImage colorImagePrimary = new BufferedImage(textColorsWidth, textColorsHeight, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics2DPrimary = colorImagePrimary.createGraphics();
    graphics2DPrimary.setPaint(colorArt.primaryColor);
    graphics2DPrimary.fillRect(0, 0, colorImagePrimary.getWidth(), colorImagePrimary.getHeight());
    graphics2DPrimary.dispose();

    Primary.setIcon(new ImageIcon(colorImagePrimary));

    final BufferedImage colorImageSecondary = new BufferedImage(textColorsWidth, textColorsHeight, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics2DSecondary = colorImageSecondary.createGraphics();
    graphics2DSecondary.setPaint(colorArt.secondaryColor);
    graphics2DSecondary.fillRect(0, 0, colorImageSecondary.getWidth(), colorImageSecondary.getHeight());
    graphics2DSecondary.dispose();

    Secondary.setIcon(new ImageIcon(colorImageSecondary));

    final BufferedImage colorImageDetail = new BufferedImage(textColorsWidth, textColorsHeight, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics2DDetail = colorImageDetail.createGraphics();
    graphics2DDetail.setPaint(colorArt.detailColor);
    graphics2DDetail.fillRect(0, 0, colorImageDetail.getWidth(), colorImageDetail.getHeight());
    graphics2DDetail.dispose();

    Detail.setIcon(new ImageIcon(colorImageDetail));
  }
}
