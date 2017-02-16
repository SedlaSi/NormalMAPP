package main;

import gui.MainScreen;
import gui.session.ImageLoader;
import gui.session.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by root on 14.7.16.
 */
public class NormalMAPP {

    public static void main(String [] args){
        new NormalMAPP();
        //new NormalMAPP("load");
        /*Session session = new Session();
        ImageLoader imageLoader = new ImageLoader(session.getSessionFolder());
        MainScreen mainScreen = new MainScreen(session,imageLoader);
        mainScreen.createFrame();*/
    }

    public NormalMAPP (){

        EventQueue.invokeLater(() -> {
            Session session = new Session();
            ImageLoader imageLoader = new ImageLoader(session.getSessionFolder());
            MainScreen mainScreen = new MainScreen(session,imageLoader);
            imageLoader.setMainFrameReference(mainScreen);
            mainScreen.createFrame();
        });

    }

    public NormalMAPP(String s){

            JButton showWaitBtn = new JButton(new ShowWaitAction("Show Wait Dialog"));
            JPanel panel = new JPanel();
            panel.add(showWaitBtn);
            Session session = new Session();
            ImageLoader imageLoader = new ImageLoader(session.getSessionFolder());
            MainScreen mainScreen = new MainScreen(session,imageLoader);
            imageLoader.setMainFrameReference(mainScreen);
            mainScreen.createFrame();
            /*JFrame frame = new JFrame("Frame");
            frame.getContentPane().add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);*/


    }

    private class ShowWaitAction extends AbstractAction {
        protected static final long SLEEP_TIME = 3 * 1000;

        public ShowWaitAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
                @Override
                protected Void doInBackground() throws Exception {

                    // mimic some long-running process here...
                    Thread.sleep(SLEEP_TIME);
                    return null;
                }
            };

            Window win = SwingUtilities.getWindowAncestor((AbstractButton)evt.getSource());
            final JDialog dialog = new JDialog(win, "Dialog", Dialog.ModalityType.APPLICATION_MODAL);

            mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("state")) {
                        if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                            dialog.dispose();
                        }
                    }
                }
            });
            mySwingWorker.execute();

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(progressBar, BorderLayout.CENTER);
            panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
            dialog.add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(win);
            dialog.setVisible(true);
        }
    }

}

