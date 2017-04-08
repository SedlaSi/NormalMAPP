package gui.sfs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by root on 23.10.16.
 */
public class EditMarkerScreen extends JDialog {

    private Marker marker;

    JPanel topPanel, bottomPanel, directionPanel, anglePanel, namePanel, buttonPanel;
    DirectionPanel directionImagePanel;
    AnglePanel angleImagePanel;
    private boolean edit;

    JSlider directionSlider, angleSlider;
    Dimension imageDimension;
    JTextArea nameArea;
    JButton cancelButton, okButton;

    BufferedImage directionImage, angleImage, backgroundDirectionImage, backgroundAngleImage;
    BufferedImage angleImageR, backgroundDirectionImageR;

    public EditMarkerScreen(JFrame mainFrame, String name, Dialog.ModalityType modalityType) {
        super(mainFrame, name, modalityType);
    }

    public EditMarkerScreen() {
        super();
    }

    public static void main(String[] args) {
        double angle = Math.toRadians(0.0);
        double dir = Math.toRadians(250.0);

        double z = Math.sqrt((Math.tan(angle) * Math.tan(angle)) / (1 + Math.tan(angle) * Math.tan(angle)));
        double x = Math.sqrt(1 / (Math.tan(angle) * Math.tan(angle) + 1 + Math.tan(dir) * Math.tan(dir) +
                Math.tan(dir) * Math.tan(dir) * Math.tan(angle) * Math.tan(angle)
        ));
        if (dir >= Math.toRadians(90.0) && dir <= Math.toRadians(270.0)) {
            x *= -1;
        }
        double y = Math.tan(dir) * x;

        x = 127.5 * (x + 1);
        y = 127.5 * (y + 1);
        z = 127.5 * (z + 1);

        System.out.println(x + " " + y + " " + z);
        double k = Math.sqrt(x * x + y * y + z * z);
        System.out.println(k);
        /*EditMarkerScreen editMarkerScreen = new EditMarkerScreen();
        editMarkerScreen.startFrame();*/
    }

