package gui;

import algorithms.ShapeFromShading;
import gui.sfs.EditMarkerScreen;
import gui.sfs.Marker;
import gui.session.ImageLoader;
import gui.session.Session;
import help.Tutorial;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sedlasi1 on 14.7.16.
 *
 * MainScreen class contains all main variables of GUI for NormalMAPP.
 * Class is initialized in main.NormalMAPP class.
 *
 */
public class MainScreen extends JFrame {

    JMenuBar menuBar;
    JMenu file, help, save, load;
    JMenuItem exit, openTexture, saveHeighMap, saveNormalMap, loadHeightMap, tutorial, about, updates;
    ImageLoader imageLoader;
    image.Image image;
    JPanel mainPanel, leftBoxPanel;
    Session session;
    Tutorial tutorialDialog;
    EditMarkerScreen editMarkerScreen;
    JLabel statusLabel;

    // synchronize Original Image, Height Map and Normal Map position and zoom
    private final boolean updateAllImages = true;

    private boolean mouseIsDragged = false ;
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

    private double angle;

    /**
     * @method main for testing purposes
     * @param args
     */
    public static void main(String[] args) {
        MainScreen mainScreen = new MainScreen(null, null);
        mainScreen.createFrame();
    }

    public MainScreen(Session session, ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        this.session = session;
    }

