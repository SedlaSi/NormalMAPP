package gui;

import gui.session.ImageLoader;
import gui.session.Session;
import image.*;
import image.Image;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by root on 14.7.16.
 */
public class MainScreen extends JFrame {

    JMenuBar menuBar;
    JMenu file, help, save, load, edit, filters, view;
    JMenuItem exit, openTexture, saveAll, loadAll, saveHeighMap, saveNormalMap, loadHeightMap, originalImage, heightMap, normalMap, invertNormal;
    JMenuItem undo, redo, sharpen, smooth;
    ImageLoader imageLoader;
    image.Image image;
    ImagePanel imagePanel;
    JPanel mainPanel,leftBoxPanel;
    Session session;

    NormalMapSettingsBox normalMapSettingsBox;
    NormalMapToolBox normalMapToolBox;
    HeightMapSettingsBox heightMapSettingsBox;
    HeightMapToolBox heightMapToolBox;

    ThisActionListener actionListener;

    private double mouseStartX, mouseStartY;
    private boolean mouseIsDragged = false;

    private double angle;
    private double normalHeight = 0.1;

    public static void main(String [] args){
        MainScreen mainScreen = new MainScreen(null,null);
        mainScreen.createFrame();
    }

    public MainScreen(Session session, ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        this.session = session;
    }

