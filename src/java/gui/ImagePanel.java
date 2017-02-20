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
    int posX = 0;
    int posY = 0;
    int squareSize = 20;
    int imgPosX = 0;
    int imgPosY = 0;
    JLabel imageLabel;
    private int highlightedSquare = -1;

    BufferedImage image;
    boolean drawSquare = true;
    Rectangle square = new Rectangle(squareSize,squareSize);
    Rectangle highlightSquare = new Rectangle(squareSize,squareSize);
    //java.util.List<Rectangle> squares;
    java.util.List<Marker> markerList;




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
        //imageLabel = new JLabel();
        //this.add(imageLabel);
        //relativePos = new ArrayList<>(3);
    }

    public void setMarkerList(java.util.List<Marker> markerList){
        this.markerList = markerList;
    }

    public void setBufferedImage(BufferedImage image){
        this.image = image;
        initScale = 1.0;
        /*while(this.getSize().height*(-initScale) < image.getHeight() || this.getSize().width*(-initScale) < image.getWidth()){
            initScale -= 2.0;
        }*/
        if(scale < initScale){
            scale  = initScale + 1.0;
        }
        //imageLabel.setIcon(new ImageIcon(this.image));
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
            //g2.drawImage(image,imgPosX,imgPosY,this);
            /*int prevX = imageLabel.getLocation().x;
            int prevY = imageLabel.getLocation().y;
            imageLabel.setLocation(prevX + posX,prevY + posY);
            imageLabel.repaint();
            posX = 0;
            posY = 0;*/
            if(drawSquare && markerList != null){ // vykreslovani zamerovacich ctvercu
                for(int i = 0; i < markerList.size(); i++){
                    if(i == highlightedSquare){
                        Marker marker = markerList.get(i);
                        //Rectangle s = marker.getSquare();
                        Rectangle s = square;
                        s.setLocation((int)(x + marker.getPosX()*(double)imageWidth - (squareSize / 2)),(int)(y+ marker.getPosY()*(double)imageHeight - (squareSize / 2)));
                        g2.draw(s);
                        g2.setColor(Color.GREEN);
                        g2.fillRect((int)(x + marker.getPosX()*(double)imageWidth - (squareSize / 2)),(int)(y+ marker.getPosY()*(double)imageHeight - (squareSize / 2)),squareSize,squareSize);
                        g2.setColor(Color.gray);
                    } else {
                        Marker marker = markerList.get(i);
                        //Rectangle s = marker.getSquare();
                        Rectangle s = square;
                        s.setLocation((int) (x + marker.getPosX() * (double) imageWidth - (squareSize / 2)), (int) (y + marker.getPosY() * (double) imageHeight - (squareSize / 2)));
                        g2.draw(s);
                    }
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
        if(scale + 0.25 <= 3 + initScale) {
            scale += 0.25;
        }
    }

    public void decreaseScale(){
        if(scale - 0.25 > initScale){
            scale -= 0.25;
        }
    }

    public void setHighlightedSquare(int i){
        highlightedSquare = i;
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

        posX = (int)(x);
        posY = (int)(y);
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
