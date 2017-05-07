package gui;

import gui.sfs.Marker;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created by sedlasi1 on 22.10.16.
 *
 * ImagePanel abstract class is JPanel for displaying loaded
 * image on the screen.
 * There are three classes extending this class:
 *      OriginalImagePanel
 *      HeightMapImagePanel
 *      NormalMapImagePanel
 *  These classes are used in gui.MainScreen class
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

    // index of square which will be highlighted in next drawcall
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

    /**
     * @method mousePosition() is set from gui.MainScreen in MouseListener
     * X,Y are coordinates of mouse on the JPanel
     *
     * @param x
     * @param y
     */
    void mousePosition(int x, int y) {
        mouseX = x + mouseX;
        mouseY = y + mouseY;
    }

    void setMarkerList(java.util.List<Marker> markerList) {
        this.markerList = markerList;
    }

    /**
     * @method setBufferedImage() sets image which will be
     * drawn in next drawcall
     *
     * @param image
     */
    void setBufferedImage(BufferedImage image) {
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

    /**
     * @method paintComponent() draw image on the screen
     * witch scale and imgPosX,imgPosY propositions
     *
     * @param g
     */
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
     * @method increaseScale() is called from gui.MainScreen
     * from MouseListener.
     */
    void increaseScale() {
        if (scale + 0.25 <= 3.0 + initScale) {
            scale += 0.25;
        }
    }

    /**
     * @method decreaseScale() is called from gui.MainScreen
     * from MouseListener.
     */
    void decreaseScale() {
        if (scale - 0.25 > initScale) {
            scale -= 0.25;
        }
    }

    /**
     * @method setHighlightedSquare() sets an index of
     * square which will be highlighted in next drawcall
     *
     * @param i
     */
    void setHighlightedSquare(int i) {
        highlightedSquare = i;
    }

    /**
     * @method moveImg() changes the posX,posY,
     * which will move the image to such position in
     * the next drawcall.
     *
     * @param x
     * @param y
     */
    void moveImg(int x, int y) {

        posX = x;
        posY = y;
    }

}