    public void createFrame() {
        ThisMenuListener menuListener = new ThisMenuListener();
        actionListener = new ThisActionListener();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        /*GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Dimension size = new Dimension(gd.getDisplayMode().getWidth(),gd.getDisplayMode().getHeight());*/
        this.setPreferredSize(new Dimension(800,600));
        this.pack();
        setLocationRelativeTo(null);
        //setResizable(false);
        setTitle("NormalMAPP");

        angle = 0;

        menuBar = new JMenuBar();

        file = new JMenu("File");
        file.addMenuListener(menuListener);
        menuBar.add(file);

        edit = new JMenu("Edit");
        edit.addMenuListener(menuListener);
        menuBar.add(edit);

        undo = new JMenuItem("Undo");
        undo.addActionListener(actionListener);
        edit.add(undo);

        redo = new JMenuItem("Redo");
        redo.addActionListener(actionListener);
        edit.add(redo);

        invertNormal = new JMenuItem("Invert Normals");
        invertNormal.addActionListener(actionListener);
        edit.add(invertNormal);

        view = new JMenu("View");
        view.addMenuListener(menuListener);
        menuBar.add(view);

        originalImage = new JMenuItem("Original Image");
        originalImage.addActionListener(actionListener);
        view.add(originalImage);

        heightMap = new JMenuItem("Height Map");
        heightMap.addActionListener(actionListener);
        view.add(heightMap);

        normalMap = new JMenuItem("Normal Map");
        normalMap.addActionListener(actionListener);
        view.add(normalMap);

        filters = new JMenu("Filters");
        filters.addMenuListener(menuListener);
        menuBar.add(filters);

        smooth = new JMenuItem("Smooth");
        smooth.addActionListener(actionListener);
        filters.add(smooth);

        sharpen = new JMenuItem("Sharpen");
        sharpen.addActionListener(actionListener);
        filters.add(sharpen);

        help = new JMenu("Help");
        help.addMenuListener(menuListener);
        menuBar.add(help);

        openTexture = new JMenuItem("Open Texture");
        openTexture.addActionListener(actionListener);
        file.add(openTexture);

        save = new JMenu("Save");
        save.addMenuListener(menuListener);
        file.add(save);

        /*saveAll = new JMenuItem("Save All");
        saveAll.addActionListener(actionListener);
        file.add(saveAll);*/

        load = new JMenu("Load");
        load.addMenuListener(menuListener);
        file.add(load);

        /*loadAll = new JMenuItem("Load All");
        loadAll.addActionListener(actionListener);
        file.add(loadAll);*/

        exit = new JMenuItem("Exit");
        exit.addActionListener(actionListener);
        file.add(exit);

        saveHeighMap = new JMenuItem("Save Height Map");
        saveHeighMap.addActionListener(actionListener);
        save.add(saveHeighMap);

        saveNormalMap = new JMenuItem("Save Normal Map");
        saveNormalMap.addActionListener(actionListener);
        save.add(saveNormalMap);

        loadHeightMap = new JMenuItem("Load Height Map");
        loadHeightMap.addActionListener(actionListener);
        load.add(loadHeightMap);

        imagePanel = new ImagePanel();
        imagePanel.addMouseWheelListener(e -> {
            if(e.getWheelRotation() > 0){
                imagePanel.decreaseScale();
            } else {
                imagePanel.increaseScale();
            }
            revalidate();
            repaint();
        });
        imagePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                int x = (int)(mouseEvent.getPoint().getX()-mouseStartX);
                int y = (int)(mouseEvent.getPoint().getY()-mouseStartY);
                System.out.println("mouse at:" + x + " " + y);
                System.out.println("image at:"  + " ");
                imagePanel.moveImg(x,y);
                revalidate();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                //imagePanel.setMouseInit(mouseEvent.getX(),mouseEvent.getY());
            }
        });

        imagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(imagePanel.getActiveLayer() == Layer.originalImage) {
                    imagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                    revalidate();
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getPoint().getX();
                mouseStartY = mouseEvent.getY();
                mouseIsDragged = true;

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                mouseIsDragged = false;
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(imagePanel, BorderLayout.CENTER);

        normalMapSettingsBox = new NormalMapSettingsBox();
        normalMapToolBox = new NormalMapToolBox();
        heightMapSettingsBox = new HeightMapSettingsBox();
        heightMapToolBox = new HeightMapToolBox();

        leftBoxPanel = new JPanel();
        leftBoxPanel.setLayout(new BorderLayout());
        leftBoxPanel.add(normalMapSettingsBox.getPanel(),BorderLayout.SOUTH);

        mainPanel.add(leftBoxPanel,BorderLayout.WEST);

//        this.add(imagePanel);
        this.add(mainPanel);
        this.setJMenuBar(menuBar);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(session != null){
                    session.endSession();
                }
                e.getWindow().dispose();
                System.exit(0);
            }
        });

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
    }

    private void updateImagePanel(BufferedImage newImage){
        imagePanel.setBufferedImage(newImage);
        revalidate();
        repaint();
    }

    private class ThisMenuListener implements MenuListener{

        @Override
        public void menuSelected(MenuEvent e) {

        }

        @Override
        public void menuDeselected(MenuEvent e) {

        }

        @Override
        public void menuCanceled(MenuEvent e) {

        }
    }

    private class ThisActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == openTexture){
                //System.out.println("opening");
                image = imageLoader.loadImage();
                if(!originalImage.isEnabled()){
                    originalImage.setEnabled(true);
                }
                if(image != null){
                    updateImagePanel(image.getNormalMap()); // uvodni obrazek po nacteni
                    imagePanel.setActiveLayer(Layer.normalMap);
                }
            } else if(e.getSource() == originalImage){
                if(image != null && image.getOriginalMap() != null){
                    updateImagePanel(image.getOriginalMap());
                    imagePanel.setActiveLayer(Layer.originalImage);
                }
            } else if(e.getSource() == heightMap){
                if(image != null){
                    updateImagePanel(image.getHeightMap());
                    imagePanel.setActiveLayer(Layer.heightMap);
                }
            } else if(e.getSource() == normalMap){
                if(image != null){
                    updateImagePanel(image.getNormalMap());
                    imagePanel.setActiveLayer(Layer.normalMap);
                }
            } else if(e.getSource() == saveNormalMap){
                imageLoader.saveNormalMap();
            } else if(e.getSource() == saveHeighMap){
                imageLoader.saveHeightMap();
            } else if(e.getSource() == exit){
                if(session!= null){
                    session.endSession();
                }
                dispose();
                System.exit(0);
            } else if(e.getSource() == invertNormal){
                angle = (Math.PI/2);
                imageLoader.refreshNormalMap(angle,normalHeight);
                updateImagePanel(image.getNormalMap());
            } else if(e.getSource() == loadHeightMap){  // uvodni obrazek po nacteni
                image = imageLoader.loadHeightMap();
                if(originalImage.isEnabled()){
                    originalImage.setEnabled(false);
                }
                if(image != null){
                    updateImagePanel(image.getNormalMap()); // uvodni obrazek po nacteni
                    imagePanel.setActiveLayer(Layer.normalMap);
                }
            }
        }
    }

    private enum Layer {
        originalImage,
        heightMap,
        normalMap
    }

    private class ImagePanel extends JPanel {
        double scale;
        int posX = 0;
        int posY = 0;
        int squareSize = 20;
        int imgPosX,imgPosY;
        BufferedImage image;
        private boolean drawSquare = true;
        private java.util.List<Rectangle> squares;
        private java.util.List<RelativeSquarePosition> relativePos;
        private Layer activeLayer = Layer.originalImage;


        private class RelativeSquarePosition {
            private double x;
            private double y;

            public RelativeSquarePosition(double x, double y){
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
            squares = new ArrayList<>(3);
            relativePos = new ArrayList<>(3);
        }

        public void setBufferedImage(BufferedImage image){
            this.image = image;
        }

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
                if(drawSquare){ // vykreslovani zamerovacich ctvercu
                    for(int i = 0; i < squares.size(); i++){
                        Rectangle s = squares.get(i);
                        RelativeSquarePosition rel = relativePos.get(i);
                        s.setLocation((int)(x + rel.getX()*(double)imageWidth),(int)(y+ rel.getY()*(double)imageHeight));
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
            posX = x;
            posY = y;
        }

        public void addSquare(int x, int y){
            Rectangle square = new Rectangle(x,y,squareSize,squareSize);
            squares.add(square);
            /*System.out.println(imgPosX + " "+ imgPosY);
            System.out.println(x + " "+ y);*/
            /*System.out.println("layer: "+ posX +" "+posY);
            System.out.println();*/
            double xRel;
            double yRel;
            if(imgPosX < 0){
                 xRel = (Math.abs(scale*imgPosX)+ x - scale*squareSize/2);
            } else {
                 xRel = (x - scale*imgPosX - scale*squareSize/2);
            }
            if(posX < 0){
                xRel += Math.abs(posX);
            } else {
                xRel -= posX;
            }
            xRel /= (scale*image.getWidth());
            if(imgPosY < 0){
                 yRel = (Math.abs(scale*imgPosY)+ y- scale*squareSize/2);
            } else {
                 yRel = (y - scale*imgPosY- scale*squareSize/2);
            }
            if(posY < 0){
                yRel += Math.abs(posY);
            } else {
                yRel -= posY;
            }
            yRel /=(scale*image.getHeight());
            System.out.println(xRel+ " "+ yRel);
            System.out.println();

            RelativeSquarePosition rel = new RelativeSquarePosition(xRel,yRel);
            relativePos.add(rel);
        }

        public void removeSquare(int i){
            if(squares.get(i) != null){
                squares.remove(i);
            }
            if(relativePos.get(i) != null){
                relativePos.remove(i);
            }
        }

        private void enableSquare(){
            drawSquare = true;
        }

        private void disableSquare(){
            drawSquare = false;
        }

        public void setSquareSize(int size){
            squareSize = size;
        }

        public void setActiveLayer(Layer layer){
            activeLayer = layer;
            if(activeLayer == Layer.originalImage){
                enableSquare();
            } else {
                disableSquare();
            }
        }

        public Layer getActiveLayer(){
            return activeLayer;
        }
    }

    private class NormalMapSettingsBox {
        JPanel settingBox;
        JPanel lightPanel, heightPanel, recalculatePanel, lightToolPanel, lightToolPanelLeft, lightToolPanelRight;
        ReviewPanel reviewNormalPanelPP, reviewNormalPanelPM, reviewNormalPanelMP, reviewNormalPanelMM;
        JButton recalculateButton, plusPlusButton, plusMinusButton, minusPlusButton, minusMinusButton;
        JSlider height;

        private static final String DIR_PP = "++";
        private static final String DIR_PM = "+-";
        private static final String DIR_MM = "--";
        private static final String DIR_MP = "-+";

        private Dimension reviewDimension;
        private Dimension reviewButtonDimension;


        BufferedImage ppImage, pmImage, mpImage, mmImage;

        JPanel getPanel(){
            if(settingBox == null){
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());
                reviewButtonDimension = new Dimension(35,35);

                lightPanel = new JPanel();
                lightPanel.setLayout(new BorderLayout());
                lightPanel.add(new JLabel("Lightning:"),BorderLayout.NORTH);
                lightToolPanel = new JPanel();
                lightToolPanel.setLayout(new BorderLayout());
                lightToolPanelLeft = new JPanel();
                lightToolPanelLeft.setLayout(new BorderLayout());
                plusPlusButton = new JButton();
                plusPlusButton.setPreferredSize(reviewButtonDimension);
                plusPlusButton.addActionListener(reviewActionListener);
                try {
                    plusPlusButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/review_lamp_icon/PP.png"))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lightToolPanelLeft.add(plusPlusButton,BorderLayout.NORTH);
                //lightToolPanelLeft.add(new JLabel("left"));

                plusMinusButton = new JButton();
                plusMinusButton.setPreferredSize(reviewButtonDimension);
                plusMinusButton.addActionListener(reviewActionListener);
                try {
                    plusMinusButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/review_lamp_icon/PM.png"))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lightToolPanelLeft.add(plusMinusButton,BorderLayout.SOUTH);
                //lightToolPanelLeft.add(spacePanel,BorderLayout.CENTER);

                lightToolPanel.add(lightToolPanelLeft,BorderLayout.WEST);

                lightToolPanelRight = new JPanel();
                lightToolPanelRight.setLayout(new BorderLayout());

                minusPlusButton = new JButton();
                minusPlusButton.setPreferredSize(reviewButtonDimension);
                minusPlusButton.addActionListener(reviewActionListener);
                try {
                    minusPlusButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/review_lamp_icon/MP.png"))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lightToolPanelRight.add(minusPlusButton,BorderLayout.NORTH);

                minusMinusButton = new JButton();
                minusMinusButton.setPreferredSize(reviewButtonDimension);
                minusMinusButton.addActionListener(reviewActionListener);
                try {
                    minusMinusButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/review_lamp_icon/MM.png"))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lightToolPanelRight.add(minusMinusButton,BorderLayout.SOUTH);
                //lightToolPanelRight.add(new JLabel("right"));

                lightToolPanel.add(lightToolPanelRight,BorderLayout.EAST);

                reviewDimension = new Dimension(120,120);
                reviewNormalPanelPP = new ReviewPanel(DIR_PP);
                reviewNormalPanelPP.setPreferredSize(reviewDimension);

                lightToolPanel.add(reviewNormalPanelPP,BorderLayout.CENTER);

                lightPanel.add(lightToolPanel,BorderLayout.SOUTH);

                heightPanel = new JPanel();
                JLabel heightLabel = new JLabel("Height:");
                height = new JSlider(JSlider.HORIZONTAL,0,100,91);
                height.setPreferredSize(new Dimension(120,50));
                height.setMajorTickSpacing(50);
                height.setMinorTickSpacing(10);
                height.setPaintTicks(true);
                height.setPaintLabels(true);

                heightPanel.add(heightLabel);
                heightPanel.add(height);

                recalculatePanel = new JPanel();
                recalculateButton = new JButton("Recalculate");
                recalculateButton.addActionListener(reviewActionListener);
                recalculatePanel.add(recalculateButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                settingBox.add(lightPanel,BorderLayout.NORTH);
                settingBox.add(heightPanel,BorderLayout.CENTER);
                settingBox.add(recalculatePanel, BorderLayout.SOUTH);


            }
            return settingBox;
        }

        private ActionListener reviewActionListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == plusPlusButton) {
                    angle = 0;
                    if(reviewNormalPanelPP == null){
                        reviewNormalPanelPP = new ReviewPanel(DIR_PP);
                        reviewNormalPanelPP.setPreferredSize(reviewDimension);
                    }
                    lightToolPanel.remove(2);
                    lightToolPanel.add(reviewNormalPanelPP,BorderLayout.CENTER);
                    lightToolPanel.invalidate();
                    lightToolPanel.revalidate();
                    lightToolPanel.repaint();
                } else if(e.getSource() == plusMinusButton){
                    angle = (Math.PI + Math.PI/2);
                    if(reviewNormalPanelPM == null){
                        reviewNormalPanelPM = new ReviewPanel(DIR_PM);
                        reviewNormalPanelPM.setPreferredSize(reviewDimension);
                    }
                    lightToolPanel.remove(2);
                    lightToolPanel.add(reviewNormalPanelPM,BorderLayout.CENTER);
                    lightToolPanel.invalidate();
                    lightToolPanel.revalidate();
                    lightToolPanel.repaint();
                } else if(e.getSource() == minusPlusButton){
                    angle = (Math.PI/2);
                    if(reviewNormalPanelMP == null){
                        reviewNormalPanelMP = new ReviewPanel(DIR_MP);
                        reviewNormalPanelMP.setPreferredSize(reviewDimension);
                    }
                    lightToolPanel.remove(2);
                    lightToolPanel.add(reviewNormalPanelMP,BorderLayout.CENTER);
                    lightToolPanel.invalidate();
                    lightToolPanel.revalidate();
                    lightToolPanel.repaint();
                } else if(e.getSource() == minusMinusButton){
                    angle = (Math.PI);
                    if(reviewNormalPanelMM == null){
                        reviewNormalPanelMM = new ReviewPanel(DIR_MM);
                        reviewNormalPanelMM.setPreferredSize(reviewDimension);
                    }
                    lightToolPanel.remove(2);
                    lightToolPanel.add(reviewNormalPanelMM,BorderLayout.CENTER);
                    lightToolPanel.invalidate();
                    lightToolPanel.revalidate();
                    lightToolPanel.repaint();
                } else if(e.getSource() == recalculateButton){
                    if(imageLoader != null && image != null) {
                        //System.out.println("refresh");
                        //System.out.println("height: "+height.getValue());
                        imageLoader.refreshNormalMap(angle,normalHeight = (((double)height.getValue()*(-99.0))/10000.0+1.0));
                        updateImagePanel(image.getNormalMap());
                    }
                }
            }
        };

        private class ReviewPanel extends JPanel{

            private BufferedImage image;

            public ReviewPanel(String direction){
                switch (direction){
                    case DIR_PP: image = getPlusPlus(); break;
                    case DIR_PM: image = getPlusMinus(); break;
                    case DIR_MP: image = getMinusPlus(); break;
                    default: image = getMinusMinus(); break;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
            }

            private BufferedImage getPlusPlus(){
                try {
                    ppImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/PP.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return ppImage;
            }

            private BufferedImage getPlusMinus(){
                try {
                    ppImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/PM.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return ppImage;
            }

            private BufferedImage getMinusPlus(){
                try {
                    ppImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/MP.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return ppImage;
            }

            private BufferedImage getMinusMinus(){
                try {
                    ppImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/MM.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return ppImage;
            }

        }
    }

    private class NormalMapToolBox {

    }

    private class HeightMapSettingsBox {

    }

    private class HeightMapToolBox {

    }


}

