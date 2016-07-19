package gui.session;

import java.io.File;
import java.io.IOException;

/**
 * Created by root on 19.7.16.
 */
public class Session {

    private static String ROOT_FOLDER = "/tmp/.NormalMAPP";
    private String sessionFolder;
    String sessionId;

    public Session(){
        sessionId = Long.toString(System.nanoTime()) + ((int)Math.random()*100);
        sessionFolder = ROOT_FOLDER + "/session_" + sessionId;
        init();
    }

    private void init(){
        File rootFolder = new File(ROOT_FOLDER);

        if(!rootFolder.exists() && !rootFolder.mkdir()){
            System.out.println("Cannot create root folder for session " +sessionId+".");
        }

        File sessionFolderFile = new File(sessionFolder);
        if(!sessionFolderFile.mkdir()){
            System.out.println("Cannot create session folder for session "+sessionId+".");
        }


    }

    public void endSession(){
        File sessionFolderFile = new File(sessionFolder);
        File [] subFiles = sessionFolderFile.listFiles();
        for(File f : subFiles){
            if(!f.delete()){
                System.out.println("Cannot delete file "+f.getName());
            }
        }
        if(!sessionFolderFile.delete()){
            System.out.println("Cannot delete file "+sessionFolderFile.getName());
        }
        File rootFile = new File(ROOT_FOLDER);
        if(rootFile.listFiles().length == 0 && !rootFile.delete()){
            System.out.println("Cannot delete ROOT_FOLDER.");
        }
    }

    public String getSessionFolder(){
        return sessionFolder;
    }


}
