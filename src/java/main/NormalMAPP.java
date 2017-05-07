package main;

import gui.MainScreen;
import gui.session.ImageLoader;
import gui.session.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by sedlasi1 on 14.7.16.
 *
 * NormalMAPP is free software created by Simon Sedlacek.
 *
 * What does it do:
 *      NormalMAPP can create height maps and normal maps from single
 *      input image. NormalMAPP is using one of the newest algorithms
 *      for surface reconstruction called "Interactive normal reconstruction
 *      from a single image". For further information look in class
 *          java.algorithms.ShapeFromShading
 *
 * NormalMAPP class contains main() method for starting the program
 *
 */
public class NormalMAPP {

    public static void main(String[] args) {
        new NormalMAPP();
    }

    public NormalMAPP() {
        EventQueue.invokeLater(() -> {
            Session session = new Session();
            ImageLoader imageLoader = new ImageLoader(session.getSessionFolder());
            MainScreen mainScreen = new MainScreen(session, imageLoader);
            imageLoader.setMainFrameReference(mainScreen);
            mainScreen.createFrame();
        });

    }
}

