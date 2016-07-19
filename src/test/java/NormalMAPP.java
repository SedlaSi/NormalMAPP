

import gui.session.ImageLoader;
import gui.MainScreen;
import gui.session.Session;

/**
 * Created by root on 14.7.16.
 */
public class NormalMAPP {

    public static void main(String [] args){
        Session session = new Session();
        ImageLoader imageLoader = new ImageLoader(session.getSessionFolder());
        MainScreen mainScreen = new MainScreen(session,imageLoader);
        mainScreen.createFrame();
    }
}
