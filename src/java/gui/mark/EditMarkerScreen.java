package gui.mark;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

/**
 * Created by root on 23.10.16.
 */
public class EditMarkerScreen extends JDialog {

    private Marker marker;

    JPanel topPanel,bottomPanel,directionPanel,anglePanel,namePanel,buttonPanel;
    JPanel directionImagePanel,angleImagePanel;

    JSlider directionSlider, angleSlider;
    Dimension imageDimension;
    JTextArea nameArea;
    JButton cancelButton,okButton;

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
        this.setPreferredSize(new Dimension(450,250));
        this.pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Edit Marker");

        imageDimension = new Dimension(120,120);

        topPanel = new JPanel(new GridLayout(1,2));
        directionPanel = new JPanel(new BorderLayout());
        directionPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK,1));
        anglePanel = new JPanel(new BorderLayout());
        anglePanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK,1));
        topPanel.add(directionPanel);
        topPanel.add(anglePanel);

        directionPanel.add(new JLabel("  Direction of Surface descent  "),BorderLayout.NORTH);
        directionImagePanel = new JPanel();
        directionImagePanel.setPreferredSize(imageDimension);
        directionPanel.add(directionImagePanel,BorderLayout.CENTER);
        directionSlider = new JSlider(JSlider.HORIZONTAL,0,360,0);
        directionPanel.add(directionSlider,BorderLayout.SOUTH);

        anglePanel.add(new JLabel("  Angle of Surface descent  "),BorderLayout.NORTH);
        angleImagePanel = new JPanel();
        angleImagePanel.setPreferredSize(imageDimension);
        anglePanel.add(angleImagePanel,BorderLayout.CENTER);
        angleSlider = new JSlider(JSlider.HORIZONTAL,0,90,45);
        anglePanel.add(angleSlider,BorderLayout.SOUTH);

        bottomPanel = new JPanel(new GridLayout(1,2));
        namePanel = new JPanel(new GridLayout(2,1));
        namePanel.add(new JLabel("Name:"));
        nameArea = new JTextArea();
        namePanel.add(nameArea);

        buttonPanel = new JPanel(new BorderLayout());
        cancelButton = new JButton("Cancel");
        okButton = new JButton("OK");
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

    public void setMarker(Marker marker){
        this.marker = marker;
    }

}
