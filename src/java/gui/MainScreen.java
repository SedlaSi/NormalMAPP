package gui;

import gui.session.ImageLoader;
import gui.session.Session;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.tools.Tool;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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


    OriginalMapSettingsBox originalMapSettingsBox;
    OriginalMapToolBox originalMapToolBox;
    NormalMapSettingsBox normalMapSettingsBox;
    NormalMapToolBox normalMapToolBox;
    HeightMapSettingsBox heightMapSettingsBox;
    HeightMapToolBox heightMapToolBox;

    OriginalMapImagePanel originalMapImagePanel;
    HeightMapImagePanel heightMapImagePanel;
    NormalMapImagePanel normalMapImagePanel;


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


        originalMapImagePanel = new OriginalMapImagePanel();

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
                //imagePanel.setMouseInit(mouseEvent.getX(),mouseEvent.getY());
            }
        });

        originalMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                originalMapImagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                revalidate();
                repaint();
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
        originalMapToolBox = new OriginalMapToolBox();
        normalMapSettingsBox = new NormalMapSettingsBox();
        normalMapToolBox = new NormalMapToolBox();
        heightMapSettingsBox = new HeightMapSettingsBox();
        heightMapToolBox = new HeightMapToolBox();

        leftBoxPanel = new JPanel();
        leftBoxPanel.setLayout(new BorderLayout());
        leftBoxPanel.add(originalMapSettingsBox.getPanel(),BorderLayout.SOUTH);

        tabbedPanel.addChangeListener(changeEvent -> {
            if(tabbedPanel.getSelectedComponent() == originalMapImagePanel){
                leftBoxPanel.remove(0);
                leftBoxPanel.add(originalMapSettingsBox.getPanel(),BorderLayout.SOUTH);
            } else if(tabbedPanel.getSelectedComponent() == heightMapImagePanel){
                leftBoxPanel.remove(0);
                leftBoxPanel.add(heightMapSettingsBox.getPanel(),BorderLayout.SOUTH);
            } else if(tabbedPanel.getSelectedComponent() == normalMapImagePanel) {
                leftBoxPanel.remove(0);
                leftBoxPanel.add(normalMapSettingsBox.getPanel(),BorderLayout.SOUTH);
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

    private class HeightMapSettingsBox extends SettingsBox {

    }

    private class HeightMapToolBox {

    }

    private class OriginalMapSettingsBox extends SettingsBox {

    }

    private class OriginalMapToolBox {

    }

}

