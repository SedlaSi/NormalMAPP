package gui.session;

import algorithms.HeightMap;
import algorithms.NormalMap;
import image.Image;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by root on 14.7.16.
 */
public class ImageLoader extends JFrame{

    JFileChooser fileChooser;
    JFileChooser fileSaver;
    private String sessionFolder;
    private static final String ORIGINAL_NAME = "original.ppm";
    private static final String HEIGHT_NAME = "height.ppm";
    private static final String NORMAL_NAME = "normal.ppm";

    Image image;

    public ImageLoader(String sessionFolder){
        fileChooser = new JFileChooser();
        fileSaver = new JFileChooser();
        this.sessionFolder = sessionFolder;
    }


    public Image loadImage(){

        int ret = fileChooser.showOpenDialog(ImageLoader.this);
        File file;
        if(ret == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        } else {
            return null;
        }

        String newImagePath = sessionFolder + "/" + ORIGINAL_NAME;

        try {
            // Use IM
            IMOperation op = new IMOperation();
            // Pipe
            op.addImage(file.getAbsolutePath());
            op.addImage("ppm:"+newImagePath);
            // CC command
            ConvertCmd convert = new ConvertCmd(true);
            // Run
            convert.run(op);
            System.out.println("converted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        HeightMap.write(HeightMap.read(newImagePath),sessionFolder + "/" + HEIGHT_NAME);
        NormalMap.write(NormalMap.normalMap(NormalMap.read(sessionFolder + "/" + HEIGHT_NAME)),sessionFolder + "/" + NORMAL_NAME);

        image = null;
        try {
            File imageFile = new File(newImagePath);
            BufferedImage imData = ImageIO.read(imageFile);
            image = new Image(imageFile,imData);
            image.setHeightMap(ImageIO.read(new File(sessionFolder + "/" + HEIGHT_NAME)));
            image.setNormalMap(ImageIO.read(new File(sessionFolder + "/" + NORMAL_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public void saveHeightMap(){
        int ret = fileChooser.showSaveDialog(ImageLoader.this);
        File file;
        if(ret == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        } else {
            return;
        }
        try {
            // Use IM
            IMOperation op = new IMOperation();
            // Pipe
            op.addImage(sessionFolder + "/" + HEIGHT_NAME);
            op.addImage("png:"+file.getAbsolutePath());
            // CC command
            ConvertCmd convert = new ConvertCmd(true);
            // Run
            convert.run(op);
            System.out.println("saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveNormalMap(){
        int ret = fileChooser.showSaveDialog(ImageLoader.this);
        File file;
        if(ret == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        } else {
            return;
        }
        try {
            // Use IM
            IMOperation op = new IMOperation();
            // Pipe
            op.addImage(sessionFolder + "/" + NORMAL_NAME);
            op.addImage("png:"+file.getAbsolutePath());
            // CC command
            ConvertCmd convert = new ConvertCmd(true);
            // Run
            convert.run(op);
            System.out.println("saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Image getImage(){
        return image;
    }

}
