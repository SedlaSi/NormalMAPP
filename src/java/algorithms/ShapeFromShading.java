package algorithms;

import Jama.Matrix;
import gui.sfs.Marker;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by root on 5.11.16.
 */
public class ShapeFromShading implements Algorithm {

    private final static double ROUND_ANGLE = 2.0;
    private final static double FLAT_ANGLE = 10.0;
    //private final static double FLAT_ANGLE = 0;
    private final static double MAX_RELATIVE_HEIGHT = 2;
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

        //getDepthMap();
        normalField = getHeightMap();
        grayscale = null;

        // VYPISOVANI RELATIVNICH VYSEK
        /*double [] rel = relativeHeights();
        for(int i = bodyStart; i< rel.length-1 ; i+=2){
            fr[(i/2)*3] = (byte)((rel[i+1])*255);
            fr[(i/2)*3+1] = (byte)((rel[i+1])*255);
            fr[(i/2)*3+2] = (byte)((rel[i+1])*255);
        }*/

        // VYPISOVANI PLOCHYCH NORMAL
        /*for(int i = bodyStart; i< normalField.length ; i++){
            //fr[i] = (byte)((normalField[i]+1)*127.5);
            fr[i] = (byte)((normalField[i])*255);
        }*/

        // VYPISOVANI VYSKOVE MAPY
        //finishDepths(relativeHeights());

