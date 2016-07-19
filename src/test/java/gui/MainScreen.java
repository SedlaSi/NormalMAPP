package gui;

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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by root on 14.7.16.
 */
public class MainScreen extends JFrame {

    JMenuBar menuBar;
    JMenu file, help, save, load, edit, filters, view;
    JMenuItem exit, openTexture, saveAll, loadAll, saveHeighMap, saveNormalMap, loadHeightMap, originalImage, heightMap, normalMap;
    JMenuItem undo, redo, sharpen, smooth;
    ImageLoader imageLoader;
    image.Image image;
    ImagePanel imagePanel;
    Session session;

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
        ThisActionListener actionListener = new ThisActionListener();
        this.setPreferredSize(new Dimension(500,500));
        setLocationRelativeTo(null);
        //setResizable(false);
        setTitle("NormalMAPP");

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

        saveAll = new JMenuItem("Save All");
        saveAll.addActionListener(actionListener);
        file.add(saveAll);

        load = new JMenu("Load");
        load.addMenuListener(menuListener);
        file.add(load);

        loadAll = new JMenuItem("Load All");
        loadAll.addActionListener(actionListener);
        file.add(loadAll);

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

        this.add(imagePanel);
        this.setJMenuBar(menuBar);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(session != null){
                    session.endSession();
                }
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
                System.out.println("opening");
                image = imageLoader.loadImage();
                updateImagePanel(image.getOriginalMap());
            } else if(e.getSource() == originalImage){
                updateImagePanel(image.getOriginalMap());
            } else if(e.getSource() == heightMap){
                updateImagePanel(image.getHeightMap());
            } else if(e.getSource() == normalMap){
                updateImagePanel(image.getNormalMap());
            } else if(e.getSource() == saveNormalMap){
                imageLoader.saveNormalMap();
            } else if(e.getSource() == saveHeighMap){
                imageLoader.saveHeightMap();
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


}

