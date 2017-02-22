package gui;

import gui.sfs.AlgorithmSettingsDialog;
import gui.sfs.EditMarkerScreen;
import gui.sfs.Marker;
import gui.session.ImageLoader;
import gui.session.Session;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 14.7.16.
 */
public class MainScreen extends JFrame {

    JMenuBar menuBar;
    JMenu file, help, save, load;
    JMenuItem exit, openTexture, saveHeighMap, saveNormalMap, loadHeightMap;
    ImageLoader imageLoader;
    image.Image image;
    //ImagePanel imagePanel;
    JPanel mainPanel,leftBoxPanel;
    Session session;

    private final boolean updateAllImages = true;


    JTabbedPane tabbedPanel;
    JPanel cardSettingsBoxPanel;
    CardLayout cardSettingsBoxLayout;


    OriginalMapSettingsBox originalMapSettingsBox;
    NormalMapSettingsBox normalMapSettingsBox;
    HeightMapSettingsBox heightMapSettingsBox;

    OriginalMapImagePanel originalMapImagePanel;
    HeightMapImagePanel heightMapImagePanel;
    NormalMapImagePanel normalMapImagePanel;

    java.util.List<Marker> markerList;

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

        //CardLayout cl = new CardLayout();

        tabbedPanel = new JTabbedPane();

        markerList = new ArrayList<>(3);

        originalMapImagePanel = new OriginalMapImagePanel();
        originalMapImagePanel.setMarkerList(markerList);

