package algorithms;

import Jama.Matrix;
import gui.sfs.Marker;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by root on 5.11.16.
 */
public class ShapeFromShading implements Algorithm {

    private final static double ROUND_ANGLE = 2.0; //2.0
    private final static double FLAT_ANGLE = 5.0; // 5.0
    //private final static double FLAT_ANGLE = 0;
    private final static double MAX_RELATIVE_HEIGHT = 2.0; // 2.0
    //private final static double MAX_RELATIVE_HEIGHT = Double.MAX_VALUE;
    private int collumns;
    private int rows;
    private byte [] fr;
    private double [] grayscale;
    private List<Marker> markers;
    private double lightX;
    private double lightY;
    private double lightZ;
    private byte [] heightMap;
    private int steps=20;
    private double q = 1;
    private double lm = 0.1;
    int bodyStart;
    private double [] normalField;

    public ShapeFromShading(){
    }



    public void setImage(String path){

        try {
            fr =  Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setSteps(int steps){
        this.steps = steps;
    }

    public void setMarkers(List<Marker> markers){
        this.markers = markers;
    }

    public byte [] shapeFromShading(){
        if(fr == null || markers == null){
            System.out.println("NULL fr or markers");
            return null;
        }

        getGrayscale(); // 2. step
        getLightSource(); // 3. step


        for(int i = 0; i < markers.size(); i++){
            System.out.println(markers.get(i).getX() + ", " + markers.get(i).getY() + ", " + markers.get(i).getZ());
        }
        System.out.println();
        System.out.println("LightX = "+lightX);
        System.out.println("LightY = "+lightY);
        System.out.println("LightZ = "+lightZ);

        /*lightX = 0.8434105885819642;
        lightY = 0.3284961304866877;
        lightZ = 0.42514570599160206;*/

        //normalField = getHeightMap();
        //normalField = getDepthMap2();
        //normalField = getDepthMap3();
        steps = steps*10;
        //steps = 5000;
        //lightX = - lightX;
        //normalField = getDepthMap4(); //
        //normalField = getDepthMapVEC(); // now
        normalField = interpolatedNormalEstimation(); // now
        //normalField = getDepthMap(); // now , pomale
        grayscale = null;

        // VYPISOVANI RELATIVNICH VYSEK
        /*double [] rel = relativeHeights();
        for(int i = bodyStart; i< rel.length-1 ; i+=2){
            fr[(i/2)*3] = (byte)((rel[i+1])*255.0);
            fr[(i/2)*3+1] = (byte)((rel[i+1])*255.0);
            fr[(i/2)*3+2] = (byte)((rel[i+1])*255.0);
        }*/

        /*for(int i = bodyStart; i < normalField.length ; i+=3){
            //fr[i] = (byte)((normalField[i]+1)*127.5);
            fr[i] = (byte)((lightX*255));
            fr[i+1] = (byte)((lightY*255));
            fr[i+2] = (byte)((lightZ*255));
        }*/


        // VYPISOVANI PLOCHYCH NORMAL
        for(int i = bodyStart; i < normalField.length+bodyStart ; i++){
            //fr[i] = (byte)((normalField[i-bodyStart]+1.0)*127.0);
            fr[i] = (byte)((normalField[i-bodyStart]/2+0.5)*255.0);
            //fr[i] = (byte)((normalField[i-bodyStart])*255.0);
        }

        // VYPISOVANI VYSKOVE MAPY
        //finishDepths(relativeHeights());

        // SKUTECNY KONEC
        //absoluteHeightsOld(relativeHeights());
        //absoluteHeights(relativeHeights());
        steps = 5000;
        //absoluteHeightsNEW(relativeHeights()); // opravuje extremy v okrajich --> 18.3.2017 VPORADKU !!!!
        //absoluteHeightsSW(relativeHeights()); // opravuje extremy v okrajich
        //absoluteHeightsQUAD(relativeHeights()); // opravuje extremy v okrajich
        /*if(playGong){
            gong();
        }*/
       /* Marker m;
        int p;
        System.out.println("col "+collumns);
        System.out.println("rows "+rows);
        java.text.DecimalFormat numberFormatter = new DecimalFormat("#0.000000");
        for(int i = 0; i < collumns*rows; i++){
            for(int j = 0; j< markers.size(); j++){
                m = markers.get(j);
                p = ((int)(m.getPosX()*(collumns-1)) + (int)(m.getPosY()*((rows-1)))*(collumns));
                if(p == i){
                    System.out.println("===");
                    System.out.println(p);
                    System.out.println(i);
                    System.out.println("posX: " + numberFormatter.format(m.getPosX()));
                    System.out.println("posY: " + numberFormatter.format(m.getPosY()));
                    System.out.println("X = "+(int)(m.getPosX()*(collumns-1)));
                    System.out.println("Y = "+(int)(m.getPosY()*(rows-1)));
                    fr[3*i + bodyStart] = (byte)m.getX();
                    fr[3*i + bodyStart+1] = (byte)m.getY();
                    fr[3*i + bodyStart+2] = (byte)m.getZ();
                    break;
                } else {
                    fr[3*i + bodyStart] = (byte)0;
                    fr[3*i + bodyStart+1] = (byte)0;
                    fr[3*i + bodyStart+2] = (byte)0;
                }
            }

        }*/


        return fr;
    }

    private double [] interpolatedNormalEstimation(){
        int size = collumns*rows;
        double [] n = new double[3*size];
        //p = ((int)(m.getPosX()*(collumns-1)) + (int)(m.getPosY()*((rows-1)))*(collumns));
        double [] markerLen = new double[markers.size()];
        double xM [] = new double[markers.size()];
        double yM [] = new double[markers.size()];
        Marker m;
        for(int i = 0; i < markers.size(); i++){ // pozice markeru v x a y
            m = markers.get(i);
            xM[i] = (m.getPosX()*(collumns-1));
            yM[i] = (m.getPosY()*((rows-1)));
        }
        double len;
        double x,y,z;
        for(double j = 0; j < rows; j++){
            pointLabel:
            for(double i = 0; i < collumns; i++){
                len = 0.0d;
                x = 0.0d;
                y = 0.0d;
                z = 0.0d;
                for(int k = 0; k < markers.size(); k++){ // vzdalenost markeru k od bodu (i,j)
                    m = markers.get(k);
                    markerLen[k] = Math.sqrt((i-xM[k])*(i-xM[k]) + (j-yM[k])*(j-yM[k]));
                    if(markerLen[k] == 0.0){ // pokud jsme na bode
                        x += (double)m.getX()-127.5;
                        y += (double)m.getY()-127.5;
                        z += (double)m.getZ()-127.5;
                        // normalizace
                        len = Math.sqrt(x*x + y*y + z*z);
                        x /= len;
                        y /= len;
                        z /= len;
                        n[3*(int)(j*collumns+i)] = x;
                        n[3*(int)(j*collumns+i)+1] = y;
                        n[3*(int)(j*collumns+i)+2] = z;
                        continue pointLabel;
                    } else {
                        markerLen[k] = 1 / markerLen[k];
                    }
                }
                // duraz na nejblizsi bod
                /*y = Double.MAX_VALUE; // nejmensi vzdalenost
                z = Double.MAX_VALUE; // druha nejmensi vzdalenost
                // x je index pro y
                // len je index pro z

                for(int k = 0; k < markers.size();k++){
                    if(markerLen[k] < y){
                        x = k;
                        y = markerLen[k];
                    }

                }
                for(int k = 0; k < markers.size(); k++){
                    if(markerLen[k] < z && markerLen[k] != y){
                        len = k;
                        z = markerLen[k];
                    }
                }
                //v x mame index nejblizsiho bodu;
                //markerLen[(int)x] *= 2;
                //markerLen[(int)len] *= 2;
                len = 0;
                x = 0;
                y = 0;
                z = 0;*/

                for(int k = 0; k < markers.size(); k++){ // vypocet delky len
                    len += markerLen[k];
                }
                for(int k = 0; k < markers.size(); k++){ // vypocet interpolace
                    m = markers.get(k);
                    x += (double)(m.getX()-127.5) * (markerLen[k]/len);
                    y += (double)(m.getY()-127.5) * (markerLen[k]/len);
                    z += (double)(m.getZ()-127.5) * (markerLen[k]/len);
                }
                // normalizace
                len = Math.sqrt(x*x + y*y + z*z);
                x /= len;
                y /= len;
                z /= len;

                n[3*(int)(j*collumns+i)] = x;
                n[3*(int)(j*collumns+i)+1] = y;
                n[3*(int)(j*collumns+i)+2] = z;
            }
        }
        return n;
    }

    // nepouziva se
    private void finishDepths(double[] rel) { // prepocteni relativnich vysek do height mapy a zapsani do fr
        int size = collumns*rows;
        double [] absolute = new double[size];
        double [] absoluteInv = new double[size];
        double [] absoluteOrto = new double[size];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double minInv = Double.MAX_VALUE;
        double maxInv = Double.MIN_VALUE;
        /*double avg = 0;
        double rel_max = Double.MIN_VALUE;
        double rel_min = Double.MAX_VALUE;
        for(int i = 0; i < rel.length; i++){
            avg += rel[i];
            if(rel[i] > rel_max) rel_max = rel[i];
            if(rel[i] < rel_min) rel_min = rel[i];
        }
        System.out.println("average rel height: "+avg/rel.length);
        System.out.println("REL MAX: "+rel_max);
        System.out.println("REL MIN: "+rel_min);*/

        absolute[size-1] = 0;
        for(int i = size-2; i > (size-collumns-1); i--){ // posledni radek
            absolute[i] = absolute[i+1] + rel[2*i];
            if(absolute[i] < min){
                min = absolute[i];
            }
            if(absolute[i] > max){
                max = absolute[i];
            }
        }

        absoluteInv[0] = 0;
        for(int i = 1; i < collumns-1; i++){ // prvni radek INV
            absoluteInv[i] = absoluteInv[i-1] + rel[2*(i-1)];
            if(absoluteInv[i] < minInv){
                minInv = absoluteInv[i];
            }
            if(absoluteInv[i] > maxInv){
                maxInv = absoluteInv[i];
            }
        }

        absoluteOrto[size-collumns] = 0;
        for(int i = (size-collumns+1); i < size; i++){ // posledni radek ORTO
            absoluteOrto[i] = absoluteOrto[i-1] + rel[2*(i-1)];
            /*if(absoluteOrto[i] < min){
                min = absoluteOrto[i];
            }
            if(absoluteOrto[i] > max){
                max = absoluteOrto[i];
            }*/
        }

        for(int i = (size - collumns - 1); i >= collumns; i-= collumns){ // posledni sloupec
            absolute[i] = absolute[i+collumns] + rel[2*i];
            if(absolute[i] < min){
                min = absolute[i];
            }
            if(absolute[i] > max){
                max = absolute[i];
            }
        }

        for(int i = collumns; i <= size-collumns; i+= collumns){ // prvni sloupec INV
            absoluteInv[i] = absoluteInv[i-collumns] + rel[2*(i-collumns+1)];
            if(absoluteInv[i] < minInv){
                minInv = absoluteInv[i];
            }
            if(absoluteInv[i] > maxInv){
                maxInv = absoluteInv[i];
            }
        }

        for(int i = size-2*collumns; i >= 0; i-= collumns){ // prvni sloupec ORTO
            absoluteOrto[i] = absoluteOrto[i+collumns] + rel[2*(i+collumns+1)];
            /*if(absoluteOrto[i] < minInv){
                minInv = absoluteOrto[i];
            }
            if(absoluteOrto[i] > maxInv){
                maxInv = absoluteOrto[i];
            }*/
        }

        // absolutni vysky v doublech
        for(int i = (size-collumns-2); i >= 0; i-=collumns){ //radky
            for(int j = 0 ; j <= collumns-2; j++){ // bunky v radcich
                absolute[i-j] = ((absolute[i-j+1] + rel[2*(i-j)]) + (absolute[i-j+collumns] + rel[2*(i-j) + 1]))/2;
                //absolute[i-j] = (absolute[i-j+1] + rel[2*(i-j)]);
                if(absolute[i-j] < min){
                    min = absolute[i-j];
                }
                if(absolute[i-j] > max){
                    max = absolute[i-j];
                }
            }
        }

        for(int i = collumns + 1; i < size-collumns+1; i+=collumns){ //radky INV
            for(int j = 0 ; j < collumns-1; j++){ // bunky v radcich INV
                absoluteInv[i+j] = ((absoluteInv[i+j-1] + rel[2*(i+j-1)]) + (absoluteInv[i+j-collumns] + rel[2*(i+j-collumns) + 1]))/2;
                //absoluteInv[i+j] = (absoluteInv[i+j-1] - rel[2*(i+j-1)]);
                if(absoluteInv[i+j] < minInv){
                    minInv = absoluteInv[i+j];
                }
                if(absoluteInv[i+j] > maxInv){
                    maxInv = absoluteInv[i+j];
                }
            }
        }

        for(int i = (size-2*collumns+1); i >= 1; i-=collumns){ //radky ORTO
            for(int j = 0 ; j < collumns-1; j++){ // bunky v radcich ORTO
                absoluteOrto[i+j] = ((absoluteOrto[i+j-1] + rel[2*(i+j-1)]) + (absoluteOrto[i+j+collumns] + rel[2*(i+j-1) + 1]))/2;
                //absoluteOrto[i+j] = (absoluteOrto[i+j-1] + rel[2*(i+j-1)]);
                /*if(absoluteOrto[i+j] < min){
                    min = absoluteOrto[i+j];
                }
                if(absoluteOrto[i+j] > max){
                    max = absoluteOrto[i+j];
                }*/
            }
        }


        for(int i = 0; i < absolute.length; i++){
            /*if(absolute[i] > absoluteInv[i]){
                absolute[i] = absoluteInv[i];
            }*/
            //absolute[i] = (absolute[i] + absoluteInv[i])/2;
            absolute[i] = (absolute[i] + absoluteOrto[i] + absoluteInv[i])/3;
            //absolute[i] = (absoluteOrto[i] + absoluteInv[i])/2;
        }


        double range = Math.abs(max) + Math.abs(min);
        double rangeInv = Math.abs(maxInv) + Math.abs(minInv);
        byte value;
        min = Math.abs(min);
        minInv = Math.abs(minInv);
        System.out.println("Range: "+range);
        System.out.println("Min: -"+min);
        System.out.println("Max: "+max);
        for(int i = 0; i < size; i++){
            value = (byte)((((absolute[i]+min)/range))*255);
            //value = (byte)((((absoluteInv[i]+minInv)/rangeInv))*255);
            //value = (byte)((((absoluteOrto[i]+min)/range))*255);
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

    }

    private void absoluteHeights(double [] q){
        int size = collumns*rows;
        double [] h = new double[size];
        double h1,h2,h3,h4;
        double height;
        boolean eq = true;
        steps = 1000;
    /*  h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
        h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
        h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
        h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
     */


        for(int gauss = steps; gauss > 0; gauss--){


            // projizdeni horizontalne

            //prvek vlevo nahore
            h[0] = (h[1] + q[0] + h[collumns] + q[1])/2.0;
            //HORNI RADKA
            for(int i  = 1; i < collumns; i++){
                h[i] = (h[i-1] - q[2*(i-1)] + h[i + collumns] + (q[2*(i) + 1]))/2.0;
            }

            // TELO
            for(int j = collumns; j < size; j+= collumns){
                if(eq){ // projizdime zprava doleva <-----
                    //okrajovy prvek vpravo
                    h[j + collumns - 1] = (h[(j + collumns - 1) - collumns] - q[2*((j + collumns - 1)-collumns)+1] + h[(j + collumns - 1) - 1] - q[2*((j + collumns - 1) - 1)])/2.0;
                    for(int i = collumns-2; i >= 0; i--){
                        h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)]) + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                        //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    }
                } else { // zleva doprava ------>
                    h[j] = (h[(j) + 1] + (q[2*(j)]) + h[(j) - collumns] - q[2*((j)-collumns)+1])/2.0;
                    for(int i = 1; i < collumns; i++){
                        h[j+i] = (h[(j + i) - 1] - q[2*((j + i) - 1)] + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                        //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    }
                }
                eq = !eq;
            }
            eq = true;

            // projizdeni vertikalne

    /*  h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
        h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
        h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
        h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
     */

            //prvek vlevo nahore
            /*h[0] = (h[1] + q[0] + h[collumns] + q[1])/2.0;
            //LEVY SLOUPEC
            for(int i  = collumns; i < size; i+=collumns){
                h[i] = (h[(i) + 1] + (q[2*(i)]) + h[(i) - collumns] - q[2*((i)-collumns)+1])/2.0;
            }

            // TELO
            for(int j = 1; j < collumns; j++){ // strida sloupce
                if(eq){ // projizdime zprava doleva <-----
                    //okrajovy prvek dole
                    h[j + size - collumns] = (h[(j + size - collumns) - 1] - q[2*((j + size - collumns) - 1)] + h[(j + size - collumns) - collumns] - q[2*((j + size - collumns)-collumns)+1])/2.0;

                    for(int i = size - 2*collumns; i >= 0; i-=collumns){ // projizdi sloupec zezdola nahoru
                        h[j+i] = (h[(j + i) + collumns] + (q[2*(j + i) + 1]) + h[(j + i) - 1] - q[2*((j + i) - 1)])/2.0;
                    }
                } else { // zleva doprava ------>
                    h[j] = (h[(j) + collumns] + (q[2*(j) + 1]) + h[(j) - 1] - q[2*((j) - 1)])/2.0;
                    for(int i = collumns; i < size; i+=collumns){ // projizdi sloupec zezhora dolu
                        h[j+i] = (h[(j + i) - 1] - q[2*((j + i) - 1)] + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    }
                }
                eq = !eq;
            }*/



            eq = true;


        }


        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for(int i = 0; i < size; i++){

            if(h[i] > max){
                max = h[i];
            }
            if(h[i] < min){
                min = h[i];
            }
        }

        if(min < 0){
            if(max < 0){
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }
        /*System.out.println(formatter.format(min));
        System.out.println(formatter.format(max));
        System.out.println(formatter.format(range));
        System.out.println();
        System.out.println("round COLOR");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print((int)(((h[collumns*i+j]-min)/range)*255.0)+" ");
            }
            System.out.println(" ||");
        }*/

        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((h[i]-min)/range)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }
        NormalMap normalMap = new NormalMap();
        this.write(normalMap.normalMap(fr,180.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");
    }

    private void absoluteHeightsQUAD(double [] q){
        int size = collumns*rows;
        double [] h = new double[size];
        double h1,h2,h3,h4;
        double height;
        boolean eq = true;
        //steps = 100;
    /*  h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
        h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
        h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
        h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
     */

        // ESTIMACE NA ZACATKU

        //prvek vlevo nahore
        /*h[0] = (h[1] + q[0] + h[collumns] + q[1])/2.0;
        //HORNI RADKA
        for(int i  = 1; i < collumns; i++){
            h[i] = (h[i-1] - q[2*(i-1)] + h[i + collumns] + (q[2*(i) + 1]))/2.0;
        }

        // TELO
        for(int j = collumns; j < size; j+= collumns){
            if(eq){ // projizdime zprava doleva <-----
                //okrajovy prvek vpravo
                h[j + collumns - 1] = (h[(j + collumns - 1) - collumns] - q[2*((j + collumns - 1)-collumns)+1] + h[(j + collumns - 1) - 1] - q[2*((j + collumns - 1) - 1)])/2.0;
                for(int i = collumns-2; i >= 0; i--){
                    h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)]) + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                }
            } else { // zleva doprava ------>
                h[j] = (h[(j) + 1] + (q[2*(j)]) + h[(j) - collumns] - q[2*((j)-collumns)+1])/2.0;
                for(int i = 1; i < collumns; i++){
                    h[j+i] = (h[(j + i) - 1] - q[2*((j + i) - 1)] + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                }
            }
            eq = !eq;
        }
        eq = true;*/

        for(int gauss = steps; gauss > 0; gauss--){


            // projizdeni horizontalne

            //prvek vlevo nahore
            h[0] = (h[1] + q[0] + h[collumns] + q[1])/2.0;
            //HORNI RADKA
            for(int i  = 1; i < collumns-1; i++){
                h[i] = (h[i-1] - q[2*(i-1)] + h[i + collumns] + (q[2*(i) + 1]) + h[(i) + 1] + (q[2*(i)]))/3.0;
            }
            //prvek vpravo nahore
            h[collumns-1] = (h[collumns-1-1] - q[2*(collumns-1-1)] + h[collumns-1 + collumns] + (q[2*(collumns-1) + 1]))/2.0;

            // TELO
            for(int j = collumns; j < size-collumns; j+= collumns){
                if(eq){ // projizdime zprava doleva <-----
                    //okrajovy prvek vpravo
                    h[j + collumns - 1] = (h[(j + collumns - 1) - collumns] - q[2*((j + collumns - 1)-collumns)+1] + h[(j + collumns - 1) - 1] - q[2*((j + collumns - 1) - 1)] + h[(j + collumns - 1) + collumns] + (q[2*(j + collumns - 1) + 1]))/3.0;
                    for(int i = collumns-2; i > 0; i--){
                        //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)]) + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                        h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/4.0;
                    }
                    //okrajovy prvek vlevo
                    h[j] = (h[(j) + 1] + (q[2*(j)]) + h[(j) - collumns] - q[2*((j)-collumns)+1] + h[(j) + collumns] + (q[2*(j) + 1]))/3.0;
                } else { // zleva doprava ------>
                    h[j] = (h[(j) + 1] + (q[2*(j)]) + h[(j) - collumns] - q[2*((j)-collumns)+1] + h[(j) + collumns] + (q[2*(j) + 1]))/3.0;
                    for(int i = 1; i < collumns-1; i++){
                        //h[j+i] = (h[(j + i) - 1] - q[2*((j + i) - 1)] + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                        h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/4.0;
                    }
                    h[j + collumns - 1] = (h[(j + collumns - 1) - collumns] - q[2*((j + collumns - 1)-collumns)+1] + h[(j + collumns - 1) - 1] - q[2*((j + collumns - 1) - 1)] + h[(j + collumns - 1) + collumns] + (q[2*(j + collumns - 1) + 1]))/3.0;
                }
                eq = !eq;
            }

    /*  h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
        h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
        h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
        h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
     */

            if(eq){ // projizdime doleva <-----
                //prvek vpravo dole
                h[size-1] = (h[(size-1) - 1] - q[2*((size-1) - 1)] + h[(size-1) - collumns] - q[2*((size-1)-collumns)+1])/2.0;
                //SPODNI RADKA
                for(int i  = size-2; i > size-collumns; i--){
                    h[i] = (h[(i) + 1] + (q[2*(i)]) + h[(i) - 1] - q[2*((i) - 1)] + h[(i) - collumns] - q[2*((i)-collumns)+1])/3.0;
                }
                //prvek vlevo dole
                h[size-collumns] = (h[(size-collumns) + 1] + (q[2*(size-collumns)]) + h[(size-collumns) - collumns] - q[2*((size-collumns)-collumns)+1])/2.0;


            } else { // projizdime doprava ----->
                //prvek vlevo dole
                h[size-collumns] = (h[(size-collumns) + 1] + (q[2*(size-collumns)]) + h[(size-collumns) - collumns] - q[2*((size-collumns)-collumns)+1])/2.0;
                //SPODNI RADKA
                for(int i  = size-collumns+1; i < size-1; i++){
                    h[i] = (h[(i) + 1] + (q[2*(i)]) + h[(i) - 1] - q[2*((i) - 1)] + h[(i) - collumns] - q[2*((i)-collumns)+1])/3.0;
                }
                //prvek vpravo dole
                h[size-1] = (h[(size-1) - 1] - q[2*((size-1) - 1)] + h[(size-1) - collumns] - q[2*((size-1)-collumns)+1])/2.0;
            }

            eq = true;

            //opisovani okraju
            // HORNI RADKA
            //levy horni roh
            h[0] = h[1+collumns];
            //horni radka
            for(int i = 1; i < collumns-1; i++){
                h[i] = h[i+collumns];
            }
            // pravy horni roh
            h[collumns-1] = h[collumns-2+collumns];
            // SPODNI RADKA
            //levy spodni roh
            h[(size - collumns)] = h[(size - 2*collumns+1)];
            //spodni radek
            for(int i = size-collumns+1; i < size-1; i++){
                h[i] = h[i-collumns];
            }
            //pravy spodni roh
            h[size-1] = h[(size-2)-collumns];

            //boky
            //TELO
            for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
                //levy prvek
                h[j] = h[j+1];
                //pravy prvek
                h[(j + collumns-1)] = h[(j + collumns-2)];
            }
        }


        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for(int i = 0; i < size; i++){

            if(h[i] > max){
                max = h[i];
            }
            if(h[i] < min){
                min = h[i];
            }
        }

        if(min < 0){
            if(max < 0){
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }
        /*System.out.println(formatter.format(min));
        System.out.println(formatter.format(max));
        System.out.println(formatter.format(range));
        System.out.println();
        System.out.println("round COLOR");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print((int)(((h[collumns*i+j]-min)/range)*255.0)+" ");
            }
            System.out.println(" ||");
        }*/

        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((h[i]-min)/range)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }
        NormalMap normalMap = new NormalMap();
        this.write(normalMap.normalMap(fr,180.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");
    }

    private void absoluteHeightsNEW(double [] q){
        int size = collumns*rows;
        double [] h = new double[size];
        double h1,h2,h3,h4;
        double height;
        /*for(int i = 0; i < h.length; i++){
            h[i] = 0.5;
        }*/

        // ESTIMACE NA ZACATKU
        /*boolean eq = true;
        //prvek vlevo nahore
        h[0] = (h[1] + q[0] + h[collumns] + q[1])/2.0;
        //HORNI RADKA
        for(int i  = 1; i < collumns; i++){
            h[i] = (h[i-1] - q[2*(i-1)] + h[i + collumns] + (q[2*(i) + 1]))/2.0;
        }

        // TELO
        for(int j = collumns; j < size; j+= collumns){
            if(eq){ // projizdime zprava doleva <-----
                //okrajovy prvek vpravo
                h[j + collumns - 1] = (h[(j + collumns - 1) - collumns] - q[2*((j + collumns - 1)-collumns)+1] + h[(j + collumns - 1) - 1] - q[2*((j + collumns - 1) - 1)])/2.0;
                for(int i = collumns-2; i >= 0; i--){
                    h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)]) + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                }
            } else { // zleva doprava ------>
                h[j] = (h[(j) + 1] + (q[2*(j)]) + h[(j) - collumns] - q[2*((j)-collumns)+1])/2.0;
                for(int i = 1; i < collumns; i++){
                    h[j+i] = (h[(j + i) - 1] - q[2*((j + i) - 1)] + h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                    //h[j+i] = (h[(j + i) + 1] + (q[2*(j + i)])+h[(j + i) + collumns] + (q[2*(j + i) + 1])+h[(j + i) - 1] - q[2*((j + i) - 1)]+h[(j + i) - collumns] - q[2*((j + i)-collumns)+1])/2.0;
                }
            }
            eq = !eq;
        }
        eq = true;*/






        /*System.out.println();
        NumberFormat formatter = new DecimalFormat("#0.0000");
        System.out.println("RELATIVE HEIGHTS:");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print(formatter.format(q[2*(collumns*i+j)])+" ; "+formatter.format(q[2*(collumns*i+j)+1])+"|");
            }
            System.out.println(" ||");
        }*/

        int mod = 0;
        double [] buffer = new double [2*collumns];
        for(int gauss = steps; gauss >= 0; gauss--){ // LOOP GAUSS-SEIDEL
            // HORNI RADKA
            //System.out.println("1");
            //levy horni roh
            //h[0] = height;

            //buffer[mod] = height;
            buffer[mod] = h[1+collumns];
            mod++;
            /*if(mod == 2*collumns){
                mod = 0;
            }*/



            //horni radka
            for(int i = 1; i < collumns-1; i++){
                //h[i] = height;



                //buffer[mod] = height;
                buffer[mod] = h[i+collumns];
                mod++;
                /*if(mod == 2*collumns){
                    mod = 0;
                }*/
            }
            // pravy horni roh
            //h[collumns-1] = height;


            //buffer[mod] = height;
            buffer[mod] = h[collumns-2+collumns];
            mod++;
            /*if(mod == 2*collumns){
                mod = 0;
            }*/

            //TELO
            for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
                //levy prvek
                //h[j] = height;


                if((j - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(j - 2*collumns)] = buffer[mod];
                }
                //buffer[mod] = height;
                buffer[mod] = h[j+1];
                mod++;
                /*if(mod == 2*collumns){
                    mod = 0;
                }*/
                int nei;
                double EDGE_SHARPNESS = 0.1;
                double MAX_HEIGHT = 0.5;

                for(int i = 1; i < collumns-1; i++){ // projizdime bunky v radcich
                    // novy
                    /*height = 0;
                    nei = 0;
                    if(q[2*(j + i)] > EDGE_SHARPNESS && q[2*(j + i)] < (1-EDGE_SHARPNESS)){
                        nei++;
                        height += h[(j + i) + 1] + q[2*(j + i)];
                        /*mod++;
                        continue;*/
                    //}
                    /*if(q[2*(j + i) + 1] > EDGE_SHARPNESS && q[2*(j + i) + 1] < (1-EDGE_SHARPNESS)){
                        nei++;
                        height += h[(j + i) + collumns] + q[2*(j + i) + 1];
                        /*mod++;
                        continue;*/
                    //}
                    /*if(q[2*((j + i)-1)] > EDGE_SHARPNESS && q[2*((j + i)-1)] < (1-EDGE_SHARPNESS)){
                        nei++;
                        height += h[(j + i)-1] - q[2*((j + i)-1)];
                        /*mod++;
                        continue;*/
                    /*}
                    if(q[2*((j + i)-collumns)+1] > EDGE_SHARPNESS && q[2*((j + i)-collumns)+1] < (1-EDGE_SHARPNESS)){
                        nei++;
                        height += h[(j + i) - collumns] - q[2*((j + i)-collumns)+1];
                        /*mod++;
                        continue;*/
                    /*}
                    if(nei > 0){
                        height = height/nei;
                    } else {
                        //height = h[(j + i) + 1];
                    }*/
                    //puvodni
                    h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
                    h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                    h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                    h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                    height = (h1+h2+h3+h4)/4.0;
                    //height = (h3+h4)/2.0;
                    //height = (h2+h4)/2.0;
                    // + 2.0
                    /*if(height > MAX_HEIGHT){
                        height = MAX_HEIGHT;
                    } else if(height < -MAX_HEIGHT){
                        height = -MAX_HEIGHT;
                    }*/
                    //h[(j + i)] = height;


                    if((j+i - 2*collumns) >= 0){ // pokud jsme v poli
                        h[(j+i - 2*collumns)] = buffer[mod];
                    }
                    buffer[mod] = height;
                    mod++;
                    /*if(mod == 2*collumns){
                        mod = 0;
                    }*/
                }
                //pravy prvek
                //h[(j + collumns-1)] = height;


                if((j + collumns-1 - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(j + collumns-1 - 2*collumns)] = buffer[mod];
                }
                //buffer[mod] = height;
                buffer[mod] = h[(j + collumns-2)];
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }


            }

            // SPODNI RADKA
            //levy spodni roh
            //h[(size - collumns)] = height;


            if((size - collumns - 2*collumns) >= 0){ // pokud jsme v poli
                h[(size - collumns - 2*collumns)] = buffer[mod];
            }
            //buffer[mod] = height;
            buffer[mod] = h[(size - 2*collumns+1)];
            mod++;
            /*if(mod == 2*collumns){
                mod = 0;
            }*/
            //spodni radek
            for(int i = size-collumns+1; i < size-1; i++){
                //h[i] = height;


                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(i - 2*collumns)] = buffer[mod];
                }
                //buffer[mod] = height;
                buffer[mod] = h[i-collumns];
                mod++;
                /*if(mod == 2*collumns){
                    mod = 0;
                }*/
            }
            //System.out.println("6");

            //pravy spodni roh
            //h[size-1] = height;


            if((size-1 - 2*collumns) >= 0){ // pokud jsme v poli
                h[(size-1 - 2*collumns)] = buffer[mod];
            }

            //buffer[mod] = height;
            buffer[mod] = h[(size-2)-collumns];
            mod++;
            if(mod == 2*collumns){
                mod = 0;
            }
            for(int i = (size - 2*collumns); i < size; i++){
                h[i] = buffer[mod];
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }
            }

            /**
             *
             */
            // HORNI RADKA
            //levy horni roh
            h[0] = h[1+collumns];
            //horni radka
            for(int i = 1; i < collumns-1; i++){
               h[i] = h[i+collumns];
            }
            // pravy horni roh
            h[collumns-1] = h[collumns-2+collumns];
            // SPODNI RADKA
            //levy spodni roh
            h[(size - collumns)] = h[(size - 2*collumns+1)];
            //spodni radek
            for(int i = size-collumns+1; i < size-1; i++){
                h[i] = h[i-collumns];
            }
            //pravy spodni roh
            h[size-1] = h[(size-2)-collumns];

            //boky
            //TELO
            for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
                //levy prvek
                h[j] = h[j+1];
                //pravy prvek
                h[(j + collumns-1)] = h[(j + collumns-2)];
            }



            //System.out.println("l");
            mod = 0;
            //System.out.println(gauss);
            //TISK POLE
            /*System.out.println();
            System.out.println("round "+(steps-gauss));
            for(int i = 0; i < rows; i++){
                System.out.print("|| ");
                for(int j = 0; j < collumns; j++){
                    System.out.print(formatter.format(h[collumns*i+j])+" ");
                }
                System.out.println(" ||");
            }*/


        }

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for(int i = 0; i < size; i++){

            if(h[i] > max){
                max = h[i];
            }
            if(h[i] < min){
                min = h[i];
            }
        }

        if(min < 0){
            if(max < 0){
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }
        /*System.out.println(formatter.format(min));
        System.out.println(formatter.format(max));
        System.out.println(formatter.format(range));
        System.out.println();
        System.out.println("round COLOR");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print((int)(((h[collumns*i+j]-min)/range)*255.0)+" ");
            }
            System.out.println(" ||");
        }*/

        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((h[i]-min)/range)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

    }

    private void absoluteHeightsAF(double [] q){
        int size = collumns*rows;
        double [] h = new double[size];
        double [] hA = new double[size];
        double [] hB = new double[size];
        double [] hC = new double[size];
        double [] hD = new double[size];
        double h1,h2,h3,h4;
        double height;
        steps = 500;
        boolean pff = false;

        /*//TELO -> prvky vlevo
        for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
            for(int i = 1; i < collumns-1; i++){ // projizdime bunky v radcich
                //puvodni
                h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
                h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                //height = (h1+h2+h3+h4)/4.0;
                height = (h3+h4)/2.0;
                h[(j + i)] = h4;

            }

        }*/

        for(int gauss = steps; gauss >= 0; gauss--){ // LOOP GAUSS-SEIDEL
            //TELO -> prvky vlevo
            for(int j = collumns; j < size - 2*collumns; j+=collumns){ // projizdime radky
                //prvek vlevo -> koukam dolu
                h[j+1] = h[(j + 1) + collumns] + (q[2*(j + 1) + 1]); //+

                //zbytek radku -> koukam doleva
                for(int i = 2; i < collumns-1; i++){ // projizdime bunky v radcich
                    //puvodni
                    h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
                    h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                    h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                    h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                    //height = (h1+h2+h3+h4)/4.0;
                    height = (h3+h4)/2.0;
                    h[(j + i)] = h3;

                }

            }

            //posledni prvel vlevo -> koukam nahoru
            h[size-2*collumns+1] = h[(size-2*collumns+1) - collumns] - q[2*((size-2*collumns+1)-collumns)+1]; //-


            //zbytek radku -> koukam doleva
            for(int i = size-2*collumns+1; i < size-collumns-1; i++){ // projizdime bunky v radcich
                //puvodni
                h1 = h[(i) + 1] + (q[2*(i)]); //+
                h2 = h[(i) + collumns] + (q[2*(i) + 1]); //+
                h3 = h[(i) - 1] - q[2*((i) - 1)]; //-
                h4 = h[(i) - collumns] - q[2*((i)-collumns)+1]; //-
                //height = (h1+h2+h3+h4)/4.0;
                height = (h3+h4)/2.0;
                h[(i)] = h3;

            }




            //TELO -> prvky nahore

            //prvni radek -> koukam doprava
            for(int i = 1; i < collumns-2; i++){ // projizdime bunky v radcich
                //puvodni
                h1 = h[(collumns + i) + 1] + (q[2*(collumns + i)]); //+
                h2 = h[(collumns + i) + collumns] + (q[2*(collumns + i) + 1]); //+
                h3 = h[(collumns + i) - 1] - q[2*((collumns + i) - 1)]; //-
                h4 = h[(collumns + i) - collumns] - q[2*((collumns + i)-collumns)+1]; //-
                //height = (h1+h2+h3+h4)/4.0;
                height = (h3+h4)/2.0;
                h[(collumns + i)] = h1;

            }
            //posledni prvek -> koukam doleva
            h[collumns-2] = h[(collumns + collumns-2) - 1] - q[2*((collumns + collumns-2) - 1)]; //-
            //h[collumns-2] = h[(collumns-2) + collumns] + (q[2*(collumns-2) + 1]); //+

            //zbytek koukam nahoru
            for(int j = 2*collumns; j < size - collumns; j+=collumns){ // projizdime radky
                for(int i = 1; i < collumns-1; i++){ // projizdime bunky v radcich
                    //puvodni
                    h1 = h[(j + i) + 1] + (q[2*(j + i)]); //+
                    h2 = h[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                    h3 = h[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                    h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                    //height = (h1+h2+h3+h4)/4.0;
                    height = (h3+h4)/2.0;
                    h[(j + i)] = h4;

                }

            }


        }

        /**
         * DOPISOVANI OKRAJU
         */
        // HORNI RADKA
        //levy horni roh
        h[0] = h[1+collumns];
        //horni radka
        for(int i = 1; i < collumns-1; i++){
            h[i] = h[i+collumns];
        }
        // pravy horni roh
        h[collumns-1] = h[collumns-2+collumns];
        // SPODNI RADKA
        //levy spodni roh
        h[(size - collumns)] = h[(size - 2*collumns+1)];
        //spodni radek
        for(int i = size-collumns+1; i < size-1; i++){
            h[i] = h[i-collumns];
        }
        //pravy spodni roh
        h[size-1] = h[(size-2)-collumns];

        //boky
        //TELO
        for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
            //levy prvek
            h[j] = h[j+1];
            //pravy prvek
            h[(j + collumns-1)] = h[(j + collumns-2)];
        }

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for(int i = 0; i < size; i++){

            if(h[i] > max){
                max = h[i];
            }
            if(h[i] < min){
                min = h[i];
            }
        }

        if(min < 0){
            if(max < 0){
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }

        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((h[i]-min)/range)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        NormalMap normalMap = new NormalMap();
        this.write(normalMap.normalMap(fr,180.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");

    }

    private void absoluteHeightsSW(double [] q){
        int size = collumns*rows;
        double [] hA = new double[size];
        double [] hB = new double[size];
        double [] hC = new double[size];
        double [] hD = new double[size];
        double h1,h2,h3,h4;
        double height;


        // vse bez okraju, okraje nakonec
        // hA levy horni roh
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = 0; i < collumns-2; i++){
                h3 = hA[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                h4 = hA[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                height = (h3+h4)/2.0;
                hA[(j + i)] = height;
            }
        }

        // hB pravy horni roh
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = collumns-3; i > -1; i--){
                h1 = hB[(j + i) + 1] + (q[2*(j + i)]); //+
                h4 = hB[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                height = (h1+h4)/2.0;
                hB[(j + i)] = height;
            }
        }

        // hC levy spodni roh
        for(int j = size-2*collumns+1; j > 1; j-=collumns){
            for(int i = 0; i < collumns-2; i++){
                h3 = hC[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                h2 = hC[(j + i) + collumns] + (q[2*(j + i) + 1]); //-
                height = (h3+h2)/2.0;
                hC[(j + i)] = height;
            }
        }

        // hD pravy spodni roh
        for(int j = size-2*collumns+1; j > 1; j-=collumns){
            for(int i = collumns-3; i > -1; i--){
                h1 = hD[(j + i) + 1] + (q[2*(j + i)]); //-
                h2 = hD[(j + i) + collumns] + (q[2*(j + i) + 1]); //-
                height = (h1+h2)/2.0;
                hD[(j + i)] = height;
            }
        }

        //prumerovani
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = 0; i < collumns-2; i++){
                //height = (hA[i+j]+hB[i+j]+hC[i+j]+hD[i+j])/4.0;
                //height = (hA[i+j]+hD[i+j])/2.0;
                /*hA[(j + i)] = height;
                hB[(j + i)] = height;
                hC[(j + i)] = height;
                hD[(j + i)] = height;*/

            }
        }
        // dopisovani okraju
        hA[0] = hA[collumns+1];
        hB[0] = hA[collumns+1];
        hC[0] = hA[collumns+1];
        hD[0] = hA[collumns+1];

        // horni radka
        for(int i = 1; i < collumns-1; i++){
            hA[i] = hA[i+collumns];
            hB[i] = hA[i+collumns];
            hC[i] = hA[i+collumns];
            hD[i] = hA[i+collumns];
        }

        hA[collumns-1] = hA[collumns-1 -1 + collumns];
        hB[collumns-1] = hA[collumns-1 -1 + collumns];
        hC[collumns-1] = hA[collumns-1 -1 + collumns];
        hD[collumns-1] = hA[collumns-1 -1 + collumns];

        //boky
        for(int j = collumns; j > size - collumns; j+=collumns){
            hA[j] = hA[j+1];
            hB[j] = hA[j+1];
            hC[j] = hA[j+1];
            hD[j] = hA[j+1];

            hA[j+collumns-1] = hA[j+collumns-2];
            hB[j+collumns-1] = hA[j+collumns-2];
            hC[j+collumns-1] = hA[j+collumns-2];
            hD[j+collumns-1] = hA[j+collumns-2];
        }

        hA[size - collumns] = hA[size-2*collumns];
        hB[size - collumns] = hA[size-2*collumns];
        hC[size - collumns] = hA[size-2*collumns];
        hD[size - collumns] = hA[size-2*collumns];

        //spodni radka
        for(int i = size - collumns+1; i < size-1; i++){
            hA[i] = hA[i-collumns];
            hB[i] = hA[i-collumns];
            hC[i] = hA[i-collumns];
            hD[i] = hA[i-collumns];
        }

        hA[size-1] = hA[size-2];
        hB[size-1] = hA[size-2];
        hC[size-1] = hA[size-2];
        hD[size-1] = hA[size-2];

        double [] buffer = new double [2*(collumns-2)];
        int mod = 0;
        for(int gauss = steps; gauss > 0; gauss--){
            //TELO
            if(steps > -1 )break; // BREAK
            for(int j = collumns+1; j < size - collumns+1; j+=collumns){ // projizdime radky

                for(int i = 0; i < collumns-2; i++){ // projizdime bunky v radcich
                    //puvodni
                    h1 = hA[(j + i) + 1] + (q[2*(j + i)]); //+
                    h2 = hA[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                    h3 = hA[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                    h4 = hA[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                    height = (h1+h2+h3+h4)/4.0;
                    //height = (h1+h4)/2.0;
                    //height = (h2+h4)/2.0;
                    if((j+i)-2*collumns > 1){ // jsme v poli
                        hA[j+i-2*collumns] = buffer[mod];
                    }
                    //hA[(j + i)] = height;
                    buffer[mod] = height;
                    mod++;
                    if(mod == buffer.length){
                        mod = 0;
                    }
                }


            }
            //vyprazdneni bufferu
            for(int i = size-3*collumns+1; i < size-collumns-1; i++){
                hA[i] = buffer[mod];
                mod++;
                if(mod == buffer.length){
                    mod = 0;
                }
            }

            // dopisovani okraju
            hA[0] = hA[collumns+1];

            // horni radka
            for(int i = 1; i < collumns-1; i++){
                hA[i] = hA[i+collumns];
            }

            hA[collumns-1] = hA[collumns-1 -1 + collumns];

            //boky
            for(int j = collumns; j > size - collumns; j+=collumns){
                hA[j] = hA[j+1];

                hA[j+collumns-1] = hA[j+collumns-2];
            }

            hA[size - collumns] = hA[size-2*collumns];

            //spodni radka
            for(int i = size - collumns+1; i < size-1; i++){
                hA[i] = hA[i-collumns];
            }

            hA[size-1] = hA[size-2];
        }


        double maxA = -Double.MAX_VALUE;
        double maxB = -Double.MAX_VALUE;
        double maxC = -Double.MAX_VALUE;
        double maxD = -Double.MAX_VALUE;
        double minA = Double.MAX_VALUE;
        double minB = Double.MAX_VALUE;
        double minC = Double.MAX_VALUE;
        double minD = Double.MAX_VALUE;
        double rangeA;
        double rangeB;
        double rangeC;
        double rangeD;
        for(int i = 0; i < size; i++){

            if(hA[i] > maxA){
                maxA = hA[i];
            }
            if(hA[i] < minA){
                minA = hA[i];
            }
            /*if(hB[i] > maxB){
                maxB = hB[i];
            }
            if(hB[i] < minB){
                minB = hB[i];
            }
            if(hC[i] > maxC){
                maxC = hC[i];
            }
            if(hC[i] < minC){
                minC = hC[i];
            }
            if(hD[i] > maxD){
                maxD = hD[i];
            }
            if(hD[i] < minD){
                minD = hD[i];
            }*/
        }

        if(minA < 0){
            if(maxA < 0){
                rangeA = Math.abs(minA) + maxA;
            } else {
                rangeA = maxA - minA;
            }
        } else {
            rangeA = maxA - minA;
        }

        /*if(minB < 0){
            if(maxB < 0){
                rangeB = Math.abs(minB) + maxB;
            } else {
                rangeB = maxB - minB;
            }
        } else {
            rangeB = maxB - minB;
        }

        if(minC < 0){
            if(maxC < 0){
                rangeC = Math.abs(minC) + maxC;
            } else {
                rangeC = maxC - minC;
            }
        } else {
            rangeC = maxC - minC;
        }

        if(minD < 0){
            if(maxD < 0){
                rangeD = Math.abs(minD) + maxD;
            } else {
                rangeD = maxD - minD;
            }
        } else {
            rangeD = maxD - minD;
        }*/


        /*System.out.println(formatter.format(min));
        System.out.println(formatter.format(max));
        System.out.println(formatter.format(range));
        System.out.println();
        System.out.println("round COLOR");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print((int)(((h[collumns*i+j]-min)/range)*255.0)+" ");
            }
            System.out.println(" ||");
        }*/

        NormalMap normalMap = new NormalMap();
        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hA[i]-minA)/rangeA)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }
        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");

        /*for(int i = 0; i < size; i++){
            value = (byte)((int)(((hB[i]-minB)/rangeB)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/B.ppm");

        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hC[i]-minC)/rangeC)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/C.ppm");

        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hD[i]-minD)/rangeD)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/D.ppm");
*/
    }

    private void absoluteHeightsSC(double [] q){
        int size = collumns*rows;
        double [] hA = new double[size];
        double [] hB = new double[size];
        double [] hC = new double[size];
        double [] hD = new double[size];
        double h1,h2,h3,h4;
        double height;


        // vse bez okraju, okraje nakonec
        // hA levy horni roh
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = 0; i < collumns-2; i++){
                h3 = hA[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                h4 = hA[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                height = (h3+h4)/2.0;
                hA[(j + i)] = height;
                //hA[(j + i)] = h3;
            }
        }

        // hB pravy horni roh
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = 0; i < collumns-2; i++){
                h3 = hB[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                h4 = hB[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                //height = (h3+h4)/2.0;
                //hB[(j + i)] = height;
                hB[(j + i)] = h4;
            }
        }

        // hC levy spodni roh
        for(int j = size-2*collumns+1; j > 1; j-=collumns){
            for(int i = collumns-3; i > -1; i--){
                h1 = hC[(j + i) + 1] + (q[2*(j + i)]); //-
                h2 = hC[(j + i) + collumns] + (q[2*(j + i) + 1]); //-
                height = (h1+h2)/2.0;
                //hC[(j + i)] = height;
                hC[(j + i)] = h1;
            }
        }

        // hD pravy spodni roh
        for(int j = size-2*collumns+1; j > 1; j-=collumns){
            for(int i = collumns-3; i > -1; i--){
                h1 = hD[(j + i) + 1] + (q[2*(j + i)]); //-
                h2 = hD[(j + i) + collumns] + (q[2*(j + i) + 1]); //-
                height = (h1+h2)/2.0;
                //hD[(j + i)] = height;
                hD[(j + i)] = h2;
            }
        }

        //prumerovani
        for(int j = collumns+1; j < size-collumns; j+=collumns){
            for(int i = 0; i < collumns-2; i++){
                //height = (hA[i+j]+hB[i+j]+hC[i+j]+hD[i+j])/4.0;
                height = (hA[i+j]+hD[i+j])/2.0;
                hA[(j + i)] = height;
                hB[(j + i)] = height;
                hC[(j + i)] = height;
                hD[(j + i)] = height;

            }
        }



        // dopisovani okraju
        hA[0] = hA[collumns+1];
        hB[0] = hA[collumns+1];
        hC[0] = hA[collumns+1];
        hD[0] = hA[collumns+1];

        // horni radka
        for(int i = 1; i < collumns-1; i++){
            hA[i] = hA[i+collumns];
            hB[i] = hA[i+collumns];
            hC[i] = hA[i+collumns];
            hD[i] = hA[i+collumns];
        }

        hA[collumns-1] = hA[collumns-1 -1 + collumns];
        hB[collumns-1] = hA[collumns-1 -1 + collumns];
        hC[collumns-1] = hA[collumns-1 -1 + collumns];
        hD[collumns-1] = hA[collumns-1 -1 + collumns];

        //boky
        for(int j = collumns; j > size - collumns; j+=collumns){
            hA[j] = hA[j+1];
            hB[j] = hA[j+1];
            hC[j] = hA[j+1];
            hD[j] = hA[j+1];

            hA[j+collumns-1] = hA[j+collumns-2];
            hB[j+collumns-1] = hA[j+collumns-2];
            hC[j+collumns-1] = hA[j+collumns-2];
            hD[j+collumns-1] = hA[j+collumns-2];
        }

        hA[size - collumns] = hA[size-2*collumns];
        hB[size - collumns] = hA[size-2*collumns];
        hC[size - collumns] = hA[size-2*collumns];
        hD[size - collumns] = hA[size-2*collumns];

        //spodni radka
        for(int i = size - collumns+1; i < size-1; i++){
            hA[i] = hA[i-collumns];
            hB[i] = hA[i-collumns];
            hC[i] = hA[i-collumns];
            hD[i] = hA[i-collumns];
        }

        hA[size-1] = hA[size-2];
        hB[size-1] = hA[size-2];
        hC[size-1] = hA[size-2];
        hD[size-1] = hA[size-2];

        /*for(int i = 0 ; i < size; i++){
            hA[i] = Math.abs(q[2*i]) + Math.abs(q[2*i+1]);
        }*/


        double [] buffer = new double [2*(collumns-2)];
        int mod = 0;
        for(int gauss = steps; gauss > 0; gauss--){
            //TELO
            //if(steps > -1 )break; // BREAK
            for(int j = collumns+1; j < size - collumns+1; j+=collumns){ // projizdime radky

                for(int i = 0; i < collumns-2; i++){ // projizdime bunky v radcich
                    //puvodni
                    h1 = hA[(j + i) + 1] + (q[2*(j + i)]); //+
                    h2 = hA[(j + i) + collumns] + (q[2*(j + i) + 1]); //+
                    h3 = hA[(j + i) - 1] - q[2*((j + i) - 1)]; //-
                    h4 = hA[(j + i) - collumns] - q[2*((j + i)-collumns)+1]; //-
                    height = (h1+h2+h3+h4)/4.0;
                    //height = (h1+h4)/2.0;
                    //height = (h2+h4)/2.0;
                    if((j+i)-2*collumns > 1){ // jsme v poli
                        hA[j+i-2*collumns] = buffer[mod];
                    }
                    //hA[(j + i)] = height;
                    buffer[mod] = height;
                    mod++;
                    if(mod == buffer.length){
                        mod = 0;
                    }
                }


            }
            //vyprazdneni bufferu
            for(int i = size-3*collumns+1; i < size-collumns-1; i++){
                hA[i] = buffer[mod];
                mod++;
                if(mod == buffer.length){
                    mod = 0;
                }
            }

            // dopisovani okraju
            hA[0] = hA[collumns+1];

            // horni radka
            for(int i = 1; i < collumns-1; i++){
                hA[i] = hA[i+collumns];
            }

            hA[collumns-1] = hA[collumns-1 -1 + collumns];

            //boky
            for(int j = collumns; j > size - collumns; j+=collumns){
                hA[j] = hA[j+1];

                hA[j+collumns-1] = hA[j+collumns-2];
            }

            hA[size - collumns] = hA[size-2*collumns];

            //spodni radka
            for(int i = size - collumns+1; i < size-1; i++){
                hA[i] = hA[i-collumns];
            }

            hA[size-1] = hA[size-2];
        }


        double maxA = -Double.MAX_VALUE;
        double maxB = -Double.MAX_VALUE;
        double maxC = -Double.MAX_VALUE;
        double maxD = -Double.MAX_VALUE;
        double minA = Double.MAX_VALUE;
        double minB = Double.MAX_VALUE;
        double minC = Double.MAX_VALUE;
        double minD = Double.MAX_VALUE;
        double rangeA;
        double rangeB;
        double rangeC;
        double rangeD;
        for(int i = 0; i < size; i++){

            if(hA[i] > maxA){
                maxA = hA[i];
            }
            if(hA[i] < minA){
                minA = hA[i];
            }
            if(hB[i] > maxB){
                maxB = hB[i];
            }
            if(hB[i] < minB){
                minB = hB[i];
            }
            if(hC[i] > maxC){
                maxC = hC[i];
            }
            if(hC[i] < minC){
                minC = hC[i];
            }
            if(hD[i] > maxD){
                maxD = hD[i];
            }
            if(hD[i] < minD){
                minD = hD[i];
            }
        }

        if(minA < 0){
            if(maxA < 0){
                rangeA = Math.abs(minA) + maxA;
            } else {
                rangeA = maxA - minA;
            }
        } else {
            rangeA = maxA - minA;
        }

        if(minB < 0){
            if(maxB < 0){
                rangeB = Math.abs(minB) + maxB;
            } else {
                rangeB = maxB - minB;
            }
        } else {
            rangeB = maxB - minB;
        }

        if(minC < 0){
            if(maxC < 0){
                rangeC = Math.abs(minC) + maxC;
            } else {
                rangeC = maxC - minC;
            }
        } else {
            rangeC = maxC - minC;
        }

        if(minD < 0){
            if(maxD < 0){
                rangeD = Math.abs(minD) + maxD;
            } else {
                rangeD = maxD - minD;
            }
        } else {
            rangeD = maxD - minD;
        }


        /*System.out.println(formatter.format(min));
        System.out.println(formatter.format(max));
        System.out.println(formatter.format(range));
        System.out.println();
        System.out.println("round COLOR");
        for(int i = 0; i < rows; i++){
            System.out.print("|| ");
            for(int j = 0; j < collumns; j++){
                System.out.print((int)(((h[collumns*i+j]-min)/range)*255.0)+" ");
            }
            System.out.println(" ||");
        }*/

        NormalMap normalMap = new NormalMap();
        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hA[i]-minA)/rangeA)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }
        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");

       /* for(int i = 0; i < size; i++){
            value = (byte)((int)(((hB[i]-minB)/rangeB)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/B.ppm");

        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hC[i]-minC)/rangeC)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/C.ppm");

        for(int i = 0; i < size; i++){
            value = (byte)((int)(((hD[i]-minD)/rangeD)*255.0));
            //value = (byte)((int)(h[i])*255);
            //value = (byte)(255 - (value & 0xFF)); // invert
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }

        this.write(normalMap.normalMap(fr,0.0,0.05),"/home/sedlasi1/Desktop/testovaci_obrazky/D.ppm");*/

    }

