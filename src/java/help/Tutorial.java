package help;

import sun.awt.im.CompositionArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import static help.Tutorial.Cards.*;

/**
 * Created by root on 7.3.17.
 */
public class Tutorial {

    JPanel tutorialPanel;
    JDialog dialog;
    JPanel cardPanel, buttonPanel;
    CardLayout cardLayout;
    JButton next, previous, end;
    int pointer = 0;
    private final static int WIDTH = 600;
    private final static int HEIGHT = 300;

    public static void main(String[] args) {
        Tutorial tut = new Tutorial();
        tut.start(null);
    }

    public void start(JFrame parent) {
        prepareTutorialPanel();
        prepareDialog(parent, tutorialPanel);

        dialog.setVisible(true);

        cardLayout.show(cardPanel, "0");
        dialog.revalidate();
        dialog.repaint();
        dialog.setLocationRelativeTo(null);
    }

    private void prepareDialog(JFrame parent, JPanel panel) {
        if (dialog == null) {
            dialog = new JDialog(parent);
            dialog.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            dialog.pack();
            dialog.setContentPane(panel);
        }
    }

    private void prepareTutorialPanel() {
        if (tutorialPanel == null) {
            tutorialPanel = new JPanel(new BorderLayout());

            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            setCards(cardPanel);
            tutorialPanel.add(cardPanel, BorderLayout.CENTER);

            buttonPanel = new JPanel(new BorderLayout());
            next = new JButton("Next");
            next.addActionListener(actionEvent -> {
                if (pointer < 10) {
                    pointer++;
                    cardLayout.show(cardPanel, pointer + "");
                }
            });
            previous = new JButton("Previous");
            previous.addActionListener(actionEvent -> {
                if (pointer > 0) {
                    pointer--;
                    cardLayout.show(cardPanel, pointer + "");
                }
            });
            end = new JButton("End");
            end.addActionListener(actionEvent -> dialog.dispose());

            // pridani buttonu do buttonPanelu
            JPanel e = new JPanel();
            e.add(end);
            buttonPanel.add(e, BorderLayout.WEST);
            JPanel b = new JPanel();
            b.add(next);
            b.add(previous);
            buttonPanel.add(b, BorderLayout.EAST);
            //=============================
            tutorialPanel.add(buttonPanel, BorderLayout.PAGE_END);
        }
    }

    private void setCards(JPanel cardPanel) {
        cardPanel.add(getWelcome(), "0");
        cardPanel.add(getOpenImage(), "1");
        cardPanel.add(getAddMarker(), "2");
        cardPanel.add(getSetUpMarker(), "3");
        cardPanel.add(getEditRemoveMarker(), "4");
        cardPanel.add(getAlbedo(), "5");
        cardPanel.add(getSmoothness(), "6");
        cardPanel.add(getCalculationSteps(), "7");
        cardPanel.add(getHeightMap(), "8");
        cardPanel.add(getNormalMap(), "9");
        cardPanel.add(getSavingOutput(), "10");

    }

    protected static class Cards {

        static JPanel getWelcome() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/Welcome.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getOpenImage() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/OpenImage.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getAddMarker() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/AddMarker.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getSetUpMarker() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/SetUpMarker.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getEditRemoveMarker() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/EditRemoveMarker.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getAlbedo() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/Albedo.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getSmoothness() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/Smoothness.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getCalculationSteps() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/CalculationSteps.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getHeightMap() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/HeightMap.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getNormalMap() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/NormalMap.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

        static JPanel getSavingOutput() {
            JPanel panel = new JPanel();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

            editorPane.setEditable(false);
            java.net.URL helpURL = Tutorial.class.getResource(
                    "/help/SavingOutput.html");
            if (helpURL != null) {
                try {
                    editorPane.setPage(helpURL);
                } catch (IOException e) {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            } else {
                System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
            }

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 72));
            editorScrollPane.setMinimumSize(new Dimension(WIDTH - 20, HEIGHT - 72));

            panel.add(editorScrollPane);
            return panel;
        }

    }

}
