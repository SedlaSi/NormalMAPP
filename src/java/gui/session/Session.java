package gui.session;

import org.im4java.process.ProcessStarter;

import java.io.File;

/**
 * Created by root on 19.7.16.
 */
public class Session {

    private static String ROOT_FOLDER = "/tmp/.NormalMAPP";
    public static String SLASH = "/";
    private String sessionFolder;
    String sessionId;

    public Session() {
        if (System.getProperty("os.name").contains("Windows")) {
            ROOT_FOLDER = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Temp\\.NormalMAPP";
            SLASH = "\\";
            String myPath = this.getClass().getResource("/GraphicsMagick").getPath();
            ProcessStarter.setGlobalSearchPath(myPath);
        }
        sessionId = Long.toString(System.nanoTime()) + ((int) (Math.random() * 100));
        sessionFolder = ROOT_FOLDER + SLASH + "session_" + sessionId;
        init();
    }

    private void init() {

        File rootFolder = new File(ROOT_FOLDER);

        if (!rootFolder.exists() && !rootFolder.mkdir()) {
            System.out.println("Cannot create root folder for session " + sessionId + ".");
            System.exit(120);
        }

        File sessionFolderFile = new File(sessionFolder);
        if (!sessionFolderFile.mkdir()) {
            System.out.println("Cannot create session folder for session " + sessionId + ".");
            System.exit(120);
        }


    }

    public void endSession() {
        File sessionFolderFile = new File(sessionFolder);
        File[] subFiles = sessionFolderFile.listFiles();
        for (File f : subFiles) {
            if (!f.delete()) {
                System.out.println("Cannot delete file " + f.getName());
            }
        }
        if (!sessionFolderFile.delete()) {
            System.out.println("Cannot delete file " + sessionFolderFile.getName());
        }
        File rootFile = new File(ROOT_FOLDER);
        if (rootFile.listFiles().length == 0 && !rootFile.delete()) {
            System.out.println("Cannot delete ROOT_FOLDER.");
        }
    }

    public String getSessionFolder() {
        return sessionFolder;
    }


}