        // SKUTECNY KONEC
        absoluteHeights(relativeHeights());
        gong();
        return fr;
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
        for(int gauss = steps; gauss >= 0; gauss--){ // LOOP GAUSS-SEIDEL
            // HORNI RADKA

            //levy horni roh
            h1 = h[1] + q[0];
            h2 = h[collumns] + q[1];

            height = (h1 + h2)/2;
            h[0] = height;

            //horni radka
            for(int i = 1; i < collumns-1; i++){
                h1 = h[i+1] + q[2*i];
                h2 = h[i+collumns] + q[2*i+1];
                h3 = h[i-1] - q[2*(i-1)];

                height = (h1 + h2 + h3)/3;
                h[i] = height;
            }

            // pravy horni roh
            h2 = h[collumns-1+collumns] + q[2*(collumns-1)+1];
            h3 = h[collumns-1-1] - q[2*(collumns-1-1)];

            height = (h2 + h3)/2;
            h[collumns-1] = height;

            //TELO
            for(int j = collumns; j <= (rows-2)*collumns; j++){ // projizdime radky
                //levy prvek
                h1 = h[j + 1] + q[2*j];
                h2 = h[j + collumns] + q[2*j + 1];
                h4 = h[j - collumns] - q[2*(j-collumns)+1];
                height = (h1 + h2 + h4)/3;
                h[j] = height;

                for(int i = 1; i < collumns-1; i++){ // projizdime bunky v radcich
                    h1 = h[(j + i) + 1] + q[2*(j + i)];
                    h2 = h[(j + i) + collumns] + q[2*(j + i) + 1];
                    h3 = h[(j + i)-1] - q[2*((j + i)-1)];
                    h4 = h[(j + i) - collumns] - q[2*((j + i)-collumns)+1];

                    height = (h1 + h2 + h3 + h4)/4;
                    h[(j + i)] = height;
                }

                //pravy prvek
                h2 = h[(j + collumns-1) + collumns] + q[2*(j + collumns-1) + 1];
                h3 = h[(j + collumns-1)-1] - q[2*((j + collumns-1)-1)];
                h4 = h[(j + collumns-1) - collumns] - q[2*((j + collumns-1)-collumns)+1];
                height = (h2 + h3 + h4)/3;
                h[(j + collumns-1)] = height;


            }
            // SPODNI RADKA

            //levy spodni roh
            h1 = h[(size - collumns)+1] + q[2*(size - collumns)];
            h4 = h[(size - collumns) - collumns] - q[2*((size - collumns)-collumns)+1];

            height = (h1 + h4)/2;
            h[(size - collumns)] = height;

            //spodni radek
            for(int i = size-collumns; i < size-1; i++){
                h1 = h[i+1] + q[2*i];
                h3 = h[i-1] - q[2*(i-1)];
                h4 = h[i - collumns] - q[2*(i-collumns)+1];

                height = (h1 + h3 + h4)/3;
                h[i] = height;
            }

            //pravy spodni roh
            h3 = h[(size-1)-1] - q[2*((size-1)-1)];
            h4 = h[(size-1) - collumns] - q[2*((size-1)-collumns)+1];


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
                if((180-alpha) < FLAT_ANGLE || alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    qij = -(b/a);
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
                if((180-alpha) < FLAT_ANGLE || alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    qij = -(b/a);
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
                if((180-alpha) < FLAT_ANGLE || alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    qij = -(b/a);
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
                if((180-alpha) < FLAT_ANGLE || alpha < FLAT_ANGLE){ // skoro kolmice
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    qij = -(b/a);
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
        min = Math.abs(min);
        range = min + Math.abs(max);
        for(int i = 0; i < relativeHeights.length; i++ ){
            relativeHeights[i] = (relativeHeights[i]+min)/range;
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

    public static void main(String [] args){
        String headerString = "P3\n2 1\n";
        byte [] header = headerString.getBytes();
        byte [] fr = new byte [header.length + 6];

        ShapeFromShading sfs = new ShapeFromShading();
        sfs.normalField = new double[6];
        sfs.collumns = 2;
        sfs.rows = 1;
        double Ax = -1; //Math.sqrt(2)/2
        double Ay = 0;
        double Az = 0;
        double Bx = -Math.sqrt(2)/2;
        double By = 0;
        double Bz = Math.sqrt(2)/2;
        sfs.normalField[0] = Ax;
        sfs.normalField[1] = Ay;
        sfs.normalField[2] = Az;
        sfs.normalField[3] = Bx;
        sfs.normalField[4] = By;
        sfs.normalField[5] = Bz;

        double [] relatives = sfs.relativeHeights();
        /*System.out.println("=================");
        System.out.println("relative A: "+relatives[0]+" "+relatives[1]);
        System.out.println("relative B: "+relatives[2]+" "+relatives[3]);*/
        /*System.out.println("===================");
        System.out.println("A:");
        System.out.println("x = "+relatives[0]);
        System.out.println("y = "+relatives[1]);
        System.out.println("z = "+relatives[2]);
        System.out.println();
        System.out.println("B:");
        System.out.println("x = "+relatives[3]);
        System.out.println("y = "+relatives[4]);
        System.out.println("z = "+relatives[5]);*/

    }

    private void getDepthMap(){
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

    // AKTUALNI METODA!!!!!!
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

    private void getLightSource(){
        double[][] valsA;
        double[][] valsB;

        valsA = new double[markers.size()][3];
        valsB = new double[markers.size()][1];

        Marker m;
        for(int i = 0; i < markers.size(); i++){
            m = markers.get(i);
            valsA[i] = new double[]{m.getX(),m.getY(),m.getZ()};
            valsB[i] = new double[]{(double)(grayscale[(int)((rows*m.getPosY())*collumns+collumns*m.getPosX())-1])};
        }

        Matrix A = new Matrix(valsA);
        Matrix b = new Matrix(valsB);
        Matrix x = A.solve(b);

        lightX = x.get(0,0)/q;
        lightY = x.get(1,0)/q;
        lightZ = x.get(2,0)/q;

        double size = Math.sqrt((lightX*lightX)+lightY*lightY+lightZ*lightZ);
        lightX=lightX/size;
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

    public static void gong() {
        new Thread(() -> {
            try {
                Clip clip;
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        ShapeFromShading.class.getResourceAsStream("/gong/gong.wav"));
                DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
