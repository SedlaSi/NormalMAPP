package gui.session;

import algorithms.HeightMap;
import algorithms.NormalMap;
import image.Image;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.PriorityQueue;

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
    private final HeightMap heightMap = new HeightMap();
    private final NormalMap normalMap = new NormalMap();

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

        LoadingScreen loadingImageProgressBar = new LoadingImageProgressBar();
        loadingImageProgressBar.startLoading(heightMap.getSteps() + normalMap.getSteps());
        //loadingImageProgressBar.startLoading(heightMap.getSteps() + normalMap.getSteps());
        heightMap.setLoadingScreen(loadingImageProgressBar);
        normalMap.setLoadingScreen(loadingImageProgressBar);

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
            //System.out.println("converted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        heightMap.write(heightMap.heightMap(heightMap.read(newImagePath)),sessionFolder + "/" + HEIGHT_NAME);
        normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + "/" + HEIGHT_NAME),1,1,0.1),sessionFolder + "/" + NORMAL_NAME);

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
        loadingImageProgressBar.stopLoading();


        heightMap.setLoadingScreen(null);
        normalMap.setLoadingScreen(null);

        return image;
    }

    public void refreshNormalMap(int xDirection, int yDirection,double height){
        LoadingImageProgressBar loadingImageProgressBar = new LoadingImageProgressBar();
        loadingImageProgressBar.startLoading(normalMap.getSteps());
        normalMap.setLoadingScreen(loadingImageProgressBar);
        normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + "/" + HEIGHT_NAME),xDirection,yDirection,height),sessionFolder + "/" + NORMAL_NAME);

        try {
            image.setNormalMap(ImageIO.read(new File(sessionFolder + "/" + NORMAL_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadingImageProgressBar.stopLoading();
        normalMap.setLoadingScreen(null);
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

    private static class LoadingImageProgressBar extends JFrame implements LoadingScreen {


        private JProgressBar progressBar;
        private java.util.PriorityQueue<Integer> addProgressList;
        private java.util.PriorityQueue<String> messages;

        public static void main(String[] args) throws InterruptedException {

            LoadingImageProgressBar loadingImageProgressBar = new LoadingImageProgressBar();
            loadingImageProgressBar.startLoading(100);
            loadingImageProgressBar.setText("progress");
            loadingImageProgressBar.addProgress(60);


        }

        @Override
        public void startLoading(int maximum) {

            new Thread(() -> {
                this.setPreferredSize(new Dimension(300,30));
                this.setResizable(false);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                progressBar = new JProgressBar();
                progressBar.setMinimum(0);
                progressBar.setMaximum(maximum);
                progressBar.setIndeterminate(true);
                setLocationRelativeTo(null);
                setTitle("Loading image, please wait...");
                this.add(progressBar);
                pack();
                setVisible(true);

            }).start();



            //startThread();
        }

        private void startThread(){
            addProgressList = new PriorityQueue<>();
            messages = new PriorityQueue<>();
            new Thread(()->{
                Integer progress;
                String message;
                while(true) {
                    while ((progress = addProgressList.poll()) != null) ;

                    try{
                        if(progress == -1){
                            break;
                        }
                    } catch (Exception e){
                        break;
                    }

                    final int val = progress;
                    //while((message = messages.poll())!= null);
                    SwingUtilities.invokeLater(() -> {
                        progressBar.invalidate();
                        progressBar.setValue(val);
                        //progressBar.setString(message);
                        progressBar.revalidate();
                        progressBar.repaint();

                    });

                }
                //SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));


            }).start();
        }

        @Override
        public void addProgress(int amount) {
            //addProgressList.add(progressBar.getValue() + amount);
            /*invalidate();
            revalidate();
            repaint();*/
        }

        /**
         * METHOD DOES NOT UPDATE JFRAME!!!!!!
         * @param text
         */
        @Override
        public void setText(String text) {
            //messages.add(text);
            //progressBar.setStringPainted(true);
            //progressBar.setString(text);
        }

        @Override
        public void stopLoading() {
            this.dispose();
        }
    }

}
