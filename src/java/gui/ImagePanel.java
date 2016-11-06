package gui;

import gui.mark.Marker;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created by root on 22.10.16.
 */
public abstract class ImagePanel extends JPanel {
    double scale;
    int posX = 0;
    int posY = 0;
    int squareSize = 20;
    int imgPosX,imgPosY;
    BufferedImage image;
    boolean drawSquare = true;
    //java.util.List<Rectangle> squares;
    java.util.List<Marker> markerList;
    //java.util.List<RelativeSquarePosition> relativePos;
    //private Layer activeLayer = Layer.originalImage;


    class RelativeSquarePosition {
        private double x;
        private double y;

        RelativeSquarePosition(double x, double y){
            this.x = x;
            this.y = y;
        }

        public double getX(){
            return x;
        }

        public double getY(){
            return y;
        }
    }

    public ImagePanel() {
        scale = 1.0;
        setBackground(Color.gray);
        //relativePos = new ArrayList<>(3);
    }

    public void setMarkerList(java.util.List<Marker> markerList){
        this.markerList = markerList;
    }

    public void setBufferedImage(BufferedImage image){
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth();
            int h = getHeight();
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            double x = (w - scale * imageWidth) / 2;
            double y = (h - scale * imageHeight) / 2;

                /*System.out.println(w + " -> " + imageWidth);
                System.out.println(x);
                System.out.println(h + " -> " + imageHeight);
                System.out.println(y);*/
            imgPosX = (int)x;
            imgPosY = (int)y;

            AffineTransform at = AffineTransform.getTranslateInstance(x,y);
            //at.scale(1, 1);
            //at.translate(posX,posY);
            g2.translate(posX,posY);
            g2.scale(scale,scale);
            g2.drawRenderedImage(image,at);
            if(drawSquare && markerList != null){ // vykreslovani zamerovacich ctvercu
                for(int i = 0; i < markerList.size(); i++){
                    Marker marker = markerList.get(i);
                    Rectangle s = marker.getSquare();
                    s.setLocation((int)(x + marker.getPosX()*(double)imageWidth),(int)(y+ marker.getPosY()*(double)imageHeight));
                    g2.draw(s);
                }
            }
        }
    }

    /**
     * For the scroll pane.
     */
    public Dimension getPreferredSize() {
        int w = (int) (scale * image.getWidth());
        int h = (int) (scale * image.getHeight());
        return new Dimension(w, h);
    }

    public void increaseScale(){
        if(scale + 0.5 <= 3) {
            scale += 0.5;
        }
    }

    public void decreaseScale(){
        if(scale - 0.5 > 0){
            scale -= 0.5;
        }
    }

    public void moveImg(int x, int y){
            /*if(posX < 0){
                posX = x + Math.abs(posX);
            } else {
                posX = x - posX;
            }
            if(posY < 0){
                posY= y + Math.abs(posY);
            } else {
                posY = y - posY;
            }*/

        posX = x;
        posY = y;
    }

    /*public void setActiveLayer(Layer layer){
        activeLayer = layer;
        if(activeLayer == Layer.originalImage){
            enableSquare();
        } else {
            disableSquare();
        }
    }

    public Layer getActiveLayer(){
        return activeLayer;
    }*/
}
