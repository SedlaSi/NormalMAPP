package algorithms;

import Jama.Matrix;
import gui.mark.Marker;
import gui.session.LoadingScreen;

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
        double [] n = getHeightMap();
        grayscale = null;

        for(int i = bodyStart; i< n.length ; i++){
            fr[i] = (byte)((n[i]+1)*127.5);
        }
        finishDepths(relativeHeights());
        //return fr;
        return fr;
    }

    private void finishDepths(byte[] rel) { // prepocteni relativnich vysek do height mapy a zapsani do fr

    }

    private byte [] relativeHeights(){
        int size = collumns*rows;
        byte [] relativeHeights = new byte [2*collumns*rows];
        for(int i = 0; i < size; i++){

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
        ShapeFromShading sfs = new ShapeFromShading();
        sfs.getDepthMap();

        int lm = 1;
        double lightX = 0.5;
        double lightY = 0.5;
        double lightZ = 0.5;
        int collumns = 2;
        int rows = 2;

        double [] nei = new double[]{2*lm+lightX,2*lm+lightY,2*lm+lightZ,
                3*lm+lightX,3*lm+lightY,3*lm+lightZ,
                4*lm+lightX,4*lm+lightY,4*lm+lightZ
        };

        double [] n = new double[3*collumns*rows];

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
        System.out.println();
        for(int j =0; j< rows; j++){
            for(int i=0;i<4*collumns;i+=4){
                System.out.print(index[j*4*collumns+i]+" "+index[j*4*collumns+i+1]+" "+index[j*4*collumns+i+2]+" "+index[j*4*collumns+i+3]+"|");
            }
            System.out.println();
        }
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

        // matice s neighbours = nei

        double [] nei = new double[]{2*lm+lightX,2*lm+lightY,2*lm+lightZ, // 0 + 0; 0+1; 0+2
                                     3*lm+lightX,3*lm+lightY,3*lm+lightZ, // 3+0; 3+1; 3+2
                                     4*lm+lightX,4*lm+lightY,4*lm+lightZ // 6 + 0; 6 + 1; 6+2
                                    };

        double [] n = new double[3*collumns*rows];
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
        int size = collumns*rows;
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
                n_1 = Math.sqrt(newValue_x*newValue_x + newValue_y*newValue_y + newValue_z*newValue_z); // length
                n[3*i] = newValue_x/n_1;
                n[3*i+1] = newValue_y/n_1;
                n[3*i+2] = newValue_z/n_1;

                //System.out.println(n[3*i]+" "+n[3*i+1]+" "+n[3*i+2]);

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

        lightX = x.get(0,0);
        lightY = x.get(1,0);
        lightZ = x.get(2,0);

        double size = Math.sqrt((lightX*lightX)+lightY*lightY+lightZ*lightZ);
        lightX=lightX/size;
        lightY=lightY/size;
        lightZ=lightZ/size;

        /*lightX = 127.5*(lightX+1);
        lightY = 127.5*(lightY+1);
        lightZ = 127.5*(lightZ+1);*/
        System.out.println(lightX+" "+lightY+" "+lightZ);

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

    @Override
    public void setLoadingScreen(LoadingScreen loadingScreen) {

    }
}
