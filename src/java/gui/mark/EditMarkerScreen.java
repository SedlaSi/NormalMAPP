package gui.mark;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by root on 23.10.16.
 */
public class EditMarkerScreen extends JDialog {

    private Marker marker;

    JPanel topPanel,bottomPanel,directionPanel,anglePanel,namePanel,buttonPanel;
    DirectionPanel directionImagePanel;
    AnglePanel angleImagePanel;

    JSlider directionSlider, angleSlider;
    Dimension imageDimension;
    JTextArea nameArea;
    JButton cancelButton,okButton;

    BufferedImage directionImage,angleImage;

    public EditMarkerScreen(JFrame mainFrame, String name,Dialog.ModalityType modalityType){
        super(mainFrame,name,modalityType);
    }

    public EditMarkerScreen(){
        super();
    }

    public static void main(String [] args){
        EditMarkerScreen editMarkerScreen = new EditMarkerScreen();
        editMarkerScreen.startFrame();
    }

    public void startFrame(){
        this.setPreferredSize(new Dimension(450,350));
        this.pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Edit Marker");

        imageDimension = new Dimension(200,200);

        topPanel = new JPanel(new GridLayout(1,2));
        directionPanel = new JPanel(new BorderLayout());
        directionPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK,1));
        anglePanel = new JPanel(new BorderLayout());
        anglePanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK,1));
        topPanel.add(directionPanel);
        topPanel.add(anglePanel);

        directionPanel.add(new JLabel("  Direction of Surface descent  "),BorderLayout.NORTH);
        directionImagePanel = new DirectionPanel();
        directionPanel.setBackground(Color.WHITE);
        directionImagePanel.setBackground(Color.WHITE);
        directionImagePanel.setPreferredSize(imageDimension);
        directionPanel.add(directionImagePanel,BorderLayout.CENTER);
        directionSlider = new JSlider(JSlider.HORIZONTAL,0,360,0);
        directionSlider.setMajorTickSpacing(90);
        directionSlider.setMinorTickSpacing(45);
        directionSlider.setPaintTicks(true);
        directionSlider.setPaintLabels(true);
        directionPanel.add(directionSlider,BorderLayout.SOUTH);

        anglePanel.add(new JLabel("  Angle of Surface descent  "),BorderLayout.NORTH);
        angleImagePanel = new AnglePanel();
        anglePanel.setBackground(Color.WHITE);
        angleImagePanel.setPreferredSize(imageDimension);
        anglePanel.add(angleImagePanel,BorderLayout.CENTER);
        angleSlider = new JSlider(JSlider.HORIZONTAL,0,90,45);
        angleSlider.setMajorTickSpacing(45);
        angleSlider.setMinorTickSpacing(10);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);
        anglePanel.add(angleSlider,BorderLayout.SOUTH);

        bottomPanel = new JPanel(new GridLayout(1,2));
        namePanel = new JPanel(new GridLayout(2,1));
        namePanel.add(new JLabel("Name:"));
        nameArea = new JTextArea();
        namePanel.add(nameArea);

        buttonPanel = new JPanel(new BorderLayout());
        cancelButton = new JButton("Cancel");
        okButton = new JButton("OK");

        ActionListener buttonListener = actionEvent -> {
            if(actionEvent.getSource() == okButton){
                if(!nameArea.getText().isEmpty()) {
                    marker.setName(nameArea.getText());
                }
                //  Vypocteni x y a z pro marker
                double x =(Math.cos(angleSlider.getValue())*Math.sin(directionSlider.getValue()));
                double y =(Math.cos(angleSlider.getValue())*Math.cos(directionSlider.getValue()));
                double z = (Math.sin(angleSlider.getValue()));
                x = 127.5*(x+1);
                y = 127.5*(y+1);
                z = 127.5*(z+1);

                marker.setX((int)x);
                marker.setY((int)y);
                marker.setZ((int)z);

                // ---------------------
                disposeDialog();
            } else if(actionEvent.getSource() == cancelButton){
                marker.setX(-1);
                marker.setY(-1);
                marker.setZ(-1);
                disposeDialog();
            }
        };

        okButton.addActionListener(buttonListener);
        cancelButton.addActionListener(buttonListener);


        buttonPanel.add(cancelButton,BorderLayout.WEST);
        buttonPanel.add(okButton,BorderLayout.CENTER);

        bottomPanel.add(namePanel);
        bottomPanel.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(topPanel,BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.SOUTH);
        setVisible(true);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
    }

    private void disposeDialog(){
        this.dispose();
    }

    private BufferedImage getDirectionImage(){
        if(directionImage == null) {
            try {
                directionImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/dirMarkerImage.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directionImage;
    }

    private BufferedImage getAngleImage(){
        if(angleImage == null) {
            try {
                angleImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/angleMarkerImage.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return angleImage;
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    private class DirectionPanel extends JPanel{

        @Override
        protected void paintComponent(Graphics g) {
            //super.paint(g);
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(this.getWidth()/2,this.getHeight()/2);
            g2d.rotate(Math.toRadians(directionSlider.getValue()) );
            g2d.translate(-getDirectionImage().getWidth(this) / 2, -getDirectionImage().getHeight(this) / 2);
            g2d.drawImage(getDirectionImage(),0,0,this);

            if(directionSlider.getValue() > 90 && directionSlider.getValue() <= 270){
                angleImagePanel.switchToRightAngle();
            } else {
                angleImagePanel.switchToLeftAngle();
            }

            revalidate();
            repaint();
        }

    }

    private class AnglePanel extends JPanel{

        private int startX = 170;
        private int startY = 170;
        private int angleMult = 1;

        @Override
        protected void paintComponent(Graphics g) {
            //super.paint(g);
            g.drawImage(getAngleImage(),0,0,this);
            int angle = angleSlider.getValue();

            int length = 150;

            int endX = (int)(startX - angleMult*Math.cos(Math.toRadians(angle)) * length);
            int endY = (int)(startY - Math.sin(Math.toRadians(angle)) * length);

            g.drawLine(startX, startY, endX, endY);

            revalidate();
            repaint();
        }

        public void switchToLeftAngle(){
            startX = 30;
            angleMult = -1;
        }

        public void switchToRightAngle(){
            startX = 170;
            angleMult = 1;
        }

    }

}
