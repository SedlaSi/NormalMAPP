package algorithms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by root on 19.7.16.
 */
public class HeightMap {

    public static double NORMAL_HEIGHT = 0.1;

    public static void main(String [] args){
        //convolution(read());
        //normalMap(read());
        write(read("/home/sedlasi1/Desktop/test/house.ppm"),"/home/sedlasi1/Desktop/test/height.ppm");
        //getGrayscale(read());
    }

    public static byte [] read(String path){
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(byte [] picture, String path){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(picture);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte [] heightMap(byte [] fr){
        return null;
    }

}
