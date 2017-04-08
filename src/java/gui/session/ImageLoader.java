package gui.session;

import algorithms.NormalMap;
import algorithms.ShapeFromShading;
import gui.sfs.Marker;
import image.Image;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by root on 14.7.16.
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

    public void setMainFrameReference(JFrame mainFrameReference) {
        this.mainFrameReference = mainFrameReference;
    }

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

    public Image testloadImage() {

        File file = new File("/home/sedlasi1/Desktop/cl_koule.png");

        String newImagePath = sessionFolder + Session.SLASH + ORIGINAL_NAME;
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

        image = null;
        try {
            File imageFile = new File(newImagePath);
            BufferedImage imData = ImageIO.read(imageFile);
            image = new Image(imageFile, imData);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

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

    public String getLightVector() {
        return shapeFromShading.getLightMessage();
    }

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

    public Image getImage() {
        return image;
    }


    public void invertHeightMap() {
        shapeFromShading.write(shapeFromShading.invert(shapeFromShading.read(sessionFolder + Session.SLASH + HEIGHT_NAME)), sessionFolder + Session.SLASH + HEIGHT_NAME);
        try {
            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
