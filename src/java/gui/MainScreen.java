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


    private int xDirection, yDirection;
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

        xDirection = 1;
        yDirection = 1;

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
                if(image != null){
                    updateImagePanel(image.getOriginalMap());
                }
            } else if(e.getSource() == originalImage){
                if(image != null && image.getOriginalMap() != null){
                    updateImagePanel(image.getOriginalMap());
                }
            } else if(e.getSource() == heightMap){
                if(image != null){
                    updateImagePanel(image.getHeightMap());
                }
            } else if(e.getSource() == normalMap){
                if(image != null){
                    updateImagePanel(image.getNormalMap());
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
                xDirection = -1*xDirection;
                yDirection = -1*yDirection;
                imageLoader.refreshNormalMap(xDirection,yDirection,normalHeight);
                updateImagePanel(image.getNormalMap());
            } else if(e.getSource() == loadHeightMap){
                image = imageLoader.loadHeightMap();
            }
        }
    }

    private class ImagePanel extends JPanel {
        double scale;
        BufferedImage image;

        public ImagePanel() {
            scale = 1.0;
            setBackground(Color.gray);
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
                AffineTransform at = AffineTransform.getTranslateInstance(x, y);
                at.scale(scale, scale);
                g2.drawRenderedImage(image, at);
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

        public void setScale(double s) {
            scale = s;
            revalidate();      // update the scroll pane
            repaint();
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
                    xDirection = 1;
                    yDirection = 1;
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
                    xDirection = 1;
                    yDirection = -1;
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
                    xDirection = -1;
                    yDirection = 1;
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
                    xDirection = -1;
                    yDirection = -1;
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
                        imageLoader.refreshNormalMap(xDirection, yDirection,normalHeight = (((double)height.getValue()*(-99.0))/10000.0+1.0));
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

