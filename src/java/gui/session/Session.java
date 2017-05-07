package gui.session;

import org.im4java.process.ProcessStarter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by sedlasi1 on 19.7.16.
 *
 * Session class contains information about current session
 * of NormalMAPP. It provides those information to gui.session.ImageLoader.
 *
 * Information:
 *      path to temporary folder on current system -> ROOT_FOLDER
 *      path separator on current system -> SLASH
 *      name of the system -> SYSTEM
 *      path to the Graphics Magic folder -> GRAPHICS_MAGIC_FOLDER
 *      path to current session folder -> sessionFolder
 */
public class Session {

    private static String GRAPHICS_MAGIC_FOLDER = ".\\lib\\GraphicsMagick"; // do not put slash at the end
    private static String ROOT_FOLDER = "/tmp/.NormalMAPP";
    static String SLASH = "/";
    private static String SYSTEM = "UNIX";
    private String sessionFolder;

    // random hash created for current session
    String sessionId;

    /**
     * Edit constructor for other systems compatibility
     */
    public Session() {
        if (System.getProperty("os.name").contains("Windows")) {
            SYSTEM = "WINDOWS";
            ROOT_FOLDER = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Temp\\.NormalMAPP";
            SLASH = "\\";
            loadGraphicsMagick();
        }

        sessionId = Long.toString(System.nanoTime()) + ((int) (Math.random() * 100));
        sessionFolder = ROOT_FOLDER + SLASH + "session_" + sessionId;
        init();
    }

    /**
     * @method loadGraphicsMagick() reads path of Graphics Magick folder
     * from conf.txt file. This method is used only on Microsoft Windows systems.
     */
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

    /**
     * @method init() creates root folder of NormalMAPP
     * application in temporary folder (not created if exists).
     * Then it creates session folder in root folder.
     */
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

    /**
     * @method endSession() starts when the application
     * is about to end, it removes all files from session folder.
     * If NormalMAPP root folder is empty, it will be removed as well.
     */
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
