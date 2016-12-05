package gui.session;

import algorithms.HeightMap;
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
import java.util.PriorityQueue;

/**
 * Created by root on 14.7.16.
 */
public class ImageLoader extends JFrame{

    JFileChooser fileChooser;
    JFileChooser fileSaver;
    JFrame mainFrameReference;
    private String sessionFolder;
    private static final String ORIGINAL_NAME = "original.ppm";
    private static final String HEIGHT_NAME = "height.ppm";
    private static final String NORMAL_NAME = "normal.ppm";
    private final HeightMap heightMap = new HeightMap();
    private final NormalMap normalMap = new NormalMap();
    private final ShapeFromShading shapeFromShading = new ShapeFromShading();

    Image image;

    public ImageLoader(String sessionFolder){
        fileChooser = new JFileChooser();
        fileSaver = new JFileChooser();
        this.sessionFolder = sessionFolder;
    }

    public void setMainFrameReference(JFrame mainFrameReference){
        this.mainFrameReference = mainFrameReference;
    }

    public Image loadImage(){

        int ret = fileChooser.showOpenDialog(ImageLoader.this);
        File file;
        if(ret == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        } else {
            return null;
        }

        LoadingScreen loadingImageProgressBar = new LoadingImageProgressBar(mainFrameReference,"",Dialog.ModalityType.DOCUMENT_MODAL);
        loadingImageProgressBar.startLoading(heightMap.getSteps() + normalMap.getSteps(),true);
        //loadingImageProgressBar.startLoading(heightMap.getSteps() + normalMap.getSteps());
        heightMap.setLoadingScreen(loadingImageProgressBar);
        normalMap.setLoadingScreen(loadingImageProgressBar);

        String newImagePath = sessionFolder + Session.SLASH + ORIGINAL_NAME;
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

        //new Thread(()->{
            heightMap.write(heightMap.heightMap(heightMap.read(newImagePath)),sessionFolder + Session.SLASH + HEIGHT_NAME);
            normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME),0,0.1),sessionFolder + Session.SLASH + NORMAL_NAME);
        //}).start();
        //heightMap.write(heightMap.heightMap(heightMap.read(newImagePath)),sessionFolder + Session.SLASH + HEIGHT_NAME);
        //normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME),1,1,0.1),sessionFolder + Session.SLASH + NORMAL_NAME);

        image = null;
        try {
            File imageFile = new File(newImagePath);
            BufferedImage imData = ImageIO.read(imageFile);
            image = new Image(imageFile,imData);
            File normalFile = new File(sessionFolder + Session.SLASH + NORMAL_NAME);

            /*while(!normalFile.exists()){

            }*/

            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
            image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadingImageProgressBar.stopLoading();


        heightMap.setLoadingScreen(null);
        normalMap.setLoadingScreen(null);

        return image;
    }

    public Image testloadImage(){

        File file = new File("/home/sedlasi1/Desktop/obrazky/coin.jpg");

        String newImagePath = sessionFolder + Session.SLASH + ORIGINAL_NAME;
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

        image = null;
        try {
            File imageFile = new File(newImagePath);
            BufferedImage imData = ImageIO.read(imageFile);
            image = new Image(imageFile,imData);
            /*while(!normalFile.exists()){

            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public Image loadHeightMap(){

        int ret = fileChooser.showOpenDialog(ImageLoader.this);
        File file;
        if(ret == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        } else {
            return null;
        }

        LoadingScreen loadingImageProgressBar = new LoadingImageProgressBar(mainFrameReference,"",Dialog.ModalityType.DOCUMENT_MODAL);
        loadingImageProgressBar.startLoading(normalMap.getSteps(),true);
        //loadingImageProgressBar.startLoading(heightMap.getSteps() + normalMap.getSteps());
        //heightMap.setLoadingScreen(loadingImageProgressBar);
        normalMap.setLoadingScreen(loadingImageProgressBar);

        String newImagePath = sessionFolder + Session.SLASH + HEIGHT_NAME;
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
        //heightMap.write(heightMap.heightMap(heightMap.read(newImagePath)),sessionFolder + "/" + HEIGHT_NAME);
        normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME),0,0.1),sessionFolder + "/" + NORMAL_NAME);

        image = null;
        try {
            image = new Image(null,null);
            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
            image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadingImageProgressBar.stopLoading();

        heightMap.setLoadingScreen(null);
        normalMap.setLoadingScreen(null);

        return image;
    }

    public void refreshNormalMap(double angle ,double height){
        LoadingImageProgressBar loadingImageProgressBar = new LoadingImageProgressBar(mainFrameReference,"",Dialog.ModalityType.DOCUMENT_MODAL);
        //loadingImageProgressBar.startLoading(normalMap.getSteps(),false);
        normalMap.setLoadingScreen(loadingImageProgressBar);
        normalMap.write(normalMap.normalMap(normalMap.read(sessionFolder + Session.SLASH + HEIGHT_NAME), angle,height),sessionFolder + Session.SLASH + NORMAL_NAME);

        try {
            image.setNormalMap(ImageIO.read(new File(sessionFolder + Session.SLASH + NORMAL_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadingImageProgressBar.stopLoading();
        normalMap.setLoadingScreen(null);
    }

    public void calculateHeightMap(java.util.List<Marker> markerList, int steps, double q, double lm){
            shapeFromShading.setSteps(steps);
            shapeFromShading.setAlbedo(q);
            shapeFromShading.setRegularization(lm);
            shapeFromShading.setMarkers(markerList);
            shapeFromShading.setImage(sessionFolder + Session.SLASH + ORIGINAL_NAME);
            shapeFromShading.write(shapeFromShading.shapeFromShading(),sessionFolder + Session.SLASH + HEIGHT_NAME);
        try {
            image.setHeightMap(ImageIO.read(new File(sessionFolder + Session.SLASH + HEIGHT_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            op.addImage(sessionFolder + Session.SLASH + HEIGHT_NAME);
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
            op.addImage(sessionFolder + Session.SLASH + NORMAL_NAME);
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

    private class LoadingImageProgressBar extends JDialog implements LoadingScreen {


        private JProgressBar progressBar;
        private java.util.PriorityQueue<Integer> addProgressList;
        private java.util.PriorityQueue<String> messages;
        private ProgressWorker pw;

        /*public static void main(String[] args) throws InterruptedException {

            LoadingImageProgressBar loadingImageProgressBar = new LoadingImageProgressBar();
            loadingImageProgressBar.startLoading(100);
            loadingImageProgressBar.setText("progress");
            loadingImageProgressBar.addProgress(60);


        }*/

        public LoadingImageProgressBar(JFrame mainFrameReference,String name, Dialog.ModalityType modalityType){
            super(mainFrameReference,"", ModalityType.MODELESS);
        }

        @Override
        public void startLoading(int maximum, boolean visible) {

            //new Thread(() -> {
                //pw = new ProgressWorker();

                this.setPreferredSize(new Dimension(300,30));
                this.setResizable(false);
                //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                progressBar = new JProgressBar();
                progressBar.setMinimum(0);
                progressBar.setMaximum(maximum);
                //progressBar.setIndeterminate(true);
                setLocationRelativeTo(null);
                setTitle("Loading image, please wait...");
                this.add(progressBar);
                pack();
                setVisible(visible);

            //}).start();



            //startThread();
        }

        public ProgressWorker getProgressWorker(){
            return pw;
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
            //progressBar.setValue(progressBar.getValue() + amount);
            //addProgressList.add(progressBar.getValue() + amount);
            //System.out.println(progressBar.getValue());
            //repaint(0);
            /*EventQueue.invokeLater(() -> {
                pw.newUpdate();
            });*/

        }

        /**
         * METHOD DOES NOT UPDATE JFRAME!!!!!!
         * @param text
         */
        @Override
        public void setText(String text) {
            //messages.add(text);
            /*EventQueue.invokeLater(() -> {
                pw.newMessage(text);
            });*/
        }

        @Override
        public void stopLoading() {
            //this.notifyAll();
            this.dispose();
        }

        public class ProgressWorker extends SwingWorker<Object, Object> {

            private boolean update = false;
            private String message = "";

            @Override
            protected Object doInBackground() throws Exception {

                while(message != null){
                    System.out.println("blah");
                    if(true) return null;
                    while(!update);
                    System.out.println("doing");
                    setText(message);
                    addProgress(1);

                    update = false;

                    invalidate();
                    revalidate();
                    repaint(0);
                }

                return null;
            }

            protected void newUpdate(){
                update = true;
            }

            protected void newMessage(String message){
                this.message = message;
            }
        }


    }



}