    private byte [] invert(byte [] fr){
        int off = 3; // offset in array

        while(fr[off] != 10) off++;
        int i = 3;
        while(true){
            if(fr[i] == '#'){
                i++;
                while(fr[i] != '\n') i++;
                while(fr[i] == '\n') i++;
            } else break;

        }
        off = i;
        while(fr[off] != 10 && fr[off] != ' ') off++;

        off++;
        while(fr[off] != 10 && fr[off] != ' ') off++;
        off += 5;

        for(i = off; i < fr.length; i++){
            fr[i] = (byte)(255 - (fr[i] & 0xFF));
        }
        return fr;
    }

    private void absoluteHeightsOld(double [] q){
        int size = collumns*rows;
        double [] h = new double[size];
        double h1,h2,h3,h4;
        double height;

        int mod = 0;
        double [] buffer = new double [2*collumns];
        for(int gauss = steps; gauss >= 0; gauss--){ // LOOP GAUSS-SEIDEL
            // HORNI RADKA
            //System.out.println("1");
            //levy horni roh
            h1 = h[1] + q[0];
            h2 = h[collumns] + q[1];

            height = (h1+h2)/2;
            h[0] = height;

            /*buffer[mod] = height;
            mod++;
            if(mod == 2*collumns){
                mod = 0;
            }*/



            //horni radka
            for(int i = 1; i < collumns-1; i++){
                h1 = h[i+1] + q[2*i];
                h2 = h[i+collumns] + q[2*i+1];
                h3 = h[i-1] - q[2*(i-1)];

                height = (h1 + h2 + h3)/3;
                h[i] = height;



                /*buffer[mod] = height;
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }*/
            }
            // pravy horni roh
            h2 = h[collumns-1+collumns] + q[2*(collumns-1)+1];
            h3 = h[collumns-1-1] - q[2*(collumns-1-1)];

            height = (h2 + h3)/2;
            h[collumns-1] = height;


            /*buffer[mod] = height;
            mod++;
            if(mod == 2*collumns){
                mod = 0;
            }*/

            //TELO
            for(int j = collumns; j <= (rows-2)*collumns; j+=collumns){ // projizdime radky
                //levy prvek
                h1 = h[j + 1] + q[2*j];
                h2 = h[j + collumns] + q[2*j + 1];
                h4 = h[j - collumns] - q[2*(j-collumns)+1];
                height = (h1 + h2 + h4)/3;
                h[j] = height;


                /*if((j - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(j - 2*collumns)] = buffer[mod];
                }
                buffer[mod] = height;
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }*/
                for(int i = 1; i < collumns-1; i++){ // projizdime bunky v radcich
                    h1 = h[(j + i) + 1] + q[2*(j + i)];
                    h2 = h[(j + i) + collumns] + q[2*(j + i) + 1];
                    h3 = h[(j + i)-1] - q[2*((j + i)-1)];
                    h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1];

                    height = (h1 + h2 + h3 + h4)/4;
                    h[(j + i)] = height;


                    /*if((j+i - 2*collumns) >= 0){ // pokud jsme v poli
                        h[(j+i - 2*collumns)] = buffer[mod];
                    }
                    buffer[mod] = height;
                    mod++;
                    if(mod == 2*collumns){
                        mod = 0;
                    }*/
                }
                //pravy prvek
                h2 = h[(j + collumns-1) + collumns] + q[2*(j + collumns-1) + 1];
                h3 = h[(j + collumns-1)-1] - q[2*((j + collumns-1)-1)];
                h4 = h[(j + collumns-1) - collumns] - q[2*((j + collumns-1)-collumns)+1];
                height = (h2 + h3 + h4)/3;
                h[(j + collumns-1)] = height;


                /*if((j + collumns-1 - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(j + collumns-1 - 2*collumns)] = buffer[mod];
                }
                buffer[mod] = height;
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }*/


            }
            // SPODNI RADKA
            //levy spodni roh
            h1 = h[(size - collumns)+1] + q[2*(size - collumns)];
            h4 = h[(size - collumns) - collumns] - q[2*((size - collumns)-collumns)+1];

            height = (h1 + h4)/2;
            h[(size - collumns)] = height;


            /*if((size - collumns - 2*collumns) >= 0){ // pokud jsme v poli
                h[(size - collumns - 2*collumns)] = buffer[mod];
            }
            buffer[mod] = height;
            mod++;
            if(mod == 2*collumns){
                mod = 0;
            }*/
            //spodni radek
            for(int i = size-collumns+1; i < size-1; i++){
                h1 = h[i+1] + q[2*i];
                h3 = h[i-1] - q[2*(i-1)];
                h4 = h[i - collumns] - q[2*(i-collumns)+1];

                height = (h1 + h3 + h4)/3;
                h[i] = height;


                /*if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    h[(i - 2*collumns)] = buffer[mod];
                }
                buffer[mod] = height;
                mod++;
                if(mod == 2*collumns){
                    mod = 0;
                }*/
            }
            //System.out.println("6");

            //pravy spodni roh
            h3 = h[(size-1)-1] - q[2*((size-1)-1)];
            h4 = h[(size-1) - collumns] - q[2*((size-1)-collumns)+1];

            height = (h3 + h4)/2;
            h[size-1] = height;


            /*if((size-1 - 2*collumns) >= 0){ // pokud jsme v poli
                h[(size-1 - 2*collumns)] = buffer[mod];
            }*/

            /*buffer[mod] = height;
            mod++;
            if(mod == 2*collumns){
                mod = 0;
            }*/

            mod = 0;
            /*for(int i = (size - 2*collumns); i < size; i++){
                h[i] = buffer[mod];
                mod++;
            }*/
            //System.out.println("l");
            mod = 0;
            //System.out.println(gauss);

        }
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for(int i = 0; i < size; i++){

            if(h[i] > max){
                max = h[i];
            }
            if(h[i] < min){
                min = h[i];
            }
        }
        min = Math.abs(min);
        range = min + Math.abs(max);

