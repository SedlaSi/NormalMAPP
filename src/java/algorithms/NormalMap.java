package algorithms;

import gui.session.LoadingScreen;
import main.NormalMAPP;
import sun.misc.IOUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class NormalMap implements Algorithm {

    private LoadingScreen loadingScreen;
    private static final int STEPS = 5;

    public static double NORMAL_HEIGHT = 0.1;

    public static void main(String [] args){
        //convolution(read());
        //normalMap(read());
        LoadingScreen loadingScreen = new LoadingScreen() {

            @Override
            public void startLoading(int maximum, boolean visible) {

            }

            @Override
            public void addProgress(int amount) {

            }

            @Override
            public void setText(String text) {

            }

            @Override
            public void stopLoading() {

            }
        };
        NormalMap normalMap = new NormalMap();
        normalMap.setLoadingScreen(loadingScreen);
        normalMap.write(normalMap.normalMap(normalMap.read("/home/sedlasi1/Desktop/obrazky/5415-small.ppm"),90,1),"/home/sedlasi1/Desktop/obrazky/MP_min.ppm");
        //getGrayscale(read());
    }

    public byte [] read(String path){
        try {
            loadingScreen.setText("Loading height maps");
            loadingScreen.addProgress(1);
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void write(byte [] picture, String path){
        FileOutputStream fos = null;
        try {
            loadingScreen.setText("Preparing Normal Maps to open");
            loadingScreen.addProgress(1);
            fos = new FileOutputStream(path);
            fos.write(picture);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte [] getGrayscale(byte[] fr){
        int collumns;
        int rows;
        int off = 3; // offset in array
        byte [] out;

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
        //System.out.println("collumns: "+collumns+" rows: "+rows);
        out = new byte [collumns*rows];
        off += 5;
        int val;
        for(i = 0; i < out.length; i++){
            //val = (int)(0.2126*(fr[i*3 + off] & 0xFF) + 0.7152*(fr[i*3 + 1 + off] & 0xFF) + 0.0722*(fr[i*3 + 2 + off] & 0xFF));
            val = (int)(((fr[i*3 + off] & 0xFF) + (fr[i*3 + 1 + off] & 0xFF) + (fr[i*3 + 2 + off] & 0xFF))/3);
            if(val < 0) val = 0;
            else if (val > 255) val = 255;
            out[i] = (byte) val;
            //System.out.println(out[i]);
        }

        return out;
    }

    public byte [] normalMap(byte [] fr, double angle ,double height){
        loadingScreen.setText("Starting calculation of normals");
        loadingScreen.addProgress(1);
        byte [] gray = getGrayscale(fr);
        byte [] out = Arrays.copyOf(fr,fr.length);

        int collumns;
        int rows;
        int off = 3; // offset in array

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
        loadingScreen.setText("Starting Sobel Operations");
        loadingScreen.addProgress(1);
        int readen_lines = 1;
        double valX;
        double valY;
        //System.out.println("height: "+height);
        double valZ = height;
        double length;

        while(readen_lines < rows-1){
            for(i = 1; i < collumns-1 ; i++){
                valX = -(gray[(readen_lines-1)*collumns + i - 1] & 0xFF) + (gray[(readen_lines-1)*collumns + i + 1] & 0xFF)
                        -2*(gray[readen_lines*collumns + i - 1] & 0xFF) + 2*(gray[readen_lines*collumns + i + 1] & 0xFF)
                        -(gray[(readen_lines+1)*collumns + i - 1] & 0xFF) + (gray[(readen_lines+1)*collumns + i + 1] & 0xFF);
                valY = -(gray[(readen_lines-1)*collumns + i - 1] & 0xFF) - 2*(gray[(readen_lines-1)*collumns + i] & 0xFF) - (gray[(readen_lines-1)*collumns + i + 1] & 0xFF)
                        +(gray[(readen_lines+1)*collumns + i - 1] & 0xFF) + 2*(gray[(readen_lines+1)*collumns + i] & 0xFF) + (gray[(readen_lines+1)*collumns + i + 1] & 0xFF);

                valX = ((valX*Math.cos(angle)) - (valY*Math.sin(angle)))/255.0;
                valY = ((valY*Math.cos(angle)) + (valX*Math.sin(angle)))/255.0;
                /*if(valX < 0) valX = 0;
                else if(valX > 255) valX = 255;
                if(valY < 0) valY = 0;
                else if(valY > 255) valY = 255;*/

                length = Math.sqrt((valX*valX) + (valY*valY) + (valZ*valZ));

                valX = ((valX/length + 1.0) * (255.0/2.0));
                valY = ((valY/length + 1.0) * (255.0/2.0));
                out[off + (readen_lines*collumns*3)+i*3] = (byte)valX;

                out[off + (readen_lines*collumns*3)+1+i*3] = (byte)valY;
                out[off + (readen_lines*collumns*3)+2+i*3] = (byte)((valZ/length + 1.0) * (255.0/2.0));

            }
            readen_lines++;
        }
        loadingScreen.setText("Normals calculated");
        loadingScreen.addProgress(1);
        return out;
    }

    public byte [] convolution(byte [] fr){
        int collumns;
        int rows;
        int off = 3; // offset in array
        byte [] out = Arrays.copyOf(fr,fr.length);

        int upper, middle, lower;


        System.out.println("P = " + fr[0]);
        System.out.println("6 = " + fr[1]);
        System.out.println("/n = " + fr[2]);
        while(fr[off] != 10) off++;
        int i = 3;
        StringBuilder stb = new StringBuilder();
        while(true){
            if(fr[i] == '#'){
                i++;
                while(fr[i] != '\n') i++;
            } else break;
        }
        i++;
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
        /*System.out.println("rows = " + rows);
        System.out.println("/n = " + fr[off]);
        System.out.println("2 = " + fr[off+1]);
        System.out.println("5 = " + fr[off+2]);
        System.out.println("5 = " + fr[off+3]);
        System.out.println("/n = " + fr[off+4]);
        System.out.println("zacatek dat = " + fr[off+5]);//
        System.out.println("zacatek dat = " + fr[off+6]);//    PRVNI PIXEL
        System.out.println("zacatek dat = " + fr[off+7]);//*/

        off += 5;
        //System.out.println("zacatek = "+fr[off]);
        upper = off;
        middle = off + 3*collumns;
        lower = off + 6*collumns;
        /*System.out.println("upper " + fr[upper] +" "+ fr[upper+1]+" "+fr[upper+2]);
        System.out.println("middle " + fr[middle] +" "+ fr[middle+1]+" "+fr[middle+2]);
        System.out.println("lower " + fr[lower] +" "+ fr[lower+1]+" "+fr[lower+2]);*/

        int readen_lines = 3;
        //unsigned char val;
        int valR;
        int valG;
        int valB;

        while(readen_lines <= rows){
		      for(i = 3; i < collumns*3-3 ; i+=3){

                valR = (5*(fr[middle + i]& 0xFF) - (fr[middle + i - 3]& 0xFF) - (fr[middle + i + 3]& 0xFF) - (fr[upper + i]& 0xFF) - (fr[lower + i]& 0xFF)); // R
                //System.out.println("valR: "+valR);
                if(valR > 255){
                    valR = 255;
                } else if (valR < 0){
                    valR = 0;
                }

                out[middle + i] = (byte)valR;

                valG = (5*(fr[middle + i + 1]& 0xFF) - (fr[middle + i - 2]& 0xFF) - (fr[middle + i + 4]& 0xFF) - (fr[upper + i + 1]& 0xFF) - (fr[lower + i + 1]& 0xFF)); // G
                //System.out.println("valG: "+valG);
                if(valG > 255){
                    valG = 255;
                } else if (valG < 0){
                    valG = 0;
                }
                out[middle + i + 1] = (byte)valG;

                valB = (5*(fr[middle + i + 2]& 0xFF) - (fr[middle + i - 1]& 0xFF) - (fr[middle + i + 5]& 0xFF) - (fr[upper + i + 2]& 0xFF) - (fr[lower + i + 2]& 0xFF)); // B
                //System.out.println("valB: "+valB);
                if(valB > 255){
                    valB = 255;
                } else if (valB < 0){
                    valB = 0;
                }
                out[middle + i + 2] = (byte)valB;
            }

            upper = middle;
            middle = lower;
            lower += 3*collumns;
            readen_lines++;
        }

        return out;
    }

    @Override
    public int getSteps() {
        return STEPS;
    }

    @Override
    public void setLoadingScreen(LoadingScreen loadingScreen) {
        this.loadingScreen = loadingScreen;
    }
}
