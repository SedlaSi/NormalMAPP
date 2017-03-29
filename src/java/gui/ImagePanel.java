package gui;

import gui.sfs.Marker;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created by root on 22.10.16.
 */
public abstract class ImagePanel extends JPanel {
    double scale;
    double initScale = 0.0;
    static int posX = 0;
    static int posY = 0;
    int squareSize = 20;
    double imgPosX = 0;
    double imgPosY = 0;

    static int mouseX = 0;
    static int mouseY = 0;

    private int highlightedSquare = -1;
    Graphics2D g2;

    BufferedImage image;
    boolean drawSquare = true;
    Rectangle square = new Rectangle(squareSize, squareSize);
    java.util.List<Marker> markerList;

    public ImagePanel() {
        scale = 1.0;
        setBackground(Color.gray);
    }

    public void mousePosition(int x, int y) {
        mouseX = x + mouseX;
        mouseY = y + mouseY;
    }

    public void setMarkerList(java.util.List<Marker> markerList) {
        this.markerList = markerList;
    }

    public void setBufferedImage(BufferedImage image) {
        this.image = image;
        initScale = 0.0;

        if (scale < initScale) {
            scale = initScale + 1.0;
        }
        int w = getWidth();
        int h = getHeight();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        double x = (w - scale * imageWidth) / 2;
        double y = (h - scale * imageHeight) / 2;
        imgPosX = (int) x;
        imgPosY = (int) y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // puvodni reseni
            int w = getWidth();
            int h = getHeight();
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            double x = (w - scale * imageWidth) / 2;
            double y = (h - scale * imageHeight) / 2;
            imgPosX = (int) x;
            imgPosY = (int) y;

            AffineTransform at = AffineTransform.getTranslateInstance(imgPosX, imgPosY);

            g2.scale(scale, scale);
            g2.translate(posX + mouseX, posY + mouseY);

            g2.drawRenderedImage(image, at);

            if (drawSquare && markerList != null) { // vykreslovani zamerovacich ctvercu
                for (int i = 0; i < markerList.size(); i++) {
                    if (i == highlightedSquare) {
                        Marker marker = markerList.get(i);

                        Rectangle s = square;
                        s.setLocation((int) (imgPosX + marker.getPosX() * (double) image.getWidth() - (squareSize / 2)), (int) (imgPosY + marker.getPosY() * (double) image.getHeight() - (squareSize / 2)));
                        g2.draw(s);
                        g2.setColor(Color.GREEN);
                        g2.fillRect((int) (imgPosX + marker.getPosX() * (double) image.getWidth() - (squareSize / 2)), (int) (imgPosY + marker.getPosY() * (double) image.getHeight() - (squareSize / 2)), squareSize, squareSize);
                        g2.setColor(Color.gray);
                    } else {
                        Marker marker = markerList.get(i);

                        Rectangle s = square;
                        s.setLocation((int) (imgPosX + marker.getPosX() * (double) image.getWidth() - (squareSize / 2)), (int) (imgPosY + marker.getPosY() * (double) image.getHeight() - (squareSize / 2)));
                        g2.draw(s);
                    }
                }
            }
        }
    }

    public Graphics2D getGraphic() {
        return g2;
    }

    /**
     * For the scroll pane.
     */
    public Dimension getPreferredSize() {
        int w = (int) (scale * image.getWidth());
        int h = (int) (scale * image.getHeight());
        return new Dimension(w, h);
    }

    public void increaseScale() {
        if (scale + 0.25 <= 3.0 + initScale) {
            scale += 0.25;
        }
    }

    public void decreaseScale() {
        if (scale - 0.25 > initScale) {
            scale -= 0.25;
        }
    }

    public void setHighlightedSquare(int i) {
        highlightedSquare = i;
    }

    public void moveImg(int x, int y) {

        posX = x;
        posY = y;
    }

}
