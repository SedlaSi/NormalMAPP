package main.java.algorithms;

import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class NormalMap {

    public static void main(String [] args){
        write(normalMap(read()));
    }

    public static byte [] read(){
        String path = "/home/sedlasi1/Desktop/Skola/Semestr_04/APO/Ukol_02/vit_small.ppm";
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(byte [] picture){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("/home/sedlasi1/Desktop/Skola/Semestr_04/output.ppm");
            fos.write(picture);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte [] normalMap(byte [] fr){
        int collumns;
        int rows;
        int off = 3; // offset in array
        byte [] out = Arrays.copyOfRange(fr,0,fr.length);

        int upper, middle, lower;


        System.out.println("P = " + fr[0]);
        System.out.println("6 = " + fr[1]);
        System.out.println("/n = " + fr[2]);
        while(fr[off] != 10) off++;
        int i = 3;
        StringBuilder stb = new StringBuilder();
        while(i < off){
            stb.append((char)fr[i]);
            i++;
        }
        collumns = Integer.parseInt(stb.toString());
        System.out.println("collumns = " + collumns);
        off++;
        i = off;
        while(fr[off] != 10) off++;
        stb = new StringBuilder();
        while(i < off){
            stb.append((char)fr[i]);
            i++;
        }
        rows = Integer.parseInt(stb.toString());
        System.out.println("rows = " + rows);
        System.out.println("/n = " + fr[off]);
        System.out.println("2 = " + fr[off+1]);
        System.out.println("5 = " + fr[off+2]);
        System.out.println("5 = " + fr[off+3]);
        System.out.println("/n = " + fr[off+4]);
        System.out.println("zacatek dat = " + fr[off+5]);//
        System.out.println("zacatek dat = " + fr[off+6]);//    PRVNI PIXEL
        System.out.println("zacatek dat = " + fr[off+7]);//

        off += 5;
        System.out.println("zacatek = "+fr[off]);
        upper = off;
        middle = off + 3*collumns;
        lower = off + 6*collumns;
        System.out.println("upper " + fr[upper] +" "+ fr[upper+1]+" "+fr[upper+2]);
        System.out.println("middle " + fr[middle] +" "+ fr[middle+1]+" "+fr[middle+2]);
        System.out.println("lower " + fr[lower] +" "+ fr[lower+1]+" "+fr[lower+2]);

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
}