        byte value;
        for(int i = 0; i < size; i++){
            value = (byte)(((h[i]+min)/range)*255);
            //System.out.println(value);
            fr[3*i + bodyStart] = value;
            fr[3*i + bodyStart + 1] = value;
            fr[3*i + bodyStart + 2] = value;
        }
    }

    private double [] relativeHeights(){
        int size = collumns*rows;
        double [] relativeHeights = new double [2*collumns*rows];
        double x_1,x_2,z_1,z_2,y_1,y_2;
        double a,b,c,d;
        double qij;
        double A,B,h_1,h_2,beta,alpha;
        double aEq,bEq,cEq;
        for(int i = (size-collumns-2); i >= 0; i--){ // stred
            x_1 = normalField[3*i];
            x_2 = normalField[3*i + 3];
            z_1 = normalField[3*i + 2];
            z_2 = normalField[3*i + 3 + 2];
            // Rovnice pro (x_1,z_1)  a (x_2,z_2)
            a = x_1;
            b = z_1;
            c = x_2;
            d = z_2;

            if(a == 0){
                a += Double.MIN_VALUE;
            }
            if(c == 0){
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b/a);
            alpha = Math.toDegrees(alpha);
            if(a < 0.0){
                alpha += 180d;
            }
            beta = Math.atan(d/c);
            beta = Math.toDegrees(beta);
            if(c < 0){
                beta += 180d;
            }
            if(beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)){ // mame podobne uhly
                if(alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if((180d-alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a/b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b/a)*c + d;

                if(cEq != 0){ // kvadraticka rovnice
                    A = (b/a)*c + d;
                    B = c - (b/a)*d;

                    h_1 = B/(-A) + Math.sqrt(B*B + A*A)/(-A);
                    h_2 = B/(-A) - Math.sqrt(B*B + A*A)/(-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b/a)) / ((b/a)*c - d);
                alpha = (beta*c + 1) / a;

                if(beta*alpha > 0){
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // ========
            if(qij < -1*MAX_RELATIVE_HEIGHT){
                qij = -1*MAX_RELATIVE_HEIGHT;
            } else if(qij > MAX_RELATIVE_HEIGHT){
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2*i] = qij;

            z_2 = normalField[3*i + 3*collumns + 2];
            y_1 = normalField[3*i + 1];
            y_2 = normalField[3*i + 3*collumns + 1];

            // Rovnice pro (y_1,z_1)  a (y_2,z_2)
            a = y_1;
            b = z_1;
            c = y_2;
            d = z_2;

            if(a == 0){
                a += Double.MIN_VALUE;
            }
            if(c == 0){
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b/a);
            alpha = Math.toDegrees(alpha);
            if(a < 0.0){
                alpha += 180d;
            }
            beta = Math.atan(d/c);
            beta = Math.toDegrees(beta);
            if(c < 0){
                beta += 180d;
            }
            if(beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)){ // mame podobne uhly
                if(alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if((180d-alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                }  else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a/b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b/a)*c + d;

                if(cEq != 0){ // kvadraticka rovnice
                    A = (b/a)*c + d;
                    B = c - (b/a)*d;

                    h_1 = B/(-A) + Math.sqrt(B*B + A*A)/(-A);
                    h_2 = B/(-A) - Math.sqrt(B*B + A*A)/(-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b/a)) / ((b/a)*c - d);
                alpha = (beta*c + 1) / a;

                if(beta*alpha > 0){
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if(qij < -1*MAX_RELATIVE_HEIGHT){
                qij = -1*MAX_RELATIVE_HEIGHT;
            } else if(qij > MAX_RELATIVE_HEIGHT){
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2*i + 1] = qij;
        }
        for(int i = size-2; i > (size-collumns-1); i--){ //spodni radka
            x_1 = normalField[3*i];
            x_2 = normalField[3*i + 3];
            z_1 = normalField[3*i + 2];
            z_2 = normalField[3*i + 3 + 2];

            // Rovnice pro (x_1,z_1)  a (x_2,z_2)
            a = x_1;
            b = z_1;
            c = x_2;
            d = z_2;

            if(a == 0){
                a += Double.MIN_VALUE;
            }
            if(c == 0){
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b/a);
            alpha = Math.toDegrees(alpha);
            if(a < 0.0){
                alpha += 180d;
            }
            beta = Math.atan(d/c);
            beta = Math.toDegrees(beta);
            if(c < 0){
                beta += 180d;
            }
            /*System.out.println("aplha "+alpha);
            System.out.println("beta "+beta);*/
            if(beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)){ // mame podobne uhly
                if(alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if((180d-alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                }  else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a/b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b/a)*c + d;
                if(cEq != 0){ // kvadraticka rovnice
                    A = (b/a)*c + d;
                    B = c - (b/a)*d;
                    {} // jenom zalozka
                    h_1 = B/(-A) + Math.sqrt(B*B + A*A)/(-A);
                    h_2 = B/(-A) - Math.sqrt(B*B + A*A)/(-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                /*System.out.println("h1 "+h_1);
                System.out.println("h2 "+h_2);*/
                beta = (h_1 - (b/a)) / ((b/a)*c - d);
                alpha = (beta*c + 1) / a;

                if(beta*alpha > 0){
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if(qij < -1*MAX_RELATIVE_HEIGHT){
                qij = -1*MAX_RELATIVE_HEIGHT;
            } else if(qij > MAX_RELATIVE_HEIGHT){
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2*i] = qij;
        }
        for(int i = (size - collumns - 1); i >= collumns; i-= collumns){ //pravy sloupec
            z_1 = normalField[3*i + 2];
            z_2 = normalField[3*i + 3*collumns + 2];
            y_1 = normalField[3*i + 1];
            y_2 = normalField[3*i + 3*collumns + 1];
            // Rovnice pro (y_1,z_1)  a (y_2,z_2)
            a = y_1;
            b = z_1;
            c = y_2;
            d = z_2;

            if(a == 0){
                a += Double.MIN_VALUE;
            }
            if(c == 0){
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b/a);
            alpha = Math.toDegrees(alpha);
            if(a < 0.0){
                alpha += 180d;
            }
            beta = Math.atan(d/c);
            beta = Math.toDegrees(beta);
            if(c < 0){
                beta += 180d;
            }
            if(beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)){ // mame podobne uhly
                if(alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if((180d-alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                }  else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a/b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b/a)*c + d;

                if(cEq != 0){ // kvadraticka rovnice
                    A = (b/a)*c + d;
                    B = c - (b/a)*d;

                    h_1 = B/(-A) + Math.sqrt(B*B + A*A)/(-A);
                    h_2 = B/(-A) - Math.sqrt(B*B + A*A)/(-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b/a)) / ((b/a)*c - d);
                alpha = (beta*c + 1) / a;

                if(beta*alpha > 0){
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if(qij < -1*MAX_RELATIVE_HEIGHT){
                qij = -1*MAX_RELATIVE_HEIGHT;
            } else if(qij > MAX_RELATIVE_HEIGHT){
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2*i+1] = qij;
        }
        normalField = null;



        // normalizace
        double range;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for(int i = 0; i < relativeHeights.length; i++ ){
            if(relativeHeights[i] < min){
                min = relativeHeights[i];
            }
            if(relativeHeights[i] > max){
                max = relativeHeights[i];
            }
        }
        /*if(min < 0){
            range = Math.abs(min) + Math.abs(max);
        } else {
            range = Math.abs(max) - min;
        }*/
        if(min < 0){
            if(max < 0){
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }

        for(int i = 0; i < relativeHeights.length; i++ ){
            relativeHeights[i] = (((relativeHeights[i]-min)/range)-0.5)*2;
        }

        //otoceni do spravnehos smeru
        for(int i = 0; i < size; i++){
            relativeHeights[2*i+1] = -relativeHeights[2*i+1];
        }


        return relativeHeights;
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

    public byte [] read(String path){
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String [] args){
        ShapeFromShading sfs=  new ShapeFromShading();
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/cl_koule.ppm");
        sfs.fr = sfs.read("/home/sedlasi1/Desktop/GG.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/B_2.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/testovaci_obrazky/7364-normal.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/testovaci_obrazky/psfixnormal.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/KK.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/testovaci_obrazky/center.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/testovaci_obrazky/7259d9158be0b7e8c62c887fac57ed81.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/testovaci_obrazky/7063-normal.ppm");
        //sfs.fr = sfs.read("/home/sedlasi1/Desktop/heightmap.ppm");
        sfs.lightX = -0.6907345851123583;
        sfs.lightY = 0.7215316076253877;
        sfs.lightZ = -0.0477270586479161;
        sfs.q = 1.0;
        sfs.lm = 0.05;
        sfs.steps = 25;
        sfs.getGrayscale();
        //sfs.normalField = sfs.getDepthMap3();
        sfs.normalField = sfs.getDepthMap3();
        /*List<Marker> markerList = new ArrayList<>();
        Rectangle r1 = new Rectangle(352,335,20,20);
        Rectangle r2 = new Rectangle(767,344,20,20);
        Rectangle r3 = new Rectangle(577,187,20,20);
        Marker m1 = new Marker("15# Marker",127, 194, 235, 0.8318181818181818, 0.1707070707070707);
        Marker m2 = new Marker("16# Marker",73, 87, 235, 0.16515151515151516, 0.8313131313131313);
        Marker m3 = new Marker("18# Marker",185, 161, 235, 0.46414141414141413, 0.5262626262626262);
        m1.setSquare(r1);
        m2.setSquare(r2);
        m3.setSquare(r3);
        markerList.add(m1);
        markerList.add(m2);
        markerList.add(m3);
        sfs.setMarkers(markerList);
        sfs.lightX = 0.7101113214284756 ;
        sfs.lightY = -0.7040884279537893;
        sfs.lightZ = -0.0011818632180444913;*/
        /*sfs.lightX = 0.25;
        sfs.lightY = 0.25;
        sfs.lightZ = 0.5;
        sfs.normalField = sfs.getDepthMap();*/
        //sfs.normalField = sfs.getHeightMap();
        // VYPISOVANI PLOCHYCH NORMAL
        /*for(int i = sfs.bodyStart; i < sfs.normalField.length+sfs.bodyStart ; i++){
            //fr[i] = (byte)((normalField[i]+1)*127.5);
            sfs.fr[i] = (byte)((sfs.normalField[i-sfs.bodyStart])*255.0);
        }*/
        double x,y,z;
        int start = sfs.bodyStart;
        byte [] fr = sfs.fr;

        for(int i = 0; i < sfs.collumns* sfs.rows; i++){
            x  = (double)(fr[start + 3*i] & 0xFF)/255.0*2.0-1.0;
            y  = (double)(fr[start + 3*i+1] & 0xFF)/255.0*2.0-1.0;
            z  = (double)(fr[start + 3*i+2] & 0xFF)/255.0*2.0-1.0;
            sfs.normalField[3*i] = x;
            sfs.normalField[3*i+1] = y;
            sfs.normalField[3*i+2] = z;
        }
        /*double [] rel = sfs.relativeHeights();
        for(int i = 0; i < rel.length-1 ; i+=2){
            fr[3*(i/2) + start] = (byte)(int)((rel[i])*127.5 + 127.5);
            fr[3*(i/2) + start+1] = (byte)(int)((rel[i])*127.5 + 127.5);
            fr[3*(i/2) + start+2] = (byte)(int)((rel[i])*127.5 + 127.5);
        }
        sfs.fr = fr;*/
        //sfs.absoluteHeights(sfs.relativeHeights());
        sfs.steps = 10000;
        //sfs.absoluteHeightsQUAD(sfs.relativeHeights());
        sfs.absoluteHeightsNEW(sfs.relativeHeights());
        //sfs.absoluteHeightsSW(sfs.relativeHeights());
        //sfs.absoluteHeightsSC(sfs.relativeHeights());
        //sfs.absoluteHeightsAF(sfs.relativeHeights());
        /*double [] rel = new double[]{
                5.0,5.0 , 5.0,2.0 , -5.0,0.0 , -5.0,-2.0 , 0.0,-5.0 ,
                2.0,5.0 , -2.0,-2.0 , 0.0,-5.0 , 2.0,-2.0 , 0.0,-2.0 ,
                2.0,5.0 , -2.0,-2.0 , 0.0,-5.0 , 2.0,-2.0 , 0.0,-2.0 ,
                2.0,5.0 , -2.0,-2.0 , 0.0,-5.0 , 2.0,-2.0 , 0.0,-2.0 ,
                -5.0,0.0 , -5.0,0.0 , 0.0,0.0 ,  5.0,0.0,  0.0,0.0
        };*/
        /*double [] rel = new double[]{
                5.0,5.0 , 5.0,2.0 , -5.0,0.0 , 0.0,-5.0 ,
                2.0,5.0 , -2.0,-2.0 , 0.0,-5.0 ,  0.0,-2.0 ,
                2.0,5.0 , -2.0,-2.0 , 0.0,-5.0 ,  0.0,-2.0 ,
                -5.0,0.0 , -5.0,0.0 , 0.0,0.0 ,    0.0,0.0
        };*/
        /*double [] rel = new double[]{
                -2.0,-2.0 , 2.0,-5.0 ,  0.0,-2.0 ,
                -5.0,2.0 , 5.0,5.0 ,   0.0,2.0 ,
                -2.0,0.0 , 2.0,0.0 ,    0.0,0.0
        };

        sfs.collumns = 3;
        sfs.rows = 3;
        sfs.steps = 25;*/
        //sfs.absoluteHeights(rel);
        //sfs.absoluteHeightsSW(rel);
        //sfs.absoluteHeightsSW(sfs.relativeHeights());
        NormalMap normalMap = new NormalMap();
        sfs.write(normalMap.normalMap(sfs.fr,180.0,0.1),"/home/sedlasi1/Desktop/testovaci_obrazky/F.ppm");
        sfs.write(sfs.fr,"/home/sedlasi1/Desktop/testovaci_obrazky/uu.ppm");
        //test();
    }

    private static void test(){
        int col = 2;
        int row = 2;
        double [] h = new double[col*row];
        double [] q = new double[]{5.0,3.0,-1.0,-2.0};
        //double [] q = new double[]{5.0,5.0,-2.0,-2.0};

        for(int i = 0; i < 5; i++){
            h[0] = (h[1] + q[0] + h[2] + q[1])/2.0;
            h[1] = (h[0] - q[0] + h[3] + q[2])/2.0;
            h[3] = (h[1] - q[2] + h[2] - q[3])/2.0;
            h[2] = (h[0] - q[1] + h[3] + q[3])/2.0;

            System.out.println();
            System.out.println("iteration: "+i);
            for(int j = 0; j < col*row; j+=col){
                System.out.print("||");
                for(int k = 0; k < col; k++){
                    System.out.print(h[j+k]+" ");
                }
                System.out.println("||");
            }
        }

        for(int i = 0; i < 200; i++){
            h[0] = (h[1] + q[0] + h[2] + q[1])/2.0;
            h[1] = (h[0] - q[0] + h[3] + q[2])/2.0;
            h[3] = (h[1] - q[2] + h[2] - q[3])/2.0;
            h[2] = (h[0] - q[1] + h[3] + q[3])/2.0;
        }
        System.out.println();
        System.out.println("iteration: 200");
        for(int j = 0; j < col*row; j+=col){
            System.out.print("||");
            for(int k = 0; k < col; k++){
                System.out.print(h[j+k]+" ");
            }
            System.out.println("||");
        }

    }

    private void getDepthMapOld(){
        collumns = 2;
        rows = 2;
        lightX = 0.5;
        lightY = 0.2;
        lightZ = 0.1;

        double fourLmX = 4*lm - lightX;
        double fourLmY = 4*lm - lightY;
        double fourLmZ = 4*lm - lightZ;
        double threeLmX = 3*lm - lightX;
        double threeLmY = 3*lm - lightY;
        double threeLmZ = 3*lm - lightZ;
        double twoLmX = 2*lm - lightX;
        double twoLmY = 2*lm - lightY;
        double twoLmZ = 2*lm - lightZ;

       /* double [] b =  new double[3*collumns*rows];
        int jump = collumns*rows;
        for(int i = 0;i <3; i++){
            for(int j = 0; j < grayscale.length;j++){
                    b[i*jump + j] = q*grayscale[j];
            }
        }*/
        int size = 3*collumns*rows;
        double [][] A = new double[size][size];

        //bod v levo nahore
        A[0][3] = -lm;
        A[0][3*collumns] = -lm;
        A[1][4] = -lm;
        A[1][3*collumns+1] = -lm;
        A[2][5] = -lm;
        A[2][3*collumns+2] = -lm;
        A[0][0] = twoLmX;
        A[1][1] = twoLmY;
        A[2][2] = twoLmZ;

        //prvni radka
        double [] l;

        for (int i = 1; i < collumns-1; i++) {
            l = A[i];
            l[3*i+3] = -lm;
            l[3*i-3] = -lm;
            l[3*(i+collumns)] = -lm;
            l[i] = threeLmX;
            l = A[i+1];
            l[3*i+3+1] = -lm;
            l[3*i-3+1] = -lm;
            l[3*(i+collumns)+1] = -lm;
            l[i+1] = threeLmY;
            l = A[i+2];
            l[3*i+2+3] = -lm;
            l[3*i+2-3] = -lm;
            l[3*(i+collumns)+2] = -lm;
            l[i+2] = threeLmZ;

        }

        //bod vpravo nahore
        A[3*collumns-3][3*collumns-3-3] = -lm;
        A[3*collumns-3][3*collumns-3+3*collumns] = -lm;
        A[3*collumns-2][3*collumns-3-3+1] = -lm;
        A[3*collumns-2][3*collumns-3+3*collumns+1] = -lm;
        A[3*collumns-1][3*collumns-3-3+2] = -lm;
        A[3*collumns-1][3*collumns-3+3*collumns+2] = -lm;
        A[3*collumns-3][3*collumns-3] = twoLmX;
        A[3*collumns-2][3*collumns-2] = twoLmY;
        A[3*collumns-1][3*collumns-1] = twoLmZ;

        //stred matice

        for(int j = 1; j < rows-1; j++){
            l = A[j*3*collumns];
            l[j*3*collumns - 3*collumns] = -lm;
            l[j*3*collumns+3] = -lm;
            l[j*3*collumns+3*collumns]= -lm;
            l[j*3*collumns] = threeLmX;
            l = A[j*3*collumns+1];
            l[j*3*collumns - 3*collumns+1] = -lm;
            l[j*3*collumns+3+1] = -lm;
            l[j*3*collumns+3*collumns+1]= -lm;
            l[j*3*collumns+1] = threeLmY;
            l = A[j*3*collumns+2];
            l[j*3*collumns - 3*collumns+2] = -lm;
            l[j*3*collumns+3+2] = -lm;
            l[j*3*collumns+3*collumns+2]= -lm;
            l[j*3*collumns+2] = threeLmZ;

            for(int i=1; i < collumns-1; i++){
                l = A[j*3*collumns + i*3];
                l[j*3*collumns + i*3 +3]= -lm;
                l[j*3*collumns + i*3 -3]= -lm;
                l[j*3*collumns + i*3 +3*collumns]= -lm;
                l[j*3*collumns + i*3 -3*collumns]= -lm;
                l[j*3*collumns + i*3]=fourLmX;
                l = A[j*3*collumns + i*3+1];
                l[j*3*collumns + i*3 +3+1]= -lm;
                l[j*3*collumns + i*3 -3+1]= -lm;
                l[j*3*collumns + i*3 +3*collumns+1]= -lm;
                l[j*3*collumns + i*3 -3*collumns+1]= -lm;
                l[j*3*collumns + i*3+1]=fourLmY;
                l = A[j*3*collumns + i*3+2];
                l[j*3*collumns + i*3 +3+2]= -lm;
                l[j*3*collumns + i*3 -3+2]= -lm;
                l[j*3*collumns + i*3 +3*collumns+2]= -lm;
                l[j*3*collumns + i*3 -3*collumns+2]= -lm;
                l[j*3*collumns + i*3+2]=fourLmZ;

                //break;
            }

            l = A[(j+1)*3*collumns-3];
            l[(j+1)*3*collumns-3-3*collumns]= -lm;
            l[(j+1)*3*collumns-3-3]= -lm;
            l[(j+1)*3*collumns-3+3*collumns]= -lm;
            l[(j+1)*3*collumns-3]=threeLmX;
            l = A[(j+1)*3*collumns-3+1];
            l[(j+1)*3*collumns-3-3*collumns+1]= -lm;
            l[(j+1)*3*collumns-3-3+1]= -lm;
            l[(j+1)*3*collumns-3+3*collumns+1]= -lm;
            l[(j+1)*3*collumns-3+1]=threeLmY;
            l = A[(j+1)*3*collumns-3+2];
            l[(j+1)*3*collumns-3-3*collumns+2]= -lm;
            l[(j+1)*3*collumns-3-3+2]= -lm;
            l[(j+1)*3*collumns-3+3*collumns+2]= -lm;
            l[(j+1)*3*collumns-3+2]=threeLmZ;


        }




        //posledni radek
        A[3*collumns*(rows-1)][3*collumns*(rows-1)-3*collumns] = -lm;
        A[3*collumns*(rows-1)][3*collumns*(rows-1)+3] = -lm;
        A[3*collumns*(rows-1)+1][3*collumns*(rows-1)-3*collumns+1] = -lm;
        A[3*collumns*(rows-1)+1][3*collumns*(rows-1)+3+1] = -lm;
        A[3*collumns*(rows-1)+2][3*collumns*(rows-1)-3*collumns+2] = -lm;
        A[3*collumns*(rows-1)+2][3*collumns*(rows-1)+3+2] = -lm;
        A[3*collumns*(rows-1)][3*collumns*(rows-1)] = twoLmX;
        A[3*collumns*(rows-1)+1][3*collumns*(rows-1)+1] = twoLmY;
        A[3*collumns*(rows-1)+2][3*collumns*(rows-1)+2] = twoLmZ;

        int start = 3*collumns*(rows-1);
        for(int i=1; i < collumns-1; i++){
            l = A[i + start];
            l[3*collumns*(rows-1) + i -3] = -lm;
            l[3*collumns*(rows-1) + i + 3] = -lm;
            l[3*collumns*(rows-1) + i -3*collumns] = -lm;
            l[i + start] = threeLmX;
            l = A[i + start + 1];
            l[3*collumns*(rows-1) + i -3 + 1] = -lm;
            l[3*collumns*(rows-1) + i + 3 + 1] = -lm;
            l[3*collumns*(rows-1) + i -3*collumns + 1] = -lm;
            l[i + start+1] = threeLmY;
            l = A[i + start + 2];
            l[3*collumns*(rows-1) + i -3 + 2] = -lm;
            l[3*collumns*(rows-1) + i + 3 + 2] = -lm;
            l[3*collumns*(rows-1) + i -3*collumns + 2] = -lm;
            l[i + start+2] = threeLmZ;
        }

        A[3*collumns*rows-3][3*collumns*rows-3-3] = -lm;
        A[3*collumns*rows-3][3*collumns*rows-3-3*collumns] = -lm;
        A[3*collumns*rows-2][3*collumns*rows-3-3+1] = -lm;
        A[3*collumns*rows-2][3*collumns*rows-3-3*collumns+1] = -lm;
        A[3*collumns*rows-1][3*collumns*rows-3-3+2] = -lm;
        A[3*collumns*rows-1][3*collumns*rows-3-3*collumns+2] = -lm;
        A[3*collumns*rows-3][3*collumns*rows-3]=twoLmX;
        A[3*collumns*rows-3+1][3*collumns*rows-3+1]=twoLmY;
        A[3*collumns*rows-3+2][3*collumns*rows-3+2]=twoLmZ;



        //gaussSeidel(A,b);


        for(int j =0; j< 3*collumns*rows; j++){
            l = A[j];
            for(int i=0;i<3*collumns*rows;i++){
                System.out.print(l[i]+"|");
            }
            System.out.println();
        }

    }

    public void gaussSeidel(double[][] A, double[] b){
        int count = 0;
        boolean stop = false;

        double[] xNew = new double[b.length]; // x2 = 0, x3 = 0,
        double[] xOld = new double[b.length];

        do{

            for(int i = 0; i < A.length; i++){
                double sum = 0.0;
                double sum1 = 0.0;
                for(int j = 0; j < A.length; j++){

                    if( j != i)
                        sum += (A[i][j]*xOld[j]);

                    sum1 += (A[i][j]*xNew[j]);
                }

                xNew[i] = (b[i] - sum - sum1)*(1/A[i][i]);
                /*System.out.println("X_" + (i+1) + ": " + xNew[i]);
                System.out.println("Error is: " + Math.abs((xNew[i] - xOld[i])));
                System.out.println("");*/
                count++;

                if(Math.abs(xNew[i] - xOld[i]) > 0.01){
                    xNew[i] = xOld[i];
                }

                else{
                    stop = true;}
            }
        } while( !stop && count <= 10);
        normalField = xNew;
    }

    // PUVODNI METODA!!!!!!
    private double [] getHeightMap(){
        // 4. step

        //uvodni estimate bez regularizace
        int size = collumns*rows;
        double [] n = new double[3*size];
        double x,y,z;
        for(int i = 0; i < size; i++){
            x = (q*grayscale[i])/lightX;
            y = (q*grayscale[i])/lightY;
            z = (q*grayscale[i])/lightZ;
            if(z < 0) {
                x = -x;
                y = -y;
                z = -z;
            }
            n[3*i] = x;
            n[3*i+1] = y;
            n[3*i+2] = z;
        }
        /*if(n != null)
        return n;*/
        // buffer field
        double mod = 2*3*collumns;
        double [] buffer = new double [(int)mod];

        // matice s neighbours = nei

        double [] nei = new double[]{2*lm+lightX,2*lm+lightY,2*lm+lightZ, // 0 + 0; 0+1; 0+2
                                     3*lm+lightX,3*lm+lightY,3*lm+lightZ, // 3+0; 3+1; 3+2
                                     4*lm+lightX,4*lm+lightY,4*lm+lightZ // 6 + 0; 6 + 1; 6+2
                                    };


        // vytvoreni matice index - matice sousednosti

        //prvni radek
        int [] index = new int[(4*collumns)*rows];

        index[0] = 3;
        index[1] = 3*collumns;
        index[2] = -1;
        index[3] = -1;

        for(int i=1; i < collumns-1; i++){
            index[4*i] = 3*i+3;
            index[4*i+1] = 3*i-3;
            index[4*i+2] = 3*(i+collumns);
            index[4*i+3] = -1;
        }

        index[4*collumns-4] = 3*collumns-2*3;
        index[4*collumns-3] = 2*3*collumns-3;
        index[4*collumns-2] = -1;
        index[4*collumns-1] = -1;

        //stred matice

        for(int j = 1; j < rows-1; j++){

            index[j*4*collumns] = j*3*collumns-3*collumns;
            index[j*4*collumns+1] = j*3*collumns+3;
            index[j*4*collumns+2] = j*3*collumns+3*collumns;
            index[j*4*collumns+3] = -1;

            for(int i=1; i < collumns-1; i++){
                index[j*4*collumns + i*4 ] = j*3*collumns + i*3 +3;
                index[j*4*collumns + i*4 + 1] = j*3*collumns + i*3 -3;
                index[j*4*collumns + i*4 + 2] = j*3*collumns + i*3 +3*collumns;
                index[j*4*collumns + i*4 + 3] = j*3*collumns + i*3 -3*collumns;
                //break;
            }


            index[(j+1)*4*collumns-4] = (j+1)*3*collumns-3-3*collumns;
            index[(j+1)*4*collumns-3] = (j+1)*3*collumns-3-3;
            index[(j+1)*4*collumns-2] = (j+1)*3*collumns-3+3*collumns;
            index[(j+1)*4*collumns-1] = -1;

        }

        //posledni radek
        int start = 4*(collumns)*(rows-1);
        index[start] = 3*(collumns)*(rows-2);
        index[start + 1] = 3*(collumns)*(rows-1)+3;
        index[start + 2] = -1;
        index[start + 3] = -1;

        for(int i=1; i < collumns-1; i++){
            index[4*i + start] = 3*i+3 + 3*(collumns)*(rows-1);
            index[4*i+1 + start] = 3*i-3 + 3*(collumns)*(rows-1);
            index[4*i+2+ start] = 3*(i-collumns) + 3*(collumns)*(rows-1);
            index[4*i+3+start] = -1;
        }

        start = (4*collumns)*rows -4;

        index[start] = 3*(collumns)*(rows-1)-3;
        index[start+1] = 3*(collumns)*rows-2*3;
        index[start+2] = -1;
        index[start+3] = -1;

        //
        /*for(int j =0; j< rows; j++){
            for(int i=0;i<4*collumns;i+=4){
                System.out.print(index[j*4*collumns+i]+" "+index[j*4*collumns+i+1]+" "+index[j*4*collumns+i+2]+" "+index[j*4*collumns+i+3]+"|");
            }
            System.out.println();
        }*/

        //
        // Pocitame rovnici x_i = a_i /d_i

        int neighbourSize;
        double s;
        double n_y_left;
        double n_x_left;
        double n_z_left;
        //double [] s = new double[size];
        for(int g = 0; g < steps; g++){ // hlavni loop = pocet kroku gauss-siedela
            for(int i = 0; i <(size); i++){ // prochazim N, v tomto loop spocitam n_x, n_y a n_z pro g-tou iteraci gauss-saidela
                neighbourSize = 0;
                while(index[4*i + neighbourSize] != -1 && neighbourSize != 4) neighbourSize++;
                neighbourSize--;
                double n_1 = 0;
                double n_2 = 0;
                double n_3 = 0;
                for(int ne=0; ne<neighbourSize;ne++){
                    n_1 += n[index[4*i+ne]]; // prvni pridavame prvky v zavorce s lambda -- hlavni rovnice kde n_x je s (4lm + l_x)
                    n_2 += n[index[4*i+ne]+1]; //  vedlejsi rovnice kde n_x je n_x*l_x a hlavni je y
                    n_3 += n[index[4*i+ne]+2]; //  vedlejsi rovnice kde n_x je n_x*l_x a hlavni je z

                }
                n_1 *= lm; // pridame lambda
                n_1 += q*grayscale[i]; // pridame b[i]
                n_2 *= lm; // pridame lambda
                n_2 += q*grayscale[i]; // pridame b[i]
                n_3 *= lm; // pridame lambda
                n_3 += q*grayscale[i]; // pridame b[i]
                n_x_left = n_1;
                n_y_left = n_2;
                n_z_left = n_3;
                //nyni odecteme cleny n[i+1]*nei[]
                int neiCoef = 0;
                if(neighbourSize == 2){
                    neiCoef = 3;
                } else if(neighbourSize == 3) {
                    neiCoef = 6;
                }

                n_1 -= (n[3*i+1]*lightY + n[3*i+2]*lightZ);
                n_1 /= nei[neiCoef]; // prvni vysledek

                n_2 -= (n[3*i+1]*nei[neiCoef+1] + n[3*i+2]*lightZ);
                n_2 /= lightX; // druhy vysledek

                n_3 -= (n[3*i+1]*lightY + n[3*i+2]*nei[neiCoef+2]);
                n_3 /= lightX; // treti vysledek


                double newValue_x = n_1 + n_2 + n_3;
                double sum = 3;
                for(int r = 0; r < neighbourSize; r++){ // budu projizdet rovnice neighbouru a vyjadrovat z nich n_1
                    int u = index[4*i + r]; // index na neighboura
                    int uNeighbourSize = 0;
                    while(index[(u/3)*4 + uNeighbourSize] != -1 && uNeighbourSize != 4) uNeighbourSize++;
                    uNeighbourSize--;
                    int uNeiCoef = 0;
                    if(uNeighbourSize == 2){
                        uNeiCoef = 3;
                    } else if(uNeighbourSize == 3) {
                        uNeiCoef = 6;
                    }
                    double n_i = 0;
                    for(int ne=0; ne<uNeighbourSize;ne++){
                        if(index[(u/3)*4+ne] == 3*i) continue;
                        n_i += n[index[(u/3)*4+ne]]; // prvni pridavame prvky v zavorce s lambda
                    }
                    n_i *= lm; // pridame lambda
                    n_i += (q*grayscale[u/3] - n[u]*nei[uNeiCoef]);
                    n_i -= (n[u+1]*lightY + n[u+2]*lightZ);
                    n_i *= -1;
                    n_i /= lm; // vysledek z rovnice souseda
                    newValue_x += n_i;
                    sum++;
                }


                newValue_x /= sum;



                // value y
                //neighbourSize je stejny !!!

                n_1 = n_x_left;
                n_2 = n_y_left;
                n_3 = n_z_left;

                //nyni odecteme cleny n[i+1]*nei[]
                neiCoef = 0;
                if(neighbourSize == 2){
                    neiCoef = 3;
                } else if(neighbourSize == 3) {
                    neiCoef = 6;
                }

                n_1 -= (n[3*i]*nei[neiCoef] + n[3*i+2]*lightZ);
                n_1 /= lightY; // prvni vysledek

                n_2 -= (n[3*i]*lightX + n[3*i+2]*lightZ);
                n_2 /= nei[neiCoef+1]; // druhy vysledek

                n_3 -= (n[3*i]*lightX + n[3*i+2]*nei[neiCoef+2]);
                n_3 /= lightY; // treti vysledek


                double newValue_y = n_1 + n_2 + n_3;
                sum = 3;
                for(int r = 0; r < neighbourSize; r++){ // budu projizdet rovnice neighbouru a vyjadrovat z nich n_1
                    int u = index[4*i + r]; // index na neighboura
                    int uNeighbourSize = 0;
                    while(index[(u/3)*4 + uNeighbourSize] != -1 && uNeighbourSize != 4) uNeighbourSize++;
                    uNeighbourSize--;
                    int uNeiCoef = 0;
                    if(uNeighbourSize == 2){
                        uNeiCoef = 3;
                    } else if(uNeighbourSize == 3) {
                        uNeiCoef = 6;
                    }
                    double n_i = 0;
                    for(int ne=0; ne<uNeighbourSize;ne++){
                        if(index[(u/3)*4+ne] == 3*i) continue;
                        n_i += n[index[(u/3)*4+ne]+1]; // prvni pridavame prvky v zavorce s lambda
                    }
                    n_i *= lm; // pridame lambda
                    n_i += (q*grayscale[u/3] - n[u+1]*nei[uNeiCoef+1]);
                    n_i -= (n[u]*lightX + n[u+2]*lightZ);
                    n_i *= -1;
                    n_i /= lm; // vysledek z rovnice souseda
                    newValue_y += n_i;
                    sum++;
                }


                newValue_y /= sum;



                // value z
                //neighbourSize je stejny !!!

                n_1 = n_x_left;
                n_2 = n_y_left;
                n_3 = n_z_left;

                //nyni odecteme cleny n[i+1]*nei[]
                neiCoef = 0;
                if(neighbourSize == 2){
                    neiCoef = 3;
                } else if(neighbourSize == 3) {
                    neiCoef = 6;
                }

                n_1 -= (n[3*i]*nei[neiCoef] + n[3*i+1]*lightY);
                n_1 /= lightZ; // prvni vysledek

                n_2 -= (n[3*i]*lightX + n[3*i+1]*nei[neiCoef+1]);
                n_2 /= lightZ; // druhy vysledek

                n_3 -= (n[3*i]*lightX + n[3*i+1]*lightY);
                n_3 /= nei[neiCoef+2]; // treti vysledek


                double newValue_z = n_1 + n_2 + n_3;
                sum = 3;
                for(int r = 0; r < neighbourSize; r++){ // budu projizdet rovnice neighbouru a vyjadrovat z nich n_1
                    int u = index[4*i + r]; // index na neighboura
                    int uNeighbourSize = 0;
                    while(index[(u/3)*4 + uNeighbourSize] != -1 && uNeighbourSize != 4) uNeighbourSize++;
                    uNeighbourSize--;
                    int uNeiCoef = 0;
                    if(uNeighbourSize == 2){
                        uNeiCoef = 3;
                    } else if(uNeighbourSize == 3) {
                        uNeiCoef = 6;
                    }
                    double n_i = 0;
                    for(int ne=0; ne<uNeighbourSize;ne++){
                        if(index[(u/3)*4+ne] == 3*i) continue;
                        n_i += n[index[(u/3)*4+ne]+2]; // prvni pridavame prvky v zavorce s lambda
                    }
                    n_i *= lm; // pridame lambda
                    n_i += (q*grayscale[u/3] - n[u+2]*nei[uNeiCoef+2]);
                    n_i -= (n[u]*lightX + n[u+1]*lightY);
                    n_i *= -1;
                    n_i /= lm; // vysledek z rovnice souseda
                    newValue_z += n_i;
                    sum++;
                }

                newValue_z /= sum;

                // normalizace a prirazeni
                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }
                n_1 = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length

                // zapiseme do n z bufferu a do bufferu zapiseme nove hodnoty na stejne pozice
                // obnova dat z bufferu
                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[(int)((3*(i - 2*collumns)) % mod)];
                    n[3*(i - 2*collumns)+1] = buffer[(int)((3*(i - 2*collumns)+1) % mod)];
                    n[3*(i - 2*collumns)+2] = buffer[(int)((3*(i - 2*collumns)+2) % mod)];
                }
                // zapis do bufferu
                /*System.out.println(i);
                System.out.println(collumns);
                System.out.println(buffer.length);
                System.out.println((int)((3*(i - 2*collumns)) % mod));
                System.out.println("============");*/
                buffer[(int)((3*i) % mod)] = newValue_x/n_1;
                buffer[(int)((3*i+1) % mod)] = newValue_y/n_1;
                buffer[(int)((3*i+2) % mod)] = newValue_z/n_1;
                // stary zpusob
                /*n[3*i] = newValue_x/n_1;
                n[3*i+1] = newValue_y/n_1;
                n[3*i+2] = newValue_z/n_1;*/



                //System.out.println(n[3*i]+" "+n[3*i+1]+" "+n[3*i+2]);

            }
            for(int i = 3*(size - 2*collumns); i < 3*size; i++){
                n[i] = buffer[(int)(i % mod)];
            }
        }

        //normalizace
        /*double len;
        for(int i = 0; i< size; i++){
            len = Math.sqrt(n[3*i]*n[3*i]+n[3*i+1]*n[3*i+1]+n[3*i+2]*n[3*i+2]);
            n[3*i] = n[3*i]/len;
            n[3*i+1] = n[3*i+1]/len;
            n[3*i+2] = n[3*i+2]/len;
        }*/
        //
        return n;
    }


    // 27.2.2017 -- moje vypocty
    private double [] getDepthMap2(){
        // 4. step

        //uvodni estimate bez regularizace
        int size = collumns*rows;
        double [] n = new double[3*size];
        double x,y,z,l;
        for(int i = 0; i < size; i++){
            x = (q*grayscale[i])/lightX;
            y = (q*grayscale[i])/lightY;
            z = (q*grayscale[i])/lightZ;
            if(z < 0) {
                x = -x;
                y = -y;
                z = -z;
            }
            l = Math.sqrt(x*x + y*y + z*z);

            n[3*i] = x/l;
            n[3*i+1] = y/l;
            n[3*i+2] = z/l;
        }
        /*if(n != null)
        return n;*/
        // buffer field
        double mod = 2*3*collumns;
        double [] buffer = new double [(int)mod];

        // matice s neighbours = nei

        double [] nei = new double[]{2*lm+lightX,2*lm+lightY,2*lm+lightZ, // 0 + 0; 0+1; 0+2
                3*lm+lightX,3*lm+lightY,3*lm+lightZ, // 3+0; 3+1; 3+2
                4*lm+lightX,4*lm+lightY,4*lm+lightZ // 6 + 0; 6 + 1; 6+2
        };


        // vytvoreni matice index - matice sousednosti

        //prvni radek
        int [] index = new int[(4*collumns)*rows];

        index[0] = 3;
        index[1] = 3*collumns;
        index[2] = -1;
        index[3] = -1;

        for(int i=1; i < collumns-1; i++){
            index[4*i] = 3*i+3;
            index[4*i+1] = 3*i-3;
            index[4*i+2] = 3*(i+collumns);
            index[4*i+3] = -1;
        }

        index[4*collumns-4] = 3*collumns-2*3;
        index[4*collumns-3] = 2*3*collumns-3;
        index[4*collumns-2] = -1;
        index[4*collumns-1] = -1;

        //stred matice

        for(int j = 1; j < rows-1; j++){

            index[j*4*collumns] = j*3*collumns-3*collumns;
            index[j*4*collumns+1] = j*3*collumns+3;
            index[j*4*collumns+2] = j*3*collumns+3*collumns;
            index[j*4*collumns+3] = -1;

            for(int i=1; i < collumns-1; i++){
                index[j*4*collumns + i*4 ] = j*3*collumns + i*3 +3;
                index[j*4*collumns + i*4 + 1] = j*3*collumns + i*3 -3;
                index[j*4*collumns + i*4 + 2] = j*3*collumns + i*3 +3*collumns;
                index[j*4*collumns + i*4 + 3] = j*3*collumns + i*3 -3*collumns;
                //break;
            }


            index[(j+1)*4*collumns-4] = (j+1)*3*collumns-3-3*collumns;
            index[(j+1)*4*collumns-3] = (j+1)*3*collumns-3-3;
            index[(j+1)*4*collumns-2] = (j+1)*3*collumns-3+3*collumns;
            index[(j+1)*4*collumns-1] = -1;

        }

        //posledni radek
        int start = 4*(collumns)*(rows-1);
        index[start] = 3*(collumns)*(rows-2);
        index[start + 1] = 3*(collumns)*(rows-1)+3;
        index[start + 2] = -1;
        index[start + 3] = -1;

        for(int i=1; i < collumns-1; i++){
            index[4*i + start] = 3*i+3 + 3*(collumns)*(rows-1);
            index[4*i+1 + start] = 3*i-3 + 3*(collumns)*(rows-1);
            index[4*i+2+ start] = 3*(i-collumns) + 3*(collumns)*(rows-1);
            index[4*i+3+start] = -1;
        }

        start = (4*collumns)*rows -4;

        index[start] = 3*(collumns)*(rows-1)-3;
        index[start+1] = 3*(collumns)*rows-2*3;
        index[start+2] = -1;
        index[start+3] = -1;

        //
        /*for(int j =0; j< rows; j++){
            for(int i=0;i<4*collumns;i+=4){
                System.out.print(index[j*4*collumns+i]+" "+index[j*4*collumns+i+1]+" "+index[j*4*collumns+i+2]+" "+index[j*4*collumns+i+3]+"|");
            }
            System.out.println();
        }*/

        //
        // Pocitame rovnici x_i = a_i /d_i

        int neighbourSize;
        double s;
        double n_y_left;
        double n_x_left;
        double n_z_left;
        //double [] s = new double[size];
        for(int g = 0; g < steps; g++){ // hlavni loop = pocet kroku gauss-siedela
            for(int i = 0; i <(size); i++){ // prochazim N, v tomto loop spocitam n_x, n_y a n_z pro g-tou iteraci gauss-saidela
                neighbourSize = 0;
                while(index[4*i + neighbourSize] != -1 && neighbourSize != 4) neighbourSize++;
                neighbourSize--;
                double n_1 = 0;
                double n_2 = 0;
                double n_3 = 0;

                double newValue_x;
                double newValue_y;
                double newValue_z;

                double sumX = 0;
                double sumY = 0;
                double sumZ = 0;
                for(int ne = 0; ne < neighbourSize; ne++){ //secteme xove/yove/zove slozky normal sousedu
                    sumX += n[index[4*i + ne]];
                    sumY += n[index[4*i + ne]+1];
                    sumZ += n[index[4*i + ne]+2];
                }

                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i+2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize+1));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i+2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize+1));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i+1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize+1));

                // pocitani najednou
                /*newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i+2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize+1));
                newValue_y = (lightY*((1/q)*grayscale[i] - newValue_x*lightX - n[3*i+2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize+1));
                newValue_z = (lightZ*((1/q)*grayscale[i] - newValue_x*lightX - newValue_y*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize+1));
                */

                // normalizace a prirazeni
                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }
                n_1 = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length

                // zapiseme do n z bufferu a do bufferu zapiseme nove hodnoty na stejne pozice
                // obnova dat z bufferu
                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[(int)((3*(i - 2*collumns)) % mod)];
                    n[3*(i - 2*collumns)+1] = buffer[(int)((3*(i - 2*collumns)+1) % mod)];
                    n[3*(i - 2*collumns)+2] = buffer[(int)((3*(i - 2*collumns)+2) % mod)];
                }
                // zapis do bufferu
                /*System.out.println(i);
                System.out.println(collumns);
                System.out.println(buffer.length);
                System.out.println((int)((3*(i - 2*collumns)) % mod));
                System.out.println("============");*/
                buffer[(int)((3*i) % mod)] = newValue_x/n_1;
                buffer[(int)((3*i+1) % mod)] = newValue_y/n_1;
                buffer[(int)((3*i+2) % mod)] = newValue_z/n_1;
                /*buffer[(int)((3*i) % mod)] = newValue_y/n_1;
                buffer[(int)((3*i+1) % mod)] = newValue_z/n_1;
                buffer[(int)((3*i+2) % mod)] = newValue_x/n_1;*/
                // stary zpusob
                /*n[3*i] = newValue_x/n_1;
                n[3*i+1] = newValue_y/n_1;
                n[3*i+2] = newValue_z/n_1;*/



                //System.out.println(n[3*i]+" "+n[3*i+1]+" "+n[3*i+2]);

            }
            for(int i = 3*(size - 2*collumns); i < 3*size; i++){
                n[i] = buffer[(int)(i % mod)];
            }
        }

        //normalizace
        /*double len;
        for(int i = 0; i< size; i++){
            len = Math.sqrt(n[3*i]*n[3*i]+n[3*i+1]*n[3*i+1]+n[3*i+2]*n[3*i+2]);
            n[3*i] = n[3*i]/len;
            n[3*i+1] = n[3*i+1]/len;
            n[3*i+2] = n[3*i+2]/len;
        }*/
        //
        return n;
    }

    // 12.3.2017 METODA!!!!!! -- moje vypocty
    private double [] getDepthMap3(){
        // 4. step

        //uvodni estimate bez regularizace
        int size = collumns*rows;
        double [] n = new double[3*size];
        double x,y,z,l;
        for(int i = 0; i < size; i++){
            x = (q*grayscale[i])/lightX;
            y = (q*grayscale[i])/lightY;
            z = (q*grayscale[i])/lightZ;
            if(z < 0) {
                x = -x;
                y = -y;
                z = -z;
            }
            l = Math.sqrt(x*x + y*y + z*z);

            n[3*i] = x/l;
            n[3*i+1] = y/l;
            n[3*i+2] = z/l;
        }

        /*if(n != null)
        return n;*/
        // buffer field
        double mod = 2*3*collumns;
        double [] buffer = new double [(int)mod];

        // matice s neighbours = nei

        int neighbourSize;

        double len;

        double newValue_x;
        double newValue_y;
        double newValue_z;

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        //double [] s = new double[size];
        for(int g = 0; g < steps; g++){ // hlavni loop = pocet kroku gauss-siedela

            // pixel vlevo nahore

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3] + n[3*collumns];
            sumY = n[4] + n[3*collumns+1];
            sumZ = n[5] + n[3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[0] - n[1]*lightY - n[2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[0] - n[0]*lightX - n[2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[0] - n[0]*lightX - n[1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[0] = newValue_x/len;
            buffer[1] = newValue_y/len;
            buffer[2] = newValue_z/len;

            //horni radka
            neighbourSize = 3;
            for(int i = 1; i < collumns-1;i++){ // bereme x,y,z najednou
                // soucet n_x sousedu
                sumX = n[3*i - 3] + n[3*i + 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 2] + n[3*i + 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 1] + n[3*i + 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }
                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[3*i] = newValue_x/len;
                buffer[3*i + 1] = newValue_y/len;
                buffer[3*i + 2] = newValue_z/len;
            }

            // pixel vpravo nahore

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(collumns-1) - 3] + n[3*(collumns-1) + 3*collumns];
            sumY = n[3*(collumns-1) - 2] + n[3*(collumns-1) + 3*collumns+1];
            sumZ = n[3*(collumns-1) - 1] + n[3*(collumns-1) + 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1) + 1]*lightY - n[3*(collumns-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1)]*lightX - n[3*(collumns-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1)]*lightX - n[3*(collumns-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[3*(collumns-1)] = newValue_x/len;
            buffer[3*(collumns-1) + 1] = newValue_y/len;
            buffer[3*(collumns-1) + 2] = newValue_z/len;

            neighbourSize = 3;
            // po radcich telo
            for(int i = collumns; i < size - collumns; i += collumns){
                //prvek vlevo
                // soucet n_x sousedu
                sumX = n[3*i - 3*collumns] + n[3*i + 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 3*collumns + 1] + n[3*i + 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 3*collumns + 2] + n[3*i + 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }

                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[(int)((3*(i - 2*collumns)) % mod)];
                    n[3*(i - 2*collumns)+1] = buffer[(int)((3*(i - 2*collumns)+1) % mod)];
                    n[3*(i - 2*collumns)+2] = buffer[(int)((3*(i - 2*collumns)+2) % mod)];
                }

                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[(int)((3*i) % mod)] = newValue_x/len;
                buffer[(int)((3*i + 1) % mod)] = newValue_y/len;
                buffer[(int)((3*i + 2) % mod)] = newValue_z/len;

                //prostredek
                neighbourSize = 4;
                for(int j = 1; j < collumns-1; j++){ // vnitrek radku
                    // soucet n_x sousedu
                    sumX = n[3*(i+j) - 3*collumns] + n[3*(i+j) + 3*collumns] + n[3*(i+j) + 3] + n[3*(i+j) - 3];
                    sumY = n[3*(i+j) - 3*collumns + 1] + n[3*(i+j) + 3*collumns + 1] + n[3*(i+j) + 4] + n[3*(i+j) - 2];
                    sumZ = n[3*(i+j) - 3*collumns + 2] + n[3*(i+j) + 3*collumns + 2] + n[3*(i+j) + 5] + n[3*(i+j) - 1];
                    // pocitani zvlast
                    newValue_x = (lightX*((1/q)*grayscale[(i+j)] - n[3*(i+j) + 1]*lightY - n[3*(i+j) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                    newValue_y = (lightY*((1/q)*grayscale[(i+j)] - n[3*(i+j)]*lightX - n[3*(i+j) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                    newValue_z = (lightZ*((1/q)*grayscale[(i+j)] - n[3*(i+j)]*lightX - n[3*(i+j) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                    if(newValue_z < 0){
                        newValue_z = -newValue_z;
                        newValue_x = -newValue_x;
                        newValue_y = -newValue_y;
                    }

                    if(((i+j) - 2*collumns) >= 0){ // pokud jsme v poli
                        n[3*((i+j) - 2*collumns)] = buffer[(int)((3*((i+j) - 2*collumns)) % mod)];
                        n[3*((i+j) - 2*collumns)+1] = buffer[(int)((3*((i+j) - 2*collumns)+1) % mod)];
                        n[3*((i+j) - 2*collumns)+2] = buffer[(int)((3*((i+j) - 2*collumns)+2) % mod)];
                    }

                    len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                    buffer[(int)((3*(i+j)) % mod)] = newValue_x/len;
                    buffer[(int)((3*(i+j) + 1) % mod)] = newValue_y/len;
                    buffer[(int)((3*(i+j) + 2) % mod)] = newValue_z/len;
                }

                //prvek vpravo
                neighbourSize = 3;
                // soucet n_x sousedu
                sumX = n[3*(i+collumns-1) - 3*collumns] + n[3*(i+collumns-1) + 3*collumns] + n[3*(i+collumns-1) - 3];
                sumY = n[3*(i+collumns-1) - 3*collumns + 1] + n[3*(i+collumns-1) + 3*collumns + 1] + n[3*(i+collumns-1) - 2];
                sumZ = n[3*(i+collumns-1) - 3*collumns + 2] + n[3*(i+collumns-1) + 3*collumns + 2] + n[3*(i+collumns-1) - 1];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1) + 1]*lightY - n[3*(i+collumns-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1)]*lightX - n[3*(i+collumns-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1)]*lightX - n[3*(i+collumns-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }

                if(((i+collumns-1) - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*((i+collumns-1) - 2*collumns)] = buffer[(int)((3*((i+collumns-1) - 2*collumns)) % mod)];
                    n[3*((i+collumns-1) - 2*collumns)+1] = buffer[(int)((3*((i+collumns-1) - 2*collumns)+1) % mod)];
                    n[3*((i+collumns-1) - 2*collumns)+2] = buffer[(int)((3*((i+collumns-1) - 2*collumns)+2) % mod)];
                }

                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[(int)((3*(i+collumns-1)) % mod)] = newValue_x/len;
                buffer[(int)((3*(i+collumns-1) + 1) % mod)] = newValue_y/len;
                buffer[(int)((3*(i+collumns-1) + 2) % mod)] = newValue_z/len;

            }

            // pixel vlevo dole

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(size - collumns) + 3] + n[3*(size - collumns) - 3*collumns];
            sumY = n[3*(size - collumns) + 4] + n[3*(size - collumns) - 3*collumns+1];
            sumZ = n[3*(size - collumns) + 5] + n[3*(size - collumns) - 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns) + 1]*lightY - n[3*(size - collumns) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns)]*lightX - n[3*(size - collumns) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns)]*lightX - n[3*(size - collumns) +1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[(int)((3*(size - collumns)) % mod)] = newValue_x/len;
            buffer[(int)((3*(size - collumns)+1) % mod)] = newValue_y/len;
            buffer[(int)((3*(size - collumns)+2) % mod)] = newValue_z/len;

            //spodni radka
            neighbourSize = 3;
            for(int i = size - collumns + 1; i < size-1;i++){ // bereme x,y,z najednou
                // soucet n_x sousedu
                sumX = n[3*i - 3] + n[3*i - 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 2] + n[3*i - 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 1] + n[3*i - 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    newValue_z = -newValue_z;
                    newValue_x = -newValue_x;
                    newValue_y = -newValue_y;
                }
                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[(int)((3*i) % mod)] = newValue_x/len;
                buffer[(int)((3*i+1) % mod)] = newValue_y/len;
                buffer[(int)((3*i+2) % mod)] = newValue_z/len;
            }

            // pixel vpravo dole

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(size-1) - 3] + n[3*(size-1) - 3*collumns];
            sumY = n[3*(size-1) - 2] + n[3*(size-1) - 3*collumns+1];
            sumZ = n[3*(size-1) - 1] + n[3*(size-1) - 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(size-1)] - n[3*(size-1) + 1]*lightY - n[3*(size-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[(int)((3*(size-1)) % mod)] = newValue_x/len;
            buffer[(int)((3*(size-1)+1) % mod)] = newValue_y/len;
            buffer[(int)((3*(size-1)+2) % mod)] = newValue_z/len;


            for(int i = 3*(size - 2*collumns); i < 3*size; i++){
                n[i] = buffer[(int)(i % mod)];
            }
        }

        //normalizace
        /*double len;
        for(int i = 0; i< size; i++){
            len = Math.sqrt(n[3*i]*n[3*i]+n[3*i+1]*n[3*i+1]+n[3*i+2]*n[3*i+2]);
            n[3*i] = n[3*i]/len;
            n[3*i+1] = n[3*i+1]/len;
            n[3*i+2] = n[3*i+2]/len;
        }*/
        //
        return n;
    }

    // 12.3.2017 METODA!!!!!! -- moje vypocty NEW
    private double [] getDepthMap4(){
        // 4. step

        //uvodni estimate bez regularizace
        int size = collumns*rows;
        double [] n = new double[3*size];
        double x,y,z,l;

        // seradime markery podle barvy ktere odpovidaji
        double [] gr = new double[markers.size()];

        markers.sort((m1, m2) -> {
            int posm1,posm2;
            posm1 = (int)(m1.getPosY()*rows*collumns + m1.getPosX()*collumns);
            posm2 = (int)(m2.getPosY()*rows*collumns + m2.getPosX()*collumns);
            double g1 = grayscale[posm1];
            double g2 = grayscale[posm2];
            return Double.compare(g1,g2);
        });
        for(int i = 0; i < gr.length; i++){
            Marker m = markers.get(i);
            int posm = (int)(m.getPosY()*rows*collumns + m.getPosX()*collumns);
            double gm = grayscale[posm];
            gr[i] = gm;
        }
        double gm;
        int m;
        double nx,ny,nz;
        double intv,val,pos;

        for(int i = 0; i < size; i++){
            gm = grayscale[i];
            m = 0;
            while(m < gr.length && gm > gr[m]) m++;
            if(m == 0){ // 0 - m1
                intv = gr[m];
                val = gm;
                pos = val/intv;
                nx = markers.get(m).getX()*pos;
                ny = markers.get(m).getY()*pos;
                nz = markers.get(m).getZ()*pos;
            } else if(m == gr.length){ // mn - 1
                intv = 1 - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos);
                ny = markers.get(m-1).getY()*(1-pos);
                nz = markers.get(m-1).getZ()*(1-pos);
            } else { // mi - mj
                intv = gr[m] - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos) + markers.get(m).getX()*(pos);
                ny = markers.get(m-1).getY()*(1-pos) + markers.get(m).getY()*(pos);
                nz = markers.get(m-1).getZ()*(1-pos) + markers.get(m).getZ()*(pos);
            }
            if(nz < 127.5) {
                nz = 127.5;
                /*nx = -nx;
                ny = -ny;
                nz = -nz;*/
            }
            l = Math.sqrt(nx*nx + ny*ny + nz*nz);
            n[3*i] = nx/l;
            n[3*i+1] = ny/l;
            n[3*i+2] = nz/l;

        }

        /*for(int i = 0; i < size; i++){
            x = ((q)*grayscale[i])/lightX;
            y = ((q)*grayscale[i])/lightY;
            z = ((q)*grayscale[i])/lightZ;
            if(z < 0) {
                //z = 0;
                x = -x;
                y = -y;
                z = -z;
            }
            l = Math.sqrt(x*x + y*y + z*z);

            n[3*i] = x/l;
            n[3*i+1] = y/l;
            n[3*i+2] = z/l;
        }*/
        /*if(n != null)
        return n;*/
        // buffer field
        int mod = 0;
        double [] buffer = new double [2*3*collumns];

        // matice s neighbours = nei

        int neighbourSize;

        double len;

        double newValue_x;
        double newValue_y;
        double newValue_z;

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        //double [] s = new double[size];
        for(int g = 0; g < steps; g++){ // hlavni loop = pocet kroku gauss-siedela

            // pixel vlevo nahore

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3] + n[3*collumns];
            sumY = n[4] + n[3*collumns+1];
            sumZ = n[5] + n[3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[0] - n[1]*lightY - n[2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[0] - n[0]*lightX - n[2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[0] - n[0]*lightX - n[1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[mod] = newValue_x/len;
            buffer[mod+1] = newValue_y/len;
            buffer[mod+2] = newValue_z/len;
            mod+=3;

            //horni radka
            neighbourSize = 3;
            for(int i = 1; i < collumns-1;i++){ // bereme x,y,z najednou
                // soucet n_x sousedu
                sumX = n[3*i - 3] + n[3*i + 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 2] + n[3*i + 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 1] + n[3*i + 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
                }
                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[mod] = newValue_x/len;
                buffer[mod + 1] = newValue_y/len;
                buffer[mod + 2] = newValue_z/len;
                mod+=3;
            }

            // pixel vpravo nahore

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(collumns-1) - 3] + n[3*(collumns-1) + 3*collumns];
            sumY = n[3*(collumns-1) - 2] + n[3*(collumns-1) + 3*collumns+1];
            sumZ = n[3*(collumns-1) - 1] + n[3*(collumns-1) + 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1) + 1]*lightY - n[3*(collumns-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1)]*lightX - n[3*(collumns-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1)]*lightX - n[3*(collumns-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[mod] = newValue_x/len;
            buffer[mod + 1] = newValue_y/len;
            buffer[mod + 2] = newValue_z/len;
            mod+=3;

            neighbourSize = 3;
            // po radcich telo
            for(int i = collumns; i < size - collumns; i += collumns){
                //prvek vlevo
                // soucet n_x sousedu
                sumX = n[3*i - 3*collumns] + n[3*i + 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 3*collumns + 1] + n[3*i + 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 3*collumns + 2] + n[3*i + 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
                }

                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[mod];
                    n[3*(i - 2*collumns)+1] = buffer[mod+1];
                    n[3*(i - 2*collumns)+2] = buffer[mod+2];
                }

                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[mod] = newValue_x/len;
                buffer[mod+1] = newValue_y/len;
                buffer[mod+2] = newValue_z/len;
                mod+=3;

                //prostredek
                neighbourSize = 4;
                for(int j = 1; j < collumns-1; j++){ // vnitrek radku
                    // soucet n_x sousedu
                    sumX = n[3*(i+j) - 3*collumns] + n[3*(i+j) + 3*collumns] + n[3*(i+j) + 3] + n[3*(i+j) - 3];
                    sumY = n[3*(i+j) - 3*collumns + 1] + n[3*(i+j) + 3*collumns + 1] + n[3*(i+j) + 4] + n[3*(i+j) - 2];
                    sumZ = n[3*(i+j) - 3*collumns + 2] + n[3*(i+j) + 3*collumns + 2] + n[3*(i+j) + 5] + n[3*(i+j) - 1];
                    // pocitani zvlast
                    newValue_x = (lightX*((1/q)*grayscale[(i+j)] - n[3*(i+j) + 1]*lightY - n[3*(i+j) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*((double)neighbourSize));
                    newValue_y = (lightY*((1/q)*grayscale[(i+j)] - n[3*(i+j)]*lightX - n[3*(i+j) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*((double)neighbourSize));
                    newValue_z = (lightZ*((1/q)*grayscale[(i+j)] - n[3*(i+j)]*lightX - n[3*(i+j) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*((double)neighbourSize));

                    if(newValue_z < 0){
                        //newValue_z = 0;
                       /* newValue_z = -newValue_z;
                        newValue_x = -newValue_x;
                        newValue_y = -newValue_y;*/
                    }

                    if(((i+j) - 2*collumns) >= 0){ // pokud jsme v poli
                        n[3*((i+j) - 2*collumns)] = buffer[mod];
                        n[3*((i+j) - 2*collumns)+1] = buffer[mod+1];
                        n[3*((i+j) - 2*collumns)+2] = buffer[mod+2];
                    }

                    len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                    buffer[mod] = newValue_x/len;
                    buffer[mod+1] = newValue_y/len;
                    buffer[mod+2] = newValue_z/len;
                    mod+=3;
                }

                //prvek vpravo
                neighbourSize = 3;
                // soucet n_x sousedu
                sumX = n[3*(i+collumns-1) - 3*collumns] + n[3*(i+collumns-1) + 3*collumns] + n[3*(i+collumns-1) - 3];
                sumY = n[3*(i+collumns-1) - 3*collumns + 1] + n[3*(i+collumns-1) + 3*collumns + 1] + n[3*(i+collumns-1) - 2];
                sumZ = n[3*(i+collumns-1) - 3*collumns + 2] + n[3*(i+collumns-1) + 3*collumns + 2] + n[3*(i+collumns-1) - 1];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1) + 1]*lightY - n[3*(i+collumns-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1)]*lightX - n[3*(i+collumns-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[(i+collumns-1)] - n[3*(i+collumns-1)]*lightX - n[3*(i+collumns-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
                }

                if(((i+collumns-1) - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*((i+collumns-1) - 2*collumns)] = buffer[mod];
                    n[3*((i+collumns-1) - 2*collumns)+1] = buffer[mod+1];
                    n[3*((i+collumns-1) - 2*collumns)+2] = buffer[mod+2];
                }

                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[mod] = newValue_x/len;
                buffer[mod+1] = newValue_y/len;
                buffer[mod+2] = newValue_z/len;
                mod+=3;
                if(mod == 2*3*collumns){
                    mod = 0;
                }

            }

            // pixel vlevo dole

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(size - collumns) + 3] + n[3*(size - collumns) - 3*collumns];
            sumY = n[3*(size - collumns) + 4] + n[3*(size - collumns) - 3*collumns+1];
            sumZ = n[3*(size - collumns) + 5] + n[3*(size - collumns) - 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns) + 1]*lightY - n[3*(size - collumns) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns)]*lightX - n[3*(size - collumns) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(size - collumns)] - n[3*(size - collumns)]*lightX - n[3*(size - collumns) +1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
            }
            if(((size - collumns) - 2*collumns) >= 0){ // pokud jsme v poli
                n[3*((size - collumns) - 2*collumns)] = buffer[mod];
                n[3*((size - collumns) - 2*collumns)+1] = buffer[mod+1];
                n[3*((size - collumns) - 2*collumns)+2] = buffer[mod+2];
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[mod] = newValue_x/len;
            buffer[mod+1] = newValue_y/len;
            buffer[mod+2] = newValue_z/len;
            mod+=3;

            //spodni radka
            neighbourSize = 3;
            for(int i = size - collumns + 1; i < size-1;i++){ // bereme x,y,z najednou
                // soucet n_x sousedu
                sumX = n[3*i - 3] + n[3*i - 3*collumns] + n[3*i + 3];
                sumY = n[3*i - 2] + n[3*i - 3*collumns + 1] + n[3*i + 4];
                sumZ = n[3*i - 1] + n[3*i - 3*collumns + 2] + n[3*i + 5];
                // pocitani zvlast
                newValue_x = (lightX*((1/q)*grayscale[i] - n[3*i + 1]*lightY - n[3*i + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
                newValue_y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
                newValue_z = (lightZ*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

                if(newValue_z < 0){
                    //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
                }
                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[mod];
                    n[3*(i - 2*collumns)+1] = buffer[mod+1];
                    n[3*(i - 2*collumns)+2] = buffer[mod+2];
                }
                len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                buffer[mod] = newValue_x/len;
                buffer[mod+1] = newValue_y/len;
                buffer[mod+2] = newValue_z/len;
                mod+=3;
            }

            // pixel vpravo dole

            neighbourSize = 2; // ma dva sousedy
            // soucet n_x sousedu
            sumX = n[3*(size-1) - 3] + n[3*(size-1) - 3*collumns];
            sumY = n[3*(size-1) - 2] + n[3*(size-1) - 3*collumns+1];
            sumZ = n[3*(size-1) - 1] + n[3*(size-1) - 3*collumns+2];
            // pocitani zvlast
            newValue_x = (lightX*((1/q)*grayscale[(size-1)] - n[3*(size-1) + 1]*lightY - n[3*(size-1) + 2]*lightZ) + lm*sumX)/(lightX*lightX + lm*(neighbourSize));
            newValue_y = (lightY*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1) + 2]*lightZ) + lm*sumY)/(lightY*lightY + lm*(neighbourSize));
            newValue_z = (lightZ*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1) + 1]*lightY) + lm*sumZ)/(lightZ*lightZ + lm*(neighbourSize));

            if(newValue_z < 0){
                //newValue_z = 0;
                /*newValue_z = -newValue_z;
                newValue_x = -newValue_x;
                newValue_y = -newValue_y;*/
            }
            if(((size-1) - 2*collumns) >= 0){ // pokud jsme v poli
                n[3*((size-1) - 2*collumns)] = buffer[mod];
                n[3*((size-1) - 2*collumns)+1] = buffer[mod+1];
                n[3*((size-1) - 2*collumns)+2] = buffer[mod+2];
            }
            len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
            buffer[mod] = newValue_x/len;
            buffer[mod+1] = newValue_y/len;
            buffer[mod+2] = newValue_z/len;
            mod+=3;
            if(mod == 2*3*collumns){
                mod = 0;
            }
            for(int i = (size - 2*collumns); i < size; i++){
                n[3*i] = buffer[mod];
                n[3*i+1] = buffer[mod+1];
                n[3*i+2] = buffer[mod+2];
                mod+=3;
                if(mod == 2*3*collumns){
                    mod = 0;
                }
            }
            //System.out.println("l");
            mod = 0;
        }

        //normalizace

        /*for(int i = 0; i< size; i++){
            len = Math.sqrt(n[3*i]*n[3*i]+n[3*i+1]*n[3*i+1]+n[3*i+2]*n[3*i+2]);
            n[3*i] = n[3*i]/len;
            n[3*i+1] = n[3*i+1]/len;
            n[3*i+2] = n[3*i+2]/len;
        }*/
        //
        return n;
    }

    // 19.3.2017 METODA!!!!!! -- minimalizace po celych vektorech
    private double [] getDepthMapVEC(){
        // 4. step
        int size = collumns*rows;
        double [] n = new double[3*size];
        double [] e = interpolatedNormalEstimation();
        System.arraycopy(e,0,n,0,e.length);
        double x,y,z,l;

        Marker m;
        int p;
        int [] fixes = new int [markers.size()];
        double [] fx = new double[markers.size()];
        double [] fy = new double[markers.size()];
        double [] fz = new double[markers.size()];
        for(int j = 0; j < markers.size(); j++){
            m = markers.get(j);
            p = ((int)(m.getPosX()*(collumns-1)) + (int)(m.getPosY()*((rows-1)))*(collumns));
            fixes[j] = p;
            fx[j] = ((double)m.getX()-127.5)/255.0;
            fy[j] = ((double)m.getY()-127.5)/255.0;
            fz[j] = ((double)m.getZ()-127.5)/255.0;
        }

        for(int i = 0; i < collumns*rows; i++){
            for(int j = 0; j< markers.size(); j++){
                if(fixes[j] == i){
                    m = markers.get(j);
                    n[3*i] = fx[j];
                    n[3*i+1] = fy[j];
                    n[3*i+2] = fz[j];
                    n[3*(i+1)] = fx[j];
                    n[3*(i+1)+1] = fy[j];
                    n[3*(i+1)+2] = fz[j];
                    n[3*(i-1)] = fx[j];
                    n[3*(i-1)+1] = fy[j];
                    n[3*(i-1)+2] =fz[j];
                    n[3*(i-collumns)] = fx[j];
                    n[3*(i-collumns)+1] =fy[j];
                    n[3*(i-collumns)+2] = fz[j];
                    n[3*(i+collumns)] = fx[j];
                    n[3*(i+collumns)+1] = fy[j];
                    n[3*(i+collumns)+2] =fz[j];
                    break;
                }
            }

        }


        //uvodni estimate bez regularizace
        // seradime markery podle barvy ktere odpovidaji
        /*double [] gr = new double[markers.size()];

        markers.sort((m1, m2) -> {
            int posm1,posm2;
            posm1 = (int)(m1.getPosY()*rows*collumns + m1.getPosX()*collumns);
            posm2 = (int)(m2.getPosY()*rows*collumns + m2.getPosX()*collumns);
            double g1 = grayscale[posm1];
            double g2 = grayscale[posm2];
            return Double.compare(g1,g2);
        });
        for(int i = 0; i < gr.length; i++){
            Marker m = markers.get(i);
            int posm = (int)(m.getPosY()*rows*collumns + m.getPosX()*collumns);
            double gm = grayscale[posm];
            gr[i] = gm;
        }
        double gm;
        int m;
        double nx,ny,nz;
        double intv,val,pos;

        for(int i = 0; i < size; i++){
            gm = grayscale[i];
            m = 0;
            while(m < gr.length && gm > gr[m]) m++;
            if(m == 0){ // 0 - m1
                intv = gr[m];
                val = gm;
                pos = val/intv;
                nx = markers.get(m).getX()*pos;
                ny = markers.get(m).getY()*pos;
                nz = markers.get(m).getZ()*pos;
            } else if(m == gr.length){ // mn - 1
                intv = 1 - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos);
                ny = markers.get(m-1).getY()*(1-pos);
                nz = markers.get(m-1).getZ()*(1-pos);
            } else { // mi - mj
                intv = gr[m] - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos) + markers.get(m).getX()*(pos);
                ny = markers.get(m-1).getY()*(1-pos) + markers.get(m).getY()*(pos);
                nz = markers.get(m-1).getZ()*(1-pos) + markers.get(m).getZ()*(pos);
            }
            if(nz < 127.5) {
                nz = 127.5;
                /*nx = -nx;
                ny = -ny;
                nz = -nz;*/
           /* }
            nx = nx-127.5;
            ny = ny-127.5;
            nz = nz-127.5;
            l = Math.sqrt(nx*nx + ny*ny + nz*nz);
            n[3*i] = nx/l;
            n[3*i+1] = ny/l;
            n[3*i+2] = nz/l;

        }*/
        /*boolean skip = false;
        for(int i = 0; i < size; i++){
            x = ((q)*grayscale[i])/lightX;
            y = ((q)*grayscale[i])/lightY;
            z = ((q)*grayscale[i])/lightZ;
            if(z < 0) {
                //z = 0;
                x = -x;
                y = -y;
                z = -z;
            }
            l = Math.sqrt(x*x + y*y + z*z);

            //n[3*i] = x/l;
            //n[3*i+1] = y/l;
            //n[3*i+2] = z/l;
            /*for(int k = 0; k < fixes.length; k++){
                if(i == fixes[k] || i == fixes[k]+1 || i == fixes[k] -1 || i == fixes[k]+collumns || i == fixes[k]-collumns){
                    skip = true;
                    break;
                }
            }
            if(!skip){
                n[3*i] = 0;
                n[3*i+1] = 0;
                n[3*i+2] = 1;
            }
            skip = false;
        }*/

        /*if(n != null)
        return n;*/
        // buffer field
        int mod = 0;
        double [] buffer = new double [2*3*collumns];

        // matice s neighbours = nei

        int neighbourSize;

        double len;

        double newValue_x;
        double newValue_y;
        double newValue_z;

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        double al4,al3,al2;
        double be4,be3,be2;
        double de4,de3,de2;
        double ga4,ga3,ga2;

        de4 = (lightX*lightY)/(lightX*lightX + lm*4);
        de3 = (lightX*lightY)/(lightX*lightX + lm*3);
        de2 = (lightX*lightY)/(lightX*lightX + lm*2);

        al4 = (lightY*lightX)/(lightY*lightY + lm*4 - (lightX*lightY)*de4);
        al3 = (lightY*lightX)/(lightY*lightY + lm*3 - (lightX*lightY)*de3);
        al2 = (lightY*lightX)/(lightY*lightY + lm*2 - (lightX*lightY)*de2);

        be4 = (lightY*lightZ)/(lightY*lightY + lm*4 - (lightX*lightY)*de4);
        be3 = (lightY*lightZ)/(lightY*lightY + lm*3 - (lightX*lightY)*de3);
        be2 = (lightY*lightZ)/(lightY*lightY + lm*2 - (lightX*lightY)*de2);

        ga4 = (lightX*lightZ)/(lightX*lightX + lm*4);
        ga3 = (lightX*lightZ)/(lightX*lightX + lm*3);
        ga2 = (lightX*lightZ)/(lightX*lightX + lm*2);
        //double [] s = new double[size];

        for(int g = 0; g < steps; g++){ // hlavni loop = pocet kroku gauss-siedela
            mod+=3;
            //horni radka

            for(int i = 1; i < collumns-1;i++){ // bereme x,y,z najednou
                mod+=3;
            }

            // pixel vpravo nahore
            mod+=3;

            neighbourSize = 3;
            // po radcich telo
            for(int i = collumns; i < size - collumns; i += collumns){
                //prvek vlevo

                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[mod];
                    n[3*(i - 2*collumns)+1] = buffer[mod+1];
                    n[3*(i - 2*collumns)+2] = buffer[mod+2];
                }
                mod+=3;

                //prostredek
                neighbourSize = 5;

                colskip:
                for(int j = 1; j < collumns-1; j++){ // vnitrek radku
                    // soucet n_x sousedu
                    //System.out.println("=");
                    /*for(int k = 0; k < fixes.length; k++){
                        if(i+j == fixes[k] || i+j+1 == fixes[k] || i+j -1 == fixes[k] || i+j+collumns == fixes[k] || i+j-collumns == fixes[k]){
                            if(((i+j) - 2*collumns) >= 0){ // pokud jsme v poli
                                n[3*((i+j) - 2*collumns)] = buffer[mod];
                                n[3*((i+j) - 2*collumns)+1] = buffer[mod+1];
                                n[3*((i+j) - 2*collumns)+2] = buffer[mod+2];
                            }
                            buffer[mod] = fx[k];
                            buffer[mod+1] =  fy[k];
                            buffer[mod+2] =  fz[k];
                            mod+=3;

                            continue colskip;
                        }
                    }*/

                    sumX = n[3*(i+j) - 3*collumns] + n[3*(i+j) + 3*collumns] + n[3*(i+j) + 3] + n[3*(i+j) - 3] + e[3*(i+j)];
                    sumY = n[3*(i+j) - 3*collumns + 1] + n[3*(i+j) + 3*collumns + 1] + n[3*(i+j) + 4] + n[3*(i+j) - 2] + e[3*(i+j)+1];
                    sumZ = n[3*(i+j) - 3*collumns + 2] + n[3*(i+j) + 3*collumns + 2] + n[3*(i+j) + 5] + n[3*(i+j) - 1] + e[3*(i+j)+2];

                    newValue_z =
                            ((1/q)*grayscale[(i+j)]*(lightZ - lightX*ga4 + lightY*ga4*al4 - lightY*be4 - lightX*de4*ga4*al4 + lightX*de4*be4) + lm*(sumX*de4*be4 - sumY*be4 - sumX*ga4 + sumY*ga4*al4 - sumX*de4*ga4*al4 + sumZ))
                            /
                            (lightZ*lightZ + lm*neighbourSize - lightZ*lightY*be4 + lightZ*lightX*de4*be4 - lightZ*lightX*ga4 + lightZ*lightY*al4*ga4 - lightZ*lightX*al4*de4*ga4);

                    newValue_y =
                            ((1/q)*grayscale[(i+j)]*(lightY - lightX*de4) + newValue_z*lightZ*(lightX*de4 - lightY) + lm*(sumY - sumX*de4))
                            /
                            (lightY*lightY + lm*neighbourSize - lightY*lightX*de4);

                    newValue_x =
                            ((1/q)*grayscale[(i+j)]*lightX - newValue_y*lightY*lightX - newValue_z*lightZ*lightX + lm*sumX)
                            /
                            (lightX*lightX + lm*neighbourSize);

                    if(newValue_z < 0){
                        newValue_z = -newValue_z;
                        /*newValue_z = -newValue_z;
                        newValue_x = -newValue_x;
                        newValue_y = -newValue_y;*/
                    }

                    if(((i+j) - 2*collumns) >= 0){ // pokud jsme v poli
                        n[3*((i+j) - 2*collumns)] = buffer[mod];
                        n[3*((i+j) - 2*collumns)+1] = buffer[mod+1];
                        n[3*((i+j) - 2*collumns)+2] = buffer[mod+2];
                    }

                    len = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                    buffer[mod] = newValue_x/len;
                    buffer[mod+1] = newValue_y/len;
                    buffer[mod+2] = newValue_z/len;
                    mod+=3;
                }

                //prvek vpravo
                if(((i+collumns-1) - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*((i+collumns-1) - 2*collumns)] = buffer[mod];
                    n[3*((i+collumns-1) - 2*collumns)+1] = buffer[mod+1];
                    n[3*((i+collumns-1) - 2*collumns)+2] = buffer[mod+2];
                }
                mod+=3;
                if(mod == 2*3*collumns){
                    mod = 0;
                }

            }

            // pixel vlevo dole
            if(((size - collumns) - 2*collumns) >= 0){ // pokud jsme v poli
                n[3*((size - collumns) - 2*collumns)] = buffer[mod];
                n[3*((size - collumns) - 2*collumns)+1] = buffer[mod+1];
                n[3*((size - collumns) - 2*collumns)+2] = buffer[mod+2];
            }
            mod+=3;

            //spodni radka
            neighbourSize = 3;
            for(int i = size - collumns + 1; i < size-1;i++){ // bereme x,y,z najednou
                if((i - 2*collumns) >= 0){ // pokud jsme v poli
                    n[3*(i - 2*collumns)] = buffer[mod];
                    n[3*(i - 2*collumns)+1] = buffer[mod+1];
                    n[3*(i - 2*collumns)+2] = buffer[mod+2];
                }
                mod+=3;
            }

            // pixel vpravo dole
            if(((size-1) - 2*collumns) >= 0){ // pokud jsme v poli
                n[3*((size-1) - 2*collumns)] = buffer[mod];
                n[3*((size-1) - 2*collumns)+1] = buffer[mod+1];
                n[3*((size-1) - 2*collumns)+2] = buffer[mod+2];
            }
            mod+=3;
            if(mod == 2*3*collumns){
                mod = 0;
            }
            for(int i = (size - 2*collumns); i < size; i++){
                n[3*i] = buffer[mod];
                n[3*i+1] = buffer[mod+1];
                n[3*i+2] = buffer[mod+2];
                mod+=3;
                if(mod == 2*3*collumns){
                    mod = 0;
                }
            }
            //System.out.println("l");
            mod = 0;

            /**
             *
             *  OPISOVANI DO OKRAJU
             *
             */
            // HORNI RADKA
            //levy horni roh
            n[0] = n[3*(1+collumns)];
            n[1] = n[3*(1+collumns)+1];
            n[2] = n[3*(1+collumns)+2];
            //horni radka
            for(int i = 1; i < collumns-1; i++){
                n[3*i] = n[3*(i+collumns)];
                n[3*i+1] = n[3*(i+collumns)+1];
                n[3*i+2] = n[3*(i+collumns)+2];
            }
            // pravy horni roh
            n[3*(collumns-1)] = n[3*(collumns-2+collumns)];
            n[3*(collumns-1)+1] = n[3*(collumns-2+collumns)+1];
            n[3*(collumns-1)+2] = n[3*(collumns-2+collumns)+2];
            // SPODNI RADKA
            //levy spodni roh
            n[3*(size - collumns)] = n[3*(size - 2*collumns+1)];
            n[3*(size - collumns)+1] = n[3*(size - 2*collumns+1)+1];
            n[3*(size - collumns)+2] = n[3*(size - 2*collumns+1)+2];
            //spodni radek
            for(int i = size-collumns+1; i < size-1; i++){
                n[3*i] = n[3*(i-collumns)];
                n[3*i+1] = n[3*(i-collumns)+1];
                n[3*i+2] = n[3*(i-collumns)+2];
            }
            //pravy spodni roh
            n[3*(size-1)] = n[3*((size-2)-collumns)];
            n[3*(size-1)+1] = n[3*((size-2)-collumns)+1];
            n[3*(size-1)+2] = n[3*((size-2)-collumns)+2];

            //boky
            //TELO
            for(int j = collumns; j < size - collumns; j+=collumns){ // projizdime radky
                //levy prvek
                n[3*j] = n[3*(j+1)];
                n[3*j+1] = n[3*(j+1)+1];
                n[3*j+2] = n[3*(j+1)+2];
                //pravy prvek
                n[3*(j + collumns-1)] = n[3*(j + collumns-2)];
                n[3*(j + collumns-1)+1] = n[3*(j + collumns-2)+1];
                n[3*(j + collumns-1)+2] = n[3*(j + collumns-2)+2];
            }



        }

        //normalizace

        /*for(int i = 0; i < size; i++){
            n[3*i] = mod;
            n[3*i] = n[3*i+1];
            n[3*i+1] = mod;
            //n[3*i+1] = -n[3*i+1];
        }*/

        double fi = Math.toRadians(90.0);
        double cs = Math.cos(fi);
        double sn = Math.sin(fi);
        double px,py;

        /*for(int i = 0; i < size; i++){
            px = n[3*i]*cs - n[3*i+1]*sn;
            py = n[3*i]*sn + n[3*i+1]*cs;
            n[3*i] = px;
            n[3*i+1] = py;
        }*/

        /*for(int i = 0; i< size; i++){
            len = Math.sqrt(n[3*i]*n[3*i]+n[3*i+1]*n[3*i+1]+n[3*i+2]*n[3*i+2]);
            n[3*i] = n[3*i]/len;
            n[3*i+1] = n[3*i+1]/len;
            n[3*i+2] = n[3*i+2]/len;
        }*/
        //
        return n;
    }

    private double [] getDepthMap(){
        // 4. step
        int size = collumns*rows;
        double [] n = new double[3*size];
        double x,y,z,l,sum;
        boolean eq = true;


        //uvodni estimate bez regularizace
        // seradime markery podle barvy ktere odpovidaji
        double [] gr = new double[markers.size()];

        markers.sort((m1, m2) -> {
            int posm1,posm2;
            posm1 = (int)(m1.getPosY()*rows*collumns + m1.getPosX()*collumns);
            posm2 = (int)(m2.getPosY()*rows*collumns + m2.getPosX()*collumns);
            double g1 = grayscale[posm1];
            double g2 = grayscale[posm2];
            return Double.compare(g1,g2);
        });
        for(int i = 0; i < gr.length; i++){
            Marker m = markers.get(i);
            int posm = (int)(m.getPosY()*rows*collumns + m.getPosX()*collumns);
            double gm = grayscale[posm];
            gr[i] = gm;
        }
        double gm;
        int m;
        double nx,ny,nz;
        double intv,val,pos;

        for(int i = 0; i < size; i++){
            gm = grayscale[i];
            m = 0;
            while(m < gr.length && gm > gr[m]) m++;
            if(m == 0){ // 0 - m1
                intv = gr[m];
                val = gm;
                pos = val/intv;
                nx = markers.get(m).getX()*pos;
                ny = markers.get(m).getY()*pos;
                nz = markers.get(m).getZ()*pos;
            } else if(m == gr.length){ // mn - 1
                intv = 1 - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos);
                ny = markers.get(m-1).getY()*(1-pos);
                nz = markers.get(m-1).getZ()*(1-pos);
            } else { // mi - mj
                intv = gr[m] - gr[m-1];
                val = gm - gr[m-1];
                pos = val/intv;
                nx = markers.get(m-1).getX()*(1-pos) + markers.get(m).getX()*(pos);
                ny = markers.get(m-1).getY()*(1-pos) + markers.get(m).getY()*(pos);
                nz = markers.get(m-1).getZ()*(1-pos) + markers.get(m).getZ()*(pos);
            }
            if(nz < 127.5) {
                nz = 127.5;
                /*nx = -nx;
                ny = -ny;
                nz = -nz;*/
            }
            l = Math.sqrt(nx*nx + ny*ny + nz*nz);
            n[3*i] = nx/l;
            n[3*i+1] = ny/l;
            n[3*i+2] = nz/l;

        }

        /*for(int i = 0; i < size; i++){
            x = ((q)*grayscale[i])/lightX;
            y = ((q)*grayscale[i])/lightY;
            z = ((q)*grayscale[i])/lightZ;
            if(z < 0) {
                //z = 0;
                x = -x;
                y = -y;
                z = -z;
            }
            l = Math.sqrt(x*x + y*y + z*z);

            n[3*i] = x/l;
            n[3*i+1] = y/l;
            n[3*i+2] = z/l;
        }*/
        /*if(n != null)
        return n;*/

        for(int g = 0; g < steps; g++) { // hlavni loop = pocet kroku gauss-siedela

            // projizdeni horizontalne

            //prvek vlevo nahore
            //x
            sum = n[3] + n[3*collumns];
            x = (lightX*((1/q)*grayscale[0] - n[1]*lightY - n[2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
            //y
            sum = n[4] + n[3*collumns+1];
            y = (lightY*((1/q)*grayscale[0] - n[0]*lightX - n[2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
            //z
            sum = n[5] + n[3*collumns+2];
            z = (lightZ*((1/q)*grayscale[0] - n[1]*lightY - n[0]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
            l = Math.sqrt(x*x + y*y + z*z);
            x = x/l;
            y = y/l;
            z = z/l;
            if(z < 0.0){
                z = -z;
                x = -x;
                y = -y;
            }
            n[0] = x;
            n[1] = y;
            n[2] = z;

            //HORNI RADKA
            for(int i  = 1; i < collumns-1; i++){
                //x
                sum = n[3*(i-1)] + n[3*(i+1)] + n[3*i + 3*(collumns)];
                x = (lightX*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                //y
                sum = n[3*(i-1)+1] + n[3*(i+1)+1] + n[3*i + 3*(collumns)+1];
                y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                //z
                sum = n[3*(i-1)+2] + n[3*(i+1)+2] + n[3*i + 3*(collumns)+2];
                z = (lightZ*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                l = Math.sqrt(x*x + y*y + z*z);
                x = x/l;
                y = y/l;
                z = z/l;
                if(z < 0.0){
                    z = -z;
                    x = -x;
                    y = -y;
                }
                n[3*i] = x;
                n[3*i+1] = y;
                n[3*i+2] = z;

            }
            //x
            sum = n[3*(collumns-1-1)] + n[3*(collumns-1) + 3*(collumns)];
            x = (lightX*((1/q)*grayscale[(collumns-1)] - n[3*(collumns-1)+1]*lightY - n[3*(collumns-1)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
            //y
            sum = n[3*(collumns-1-1)+1] + n[3*(collumns-1) + 3*(collumns)+1];
            y = (lightY*((1/q)*grayscale[collumns-1] - n[3*(collumns-1)]*lightX - n[3*(collumns-1)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
            //z
            sum = n[3*(collumns-1-1)+2] + n[3*(collumns-1) + 3*(collumns)+2];
            z = (lightZ*((1/q)*grayscale[collumns-1] - n[3*(collumns-1)+1]*lightY - n[3*(collumns-1)]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
            l = Math.sqrt(x*x + y*y + z*z);
            x = x/l;
            y = y/l;
            z = z/l;
            if(z < 0.0){
                z = -z;
                x = -x;
                y = -y;
            }
            n[3*(collumns-1)] = x;
            n[3*(collumns-1)+1] = y;
            n[3*(collumns-1)+2] = z;


            // TELO
            for(int j = collumns; j < size-collumns; j+= collumns){
                if(eq){ // projizdime zprava doleva <-----
                    //okrajovy prvek vpravo
                    //x
                    sum = n[3*(j + collumns - 1-1)] + n[3*(j + collumns - 1) - 3*(collumns)] + n[3*(j + collumns - 1) + 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)+1]*lightY - n[3*(j + collumns - 1)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(j + collumns - 1-1)+1] + n[3*(j + collumns - 1) - 3*(collumns)+1] + n[3*(j + collumns - 1) + 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)]*lightX - n[3*(j + collumns - 1)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(j + collumns - 1-1)+2] + n[3*(j + collumns - 1) - 3*(collumns)+2] + n[3*(j + collumns - 1) + 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)+1]*lightY - n[3*(j + collumns - 1)]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*(j + collumns - 1)] = x;
                    n[3*(j + collumns - 1)+1] = y;
                    n[3*(j + collumns - 1)+2] = z;


                    for(int i = collumns-2; i > 0; i--){
                        //x
                        sum = n[3*(j+i-1)] + n[3*(j+i) - 3*(collumns)] + n[3*(j+i) + 3*(collumns)] + n[3*(j+i + 1)];
                        x = (lightX*((1/q)*grayscale[(j+i)] - n[3*(j+i)+1]*lightY - n[3*(j+i)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*4);
                        //y
                        sum = n[3*(j+i-1)+1] + n[3*(j+i) - 3*(collumns)+1] + n[3*(j+i) + 3*(collumns)+1] + n[3*(j+i + 1)+1];
                        y = (lightY*((1/q)*grayscale[(j+i)] - n[3*(j+i)]*lightX - n[3*(j+i)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*4);
                        //z
                        sum = n[3*(j+i-1)+2] + n[3*(j+i) - 3*(collumns)+2] + n[3*(j+i) + 3*(collumns)+2] + n[3*(j+i + 1)+2];
                        z = (lightZ*((1/q)*grayscale[(j+i)] - n[3*(j+i)+1]*lightY - n[3*(j+i)]*lightX) + lm*sum)/(lightZ*lightZ + lm*4);
                        l = Math.sqrt(x*x + y*y + z*z);
                        x = x/l;
                        y = y/l;
                        z = z/l;
                        if(z < 0.0){
                            z = -z;
                            x = -x;
                            y = -y;
                        }
                        n[3*(j+i)] = x;
                        n[3*(j+i)+1] = y;
                        n[3*(j+i)+2] = z;
                    }
                    //okrajovy prvek vlevo
                    //x
                    sum = n[3*(j+1)] + n[3*(j) - 3*(collumns)] + n[3*(j) + 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[(j)] - n[3*(j)+1]*lightY - n[3*(j)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(j+1)+1] + n[3*(j) - 3*(collumns)+1] + n[3*(j) + 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[(j)] - n[3*(j)]*lightX - n[3*(j)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(j+1)+2] + n[3*(j) - 3*(collumns)+2] + n[3*(j) + 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[(j)] - n[3*(j)+1]*lightY - n[3*(j)]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*(j)] = x;
                    n[3*(j)+1] = y;
                    n[3*(j)+2] = z;

                } else { // zleva doprava ------>
                    //okrajovy prvek vlevo
                    //x
                    sum = n[3*(j+1)] + n[3*(j) - 3*(collumns)] + n[3*(j) + 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[(j)] - n[3*(j)+1]*lightY - n[3*(j)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(j+1)+1] + n[3*(j) - 3*(collumns)+1] + n[3*(j) + 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[(j)] - n[3*(j)]*lightX - n[3*(j)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(j+1)+2] + n[3*(j) - 3*(collumns)+2] + n[3*(j) + 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[(j)] - n[3*(j)+1]*lightY - n[3*(j)]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*(j)] = x;
                    n[3*(j)+1] = y;
                    n[3*(j)+2] = z;

                    for(int i = 1; i < collumns-1; i++){
                        //x
                        sum = n[3*(j+i-1)] + n[3*(j+i) - 3*(collumns)] + n[3*(j+i) + 3*(collumns)] + n[3*(j+i + 1)];
                        x = (lightX*((1/q)*grayscale[(j+i)] - n[3*(j+i)+1]*lightY - n[3*(j+i)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*4);
                        //y
                        sum = n[3*(j+i-1)+1] + n[3*(j+i) - 3*(collumns)+1] + n[3*(j+i) + 3*(collumns)+1] + n[3*(j+i + 1)+1];
                        y = (lightY*((1/q)*grayscale[(j+i)] - n[3*(j+i)]*lightX - n[3*(j+i)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*4);
                        //z
                        sum = n[3*(j+i-1)+2] + n[3*(j+i) - 3*(collumns)+2] + n[3*(j+i) + 3*(collumns)+2] + n[3*(j+i + 1)+2];
                        z = (lightZ*((1/q)*grayscale[(j+i)] - n[3*(j+i)+1]*lightY - n[3*(j+i)]*lightX) + lm*sum)/(lightZ*lightZ + lm*4);
                        l = Math.sqrt(x*x + y*y + z*z);
                        x = x/l;
                        y = y/l;
                        z = z/l;
                        if(z < 0.0){
                            z = -z;
                            x = -x;
                            y = -y;
                        }
                        n[3*(j+i)] = x;
                        n[3*(j+i)+1] = y;
                        n[3*(j+i)+2] = z;
                    }
                    //okrajovy prvek vpravo
                    //x
                    sum = n[3*(j + collumns - 1-1)] + n[3*(j + collumns - 1) - 3*(collumns)] + n[3*(j + collumns - 1) + 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)+1]*lightY - n[3*(j + collumns - 1)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(j + collumns - 1-1)+1] + n[3*(j + collumns - 1) - 3*(collumns)+1] + n[3*(j + collumns - 1) + 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)]*lightX - n[3*(j + collumns - 1)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(j + collumns - 1-1)+2] + n[3*(j + collumns - 1) - 3*(collumns)+2] + n[3*(j + collumns - 1) + 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[(j + collumns - 1)] - n[3*(j + collumns - 1)+1]*lightY - n[3*(j + collumns - 1)]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*(j + collumns - 1)] = x;
                    n[3*(j + collumns - 1)+1] = y;
                    n[3*(j + collumns - 1)+2] = z;

                }
                eq = !eq;
            }

            if(eq){ // projizdime doleva <-----
                //prvek vpravo dole
                //x
                sum = n[3*(size-1-1)] + n[3*(size-1) - 3*(collumns)];
                x = (lightX*((1/q)*grayscale[(size-1)] - n[3*(size-1)+1]*lightY - n[3*(size-1)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
                //y
                sum = n[3*(size-1-1)+1] + n[3*(size-1) - 3*(collumns)+1];
                y = (lightY*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
                //z
                sum = n[3*(size-1-1)+2] + n[3*(size-1) - 3*(collumns)+2];
                z = (lightZ*((1/q)*grayscale[(size-1)] - n[3*(size-1)+1]*lightY - n[3*(size-1)]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
                l = Math.sqrt(x*x + y*y + z*z);
                x = x/l;
                y = y/l;
                z = z/l;
                if(z < 0.0){
                    z = -z;
                    x = -x;
                    y = -y;
                }
                n[3*(size-1)] = x;
                n[3*(size-1)+1] = y;
                n[3*(size-1)+2] = z;

                //SPODNI RADKA
                for(int i  = size-2; i > size-collumns; i--){
                    //x
                    sum = n[3*(i-1)] + n[3*(i+1)] + n[3*i - 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(i-1)+1] + n[3*(i+1)+1] + n[3*i - 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(i-1)+2] + n[3*(i+1)+2] + n[3*i - 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*i] = x;
                    n[3*i+1] = y;
                    n[3*i+2] = z;
                }
                //prvek vlevo dole
                //x
                sum = n[3*(size-collumns+1)] + n[3*(size-collumns) - 3*(collumns)];
                x = (lightX*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)+1]*lightY - n[3*(size-collumns)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
                //y
                sum = n[3*(size-collumns+1)+1] + n[3*(size-collumns) - 3*(collumns)+1];
                y = (lightY*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)]*lightX - n[3*(size-collumns)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
                //z
                sum = n[3*(size-collumns+1)+2] + n[3*(size-collumns) - 3*(collumns)+2];
                z = (lightZ*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)+1]*lightY - n[3*(size-collumns)]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
                l = Math.sqrt(x*x + y*y + z*z);
                x = x/l;
                y = y/l;
                z = z/l;
                if(z < 0.0){
                    z = -z;
                    x = -x;
                    y = -y;
                }
                n[3*(size-collumns)] = x;
                n[3*(size-collumns)+1] = y;
                n[3*(size-collumns)+2] = z;


            } else { // projizdime doprava ----->
                //prvek vlevo dole
                //x
                sum = n[3*(size-collumns+1)] + n[3*(size-collumns) - 3*(collumns)];
                x = (lightX*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)+1]*lightY - n[3*(size-collumns)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
                //y
                sum = n[3*(size-collumns+1)+1] + n[3*(size-collumns) - 3*(collumns)+1];
                y = (lightY*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)]*lightX - n[3*(size-collumns)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
                //z
                sum = n[3*(size-collumns+1)+2] + n[3*(size-collumns) - 3*(collumns)+2];
                z = (lightZ*((1/q)*grayscale[(size-collumns)] - n[3*(size-collumns)+1]*lightY - n[3*(size-collumns)]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
                l = Math.sqrt(x*x + y*y + z*z);
                x = x/l;
                y = y/l;
                z = z/l;
                if(z < 0.0){
                    z = -z;
                    x = -x;
                    y = -y;
                }
                n[3*(size-collumns)] = x;
                n[3*(size-collumns)+1] = y;
                n[3*(size-collumns)+2] = z;
                //SPODNI RADKA
                for(int i  = size-collumns+1; i < size-1; i++){
                    //x
                    sum = n[3*(i-1)] + n[3*(i+1)] + n[3*i - 3*(collumns)];
                    x = (lightX*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i+2]*lightZ) + lm*sum)/(lightX*lightX + lm*3);
                    //y
                    sum = n[3*(i-1)+1] + n[3*(i+1)+1] + n[3*i - 3*(collumns)+1];
                    y = (lightY*((1/q)*grayscale[i] - n[3*i]*lightX - n[3*i+2]*lightZ) + lm*sum)/(lightY*lightY + lm*3);
                    //z
                    sum = n[3*(i-1)+2] + n[3*(i+1)+2] + n[3*i - 3*(collumns)+2];
                    z = (lightZ*((1/q)*grayscale[i] - n[3*i+1]*lightY - n[3*i]*lightX) + lm*sum)/(lightZ*lightZ + lm*3);
                    l = Math.sqrt(x*x + y*y + z*z);
                    x = x/l;
                    y = y/l;
                    z = z/l;
                    if(z < 0.0){
                        z = -z;
                        x = -x;
                        y = -y;
                    }
                    n[3*i] = x;
                    n[3*i+1] = y;
                    n[3*i+2] = z;
                }
                //prvek vpravo dole
                //x
                sum = n[3*(size-1-1)] + n[3*(size-1) - 3*(collumns)];
                x = (lightX*((1/q)*grayscale[(size-1)] - n[3*(size-1)+1]*lightY - n[3*(size-1)+2]*lightZ) + lm*sum)/(lightX*lightX + lm*2);
                //y
                sum = n[3*(size-1-1)+1] + n[3*(size-1) - 3*(collumns)+1];
                y = (lightY*((1/q)*grayscale[(size-1)] - n[3*(size-1)]*lightX - n[3*(size-1)+2]*lightZ) + lm*sum)/(lightY*lightY + lm*2);
                //z
                sum = n[3*(size-1-1)+2] + n[3*(size-1) - 3*(collumns)+2];
                z = (lightZ*((1/q)*grayscale[(size-1)] - n[3*(size-1)+1]*lightY - n[3*(size-1)]*lightX) + lm*sum)/(lightZ*lightZ + lm*2);
                l = Math.sqrt(x*x + y*y + z*z);
                x = x/l;
                y = y/l;
                z = z/l;
                if(z < 0.0){
                    z = -z;
                    x = -x;
                    y = -y;
                }
                n[3*(size-1)] = x;
                n[3*(size-1)+1] = y;
                n[3*(size-1)+2] = z;
            }

            eq = true;
        }
        return n;
    }

    private void getLightSource(){
        double[][] valsA;
        double[][] valsB;

        valsA = new double[markers.size()][3];
        valsB = new double[markers.size()][1];

        Marker m;
        for(int i = 0; i < 3; i++){
            m = markers.get(i);
            //System.out.println(m.getX()+" "+m.getY()+" "+m.getZ());
            valsA[i] = new double[]{(m.getX()-127.5),(m.getY()-127.5),m.getZ()-127.5};
            //valsB[i] = new double[]{(double)(grayscale[(int)((int)(rows*m.getPosY())*collumns+collumns*m.getPosX())-1])};
            valsB[i] = new double[]{((int)(m.getPosX()*(collumns-1)) + (int)(m.getPosY()*((rows-1)))*(collumns))};
        }

        Matrix A = new Matrix(valsA);
        Matrix b = new Matrix(valsB);
        Matrix x = A.solve(b);

        /*lightX = x.get(0,0)/q;
        lightY = x.get(1,0)/q;
        lightZ = x.get(2,0)/q;*/

        lightX = x.get(0,0);
        lightY = x.get(1,0);
        lightZ = x.get(2,0);

        double size = Math.sqrt((lightX*lightX)+lightY*lightY+lightZ*lightZ);
        lightX=-lightX/size;
        lightY=lightY/size;
        lightZ=lightZ/size;

        /*lightX = 127.5*(lightX+1);
        lightY = 127.5*(lightY+1);
        lightZ = 127.5*(lightZ+1);*/
        //System.out.println(lightX+" "+lightY+" "+lightZ);

    }


    /**
     * VRACI V POLI JEDNA HODNOTA COLLUMNS x ROWS
     *
     */
    private void getGrayscale(){
        int off; // offset in array
        double [] out;


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
        out = new double [collumns*rows];
        off += 5;
        bodyStart = off;
        int val;
        for(i = 0; i < out.length; i++){
            val = (int)(0.2126*(fr[i*3 + off] & 0xFF) + 0.7152*(fr[i*3 + 1 + off] & 0xFF) + 0.0722*(fr[i*3 + 2 + off] & 0xFF));
            //val = (int)(0.5*(fr[i*3 + off] & 0xFF) + 0.5*(fr[i*3 + 1 + off] & 0xFF) + 0.5*(fr[i*3 + 2 + off] & 0xFF));
            //val = (int)(((fr[i*3 + off] & 0xFF) + (fr[i*3 + 1 + off] & 0xFF) + (fr[i*3 + 2 + off] & 0xFF))/3);
            if(val < 0) val = 0;
            else if (val > 255) val = 255;
            out[i] = ( (val/127.5)-1);
            //System.out.println(out[i]);
        }
        grayscale = out;
        //return new GrayscaleResultClass(max,min,avg,off,collumns,rows,out);
    }


    @Override
    public int getSteps() {
        return 0;
    }

    public void setAlbedo(double albedo) {
        this.q = albedo;
    }

    public void setRegularization(double regularization) {
        this.lm = regularization;
    }


}