        originalMapImagePanel.addMouseWheelListener(e -> {
            if(e.getWheelRotation() > 0){
                if(updateAllImages){
                    heightMapImagePanel.decreaseScale();
                    normalMapImagePanel.decreaseScale();
                }
                originalMapImagePanel.decreaseScale();
            } else {
                if(updateAllImages){
                    heightMapImagePanel.increaseScale();
                    normalMapImagePanel.increaseScale();
                }
                originalMapImagePanel.increaseScale();
            }
            revalidate();
            repaint();
        });
        originalMapImagePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                int x = (int)(mouseEvent.getX()-mouseStartX);
                int y = (int)(mouseEvent.getY()-mouseStartY);
                if(updateAllImages){
                    heightMapImagePanel.moveImg(x,y);
                    normalMapImagePanel.moveImg(x,y);
                }
                originalMapImagePanel.moveImg(x,y);
                revalidate();
                repaint();
               /* Graphics2D g2O = originalMapImagePanel.getGraphic();
                Graphics2D g2H = originalMapImagePanel.getGraphic();
                Graphics2D g2N = originalMapImagePanel.getGraphic();
                g2O.
                if (g2O.getBounds2D().contains(mouseStartX, mouseStartY)) {
                    myRect.x += x;
                    myRect.y += y;
                    repaint();
                }
                mouseStartX += x;
                mouseStartY += y;*/
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                originalMapImagePanel.hightlightIfInterselectWithMouse(mouseEvent.getX(),mouseEvent.getY());
                //imagePanel.setMouseInit(mouseEvent.getX(),mouseEvent.getY());
            }
        });

        originalMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(originalMapSettingsBox.activeButton == originalMapSettingsBox.addMarkerButton){
                    originalMapImagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                    originalMapSettingsBox.updateList();
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                } else if(originalMapSettingsBox.activeButton == originalMapSettingsBox.editMarkerButton){
                    originalMapImagePanel.editSquare(mouseEvent.getX(),mouseEvent.getY());
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                } else if(originalMapSettingsBox.activeButton == originalMapSettingsBox.removeMarkerButton){
                    originalMapImagePanel.removeSquare(mouseEvent.getX(), mouseEvent.getY());
                    originalMapSettingsBox.updateList();
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getPoint().getX();
                mouseStartY = mouseEvent.getY();
                originalMapImagePanel.mousePosition(mouseStartX,mouseStartY);
                mouseIsDragged = true;

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                originalMapImagePanel.setInitPosition(mouseEvent.getX(),mouseEvent.getY());
                /*System.out.println(mouseEvent.getX());
                System.out.println(mouseEvent.getY());
                System.out.println();*/
                mouseIsDragged = false;
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        //*************
        heightMapImagePanel = new HeightMapImagePanel();

        heightMapImagePanel.addMouseWheelListener(e -> {
            if(e.getWheelRotation() > 0){
                if(updateAllImages){
                    originalMapImagePanel.decreaseScale();
                    normalMapImagePanel.decreaseScale();
                }
                heightMapImagePanel.decreaseScale();
            } else {
                if(updateAllImages){
                    originalMapImagePanel.increaseScale();
                    normalMapImagePanel.increaseScale();
                }
                heightMapImagePanel.increaseScale();
            }
            revalidate();
            repaint();
        });
        heightMapImagePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                int x = (int)(mouseEvent.getPoint().getX()-mouseStartX);
                int y = (int)(mouseEvent.getPoint().getY()-mouseStartY);
                if(updateAllImages){
                    originalMapImagePanel.moveImg(x,y);
                    normalMapImagePanel.moveImg(x,y);
                }
                heightMapImagePanel.moveImg(x,y);
                revalidate();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                //imagePanel.setMouseInit(mouseEvent.getX(),mouseEvent.getY());
            }
        });

        heightMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                //heightMapImagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                //revalidate();
                //repaint();
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

        //*************
        normalMapImagePanel = new NormalMapImagePanel();

        normalMapImagePanel.addMouseWheelListener(e -> {
            if(e.getWheelRotation() > 0){
                if(updateAllImages){
                    originalMapImagePanel.decreaseScale();
                    heightMapImagePanel.decreaseScale();
                }
                normalMapImagePanel.decreaseScale();
            } else {
                if(updateAllImages){
                    originalMapImagePanel.increaseScale();
                    heightMapImagePanel.increaseScale();
                }
                normalMapImagePanel.increaseScale();
            }
            revalidate();
            repaint();
        });
        normalMapImagePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                int x = (int)(mouseEvent.getPoint().getX()-mouseStartX);
                int y = (int)(mouseEvent.getPoint().getY()-mouseStartY);
                if(updateAllImages){
                    originalMapImagePanel.moveImg(x,y);
                    heightMapImagePanel.moveImg(x,y);
                }
                normalMapImagePanel.moveImg(x,y);
                revalidate();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                //imagePanel.setMouseInit(mouseEvent.getX(),mouseEvent.getY());
            }
        });

        normalMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                /*normalMapImagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                revalidate();
                repaint();*/
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getPoint().getX();
                mouseStartY = mouseEvent.getY();
                originalMapImagePanel.mousePosition(mouseStartX,mouseStartY);
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

        tabbedPanel.add(originalMapImagePanel,"Original Image");
        tabbedPanel.add(heightMapImagePanel,"Height Map");
        tabbedPanel.add(normalMapImagePanel,"Normal Map");

        //cl.show(tabbedPanel,"Normal Map");
        /*tabbedPanel.revalidate();
        tabbedPanel.repaint();*/

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPanel, BorderLayout.CENTER);

        originalMapSettingsBox = new OriginalMapSettingsBox();
        originalMapSettingsBox.setMarkerList(markerList);
        normalMapSettingsBox = new NormalMapSettingsBox();
        heightMapSettingsBox = new HeightMapSettingsBox();

        leftBoxPanel = new JPanel(new BorderLayout());
        //leftBoxPanel.setLayout(new BorderLayout());
        cardSettingsBoxLayout = new CardLayout();
        cardSettingsBoxPanel = new JPanel(cardSettingsBoxLayout);
        leftBoxPanel.add(cardSettingsBoxPanel,BorderLayout.CENTER);
        cardSettingsBoxPanel.add(originalMapSettingsBox.getPanel(),"os");
        cardSettingsBoxPanel.add(heightMapSettingsBox.getPanel(),"hs");
        cardSettingsBoxPanel.add(normalMapSettingsBox.getPanel(),"ns");

        tabbedPanel.addChangeListener(changeEvent -> {
            if(tabbedPanel.getSelectedComponent() == originalMapImagePanel){
                cardSettingsBoxLayout.show(cardSettingsBoxPanel,"os");
                //leftBoxPanel.remove(0);
                //leftBoxPanel.add(originalMapSettingsBox.getPanel(),BorderLayout.SOUTH);
            } else if(tabbedPanel.getSelectedComponent() == heightMapImagePanel){
                cardSettingsBoxLayout.show(cardSettingsBoxPanel,"hs");
                //leftBoxPanel.remove(0);
                //leftBoxPanel.add(heightMapSettingsBox.getPanel(),BorderLayout.SOUTH);
            } else if(tabbedPanel.getSelectedComponent() == normalMapImagePanel) {
                cardSettingsBoxLayout.show(cardSettingsBoxPanel,"ns");
                //leftBoxPanel.remove(0);
                //leftBoxPanel.add(normalMapSettingsBox.getPanel(),BorderLayout.SOUTH);
            }
            revalidate();
            repaint();
        });


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

        // TEST SEGMENT
        /*image = imageLoader.testloadImage();
        if(image != null){
            updateImagePanels(); // uvodni obrazek po nacteni
            //imagePanel.setActiveLayer(Layer.normalMap);
        }*/
        // ====== COIN ============
        /*Rectangle r1 = new Rectangle(661,153,20,20);
        Rectangle r2 = new Rectangle(746,400,20,20);
        Rectangle r3 = new Rectangle(474,586,20,20);
        Rectangle r4 = new Rectangle(630,348,20,20);
        Marker m1 = new Marker("15# Marker",123, 60, 235, 0.66, 0.31895687061183553);
        Marker m2 = new Marker("16# Marker",124, 126, 0, 0.716, 0.5667001003009027);
        Marker m3 = new Marker("18# Marker",19, 192, 148, 0.444, 0.753259779338014);
        Marker m4 = new Marker("19# Marker",139, 114, 253, 0.6, 0.5145436308926781);
        m1.setSquare(r1);
        m2.setSquare(r2);
        m3.setSquare(r3);
        m4.setSquare(r4);
        markerList.add(m1);
        markerList.add(m2);
        markerList.add(m3);
        markerList.add(m4);*/

        /*Rectangle r1 = new Rectangle(449,216,20,20);
        Rectangle r2 = new Rectangle(581,101,20,20);
        Rectangle r3 = new Rectangle(655,365,20,20);
        Rectangle r4 = new Rectangle(396,554,20,20);
        Marker m1 = new Marker("15# Marker",84, 98, 243, 0.601, 0.5170511534603811);
        Marker m2 = new Marker("16# Marker",56, 131, 21, 0.665, 0.32029421598127716);
        Marker m3 = new Marker("18# Marker",212, 60, 61, 0.713, 0.567703109327984);
        Marker m4 = new Marker("19# Marker",92, 236, 183, 0.454, 0.757271815446339);
        m1.setSquare(r1);
        m2.setSquare(r2);
        m3.setSquare(r3);
        m4.setSquare(r4);
        markerList.add(m1);
        markerList.add(m2);
        markerList.add(m3);
        markerList.add(m4);*/

        // ======== BALL_02 ============
        /*Rectangle r1 = new Rectangle(352,335,20,20);
        Rectangle r2 = new Rectangle(767,344,20,20);
        Rectangle r3 = new Rectangle(577,187,20,20);
        Marker m1 = new Marker("15# Marker",127, 194, 235, 0.12037037037037036, 0.48333333333333334);
        Marker m2 = new Marker("16# Marker",73, 87, 235, 0.8351851851851851, 0.5);
        Marker m3 = new Marker("18# Marker",185, 161, 235, 0.48333333333333334, 0.20925925925925926);
        m1.setSquare(r1);
        m2.setSquare(r2);
        m3.setSquare(r3);
        markerList.add(m1);
        markerList.add(m2);
        markerList.add(m3);*/


        //======================
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
    }

    private void updateOriginal(BufferedImage original){
        originalMapImagePanel.setBufferedImage(original);
        revalidate();
        repaint();
    }

    private void updateHeight(BufferedImage height){
        heightMapImagePanel.setBufferedImage(height);
        revalidate();
        repaint();
    }

    private void updateNormal(BufferedImage normal){
        normalMapImagePanel.setBufferedImage(normal);
        revalidate();
        repaint();
    }

    private void updateImagePanels(){

        updateNormal(image.getNormalMap());
        updateHeight(image.getHeightMap());
        if(image.getOriginalMap() != null){
            updateOriginal(image.getOriginalMap());
        }

    }

    private JFrame getMainReference(){
        return this;
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
                if(image != null){
                    updateImagePanels(); // uvodni obrazek po nacteni
                    //imagePanel.setActiveLayer(Layer.normalMap);
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
            } else if(e.getSource() == loadHeightMap){  // uvodni obrazek po nacteni
                image = imageLoader.loadHeightMap();

                if(image != null){
                    updateImagePanels();
                    //imagePanel.setActiveLayer(Layer.normalMap);
                }
            }
        }
    }

    private class OriginalMapImagePanel  extends ImagePanel {

        public OriginalMapImagePanel(){
            super();
        }

        private int markerNumber = 0;

        public void addSquare(int x, int y){
            if(image != null) {
                //Rectangle square = new Rectangle(x, y, squareSize, squareSize);
                Marker marker = new Marker(markerNumber+"# Marker");
                //marker.setSquare(square);
            /*System.out.println(imgPosX + " "+ imgPosY);
            System.out.println(x + " "+ y);*/
            /*System.out.println("layer: "+ posX +" "+posY);
            System.out.println();*/
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x /*- scale * squareSize / 2*/);
                } else {
                    xRel = (x - scale * imgPosX/* - scale * squareSize / 2*/);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y /*- scale * squareSize / 2*/);
                } else {
                    yRel = (y - scale * imgPosY /*- scale * squareSize / 2*/);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY);
                } else {
                    yRel -= posY;
                }
                yRel /= (scale * image.getHeight());
                marker.setPosX(xRel);
                marker.setPosY(yRel);

                if(xRel < 0.0  || yRel < 0.0 || xRel > 1.0 || yRel > 1.0){
                    return;
                }
                /**
                 * ZDE SE POTOM SPUSTI OBRAZOVKA NA UPRAVU UDAJU x, y A name
                 */
                EditMarkerScreen editMarkerScreen = new EditMarkerScreen(getMainReference(),"",Dialog.ModalityType.DOCUMENT_MODAL);
                editMarkerScreen.setMarker(marker);
                editMarkerScreen.startFrame();

                if(marker.getX() == -1 || marker.getY() == -1 || marker.getZ() == -1){ // Uzivatel dal "cancel"
                    return;
                }
                markerNumber++;
                // marker info
                /*System.out.println("marker:");
                System.out.println(marker.getName());
                System.out.println("X: "+marker.getX());
                System.out.println("Y: "+marker.getY());
                System.out.println("Z: "+marker.getZ());
                System.out.println("PosX: "+marker.getPosX());
                System.out.println("PosY: "+marker.getPosY());
                System.out.println();
                System.out.println("rectangle:");
                System.out.println("x: "+x);
                System.out.println("y: "+y);
                System.out.println("size: "+squareSize);
                System.out.println("=============================");*/
                // marker info

                markerList.add(marker);

            }
        }

        public void hightlightIfInterselectWithMouse(int x, int y){
            if(image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize/2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize/2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize/2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize/2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY);
                } else {
                    yRel -= posY;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize/(scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize/(scale * image.getHeight());

                for(Marker m : markerList){
                    if(m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax){
                        //Rectangle s = m.getSquare();
                        originalMapImagePanel.setHighlightedSquare(markerList.indexOf(m));
                        originalMapImagePanel.revalidate();
                        originalMapImagePanel.repaint();
                        break;
                    }
                }

            }
        }

        public void hightlightIfListClicked(int item){
            if(image != null) {
                originalMapImagePanel.setHighlightedSquare(item);
                originalMapImagePanel.revalidate();
                originalMapImagePanel.repaint();
            }
        }

        public void removeSquare(int x, int y){
            if(image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize/2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize/2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize/2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize/2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY);
                } else {
                    yRel -= posY;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize/(scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize/(scale * image.getHeight());

                for(Marker m : markerList){
                    if(m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax){
                        /*System.out.println("mouse: \n"+xMin+" - "+xMax+"\n"+yMin+" - "+yMax);
                        System.out.println("square: \n"+m.getPosX()+" \n "+m.getPosY());*/
                        markerList.remove(m);
                        break;
                    }
                }

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
            square = new Rectangle(squareSize,squareSize);
        }

        public void editSquare(int x, int y) {
            if(image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize/2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize/2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize/2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize/2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY);
                } else {
                    yRel -= posY;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize/(scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize/(scale * image.getHeight());

                for(Marker m : markerList){
                    if(m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax){
                        /*System.out.println("mouse: \n"+xMin+" - "+xMax+"\n"+yMin+" - "+yMax);
                        System.out.println("square: \n"+m.getPosX()+" \n "+m.getPosY());*/
                        //markerList.remove(m);

                        /**
                         * ZDE SE POTOM SPUSTI OBRAZOVKA NA UPRAVU UDAJU x, y A name
                         */
                        EditMarkerScreen editMarkerScreen = new EditMarkerScreen(getMainReference(),"",Dialog.ModalityType.DOCUMENT_MODAL);
                        editMarkerScreen.setMarker(m);
                        editMarkerScreen.isEdit(true);
                        editMarkerScreen.startFrame();

                        if(m.getX() == -1 || m.getY() == -1 || m.getZ() == -1){ // Uzivatel dal "cancel"
                            return;
                        }
                        // marker info
                        /*System.out.println("marker:");
                        System.out.println(m.getName());
                        System.out.println("X: "+m.getX());
                        System.out.println("Y: "+m.getY());
                        System.out.println("Z: "+m.getZ());
                        System.out.println("PosX: "+m.getPosX());
                        System.out.println("PosY: "+m.getPosY());
                        System.out.println();
                        System.out.println("rectangle:");
                        System.out.println("x: "+x);
                        System.out.println("y: "+y);
                        System.out.println("size: "+squareSize);
                        System.out.println("=============================");*/
                        // marker info
                        break;
                    }

                    }
                }

            }

        }


    private class HeightMapImagePanel extends ImagePanel {

    }

    private class NormalMapImagePanel extends ImagePanel {


    }

    private abstract class SettingsBox {
        JPanel settingBox;

        public SettingsBox(){
            settingBox = new JPanel();
        }

        JPanel getPanel() {
            return settingBox;
        }
    }

    private class NormalMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel lightPanel, heightPanel, recalculatePanel, lightToolPanel;
        JButton recalculateButton;
        JSlider height, lightAngle;
        DirectionPanel lightDirectionPanel;

        @Override
        public JPanel getPanel(){
            if(settingBox == null){
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());

                lightPanel = new JPanel();
                lightPanel.setLayout(new BorderLayout());
                lightPanel.add(new JLabel("Lightning:"),BorderLayout.NORTH);
                lightToolPanel = new JPanel();
                lightToolPanel.setLayout(new BorderLayout());
                lightToolPanel.setBackground(new Color(128,127,255));
                //lightToolPanel.setPreferredSize(new Dimension(20,165));
                lightDirectionPanel = new DirectionPanel();
                lightDirectionPanel.setBackground(new Color(128,127,255));
                lightDirectionPanel.setPreferredSize(new Dimension(120,120));
                lightToolPanel.add(lightDirectionPanel,BorderLayout.NORTH);
                lightAngle = new JSlider(JSlider.HORIZONTAL,0,360,0);
                lightAngle.setMajorTickSpacing(90);
                lightAngle.setMinorTickSpacing(45);
                lightAngle.setPaintTicks(true);
                lightAngle.setPaintLabels(true);
                lightToolPanel.add(lightAngle,BorderLayout.SOUTH);

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

        private class DirectionPanel extends JPanel{

            private BufferedImage lightImage;

            @Override
            protected void paintComponent(Graphics g) {
                //super.paint(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.translate(this.getWidth()/2,this.getHeight()/2);
                g2d.rotate(Math.toRadians(lightAngle.getValue()) );
                g2d.translate(-getLightImage().getWidth(this) / 2, -getLightImage().getHeight(this) / 2);

                g2d.drawImage(getLightImage(),0,0,this);

                revalidate();
                repaint();
            }

            private BufferedImage getLightImage(){
                if(lightImage == null) {
                    try {
                        lightImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/lightImage.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return lightImage;
            }

        }

        private ActionListener reviewActionListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == recalculateButton){
                    if(imageLoader != null && image != null) {
                        //System.out.println("refresh");
                        //System.out.println("height: "+height.getValue());
                        imageLoader.refreshNormalMap(lightAngle.getValue(),normalHeight = (((double)height.getValue()*(-99.0))/10000.0+1.0));
                        //updateImagePanel(image.getNormalMap());
                        updateNormal(image.getNormalMap());
                    }
                }
            }
        };
    }

    private class HeightMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel calculateNormalPanel;
        JButton calculateNormalButton, invert;

        @Override
        public JPanel getPanel(){
            if(settingBox == null){
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());


                calculateNormalPanel = new JPanel();
                invert = new JButton("Invert Heights");
                invert.addActionListener(invertActionListener);
                calculateNormalButton = new JButton("Calculate Normals");
                calculateNormalButton.addActionListener(reviewActionListener);
                calculateNormalPanel.add(calculateNormalButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                settingBox.add(invert,BorderLayout.NORTH);
                settingBox.add(calculateNormalPanel, BorderLayout.SOUTH);
            }
            return settingBox;
        }

        private ActionListener reviewActionListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == calculateNormalButton){
                    if(imageLoader != null && image != null && image.getHeightMap() != null) {
                        //System.out.println("refresh");
                        //System.out.println("height: "+height.getValue());
                        imageLoader.refreshNormalMap(angle,normalHeight = ((70.0*(-99.0))/10000.0+1.0));
                        //updateImagePanel(image.getNormalMap());
                        updateNormal(image.getNormalMap());
                    }
                }
            }
        };

        private ActionListener invertActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(actionEvent.getSource() == invert){
                    if(imageLoader != null && image != null && image.getHeightMap() != null){
                        imageLoader.invertHeightMap();
                        updateHeight(image.getHeightMap());
                    }
                }
            }
        };
    }

    private JFrame getFrame(){
        return this;
    }

    private class OriginalMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel recalculatePanel,editPanel,markerSizePanel,listPanel,settingsPanel;
        JButton recalculateButton;
        java.util.List<Marker> markerList;
        JList<Marker> displayMarkerList;
        ButtonGroup buttonGroup;
        AlgorithmSettingsDialog algorithmSettingsDialog;
        JSlider regularSlider,albedoSlider,stepsSlider;
        JLabel warning;
        int algorithmSteps = 20;
        double algorithmAlbedo = 1;
        double algorithmSmoothness = 0.1;

        JToggleButton addMarkerButton, editMarkerButton, removeMarkerButton, activeButton;

        private Dimension editPanelDimension,markerSizeDimension,recalculatePanelDimension;

        JSlider markerSizeSlider;

        @Override
        public JPanel getPanel(){
            if(settingBox == null && this.markerList != null){
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());

                JPanel topPanel = new JPanel(new BorderLayout());

                editPanel = new JPanel(new GridLayout(0,3));
                buttonGroup = new ButtonGroup();
                addMarkerButton = new JToggleButton();
                try {
                    Image img = ImageIO.read(getClass().getResource("/Resources/original_marker_button/add.png"));
                    addMarkerButton.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                editMarkerButton = new JToggleButton();
                try {
                    Image img = ImageIO.read(getClass().getResource("/Resources/original_marker_button/edt.png"));
                    editMarkerButton.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                removeMarkerButton = new JToggleButton();
                try {
                    Image img = ImageIO.read(getClass().getResource("/Resources/original_marker_button/rmv.png"));
                    removeMarkerButton.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                buttonGroup.add(addMarkerButton);
                buttonGroup.add(editMarkerButton);
                buttonGroup.add(removeMarkerButton);

                ActionListener acButton = actionEvent -> {
                    if(actionEvent.getSource() == addMarkerButton){
                        activeButton = addMarkerButton;
                    } else if(actionEvent.getSource() == editMarkerButton){
                        activeButton = editMarkerButton;
                    } else if(actionEvent.getSource() == removeMarkerButton){
                        activeButton = removeMarkerButton;
                    }
                };

                addMarkerButton.addActionListener(acButton);
                editMarkerButton.addActionListener(acButton);
                removeMarkerButton.addActionListener(acButton);

                editPanel.add(addMarkerButton);
                editPanel.add(editMarkerButton);
                editPanel.add(removeMarkerButton);

                topPanel.add(editPanel,BorderLayout.NORTH);

                markerSizePanel = new JPanel(new BorderLayout());
                JLabel markerSizeLabel = new JLabel("  Markers size:");
                markerSizeSlider = new JSlider(JSlider.HORIZONTAL,2,150,50);
                markerSizeSlider.addChangeListener(changeEvent -> {
                    originalMapImagePanel.setSquareSize(markerSizeSlider.getValue());
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                });
                markerSizePanel.add(markerSizeLabel,BorderLayout.NORTH);
                markerSizePanel.add(markerSizeSlider,BorderLayout.CENTER);

                topPanel.add(markerSizePanel,BorderLayout.SOUTH);

                settingBox.add(topPanel,BorderLayout.NORTH);

                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                listPanel = new JPanel(new BorderLayout());
                displayMarkerList = new JList<>();
                displayMarkerList.addListSelectionListener(listSelectionEvent ->
                        originalMapImagePanel.hightlightIfListClicked(displayMarkerList.getSelectedIndex()));
                displayMarkerList.setListData(markerList.toArray(new Marker[markerList.size()]));
                scrollPane.setViewportView(displayMarkerList);
                listPanel.add(scrollPane,BorderLayout.CENTER);

                settingBox.add(listPanel,BorderLayout.CENTER);

                JPanel bottomPanel = new JPanel(new BorderLayout());

                settingsPanel = new JPanel();
                settingsPanel.setLayout(new BorderLayout());
                JPanel up = new JPanel(new GridLayout(1,2));
                JPanel upLeft = new JPanel(new BorderLayout());
                upLeft.add(new JLabel("Albedo:"),BorderLayout.NORTH);
                albedoSlider = new JSlider(JSlider.VERTICAL,0,100,100);
                albedoSlider.setMajorTickSpacing(25);
                albedoSlider.setMinorTickSpacing(10);
                albedoSlider.setPaintTicks(true);
                albedoSlider.setPaintLabels(true);
                upLeft.add(albedoSlider,BorderLayout.CENTER);

                JPanel upRight = new JPanel(new BorderLayout());
                upRight.add(new JLabel("Smoothness:"),BorderLayout.NORTH);
                regularSlider = new JSlider(JSlider.VERTICAL,0,100,10);
                regularSlider.setMajorTickSpacing(25);
                regularSlider.setMinorTickSpacing(10);
                regularSlider.setPaintTicks(true);
                regularSlider.setPaintLabels(true);
                upRight.add(regularSlider,BorderLayout.CENTER);

                up.add(upLeft);
                up.add(upRight);


                JPanel down = new JPanel(new BorderLayout());
                down.add(new JLabel("Calculation steps"),BorderLayout.NORTH);
                stepsSlider = new JSlider(JSlider.HORIZONTAL,0,100,20);
                stepsSlider.setMajorTickSpacing(25);
                stepsSlider.setMinorTickSpacing(10);
                stepsSlider.setPaintTicks(true);
                stepsSlider.setPaintLabels(true);

                down.add(stepsSlider,BorderLayout.CENTER);
                warning = new JLabel("    Warning: long calculation time");
                warning.setForeground(Color.RED);
                warning.setVisible(false);
                down.add(warning,BorderLayout.PAGE_END);
                settingsPanel.add(up,BorderLayout.NORTH);
                settingsPanel.add(down,BorderLayout.CENTER);
                stepsSlider.addChangeListener(changeEvent -> {
                    if(stepsSlider.getValue() > 50){
                        warning.setVisible(true);
                    } else {
                        warning.setVisible(false);
                    }
                });
                //settingBox.add(settingsPanel,BorderLayout.SOUTH);

                recalculatePanel = new JPanel();
                recalculateButton = new JButton(new WaitAction("Calculate Depth"));
                recalculatePanel.add(recalculateButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                //settingBox.add(calculateNormalPanel, BorderLayout.PAGE_END);
                bottomPanel.add(recalculatePanel, BorderLayout.PAGE_END);
                bottomPanel.add(settingsPanel,BorderLayout.NORTH);
                settingBox.add(bottomPanel,BorderLayout.PAGE_END);

            }
            return settingBox;
        }

        public void updateList(){
            displayMarkerList.setListData(markerList.toArray(new Marker [markerList.size()]));
            displayMarkerList.revalidate();
            displayMarkerList.repaint();
        }

        public void setMarkerList(java.util.List<Marker> markerList){
            this.markerList = markerList;
        }

        public java.util.List<Marker> getMarkerList(){
            return markerList;
        }

        private class WaitAction extends AbstractAction {

            public WaitAction(String name) {
                super(name);
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                if(markerList.size() < 3){
                    JOptionPane.showMessageDialog(getFrame(), "You must provide at least 3 markers.");
                } else {
                    SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
                        @Override
                        protected Void doInBackground() throws Exception {

                            imageLoader.calculateHeightMap(markerList, stepsSlider.getValue(), ((double)(albedoSlider.getValue()))/100.0, ((double)(regularSlider.getValue()))/100.0);
                            updateHeight(image.getHeightMap());

                            return null;
                        }
                    };

                    Window win = SwingUtilities.getWindowAncestor((AbstractButton)evt.getSource());
                    final JDialog dialog = new JDialog(win, "Calculating Depthmap" , Dialog.ModalityType.APPLICATION_MODAL);

                    mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (evt.getPropertyName().equals("state")) {
                                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                                    dialog.dispose();
                                }
                            }
                        }
                    });
                    mySwingWorker.execute();

                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    progressBar.setPreferredSize(new Dimension(250,20));
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(progressBar, BorderLayout.CENTER);
                    dialog.add(panel);
                    dialog.pack();
                    dialog.setLocationRelativeTo(win);
                    dialog.setVisible(true);
                }

            }
        }


    }


}

