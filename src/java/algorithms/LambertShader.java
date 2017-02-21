package algorithms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by root on 20.2.17.
 */
public class LambertShader {
    double lz = 0.5;
    double ly = 0.1;
    double lx = 0.860233;

    double q = 0.9;

    public static void main(String [] args){
        //convolution(read());
        //normalMap(read());
        //write(sobelEdgeDetector(read("/home/sedlasi1/Desktop/obrazky/small_stones.ppm")),"/home/sedlasi1/Desktop/obrazky/sobel_stones.ppm");
        //write(heightMap(read("/home/sedlasi1/Desktop/obrazky/sphere.ppm")),"/home/sedlasi1/Desktop/obrazky/sphere_thr_2.ppm");
        LambertShader heightMap = new LambertShader();
        heightMap.write(heightMap.shader(heightMap.read("/home/sedlasi1/Desktop/obrazky/stones/normal.ppm")),"/home/sedlasi1/Desktop/obrazky/stones/shader.ppm");
        //getGrayscale(read());
    }

    public byte [] read(String path){
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void write(byte [] picture, String path){
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

    // insert normal map
    public byte [] shader(byte [] fr){
        int collumns;
        int rows;
        int off = 3; // offset in array
        byte [] out = Arrays.copyOf(fr,fr.length);

        int upper, middle, lower;

        while(fr[off] != 10) off++;
        int i = 3;
        StringBuilder stb = new StringBuilder();
        while(true){
            if(fr[i] == '#'){
                i++;
                while(fr[i] != '\n') i++;
                while(fr[i] == '\n') i++;
            } else break;

        }
        off = i;
        while(fr[off] != 10 && fr[off] != ' ') off++;
        while(i < off){
            stb.append((char)fr[i]);
            i++;
        }
        collumns = Integer.parseInt(stb.toString());

        off++;
        i = off;
        while(fr[off] != 10 && fr[off] != ' ') off++;
        stb = new StringBuilder();
        while(i < off){
            stb.append((char)fr[i]);
            i++;
        }
        rows = Integer.parseInt(stb.toString());

        off += 5;
        //System.out.println("zacatek = "+fr[off]);
        upper = off;
        middle = off + 3*collumns;
        lower = off + 6*collumns;
        int readen_lines = 3;
        //unsigned char val;
        int valR_x,valR_y;
        int valG_x,valG_y;
        int valB_x,valB_y;

        double val;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        double [] values = new double[fr.length];
        for(i = off; i < fr.length; i+=3){
            val = q * (lx*((double)fr[i])/255.0 + ly*((double)fr[i+1])/255.0 + lz*((double)fr[i+2])/255.0);
            values[i] = val;
            values[i+1] = val;
            values[i+2] = val;
            if(val > max){
                max = val;
            }
            if(val < min){
                min = val;
            }
        }

        double range = max - min;
        /*System.out.println(max);
        System.out.println(min);
        System.out.println((max*255.0));
        System.out.println((min*255.0));*/
        for (i = off; i < fr.length; i+=3){
            out[i] = (byte)(((values[i]+min)/range)*255.0);
            out[i+1] = (byte)(((values[i]+min)/range)*255.0);
            out[i+2] = (byte)(((values[i]+min)/range)*255.0);
        }

        return out;
    }

}