    public void startFrame() {
        this.setPreferredSize(new Dimension(450, 350));
        this.pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Edit Marker");

        imageDimension = new Dimension(200, 200);

        topPanel = new JPanel(new GridLayout(1, 2));
        directionPanel = new JPanel(new BorderLayout());
        directionPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK, 1));
        anglePanel = new JPanel(new BorderLayout());
        anglePanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.BLACK, 1));
        topPanel.add(directionPanel);
        topPanel.add(anglePanel);

        directionPanel.add(new JLabel("  Direction of Surface descent  "), BorderLayout.NORTH);
        directionImagePanel = new DirectionPanel();
        directionPanel.setBackground(Color.WHITE);
        directionImagePanel.setBackground(Color.WHITE);
        directionImagePanel.setPreferredSize(imageDimension);
        directionPanel.add(directionImagePanel, BorderLayout.CENTER);
        directionSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
        directionSlider.setMajorTickSpacing(90);
        directionSlider.setMinorTickSpacing(45);
        directionSlider.setPaintTicks(true);
        directionSlider.setPaintLabels(true);
        directionPanel.add(directionSlider, BorderLayout.SOUTH);

        anglePanel.add(new JLabel("  Angle of Surface descent  "), BorderLayout.NORTH);
        angleImagePanel = new AnglePanel();
        anglePanel.setBackground(Color.WHITE);
        angleImagePanel.setPreferredSize(imageDimension);
        anglePanel.add(angleImagePanel, BorderLayout.CENTER);
        angleSlider = new JSlider(JSlider.HORIZONTAL, 0, 90, 45);
        angleSlider.setMajorTickSpacing(45);
        angleSlider.setMinorTickSpacing(10);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);
        anglePanel.add(angleSlider, BorderLayout.SOUTH);

        bottomPanel = new JPanel(new GridLayout(1, 2));
        namePanel = new JPanel(new GridLayout(2, 1));
        namePanel.add(new JLabel("Name:"));
        nameArea = new JTextArea();
        namePanel.add(nameArea);

        buttonPanel = new JPanel(new BorderLayout());
        cancelButton = new JButton("Cancel");
        okButton = new JButton("OK");

        ActionListener buttonListener = actionEvent -> {
            if (actionEvent.getSource() == okButton) {
                if (!nameArea.getText().isEmpty()) {
                    marker.setName(nameArea.getText());
                }
                double angle = Math.toRadians(angleSlider.getValue());
                double dir = Math.toRadians(directionSlider.getValue());

                double z = Math.sqrt((Math.tan(angle) * Math.tan(angle)) / (1 + Math.tan(angle) * Math.tan(angle)));
                double x = Math.sqrt(1 / (Math.tan(angle) * Math.tan(angle) + 1 + Math.tan(dir) * Math.tan(dir) +
                        Math.tan(dir) * Math.tan(dir) * Math.tan(angle) * Math.tan(angle)
                ));
                if (dir >= Math.toRadians(90.0) && dir <= Math.toRadians(270.0)) {
                    x *= -1;
                }
                double y = Math.tan(dir) * x;

                x = 127.5 * (x + 1);
                y = 127.5 * (y + 1);
                z = 127.5 * (z + 1);

                marker.setX((int) x);
                marker.setY((int) y);
                marker.setZ((int) z);
                marker.setAngle(angleSlider.getValue());
                marker.setDirection(directionSlider.getValue());

                disposeDialog();
            } else if (actionEvent.getSource() == cancelButton) {
                marker.setX(-1);
                marker.setY(-1);
                marker.setZ(-1);
                disposeDialog();
            }
        };

        okButton.addActionListener(buttonListener);
        cancelButton.addActionListener(buttonListener);


        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(okButton, BorderLayout.CENTER);

        bottomPanel.add(namePanel);
        bottomPanel.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        if (edit) {
            angleSlider.setValue(marker.getAngle());
            directionSlider.setValue(marker.getDirection());
        }


        setVisible(true);

        pack();
    }

    private void disposeDialog() {
        this.dispose();
    }

    private BufferedImage getDirectionImage() {
        if (directionImage == null) {
            try {
                directionImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/direction_row.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directionImage;
    }

    private BufferedImage getBackgroundDirectionImage() {
        if (backgroundDirectionImage == null) {
            try {
                backgroundDirectionImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/direction_background.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return backgroundDirectionImage;
    }

    private BufferedImage getAngleImage() {
        if (angleImage == null) {
            try {
                angleImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/angle_row.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return angleImage;
    }

    private BufferedImage getBackgroundAngleImage() {
        if (backgroundAngleImage == null) {
            try {
                backgroundAngleImage = ImageIO.read(this.getClass().getResourceAsStream("/review_marker_edit/angle_background.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return backgroundAngleImage;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    private class DirectionPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(238, 238, 238));
            g2d.fillRect(0,0,500,500);
            g2d.drawImage(getBackgroundDirectionImage(), 8, 13, this);
            g2d.translate(this.getWidth() / 2, this.getHeight() / 2);
            g2d.rotate(-Math.toRadians(directionSlider.getValue()));
            g2d.translate(-getDirectionImage().getWidth(this) / 2, -getDirectionImage().getHeight(this) / 2);
            g2d.drawImage(getDirectionImage(), 0, 0, this);


            revalidate();
            repaint();
        }

    }

    private class AnglePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g2) {
            Graphics2D g = (Graphics2D) g2;
            g.setColor(new Color(238, 238, 238));
            g.fillRect(0,0,500,500);

            g.translate(8, 13);
            g.drawImage(getBackgroundAngleImage(), 0, 0, this);
            if (directionSlider.getValue() > 90 && directionSlider.getValue() <= 270) {
                if (backgroundDirectionImageR == null) {
                    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                    tx.translate(-getBackgroundAngleImage().getWidth(null), 0);
                    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    backgroundDirectionImageR = op.filter(getBackgroundAngleImage(), null);
                }
                g.drawImage(backgroundDirectionImageR, 0, 0, this);

            } else {
                g.drawImage(getBackgroundAngleImage(), 0, 0, this);

            }

            int angle = angleSlider.getValue();

            if (directionSlider.getValue() > 90 && directionSlider.getValue() <= 270) {
                g.rotate(Math.toRadians(angle - 45), 200, 200);
                if (angleImageR == null) {

                    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                    tx.translate(-getAngleImage().getWidth(null), 0);
                    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    angleImageR = op.filter(getAngleImage(), null);
                }
                g.drawImage(angleImageR, 0, 0, this);

                g.rotate(Math.toRadians(-angle + 45), 200, 200);


            } else {
                g.rotate(Math.toRadians(-angle + 45), 0, 200);
                g.drawImage(getAngleImage(), 0, 0, this);

                g.rotate(Math.toRadians(angle - 45), 0, 200);

            }

            // vycisteni okraju
            g.translate(-8, -13);
            g.setColor(new Color(238, 238, 238));
            g.fillRect(0, 0, 8, 250);
            g.translate(8, 13 + 200);
            g.fillRect(0, 0, 250, 30);
            g.translate(-8, -13 - 200);
            g.translate(8 + 200, 0);
            g.fillRect(0, 0, 15, 250);

            revalidate();
            repaint();
        }
    }

    public void isEdit(boolean edit) {
        this.edit = edit;
    }

}
