package algorithms;

import gui.session.LoadingScreen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by root on 19.7.16.
 */
public class HeightMap implements Algorithm {

    private LoadingScreen loadingScreen;
    private static final int STEPS = 13;

    public static void main(String [] args){
        //convolution(read());
        //normalMap(read());
        //write(sobelEdgeDetector(read("/home/sedlasi1/Desktop/obrazky/small_stones.ppm")),"/home/sedlasi1/Desktop/obrazky/sobel_stones.ppm");
        //write(heightMap(read("/home/sedlasi1/Desktop/obrazky/sphere.ppm")),"/home/sedlasi1/Desktop/obrazky/sphere_thr_2.ppm");
        HeightMap heightMap = new HeightMap();
        heightMap.write(heightMap.heightMap(heightMap.read("/home/sedlasi1/Desktop/obrazky/small_stones.ppm")),"/home/sedlasi1/Desktop/obrazky/stones_final_2.ppm");
        heightMap.write(heightMap.heightMap(heightMap.read("/home/sedlasi1/Desktop/obrazky/original.ppm")),"/home/sedlasi1/Desktop/obrazky/stones_final_original.ppm");
        //getGrayscale(read());
    }

    public byte [] read(String path){
        try {
            loadingScreen.setText("Loading Image");
            loadingScreen.addProgress(1);
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void write(byte [] picture, String path){
        FileOutputStream fos = null;
        loadingScreen.setText("Opening Maps");
        loadingScreen.addProgress(1);
        try {
            fos = new FileOutputStream(path);
            fos.write(picture);
            fos.close();
            loadingScreen.addProgress(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public byte [] heightMap(byte [] fr){
        loadingScreen.setText("Calculating Enviroment");
        loadingScreen.addProgress(1);
        byte [] sobel = sobelEdgeDetector(fr);
        loadingScreen.addProgress(1);
        byte [] out = Arrays.copyOf(fr,fr.length);
        loadingScreen.setText("Estimating Shapes");
        loadingScreen.addProgress(1);
        GrayscaleResultClass res = getGrayscale(sobel);
        loadingScreen.addProgress(1);
        byte [] edges = threshold(res.out,res.average);
        loadingScreen.addProgress(1);

        if(edges == null){
            System.out.println("edges field null!!");
            return null;
        }

        byte [] blackWhiteEdges = blackWhiteSummerize(edges,res.collumns,res.rows);
        blackWhiteEdges = invert(blackWhiteEdges);
        loadingScreen.addProgress(1);
        byte [] noNoise = noiseRemoval(blackWhiteEdges,res.collumns,res.rows);
        loadingScreen.addProgress(1);
        byte [] distortionMap = blackBlur(noNoise,res.collumns,res.rows);
        loadingScreen.setText("Calculating height maps");
        loadingScreen.addProgress(1);
        byte [] finalMap = combineEnviromentWithNoise(distortionMap,getGrayscale(fr).out);
        loadingScreen.addProgress(1);
        for(int i = 0; i < finalMap.length; i++){
            /*out[i*3 + offset] = (byte)(0.2126*distortionMap[i]);
            out[i*3 + offset + 1] = (byte)(0.7152*distortionMap[i]);
            out[i*3 + offset + 2] = (byte)(0.0722*distortionMap[i]);*/

            out[i*3 + res.offset] = finalMap[i];
            out[i*3 + res.offset + 1] = finalMap[i];
            out[i*3 + res.offset + 2] = finalMap[i];
        }
        loadingScreen.setText("Height maps calculated");
        loadingScreen.addProgress(1);
        return out;
    }

    private byte [] combineEnviromentWithNoise(byte [] env, byte [] noise){
        byte [] out = new byte[env.length];
        int i = 0;
        /*for(; i < env.length; i++){
            if((env[i]&0xff) < 233){
                env[i] = (byte)((env[i]&0xff) + 100);
            }
            //noise[i] = (byte)((int)((((noise[i]&0xff)*100)/255)));
            //System.out.println(noise[i] + " " + (noise[i]&0xff));
        }*/

        for(i = 0; i < env.length; i++){
            out[i] = (byte)(((noise[i]&0xff)+((env[i]&0xff)/2))/1.5);
        }
        return out;
    }

    private byte [] invert(byte [] fr){
        for(int i = 0; i < fr.length; i++){
            fr[i] = (byte)(255 - (fr[i] & 0xFF));
        }
        return fr;
    }

    private byte [] noiseRemoval(byte [] fr, int columns, int rows){

        for(int i = 2; i < rows-3; i++){
            for(int j = 2; j < columns-3; j++){
                if((fr[i*columns + j] & 0xFF) == 0 && (fr[i*columns + j + 1] & 0xFF) == 0 && (fr[(i+1)*columns + j] & 0xFF) == 0 && (fr[(i+1)*columns + j+1] & 0xFF) == 0){
                    if(((fr[(i-2)*columns + j] & 0xFF) != 0 &&
                            (fr[(i-2)*columns + j+1] & 0xFF) != 0 &&
                            (fr[(i-1)*columns + j] & 0xFF) != 0 &&
                            (fr[(i-1)*columns + j+1] & 0xFF) != 0)
                            &&
                            ((fr[(i+2)*columns + j] & 0xFF) != 0 &&
                                    (fr[(i+2)*columns + j+1] & 0xFF) != 0 &&
                                    (fr[(i+3)*columns + j] & 0xFF) != 0 &&
                                    (fr[(i+3)*columns + j+1] & 0xFF) != 0)
                            &&
                            ((fr[(i)*columns + j-2] & 0xFF) != 0 &&
                                    (fr[(i)*columns + j-1] & 0xFF) != 0 &&
                                    (fr[(i-1)*columns + j-2] & 0xFF) != 0 &&
                                    (fr[(i-1)*columns + j-2] & 0xFF) != 0)
                            &&
                            ((fr[i*columns + j+2] & 0xFF) != 0 &&
                                    (fr[i*columns + j+3] & 0xFF) != 0 &&
                                    (fr[(i-1)*columns + j+2] & 0xFF) != 0 &&
                                    (fr[(i-1)*columns + j+3] & 0xFF) != 0)
                            ){
                        fr[i*columns + j] = (byte)255;
                        fr[i*columns + j+1] = (byte)255;
                        fr[(i+1)*columns + j] = (byte)255;
                        fr[(i+1)*columns + j+1] = (byte)255;
                    }
                }
            }
        }


        return fr;
    }

    private byte [] blackBlur(byte [] fr, int columns, int rows){
        int steps = 150;
        int i = columns + 1;
        int color;
        int restrict = 0;

        while(steps > 0){
             while(i < fr.length - columns - 1){
                 if((fr[i] & 0xFF) > restrict){
                     /*fr[i] = (byte)((int)((fr[i - 1]&0xff + fr[i + 1]&0xff + fr[i + columns]&0xff + fr[i - columns]&0xff + fr[i - columns - 1]&0xff + fr[i - columns + 1]&0xff
                     + fr[i + columns - 1]&0xff + fr[i + columns + 1]&0xff
                     )/8));*/
                     color = (2*(fr[i - 1]&0xff) +
                             2*(fr[i + 1]&0xff) +
                             2*(fr[i + columns]&0xff) +
                             2*(fr[i - columns]&0xff) +
                             (fr[i - columns - 1]&0xff) +
                             (fr[i - columns + 1]&0xff) +
                             (fr[i + columns - 1]&0xff) +
                             (fr[i + columns + 1]&0xff) +
                             4*(fr[i]&0xff)
                     )/16 ;
                     //System.out.println(color);
                     if(color > 250) color = 255;
                     else if(color < 0) color = 0;

                     fr[i] = (byte) color;

                     /*if(
                             fr[i - 1] == black || fr[i + 1] == black
                             || fr[i + columns] == black || fr[i - columns] == black
                             || fr[i - columns - 1] == black || fr[i - columns + 1] == black
                             || fr[i + columns - 1] == black || fr[i + columns + 1] == black
                             ){
                         fr[i] = (byte)(black + increment);
                     }*/
                 }
                 i++;
             }
            restrict += 2;
            i = columns + 1;
            steps--;

        }

        return fr;
    }

    private byte [] blackWhiteSummerize(byte [] fr, int columns, int rows){
        int maskSize = 2;
        int end = fr.length-maskSize-maskSize*rows;
        int avg = 0;
        int i = 0;
        int j;
        int k;
        int curRow = 0;
        int edge = -1;
        while(i < end){
            for(j = 0; j < maskSize; j++){
                for(k = 0; k < maskSize; k++){
                    avg += (fr[i + j + k*columns]& 0xFF);
                    if((fr[i + j + k*columns]& 0xFF) == 255){
                        edge = i + j + k*columns;
                    }
                }
            }
            avg = avg/(maskSize*maskSize);
            if(avg < 255/2) avg = 255;
            else avg = 0;
            for(j = 0; j < maskSize; j++){
                for(k = 0; k < maskSize; k++){
                    fr[i + j + k*columns] = (byte)avg;
                    //System.out.println("d");
                }
            }
            avg = 0;
            if((i + maskSize + 1) >= curRow*columns + columns){
                curRow += maskSize;
                i = curRow*columns;
            } else {
               i += maskSize;
            }
        }


        // POCHYBNE RESENI
        /*if((fr[edge]& 0xFF) == 255){
            fr = invert(fr);
        }*/
        // ===============


        return fr;
    }

    private byte [] threshold(byte [] fr,int limit){
        for(int i = 0; i < fr.length; i++){
            if(fr[i] > limit) {
                fr[i] = (byte) 255;
            }
        }
        return fr;
    }

    private byte [] sobelEdgeDetector(byte [] fr){
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
        //System.out.println("collumns: "+collumns+" rows: "+rows);

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
        int valR_x,valR_y;
        int valG_x,valG_y;
        int valB_x,valB_y;

        while(readen_lines <= rows){
            for(i = 3; i < collumns*3-3 ; i+=3){

                valR_x = -(fr[upper + i - 3]& 0xFF) + (fr[upper + i + 3]& 0xFF) -2*(fr[middle + i - 3]& 0xFF) + 2*(fr[middle + i + 3]& 0xFF) - (fr[lower + i - 3]& 0xFF) + (fr[lower + i + 3]& 0xFF); // R
                //System.out.println("valR: "+valR);
                valR_y = (fr[upper + i - 3]& 0xFF) + 2*(fr[upper + i]& 0xFF) + (fr[upper + i + 3]& 0xFF) - ((fr[lower + i - 3]& 0xFF) + 2*(fr[lower + i]& 0xFF) + (fr[lower + i + 3]& 0xFF)); // R
                //System.out.println("valR: "+valR);
                valR_x =(int) Math.sqrt((double)(valR_x*valR_x + valR_y*valR_y));

                if(valR_x > 255){
                    valR_x = 255;
                } else if (valR_x < 0) {
                    valR_x = 0;
                }
                out[middle + i] = (byte)valR_x;

                valG_x = -(fr[upper + i - 2]& 0xFF) + (fr[upper + i + 4]& 0xFF) -2*(fr[middle + i - 2]& 0xFF) + 2*(fr[middle + i + 4]& 0xFF) - (fr[lower + i - 2]& 0xFF) + (fr[lower + i + 4]& 0xFF); // R
                //System.out.println("valR: "+valR);
                valG_y = (fr[upper + i - 2]& 0xFF) + 2*(fr[upper + i + 1]& 0xFF) + (fr[upper + i + 4]& 0xFF) - ((fr[lower + i - 2]& 0xFF) + 2*(fr[lower + i + 1]& 0xFF) + (fr[lower + i + 4]& 0xFF)); // R
                //System.out.println("valR: "+valR);
                valG_x =(int) Math.sqrt((double)(valG_x*valG_x + valG_y*valG_y));

                if(valG_x > 255){
                    valG_x = 255;
                } else if (valG_x < 0) {
                    valG_x = 0;
                }
                out[middle + i + 1] = (byte)valG_x;

                valB_x = -(fr[upper + i - 1]& 0xFF) + (fr[upper + i + 5]& 0xFF) -2*(fr[middle + i - 1]& 0xFF) + 2*(fr[middle + i + 5]& 0xFF) - (fr[lower + i - 1]& 0xFF) + (fr[lower + i + 5]& 0xFF); // R
                //System.out.println("valR: "+valR);
                valB_y = (fr[upper + i - 1]& 0xFF) + 2*(fr[upper + i + 2]& 0xFF) + (fr[upper + i + 5]& 0xFF) - ((fr[lower + i - 1]& 0xFF) + 2*(fr[lower + i + 2]& 0xFF) + (fr[lower + i + 5]& 0xFF)); // R
                //System.out.println("valR: "+valR);
                valB_x =(int) Math.sqrt((double)(valB_x*valB_x + valB_y*valB_y));

                if(valB_x > 255){
                    valB_x = 255;
                } else if (valB_x < 0){
                    valB_x = 0;
                }
                out[middle + i + 2] = (byte)valB_x;
            }

            upper = middle;
            middle = lower;
            lower += 3*collumns;
            readen_lines++;
        }

        return out;
    }

    private GrayscaleResultClass getGrayscale(byte [] fr){
        int collumns;
        int rows;
        int off; // offset in array
        int min = 255;
        int max = 0;
        int avg;
        byte [] out;
        byte [] count = new byte [256];


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
            val = (int)(0.2126*(fr[i*3 + off] & 0xFF) + 0.7152*(fr[i*3 + 1 + off] & 0xFF) + 0.0722*(fr[i*3 + 2 + off] & 0xFF));
            //val = (int)(((fr[i*3 + off] & 0xFF) + (fr[i*3 + 1 + off] & 0xFF) + (fr[i*3 + 2 + off] & 0xFF))/3);
            if(val < 0) val = 0;
            else if (val > 255) val = 255;
            if(min > val) min = val;
            if(max < val) max = val;
            count[val]++;
            out[i] = (byte) val;
            //System.out.println(out[i]);
        }
        avg = 0;
        for(i = 1; i < 256; i++){ // najde nejvetsi pocet pro dany odstin -- nejfrekventovanejsi barvu
            if(count[i] > count[avg]){
                avg = i;
            }
        }

        return new GrayscaleResultClass(max,min,avg,off,collumns,rows,out);
    }

    @Override
    public int getSteps() {
        return STEPS;
    }

    @Override
    public void setLoadingScreen(LoadingScreen loadingScreen) {
        this.loadingScreen = loadingScreen;
    }

    private class GrayscaleResultClass {
        int max;
        int min;
        int average;
        int offset;
        int collumns;
        int rows;
        byte [] out;

        GrayscaleResultClass(int max, int min, int average, int offset, int collumns, int rows, byte[] out){
            this.max = max;
            this.min = min;
            this.average = average;
            this.offset = offset;
            this.collumns = collumns;
            this.rows = rows;
            this.out = out;
        }
    }

}