    /**
     * @method createFrame() initialize the main JFrame and fill it with all the components.
     */
    public void createFrame() {
        ThisMenuListener menuListener = new ThisMenuListener();
        actionListener = new ThisActionListener();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setPreferredSize(new Dimension(800, 600));
        this.pack();

        ImageIcon icon = new ImageIcon((MainScreen.class.getResource("/logo.png")));

        this.getFrame().setIconImage(icon.getImage());
        setLocationRelativeTo(null);
        setTitle("NormalMAPP");

        angle = 0;

        menuBar = new JMenuBar();

        file = new JMenu("File");
        file.addMenuListener(menuListener);
        menuBar.add(file);

        help = new JMenu("Help");
        tutorial = new JMenuItem("Tutorial");
        tutorialDialog = new Tutorial();
        tutorial.addActionListener(actionEvent -> tutorialDialog.start());
        about = new JMenuItem("About");
        JPanel aboutP = new JPanel(new GridLayout(4,1));
        aboutP.add(new JLabel("NormalMAPP is free software for calculating"));
        aboutP.add(new JLabel("height maps and normal maps from single image."));
        aboutP.add(new JLabel("You can download NormalMAPP from following adress:"));
        aboutP.add(new JTextArea("https://github.com/SedlaSi/NormalMAPP_repo/blob/master/build/NormalMAPP.zip"));
        about.addActionListener(actionEvent -> JOptionPane.showMessageDialog(getMainReference(), aboutP));

        updates = new JMenuItem("Updates");
        JPanel updatesP = new JPanel(new GridLayout(2,1));
        updatesP.add(new JLabel("Download newest version on our websites:"));
        updatesP.add(new JTextArea("https://github.com/SedlaSi/NormalMAPP_repo/blob/master/build/NormalMAPP.zip"));
        updates.addActionListener(actionEvent -> JOptionPane.showMessageDialog(getMainReference(), updatesP));

        help.add(about);
        help.add(updates);
        help.add(tutorial);
        menuBar.add(help);



        openTexture = new JMenuItem("Open Texture");
        openTexture.addActionListener(actionListener);
        file.add(openTexture);

        save = new JMenu("Save");
        save.addMenuListener(menuListener);
        file.add(save);

        load = new JMenu("Load");
        load.addMenuListener(menuListener);
        file.add(load);

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

        tabbedPanel = new JTabbedPane();

        markerList = new ArrayList<>(3);

        originalMapImagePanel = new OriginalMapImagePanel();
        originalMapImagePanel.setMarkerList(markerList);

        originalMapImagePanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() > 0) {
                if (updateAllImages) {
                    heightMapImagePanel.decreaseScale();
                    normalMapImagePanel.decreaseScale();
                }
                originalMapImagePanel.decreaseScale();
            } else {
                if (updateAllImages) {
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
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);
                if (updateAllImages) {
                    heightMapImagePanel.moveImg(x, y);
                    normalMapImagePanel.moveImg(x, y);
                }
                originalMapImagePanel.moveImg(x, y);
                originalMapImagePanel.revalidate();
                originalMapImagePanel.repaint();

            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                originalMapImagePanel.hightlightIfInterselectWithMouse(mouseEvent.getX(), mouseEvent.getY());

            }
        });

        originalMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (originalMapSettingsBox.activeButton == originalMapSettingsBox.addMarkerButton) {
                    originalMapImagePanel.addSquare(mouseEvent.getX(), mouseEvent.getY());
                    originalMapSettingsBox.updateList();
                    if (markerList.size() == 3) { //we add a marker and there are just 3 of them -> recalculate light vector
                        if(image != null && image.getOriginalMap() != null ){
                            statusLabel.setText(ShapeFromShading.getLightDirection(markerList, image.getOriginalMap().getWidth(), image.getOriginalMap().getHeight()));
                        }
                    }
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                } else if (originalMapSettingsBox.activeButton == originalMapSettingsBox.editMarkerButton) {
                    originalMapImagePanel.editSquare(mouseEvent.getX(), mouseEvent.getY());
                    if (originalMapImagePanel.editedSquare() < 3) {
                        statusLabel.setText(ShapeFromShading.getLightDirection(markerList, image.getOriginalMap().getWidth(), image.getOriginalMap().getHeight()));
                    }
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                } else if (originalMapSettingsBox.activeButton == originalMapSettingsBox.removeMarkerButton) {
                    originalMapImagePanel.removeSquare(mouseEvent.getX(), mouseEvent.getY());
                    if (markerList.size() < 3) {
                        statusLabel.setText("Light vector = UNKNOWN");
                    } else if (originalMapImagePanel.editedSquare() < 3) {
                        statusLabel.setText(ShapeFromShading.getLightDirection(markerList, image.getOriginalMap().getWidth(), image.getOriginalMap().getHeight()));
                    }
                    originalMapSettingsBox.updateList();
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getX();
                mouseStartY = mouseEvent.getY();

                mouseIsDragged = true;

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);

                originalMapImagePanel.mousePosition(x, y);
                originalMapImagePanel.moveImg(0, 0);
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
            if (e.getWheelRotation() > 0) {
                if (updateAllImages) {
                    originalMapImagePanel.decreaseScale();
                    normalMapImagePanel.decreaseScale();
                }
                heightMapImagePanel.decreaseScale();
            } else {
                if (updateAllImages) {
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
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);
                if (updateAllImages) {
                    originalMapImagePanel.moveImg(x, y);
                    normalMapImagePanel.moveImg(x, y);
                }
                heightMapImagePanel.moveImg(x, y);

                heightMapImagePanel.revalidate();
                heightMapImagePanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
            }
        });

        heightMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getX();
                mouseStartY = mouseEvent.getY();

                mouseIsDragged = true;

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);

                heightMapImagePanel.mousePosition(x, y);
                heightMapImagePanel.moveImg(0, 0);
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
            if (e.getWheelRotation() > 0) {
                if (updateAllImages) {
                    originalMapImagePanel.decreaseScale();
                    heightMapImagePanel.decreaseScale();
                }
                normalMapImagePanel.decreaseScale();
            } else {
                if (updateAllImages) {
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
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);
                if (updateAllImages) {
                    originalMapImagePanel.moveImg(x, y);
                    heightMapImagePanel.moveImg(x, y);
                }
                normalMapImagePanel.moveImg(x, y);
                normalMapImagePanel.revalidate();
                normalMapImagePanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {

            }
        });

        normalMapImagePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseStartX = mouseEvent.getX();
                mouseStartY = mouseEvent.getY();

                mouseIsDragged = true;
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                int x = (int) (mouseEvent.getX() - mouseStartX);
                int y = (int) (mouseEvent.getY() - mouseStartY);

                normalMapImagePanel.mousePosition(x, y);
                normalMapImagePanel.moveImg(0, 0);
                mouseIsDragged = false;
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        tabbedPanel.add(originalMapImagePanel, "Original Image");
        tabbedPanel.add(heightMapImagePanel, "Height Map");
        tabbedPanel.add(normalMapImagePanel, "Normal Map");


        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel fillerImagePanel = new JPanel(new BorderLayout());
        fillerImagePanel.add(tabbedPanel, BorderLayout.CENTER);
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        statusPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), 20));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("Light vector = UNKNOWN");

        statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusPanel.add(statusLabel);

        fillerImagePanel.add(statusPanel, BorderLayout.SOUTH);
        mainPanel.add(fillerImagePanel, BorderLayout.CENTER);

        originalMapSettingsBox = new OriginalMapSettingsBox();
        originalMapSettingsBox.setMarkerList(markerList);
        normalMapSettingsBox = new NormalMapSettingsBox();
        heightMapSettingsBox = new HeightMapSettingsBox();

        leftBoxPanel = new JPanel(new BorderLayout());

        cardSettingsBoxLayout = new CardLayout();
        cardSettingsBoxPanel = new JPanel(cardSettingsBoxLayout);
        leftBoxPanel.add(cardSettingsBoxPanel, BorderLayout.CENTER);
        cardSettingsBoxPanel.add(originalMapSettingsBox.getPanel(), "os");
        cardSettingsBoxPanel.add(heightMapSettingsBox.getPanel(), "hs");
        cardSettingsBoxPanel.add(normalMapSettingsBox.getPanel(), "ns");

        tabbedPanel.addChangeListener(changeEvent -> {
            if (tabbedPanel.getSelectedComponent() == originalMapImagePanel) {
                cardSettingsBoxLayout.show(cardSettingsBoxPanel, "os");

            } else if (tabbedPanel.getSelectedComponent() == heightMapImagePanel) {
                cardSettingsBoxLayout.show(cardSettingsBoxPanel, "hs");

            } else if (tabbedPanel.getSelectedComponent() == normalMapImagePanel) {
                cardSettingsBoxLayout.show(cardSettingsBoxPanel, "ns");

            }
            revalidate();
            repaint();
        });


        mainPanel.add(leftBoxPanel, BorderLayout.WEST);

        this.add(mainPanel);
        this.setJMenuBar(menuBar);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (session != null) {
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

    /**
     * @method updateOriginal() update input image and refreshes ImagePanel
     * @param original
     */
    private void updateOriginal(BufferedImage original) {
        originalMapImagePanel.setBufferedImage(original);
        revalidate();
        repaint();
    }

    /**
     * @method updateHeight() update height map image and refreshes ImagePanel
     * @param height
     */
    private void updateHeight(BufferedImage height) {
        heightMapImagePanel.setBufferedImage(height);
        revalidate();
        repaint();
    }

    /**
     * @method updateNormal() update normal map image and refreshes ImagePanel
     * @param normal
     */
    private void updateNormal(BufferedImage normal) {
        if (image.getHeightMap() != null) {
            normalMapImagePanel.setBufferedImage(normal);
            revalidate();
            repaint();
        }
    }

    /**
     * @method updateImagePanels() update all three images with methods above
     */
    private void updateImagePanels() {
        if (image.getOriginalMap() != null) {
            updateOriginal(image.getOriginalMap());
        }
        if (image.getNormalMap() != null) {
            updateNormal(image.getNormalMap());
        }
        if (image.getHeightMap() != null) {
            updateHeight(image.getHeightMap());
        }
    }

    private JFrame getMainReference() {
        return this;
    }


    /**
     * ThisMenuListener is MenuListener class connected to all
     * menu buttons in the application GUI. Not currently in use.
     */
    private class ThisMenuListener implements MenuListener {

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

    /**
     * ThisActionListener class is ActionListener class
     * which receives Events from menu buttons:
     *      Open Texture
     *      Save Normal Map
     *      Save Height Map
     *      Load Height Map
     *      Exit
     *
     */
    private class ThisActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == openTexture) {

                image = imageLoader.loadImage();
                if (image != null) {
                    updateImagePanels(); // uvodni obrazek po nacteni

                }
            } else if (e.getSource() == saveNormalMap) {
                if(image != null && image.getNormalMap() != null){
                    imageLoader.saveNormalMap();
                } else {
                    JOptionPane.showMessageDialog(getFrame(), "No normal map to be saved.");
                }
            } else if (e.getSource() == saveHeighMap) {
                if(image != null && image.getHeightMap() != null) {
                    imageLoader.saveHeightMap();
                } else {
                    JOptionPane.showMessageDialog(getFrame(), "No height map to be saved.");
                }
            } else if (e.getSource() == exit) {
                if (session != null) {
                    session.endSession();
                }
                dispose();
                System.exit(0);
            } else if (e.getSource() == loadHeightMap) {  // uvodni obrazek po nacteni
                image = imageLoader.loadHeightMap();

                if (image != null) {
                    updateImagePanels();
                    if (image.getHeightMap() != null) {
                        tabbedPanel.setSelectedComponent(heightMapImagePanel);
                        cardSettingsBoxLayout.show(cardSettingsBoxPanel, "hs");
                    }
                }
            }
        }
    }

    /**
     * OriginalMapImagePanel upgrade ImagePanel with ability of adding markers to the image.
     * See addSquare() method for adding Marker. See ImagePanel class for further information.
     *
     */
    private class OriginalMapImagePanel extends ImagePanel {

        public OriginalMapImagePanel() {
            super();
        }

        private int markerNumber = 0;

        /**
         * @method addSquare() is used for creating Marker at the x, y position.
         * X,Y values represents mouse position on the ImagePanel.
         *
         * @param x
         * @param y
         */
        public void addSquare(int x, int y) {
            if (image != null) {

                Marker marker = new Marker(markerNumber + "# Marker");

                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x /*- scale * squareSize / 2*/);
                } else {
                    xRel = (x - scale * imgPosX/* - scale * squareSize / 2*/);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX + mouseX * scale);
                } else {
                    xRel -= (posX + mouseX * scale);
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y /*- scale * squareSize / 2*/);
                } else {
                    yRel = (y - scale * imgPosY /*- scale * squareSize / 2*/);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY + mouseY * scale);
                } else {
                    yRel -= (posY + mouseY * scale);
                }
                yRel /= (scale * image.getHeight());
                marker.setPosX(xRel);
                marker.setPosY(yRel);

                if (xRel < 0.0 || yRel < 0.0 || xRel > 1.0 || yRel > 1.0) {
                    return;
                }
                markerList.add(marker);
                originalMapImagePanel.revalidate();
                originalMapImagePanel.repaint();
                editMarkerScreen = new EditMarkerScreen(getMainReference(), "", Dialog.ModalityType.DOCUMENT_MODAL);
                editMarkerScreen.setMarker(marker);

                // starts editMarkerScreen frame
                editMarkerScreen.startFrame();

                if (marker.getX() == -1 || marker.getY() == -1 || marker.getZ() == -1) { // Uzivatel dal "cancel"
                    markerList.remove(marker);
                    return;
                }
                markerNumber++;
            }
        }

        /**
         * @method hightlightIfInterselectWithMouse() will mark Marker as highlighted.
         * Highlighted marker will fill with green collor when ImagePanel will be repainted.
         * X,Y values represents mouse position on the ImagePanel.
         *
         * @param x
         * @param y
         */
        public void hightlightIfInterselectWithMouse(int x, int y) {
            if (image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize / 2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize / 2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX + mouseX * scale);
                } else {
                    xRel -= posX + mouseX * scale;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize / 2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize / 2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY + mouseY * scale);
                } else {
                    yRel -= posY + mouseY * scale;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize / (scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize / (scale * image.getHeight());

                for (Marker m : markerList) {
                    if (m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax) {
                        originalMapImagePanel.setHighlightedSquare(markerList.indexOf(m));
                        originalMapImagePanel.revalidate();
                        originalMapImagePanel.repaint();
                        break;
                    }
                }

            }
        }


        /**
         * @method hightlightIfListClicked() will mark Marker as highlighted.
         * Highlighted marker will fill with green collor when ImagePanel will be repainted.
         *
         * @param item is index in list of markers
         */
        public void hightlightIfListClicked(int item) {
            if (image != null) {
                originalMapImagePanel.setHighlightedSquare(item);
                originalMapImagePanel.revalidate();
                originalMapImagePanel.repaint();
            }
        }

        /**
         * @method removeSquare() remove marker at position set in the input values.
         * X,Y values represents mouse position on the ImagePanel.
         *
         * @param x
         * @param y
         */
        public void removeSquare(int x, int y) {
            if (image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize / 2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize / 2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX + mouseX * scale);
                } else {
                    xRel -= posX + mouseX * scale;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize / 2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize / 2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY + mouseY * scale);
                } else {
                    yRel -= posY + mouseY * scale;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize / (scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize / (scale * image.getHeight());

                for (Marker m : markerList) {
                    if (m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax) {
                        eddited = markerList.indexOf(m);
                        markerList.remove(m);
                        break;
                    }
                }

            }
        }

        /**
         * @method setSquareSize() change size of all markers,
         * size of markers is applied when ImagePanel is repainted.
         *
         * @param size
         */
        void setSquareSize(int size) {
            squareSize = size;
            square = new Rectangle(squareSize, squareSize);
        }

        private int eddited = 0;

        /**
         * @return index of square which was edited as the last one.
         */
        int editedSquare() {
            return eddited;
        }

        /**
         * @method editSquare() starts editMarkerScreen frame with selected Marker
         * X,Y values represents mouse position on the ImagePanel.
         *
         * @param x
         * @param y
         */
        void editSquare(int x, int y) {
            if (image != null) {
                double xRel;
                double yRel;
                if (imgPosX < 0) {
                    xRel = (Math.abs(scale * imgPosX) + x - scale * squareSize / 2);
                } else {
                    xRel = (x - scale * imgPosX - scale * squareSize / 2);
                }
                if (posX < 0) {
                    xRel += Math.abs(posX + mouseX * scale);
                } else {
                    xRel -= posX + mouseX * scale;
                }
                xRel /= (scale * image.getWidth());
                if (imgPosY < 0) {
                    yRel = (Math.abs(scale * imgPosY) + y - scale * squareSize / 2);
                } else {
                    yRel = (y - scale * imgPosY - scale * squareSize / 2);
                }
                if (posY < 0) {
                    yRel += Math.abs(posY + mouseY * scale);
                } else {
                    yRel -= posY + mouseY * scale;
                }
                yRel /= (scale * image.getHeight());

                double xMin = xRel;
                double xMax = xRel + scale * squareSize / (scale * image.getWidth());
                double yMin = yRel;
                double yMax = yRel + scale * squareSize / (scale * image.getHeight());

                for (Marker m : markerList) {
                    if (m.getPosX() >= xMin && m.getPosX() <= xMax && m.getPosY() >= yMin && m.getPosY() <= yMax) {

                        eddited = markerList.indexOf(m);
                        /**
                         * ZDE SE POTOM SPUSTI OBRAZOVKA NA UPRAVU UDAJU x, y A name
                         */
                        EditMarkerScreen editMarkerScreen = new EditMarkerScreen(getMainReference(), "", Dialog.ModalityType.DOCUMENT_MODAL);
                        editMarkerScreen.setMarker(m);
                        editMarkerScreen.isEdit(true);
                        editMarkerScreen.startFrame();

                        if (m.getX() == -1 || m.getY() == -1 || m.getZ() == -1) { // Uzivatel dal "cancel"
                            return;
                        }
                        break;
                    }

                }
            }

        }

    }


    /**
     * HeightMapImagePanel class used when Height Map tab is active.
     * No further specialization so far.
     */
    private class HeightMapImagePanel extends ImagePanel {

    }

    /**
     * NormalMapImagePanel class used when Normal Map tab is active.
     * No further specialization so far.
     */
    private class NormalMapImagePanel extends ImagePanel {


    }

    /**
     * SettingsBox abstract class is used as a provider
     * for JPanel used in left side of the application -> toolbox.
     */
    private abstract class SettingsBox {
        JPanel settingBox;

        public SettingsBox() {
            settingBox = new JPanel();
        }

        /**
         *
         * @return JPanel of setting box.
         */
        JPanel getPanel() {
            return settingBox;
        }
    }

    /**
     * NormalMapSettingsBox is used when Normal Map tab is active.
     * Contains all GUI parts for editing normal maps.
     */
    private class NormalMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel lightPanel, heightPanel, recalculatePanel, lightToolPanel;
        JButton recalculateButton;
        JSlider height, lightAngle;
        DirectionPanel lightDirectionPanel;

        @Override
        public JPanel getPanel() {
            if (settingBox == null) {
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());

                lightPanel = new JPanel();
                lightPanel.setLayout(new BorderLayout());
                JLabel normRot = new JLabel("   Normals rotation:");
                normRot.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                lightPanel.add(normRot, BorderLayout.NORTH);
                lightPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
                lightToolPanel = new JPanel();
                lightToolPanel.setLayout(new BorderLayout());
                lightToolPanel.setBackground(new Color(128, 127, 255));
                lightDirectionPanel = new DirectionPanel();
                lightDirectionPanel.setBackground(new Color(128, 127, 255));
                lightDirectionPanel.setPreferredSize(new Dimension(120, 120));
                lightDirectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 50, 0));

                lightToolPanel.add(lightDirectionPanel, BorderLayout.NORTH);
                lightAngle = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
                lightAngle.setMajorTickSpacing(90);
                lightAngle.setMinorTickSpacing(45);
                lightAngle.setPaintTicks(true);
                lightAngle.setPaintLabels(true);
                lightAngle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                lightToolPanel.add(lightAngle, BorderLayout.SOUTH);

                lightPanel.add(lightToolPanel, BorderLayout.SOUTH);
                heightPanel = new JPanel();
                JLabel heightLabel = new JLabel("Height:");
                height = new JSlider(JSlider.HORIZONTAL, 0, 100, 91);
                height.setPreferredSize(new Dimension(120, 50));
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
                settingBox.add(lightPanel, BorderLayout.NORTH);
                settingBox.add(heightPanel, BorderLayout.CENTER);
                settingBox.add(recalculatePanel, BorderLayout.SOUTH);


            }
            return settingBox;
        }

        /**
         * DirectionPanel class is JPanel containing rotating image of normal map of ball.
         */
        private class DirectionPanel extends JPanel {

            private BufferedImage lightImage;

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(128, 127, 255));
                g2d.fillRect(0,0,500,500);

                g2d.translate(this.getWidth() / 2, this.getHeight() / 2);
                g2d.rotate(Math.toRadians(lightAngle.getValue()));
                g2d.translate(-getLightImage().getWidth(this) / 2, -getLightImage().getHeight(this) / 2);

                g2d.drawImage(getLightImage(), 0, 0, this);

                // backwards
                g2d.translate(getLightImage().getWidth(this) / 2, getLightImage().getHeight(this) / 2);
                g2d.rotate(-Math.toRadians(lightAngle.getValue()));
                g2d.translate(-this.getWidth() / 2, -this.getHeight() / 2);

                g2d.setColor(new Color(238, 238, 238));
                g2d.fillRect(0, 0, 80, 120);

                g2d.translate(getLightImage().getWidth(this) * 2 - 27, 0);
                g2d.fillRect(0, 0, 120, 120);


                revalidate();
                repaint();
            }

            /**
             *
             * @return image of ball which rotates in settings box
             */
            private BufferedImage getLightImage() {
                if (lightImage == null) {
                    try {
                        lightImage = ImageIO.read(this.getClass().getResourceAsStream("/review_normal/lightImage.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return lightImage;
            }

        }

        private ActionListener reviewActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == recalculateButton) {
                    if (imageLoader != null && image != null) {

                        imageLoader.refreshNormalMap(lightAngle.getValue(), (((double) height.getValue() * (-99.0)) / 10000.0 + 1.0));
                        updateNormal(image.getNormalMap());
                    }
                }
            }
        };
    }

    /**
     * HeightMapSettingsBox class contains all GUI parts for editing
     * height maps. This class is active when Height Map tab is selected.
     */
    private class HeightMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel calculateNormalPanel;
        JButton calculateNormalButton, invert;

        @Override
        public JPanel getPanel() {
            if (settingBox == null) {
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());


                calculateNormalPanel = new JPanel();
                invert = new JButton("Invert Heights");
                invert.addActionListener(invertActionListener);
                calculateNormalButton = new JButton("Calculate Normal Map");
                calculateNormalButton.addActionListener(reviewActionListener);
                calculateNormalPanel.add(calculateNormalButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                JPanel in = new JPanel();
                in.add(invert);
                in.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                settingBox.add(in, BorderLayout.NORTH);
                settingBox.add(calculateNormalPanel, BorderLayout.SOUTH);
            }
            return settingBox;
        }

        private ActionListener reviewActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == calculateNormalButton) {
                    if (imageLoader != null && image != null && image.getHeightMap() != null) {
                        imageLoader.refreshNormalMap(angle, ((70.0 * (-99.0)) / 10000.0 + 1.0));
                        updateNormal(image.getNormalMap());
                        cardSettingsBoxLayout.show(cardSettingsBoxPanel, "ns");
                        tabbedPanel.setSelectedComponent(normalMapImagePanel);
                    }
                }
            }
        };

        private ActionListener invertActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getSource() == invert) {
                    if (imageLoader != null && image != null && image.getHeightMap() != null) {
                        imageLoader.invertHeightMap();
                        updateHeight(image.getHeightMap());
                    }
                }
            }
        };
    }

    private JFrame getFrame() {
        return this;
    }

    /**
     * OriginalMapSettingsBox class is active when Original Image tab is selected.
     * This class contains all GUI parts available on the left toolbox when Original Image is selected.
     */
    private class OriginalMapSettingsBox extends SettingsBox {
        JPanel settingBox;
        JPanel recalculatePanel, editPanel, markerSizePanel, listPanel, settingsPanel;
        JButton recalculateButton;
        java.util.List<Marker> markerList;
        JList<Marker> displayMarkerList;
        ButtonGroup buttonGroup;
        JSlider regularSlider, deltaESlider, stepsSlider;

        JToggleButton addMarkerButton, editMarkerButton, removeMarkerButton, activeButton;

        JSlider markerSizeSlider;
        private boolean doGong = false;

        @Override
        public JPanel getPanel() {
            if (settingBox == null && this.markerList != null) {
                settingBox = new JPanel();
                settingBox.setLayout(new BorderLayout());

                JPanel topPanel = new JPanel(new BorderLayout());

                editPanel = new JPanel(new GridLayout(0, 3));
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
                    if (actionEvent.getSource() == addMarkerButton) {
                        activeButton = addMarkerButton;
                    } else if (actionEvent.getSource() == editMarkerButton) {
                        activeButton = editMarkerButton;
                    } else if (actionEvent.getSource() == removeMarkerButton) {
                        activeButton = removeMarkerButton;
                    }
                };

                addMarkerButton.addActionListener(acButton);
                editMarkerButton.addActionListener(acButton);
                removeMarkerButton.addActionListener(acButton);

                editPanel.add(addMarkerButton);
                editPanel.add(editMarkerButton);
                editPanel.add(removeMarkerButton);

                topPanel.add(editPanel, BorderLayout.NORTH);

                markerSizePanel = new JPanel(new BorderLayout());
                JLabel markerSizeLabel = new JLabel("  Markers size:");
                markerSizeSlider = new JSlider(JSlider.HORIZONTAL, 2, 150, 50);
                markerSizeSlider.addChangeListener(changeEvent -> {
                    originalMapImagePanel.setSquareSize(markerSizeSlider.getValue());
                    originalMapImagePanel.revalidate();
                    originalMapImagePanel.repaint();
                });
                markerSizePanel.add(markerSizeLabel, BorderLayout.NORTH);
                markerSizePanel.add(markerSizeSlider, BorderLayout.CENTER);
                markerSizePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

                topPanel.add(markerSizePanel, BorderLayout.SOUTH);

                settingBox.add(topPanel, BorderLayout.NORTH);

                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                listPanel = new JPanel(new BorderLayout());
                displayMarkerList = new JList<>();
                displayMarkerList.addListSelectionListener(listSelectionEvent ->
                        originalMapImagePanel.hightlightIfListClicked(displayMarkerList.getSelectedIndex()));
                displayMarkerList.setListData(markerList.toArray(new Marker[markerList.size()]));
                scrollPane.setViewportView(displayMarkerList);
                listPanel.add(scrollPane, BorderLayout.CENTER);
                listPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

                settingBox.add(listPanel, BorderLayout.CENTER);

                JPanel bottomPanel = new JPanel(new BorderLayout());

                settingsPanel = new JPanel();
                settingsPanel.setLayout(new BorderLayout());
                JPanel up = new JPanel(new BorderLayout());
                JPanel upLeft = new JPanel(new BorderLayout());
                upLeft.add(new JLabel("    Markers effect:"), BorderLayout.NORTH);
                deltaESlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
                deltaESlider.setMajorTickSpacing(25);
                deltaESlider.setMinorTickSpacing(10);
                deltaESlider.setPaintTicks(true);
                deltaESlider.setPaintLabels(true);
                deltaESlider.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                upLeft.add(deltaESlider, BorderLayout.CENTER);

                JPanel upRight = new JPanel(new BorderLayout());
                upRight.add(new JLabel("   Smoothness:      "), BorderLayout.NORTH);
                regularSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
                regularSlider.setMajorTickSpacing(25);
                regularSlider.setMinorTickSpacing(10);
                regularSlider.setPaintTicks(true);
                regularSlider.setPaintLabels(true);
                regularSlider.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                upRight.add(regularSlider, BorderLayout.CENTER);

                up.add(upLeft, BorderLayout.WEST);
                up.add(upRight, BorderLayout.EAST);


                JPanel down = new JPanel(new BorderLayout());
                down.setBorder(BorderFactory.createBevelBorder(1));
                JLabel calStepsLabel = new JLabel("   Calculation steps");
                calStepsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                down.add(calStepsLabel, BorderLayout.NORTH);
                stepsSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
                stepsSlider.setMajorTickSpacing(25);
                stepsSlider.setMinorTickSpacing(10);
                stepsSlider.setPaintTicks(true);
                stepsSlider.setPaintLabels(true);

                down.add(stepsSlider, BorderLayout.CENTER);
                JPanel textPanel = new JPanel(new BorderLayout());
                JCheckBox checkBox = new JCheckBox();
                checkBox.addActionListener(actionEvent -> {
                    if (checkBox.isSelected()) {
                        doGong = true;
                    } else {
                        doGong = false;
                    }
                });
                textPanel.add(new JLabel("  Notice me with sound "), BorderLayout.WEST);
                textPanel.add(checkBox, BorderLayout.CENTER);
                textPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                down.add(textPanel, BorderLayout.PAGE_END);
                settingsPanel.add(up, BorderLayout.NORTH);
                settingsPanel.add(down, BorderLayout.CENTER);


                recalculatePanel = new JPanel();
                recalculateButton = new JButton(new WaitAction("Calculate Height Map"));
                recalculatePanel.add(recalculateButton);
                settingBox.setBorder(BorderFactory.createLoweredBevelBorder());
                bottomPanel.add(recalculatePanel, BorderLayout.PAGE_END);
                bottomPanel.add(settingsPanel, BorderLayout.NORTH);
                settingBox.add(bottomPanel, BorderLayout.PAGE_END);

            }
            return settingBox;
        }

        /**
         * updates list of markers
         */
        void updateList() {
            displayMarkerList.setListData(markerList.toArray(new Marker[markerList.size()]));
            displayMarkerList.revalidate();
            displayMarkerList.repaint();
        }

        void setMarkerList(java.util.List<Marker> markerList) {
            this.markerList = markerList;
        }

        /**
         * WaitAction class contains loading dialog. Loading dialog is shown
         * when NormalMAPP is calculating height map.
         */
        private class WaitAction extends AbstractAction {

            WaitAction(String name) {
                super(name);
            }

            @Override
            public void actionPerformed(ActionEvent evt) {

                if (markerList.size() < 3) {
                    JOptionPane.showMessageDialog(getFrame(), "You must provide at least 3 markers.");
                } else {
                    SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {

                            //float tic = System.nanoTime();

                            imageLoader.calculateHeightMap(markerList, stepsSlider.getValue(), 1.0, ((double) (regularSlider.getValue())) / 100.0, ((double) (deltaESlider.getValue())) / 200.0);
                            //imageLoader.calculateHeightMap(markerList, stepsSlider.getValue(), ((double) (deltaESlider.getValue())) / 100.0, ((double) (regularSlider.getValue())) / 100.0, 0.25);

                            statusLabel.setText(imageLoader.getLightVector());
                            updateHeight(image.getHeightMap());
                            if (doGong) {
                                gong();
                            }
                            /*float toc = System.nanoTime();
                            System.out.println((double)(toc-tic)/1000000000.0);*/

                            if (image.getHeightMap() != null) {
                                tabbedPanel.setSelectedComponent(heightMapImagePanel);
                                cardSettingsBoxLayout.show(cardSettingsBoxPanel, "hs");
                            }


                            return null;
                        }
                    };

                    Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
                    final JDialog dialog = new JDialog(win, "Calculating Height Map", Dialog.ModalityType.APPLICATION_MODAL);
                    dialog.setResizable(false);
                    mySwingWorker.addPropertyChangeListener(evt1 -> {
                        if (evt1.getPropertyName().equals("state")) {
                            if (evt1.getNewValue() == SwingWorker.StateValue.DONE) {
                                dialog.dispose();
                            }
                        }
                    });
                    mySwingWorker.execute();

                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    progressBar.setPreferredSize(new Dimension(250, 20));
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

    /**
     * Plays sound of gong from Resources/gong/gong.wav
     */
    private static void gong() {
        new Thread(() -> {
            try {
                Clip clip;
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(MainScreen.class.getResourceAsStream("/gong/gong.wav")));
                DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


}

