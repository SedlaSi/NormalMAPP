package main;

import gui.MainScreen;
import gui.session.ImageLoader;
import gui.session.Session;

import java.awt.*;

/**
 * Created by root on 14.7.16.
 */
public class NormalMAPP {

    public static void main(String [] args){
        new NormalMAPP();

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
}
