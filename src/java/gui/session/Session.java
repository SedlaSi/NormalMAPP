package gui.session;

import org.im4java.process.ProcessStarter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by root on 19.7.16.
 */
public class Session {

    private static String GRAPHICS_MAGIC_FOLDER = ".\\lib\\GraphicsMagick"; // do not put slash at the end

    private static String ROOT_FOLDER = "/tmp/.NormalMAPP";
    public static String SLASH = "/";
    public static String SYSTEM = "UNIX";
    private String sessionFolder;
    String sessionId;

    public Session() {
        if (System.getProperty("os.name").contains("Windows")) {
            SYSTEM = "WINDOWS";
            ROOT_FOLDER = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Temp\\.NormalMAPP";
            SLASH = "\\";
            loadGraphicsMagick();
            //ProcessStarter.setGlobalSearchPath(GRAPHICS_MAGIC_FOLDER);
        }

        sessionId = Long.toString(System.nanoTime()) + ((int) (Math.random() * 100));
        sessionFolder = ROOT_FOLDER + SLASH + "session_" + sessionId;
        init();
    }

    private void loadGraphicsMagick(){
        try{
            String path = Session.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File config = new File(new File(path).getParent()+"\\conf.txt");

            FileReader fileReader = new FileReader(config);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(!line.isEmpty() && line.charAt(0) != '#' && line.charAt(0) != '\n'){
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
            }
            fileReader.close();
            String [] content = stringBuffer.toString().split("=");
            if(content[0].equals("GraphicsMagick")){
                GRAPHICS_MAGIC_FOLDER = content[1];
                if(GRAPHICS_MAGIC_FOLDER.charAt(GRAPHICS_MAGIC_FOLDER.length()-1) == '\n'){
                    GRAPHICS_MAGIC_FOLDER = GRAPHICS_MAGIC_FOLDER.substring(0,GRAPHICS_MAGIC_FOLDER.length()-1);
                }
                System.out.println("GraphicsMagick path found = "+GRAPHICS_MAGIC_FOLDER);
                System.out.println();
            }

        } catch (Exception e){
            e.printStackTrace();
            System.exit(200);
        }
        //String myPath = this.getClass().getResource("/GraphicsMagick").getPath();
        ProcessStarter.setGlobalSearchPath(GRAPHICS_MAGIC_FOLDER);
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
