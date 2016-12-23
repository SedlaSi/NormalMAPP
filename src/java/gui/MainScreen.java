package gui;

import gui.sfs.AlgorithmSettingsDialog;
import gui.sfs.EditMarkerScreen;
import gui.sfs.Marker;
import gui.session.ImageLoader;
import gui.session.Session;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 14.7.16.
 */
public class MainScreen extends JFrame {

    JMenuBar menuBar;
    JMenu file, help, save, load, edit, filters, view;
    JMenuItem exit, openTexture, saveAll, loadAll, saveHeighMap, saveNormalMap, loadHeightMap, invertNormal;
    JMenuItem undo, redo, sharpen, smooth;
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
    OriginalMapToolBox originalMapToolBox;
    NormalMapSettingsBox normalMapSettingsBox;
    NormalMapToolBox normalMapToolBox;
    HeightMapSettingsBox heightMapSettingsBox;
    HeightMapToolBox heightMapToolBox;

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
                int x = (int)(mouseEvent.getPoint().getX()-mouseStartX);
                int y = (int)(mouseEvent.getPoint().getY()-mouseStartY);
                if(updateAllImages){
                    heightMapImagePanel.moveImg(x,y);
                    normalMapImagePanel.moveImg(x,y);
                }
                originalMapImagePanel.moveImg(x,y);
                revalidate();
                repaint();
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
        originalMapToolBox = new OriginalMapToolBox();
        normalMapSettingsBox = new NormalMapSettingsBox();
        normalMapToolBox = new NormalMapToolBox();
        heightMapSettingsBox = new HeightMapSettingsBox();
        heightMapToolBox = new HeightMapToolBox();

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
        image = imageLoader.testloadImage();
        if(image != null){
            updateImagePanels(); // uvodni obrazek po nacteni
            //imagePanel.setActiveLayer(Layer.normalMap);
        }
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
        Rectangle r1 = new Rectangle(352,335,20,20);
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
        markerList.add(m3);


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
            } else if(e.getSource() == invertNormal){
                angle = (Math.PI/2);
                imageLoader.refreshNormalMap(angle,normalHeight);
                updateImagePanels();
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
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize / 2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize / 2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize / 2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize / 2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY);
                } else {
                    yRel -= posY;
                }
                yRel /= (scale * image.getHeight());
                marker.setPosX(xRel);
                marker.setPosY(yRel);

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
                System.out.println("marker:");
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
                System.out.println("=============================");
                // marker info

                markerList.add(marker);

            }
        }

        public void hightlightIfInterselectWithMouse(int x, int y){
            if(image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize);
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

        public void removeSquare(int x, int y){
            if(image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize);
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
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX);
                } else {
                    xRel -= posX;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize);
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
                        System.out.println("marker:");
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
                        System.out.println("=============================");
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

        @Override
        public JPanel getPanel(){
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
                        //updateImagePanel(image.getNormalMap());
                        updateNormal(image.getNormalMap());
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
                g.drawImage(image, 0, 0, null);
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

    private class HeightMapSettingsBox extends SettingsBox {

    }

    private class HeightMapToolBox {

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
                addMarkerButton = new JToggleButton("ADD");
                editMarkerButton = new JToggleButton("EDT");
                removeMarkerButton = new JToggleButton("RMV");
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

                settingsPanel.add(up,BorderLayout.NORTH);
                settingsPanel.add(down,BorderLayout.CENTER);

                //settingBox.add(settingsPanel,BorderLayout.SOUTH);

                recalculatePanel = new JPanel();
                recalculateButton = new JButton("Calculate Depth");
                recalculateButton.addActionListener(reviewActionListener);
                recalculatePanel.add(recalculateButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                //settingBox.add(recalculatePanel, BorderLayout.PAGE_END);
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

        private ActionListener reviewActionListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == recalculateButton){
                    if(!markerList.isEmpty() && markerList.size() >= 3){

                        /*algorithmSettingsDialog = new AlgorithmSettingsDialog(getMainReference(),"", Dialog.ModalityType.DOCUMENT_MODAL);
                        algorithmSettingsDialog.startFrame();*/
                        /*System.out.println(algorithmSettingsDialog.steps);
                        System.out.println(algorithmSettingsDialog.q);
                        System.out.println(algorithmSettingsDialog.lm);*/

                        imageLoader.calculateHeightMap(markerList, stepsSlider.getValue(), ((double)(albedoSlider.getValue()))/100.0, ((double)(regularSlider.getValue()))/100.0);
                        updateHeight(image.getHeightMap());

                    }
                }
            }
        };
    }



    private class OriginalMapToolBox {

    }

}

