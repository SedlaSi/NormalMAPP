package gui.session;

import algorithms.NormalMap;
import algorithms.ShapeFromShading;
import gui.sfs.Marker;
import image.Image;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by sedlasi1 on 14.7.16.
 *
 * ImageLoader class is used for removing/loading images
 * in/to the session folder. For further information
 * about session folder visit gui.session.Session class.
 */
public class ImageLoader extends JFrame {

    JFileChooser fileChooser;
    JFileChooser fileSaver;
    JFrame mainFrameReference;
    private String sessionFolder;
    private static final String ORIGINAL_NAME = "original.ppm";
    private static final String HEIGHT_NAME = "height.ppm";
    private static final String NORMAL_NAME = "normal.ppm";
    private final NormalMap normalMap = new NormalMap();
    private final ShapeFromShading shapeFromShading = new ShapeFromShading();

    Image image;

    public ImageLoader(String sessionFolder) {
        fileChooser = new JFileChooser();
        fileSaver = new JFileChooser();
        this.sessionFolder = sessionFolder;
    }

    /**
     * @method setMainFrameReference() set reference to the MainScreen object.
     * @param mainFrameReference
     */
    public void setMainFrameReference(JFrame mainFrameReference) {
        this.mainFrameReference = mainFrameReference;
    }

    /**
     * @method loadImage() is used to load
     * input image to the application.
     * JFileChooser is started inside.
     * @return image.Image
     */
    public Image loadImage() {

        int ret = fileChooser.showOpenDialog(ImageLoader.this);
        File file;
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            return null;
        }
        SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String newImagePath = sessionFolder + Session.SLASH + ORIGINAL_NAME;
                try {
                    // Use IM
                    IMOperation op = new IMOperation();
                    // Pipe
                    op.addImage(file.getAbsolutePath());
                    op.addImage("ppm:" + newImagePath);
                    // CC command
                    ConvertCmd convert = new ConvertCmd(true);
                    //convert.setSearchPath(Session.graphicsPath);
                    // Run
                    convert.run(op);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                image = null;
                try {
                    //System.out.println(newImagePath);
                    File imageFile = new File(newImagePath);
                    BufferedImage imData = ImageIO.read(imageFile);
                    image = new Image(imageFile, imData);

                    //image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
                    //image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        final JDialog dialog = new JDialog(this, "Loading Image", Dialog.ModalityType.APPLICATION_MODAL);

        mySwingWorker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
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
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return image;
    }

    /**
     * @method loadHeightMap() is used to load
     * height map image to the application.
     * JFileChooser is started inside.
     * @return image.Image
     */
    public Image loadHeightMap() {

        int ret = fileChooser.showOpenDialog(ImageLoader.this);
        File file;
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            return null;
        }

        SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String newImagePath = sessionFolder + Session.SLASH + HEIGHT_NAME;
                try {
                    // Use IM
                    IMOperation op = new IMOperation();
                    // Pipe
                    op.addImage(file.getAbsolutePath());
                    op.addImage("ppm:" + newImagePath);
                    // CC command
                    ConvertCmd convert = new ConvertCmd(true);
                    // Run
                    convert.run(op);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME), 0, 0.1), sessionFolder + "/" + NORMAL_NAME);

                image = null;
                try {
                    image = new Image(null, null);
                    image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
                    image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        final JDialog dialog = new JDialog(this, "Loading Image", Dialog.ModalityType.APPLICATION_MODAL);

        mySwingWorker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
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
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);


        return image;
    }

    /**
     * @method refreshNormalMap() recalculates normal map in session folder
     * with input angle and height of the z value.
     *
     * @param angle
     * @param height
     */
    public void refreshNormalMap(double angle, double height) {
        if (image.getHeightMap() != null) {
            normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME), angle, height), sessionFolder + Session.SLASH + NORMAL_NAME);

            try {
                image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @method calculateHeightMap() calculates height map and save it to the
     * session folder.
     * @param markerList user input markers
     * @param steps number of calculation steps
     * @param q albedo parameter, not currently in use
     * @param lm lambda parameter -> set by Smoothness slider in GUI
     * @param e e parameter -> set by Markers effect slider in GUI
     */
    public void calculateHeightMap(java.util.List<Marker> markerList, int steps, double q, double lm, double e) {
        shapeFromShading.setSteps(steps);
        shapeFromShading.setAlbedo(q);
        shapeFromShading.setRegularization(lm);
        shapeFromShading.setDeltaE(e);
        shapeFromShading.setMarkers(markerList);
        shapeFromShading.setImage(sessionFolder + Session.SLASH + ORIGINAL_NAME);
        shapeFromShading.write(shapeFromShading.shapeFromShading(), sessionFolder + Session.SLASH + HEIGHT_NAME);
        try {
            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     *
     * @return message which is shown on the bottom of the screen: Light vector ...
     */
    public String getLightVector() {
        return shapeFromShading.getLightMessage();
    }

    /**
     * @method saveHeightMap() saves height map file selected with JFileChooser
     */
    public void saveHeightMap() {
        fileChooser.setSelectedFile(new File("heightmap.png"));
        int ret = fileChooser.showSaveDialog(ImageLoader.this);
        File file;
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            return;
        }
        try {
            // Use IM
            IMOperation op = new IMOperation();
            // Pipe
            op.addImage(sessionFolder + Session.SLASH + HEIGHT_NAME);
            op.addImage("png:" + file.getAbsolutePath());
            // CC command
            ConvertCmd convert = new ConvertCmd(true);
            // Run
            convert.run(op);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @method saveNormalMap() saves height map file selected with JFileChooser
     */
    public void saveNormalMap() {
        fileChooser.setSelectedFile(new File("normalmap.png"));
        int ret = fileChooser.showSaveDialog(ImageLoader.this);
        File file;
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            return;
        }
        try {
            // Use IM
            IMOperation op = new IMOperation();
            // Pipe
            op.addImage(sessionFolder + Session.SLASH + NORMAL_NAME);
            op.addImage("png:" + file.getAbsolutePath());
            // CC command
            ConvertCmd convert = new ConvertCmd(true);
            // Run
            convert.run(op);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @method invertHeightMap() invert heights in current height map.
     * It is called by clicking "Invert heights" button in height map settings box.
     */
    public void invertHeightMap() {
        shapeFromShading.write(shapeFromShading.invert(shapeFromShading.read(sessionFolder + Session.SLASH + HEIGHT_NAME)), sessionFolder + Session.SLASH + HEIGHT_NAME);
        try {
            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
