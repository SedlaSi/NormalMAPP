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

    public static void main(String [] args){
        //convolution(read());
        //normalMap(read());
        //write(sobelEdgeDetector(read("/home/sedlasi1/Desktop/obrazky/small_stones.ppm")),"/home/sedlasi1/Desktop/obrazky/sobel_stones.ppm");
        //write(heightMap(read("/home/sedlasi1/Desktop/obrazky/sphere.ppm")),"/home/sedlasi1/Desktop/obrazky/sphere_thr_2.ppm");
        write(heightMap(read("/home/sedlasi1/Desktop/obrazky/small_stones.ppm")),"/home/sedlasi1/Desktop/obrazky/stones_thr_2.ppm");
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
        byte [] sobel = sobelEdgeDetector(fr);
        byte [] out = Arrays.copyOf(fr,fr.length);
        int min = -1;
        int max = -1;
        int average = -1;
        int offset = -1;
        GrayscaleResultClass res = getGrayscale(sobel);
        min = res.min;
        max = res.max;
        average = res.average;
        offset = res.offset;
        byte [] edges = threshold(res.out,average);

        if(min == -1 || max == -1 || average == -1 || offset == -1) System.out.println("Fault operation");
        System.out.println("min:"+min+" max:"+max+" average:"+average);

        if(edges == null){
            System.out.println("edges field null!!");
            return null;
        }

        byte [] blackWhiteEdges = blackWhiteSummerize(edges,res.collumns,res.rows);
        byte [] distortionMap = drawDistortion(blackWhiteEdges,res.collumns,res.rows);

        for(int i = 0; i < distortionMap.length; i++){
            out[i*3 + offset] = distortionMap[i];
            out[i*3 + offset + 1] = distortionMap[i];
            out[i*3 + offset + 2] = distortionMap[i];
        }

        return out;
    }

    private static byte [] drawDistortion(byte [] fr, int collumns, int rows){
        byte [] out = new byte [fr.length];
        int segmentation = 5;




        return out;
    }

    private static byte [] invert(byte [] fr){
        for(int i = 0; i < fr.length; i++){
            fr[i] = (byte)(255 - (fr[i] & 0xFF));
        }
        return fr;
    }

    private static byte [] blackWhiteSummerize(byte [] fr, int columns, int rows){
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
        if((fr[edge]& 0xFF) == 255){
            fr = invert(fr);
        }
        // ===============


        return fr;
    }

    private static byte [] threshold(byte [] fr,int limit){
        for(int i = 0; i < fr.length; i++){
            if(fr[i] > limit) {
                fr[i] = (byte) 255;
            }
        }
        return fr;
    }

    private static byte [] sobelEdgeDetector(byte [] fr){
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

    private static GrayscaleResultClass getGrayscale(byte [] fr){
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

    private static class GrayscaleResultClass {
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
