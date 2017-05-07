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
 * Created by sedlasi1 on 5.11.16.
 *
 * ShapeFromShading class is used to calculate height map
 * from input image and markers provided by user.
 *
 * Algorithm used in this class:
 *      Interactive Normal Reconstruction from Single Image
 *
 * For further information about the algorithm, please visit:
 *      https://pdfs.semanticscholar.org/0d10/f43243d6b269be4d633e2c5c14d7b26cef10.pdf
 */
public class ShapeFromShading {

    private final static double ROUND_ANGLE = 2.0; //2.0
    private final static double FLAT_ANGLE = 5.0; // 5.0
    private final static double MAX_RELATIVE_HEIGHT = 2.0; // 2.0
    private final static double INTERPOLATION_LENGTH = 0.005; //0.005
    private int collumns;
    private int rows;
    private byte[] fr;
    private double[] grayscale;
    private List<Marker> markers;
    private double lightX;
    private double lightY;
    private double lightZ;
    private int steps = 20;
    private double q = 1;
    private double lm = 0.1;
    private double deltaE = 0.1;
    int bodyStart;
    private double[] normalField;

    /**
     * @method setImage() used to read ppm input image
     * from session folder.
     * @param path path to the input image in session folder
     */
    public void setImage(String path) {

        try {
            fr = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @method setSteps() sets calculation steps.
     * Used by Calculation steps slider in GUI.
     * @param steps number of calculation steps
     */
    public void setSteps(int steps) {
        this.steps = steps;
    }

    /**
     * @method setMarkers() set list of markers which will be used
     * in the calculation.
     * @param markers
     */
    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * @method shapeFromShading() starts the main computation
     * of the height map.
     * @return byte array height map in ppm image format
     */
    public byte[] shapeFromShading() {
        if (fr == null || markers == null) {
            System.out.println("NULL fr or markers");
            return null;
        }

        getGrayscale(); // 2. step
        getLightSource(); // 3. step

        normalField = getDepthMapVEC(); // now

        // VYPISOVANI PLOCHYCH NORMAL
        /*for(int i = bodyStart; i < normalField.length+bodyStart ; i++){
            //fr[i] = (byte)((normalField[i-bodyStart]+1.0)*127.0);
            fr[i] = (byte)((normalField[i-bodyStart]/2+0.5)*255.0);
            //fr[i] = (byte)((normalField[i-bodyStart])*255.0);
        }*/

        //this.write(fr,"/home/sedlasi1/Desktop/Skola/Bakalarska_prace/Dokumentace/latex/img/ch5/e2.ppm");
        grayscale = null;


        steps = steps * 100;
        //steps = 1000;
        absoluteHeightsNEW(relativeHeights()); // opravuje extremy v okrajich --> 18.3.2017 VPORADKU !!!!

        return fr;
    }

    /**
     * @method interpolatedNormalEstimation() is used to calculate
     * mask which is used as base for computation of height map.
     * Not in original algorithm.
     * @return field of double values representing normal vectors
     */
    private double[] interpolatedNormalEstimation() {
        int size = collumns * rows;
        double[] n = new double[3 * size];
        double[] markerLen = new double[markers.size()];
        double xM[] = new double[markers.size()];
        double yM[] = new double[markers.size()];
        int[] idxMarkers = new int[markers.size()];
        Marker m;
        for (int i = 0; i < markers.size(); i++) { // pozice markeru v x a y
            m = markers.get(i);
            xM[i] = (m.getPosX() * (collumns - 1));
            yM[i] = (m.getPosY() * ((rows - 1)));
        }
        double len;
        double x, y, z;
        int g;
        int tmp;
        for (double j = 0; j < rows; j++) {
            for (double i = 0; i < collumns; i++) { // jednotlive body na adrese (j*collumns + i)
                len = 0.0d;
                z = 0.0d;
                for (int k = 0; k < markers.size(); k++) { // vzdalenost markeru k od bodu (i,j)
                    markerLen[k] = Math.sqrt((i - xM[k]) * (i - xM[k]) + (j - yM[k]) * (j - yM[k]));
                    markerLen[k] = 1 / markerLen[k];

                    idxMarkers[k] = k;
                }
                // ted seradime idxMarkers podle pole markerLen
                // serazeno od nejmensi vzdalenosti po nejvetsi -> na nule mame nasi main barvu
                for (int u = 0; u < idxMarkers.length - 1; u++) {
                    g = u + 1;
                    tmp = idxMarkers[g];
                    while (g > 0 && markerLen[tmp] > markerLen[idxMarkers[g - 1]]) {
                        idxMarkers[g] = idxMarkers[g - 1];
                        g--;
                    }
                    idxMarkers[g] = tmp;
                }
                // do g ted ukladame kolik clenu zleva doprava z idxMarkers mame pouzit
                g = 1;

                while (g < markerLen.length && (markerLen[idxMarkers[0]] - markerLen[idxMarkers[g]]) <= INTERPOLATION_LENGTH)
                    g++;

                // zacneme pocitat silu jednotlivych markeru krome 0 ktere prosli hornim testem
                for (int k = 1; k < g; k++) {
                    if (markerLen[idxMarkers[k]] > INTERPOLATION_LENGTH) {
                        // ulozime si rozdil
                        x = markerLen[idxMarkers[0]] - markerLen[idxMarkers[k]];
                        // sila markeru
                        y = markerLen[idxMarkers[0]] - (x / INTERPOLATION_LENGTH) * markerLen[idxMarkers[0]];
                        markerLen[idxMarkers[k]] = y;
                    }
                }
                x = 0.0;
                y = 0.0;
                for (int k = 0; k < g; k++) { // vypocet delky len
                    len += markerLen[idxMarkers[k]];
                }

                for (int k = 0; k < g; k++) { // vypocet interpolace
                    m = markers.get(idxMarkers[k]);
                    x += (m.getX() - 127.5) * (markerLen[idxMarkers[k]] / len);
                    y += (m.getY() - 127.5) * (markerLen[idxMarkers[k]] / len);
                    z += (m.getZ() - 127.5) * (markerLen[idxMarkers[k]] / len);
                }
                // normalizace
                len = Math.sqrt(x * x + y * y + z * z);
                x /= len;
                y /= len;
                z /= len;

                n[3 * (int) (j * collumns + i)] = x;
                n[3 * (int) (j * collumns + i) + 1] = y;
                n[3 * (int) (j * collumns + i) + 2] = z;
            }
        }
        double[] d = new double[3 * size];
        System.arraycopy(n, 0, d, 0, n.length);
        int mask = 10;
        double sumX, sumY, sumZ;
        int beginR;
        int beginC;
        int endR;
        int endC;
        // gaussian blur
        // 10
        for (int blur = 0; blur < 10; blur++) {
            for (int j = 0; j < size; j += collumns) {
                for (int i = 0; i < collumns; i++) {
                    sumX = 0.0;
                    sumY = 0.0;
                    sumZ = 0.0;

                    beginR = (j - mask * collumns + i);
                    while (beginR < 0) beginR += collumns;
                    endR = (j + mask * collumns + i);
                    while (endR >= size) endR -= collumns;
                    beginC = -mask;
                    while ((beginR + beginC) < 0) beginC++;
                    while ((i + beginC) < 0) beginC++;
                    endC = mask;
                    while ((endR + endC) >= size) endC--;
                    while ((i + endC) >= collumns) endC--;
                    // ted muzeme o mask doleva,doprava,nahoru aj dolu
                    for (int w = beginR; w <= endR; w += collumns) { // jedu stredem masky dolu
                        for (int q = beginC; q <= endC; q++) { // projizdim radky
                            sumX += n[3 * (q + w)];
                            sumY += n[3 * (q + w) + 1];
                            sumZ += n[3 * (q + w) + 2];
                        }
                    }
                    // mame naplnene sumy
                    len = Math.sqrt(sumX * sumX + sumY * sumY + sumZ * sumZ);

                    d[3 * (i + j)] = sumX / len;
                    d[3 * (i + j) + 1] = sumY / len;
                    d[3 * (i + j) + 2] = sumZ / len;
                }
            }

            // musime prekopirovat pole!!!
            for (int i = 0; i < d.length; i++) {
                n[i] = d[i];
            }

        }
        return d;
    }

    /**
     * @method absoluteHeightsNEW() calculates final height map
     * from input relative height field.
     * @param q field of relatives height between vectors
     */
    private void absoluteHeightsNEW(double[] q) {
        int size = collumns * rows;
        double[] h = new double[size];
        double h1, h2, h3, h4;
        double height;

        int mod = 0;
        double[] buffer = new double[2 * collumns];
        for (int gauss = steps; gauss >= 0; gauss--) { // LOOP GAUSS-SEIDEL

            buffer[mod] = h[1 + collumns];
            mod++;

            //horni radka
            for (int i = 1; i < collumns - 1; i++) {

                buffer[mod] = h[i + collumns];
                mod++;

            }
            // pravy horni roh

            buffer[mod] = h[collumns - 2 + collumns];
            mod++;

            //TELO
            for (int j = collumns; j < size - collumns; j += collumns) { // projizdime radky
                //levy prvek

                if ((j - 2 * collumns) >= 0) { // pokud jsme v poli
                    h[(j - 2 * collumns)] = buffer[mod];
                }
                buffer[mod] = h[j + 1];
                mod++;

                for (int i = 1; i < collumns - 1; i++) { // projizdime bunky v radcich


                    h1 = h[(j + i) + 1] + (q[2 * (j + i)]); //+
                    h2 = h[(j + i) + collumns] + (q[2 * (j + i) + 1]); //+
                    h3 = h[(j + i) - 1] - q[2 * ((j + i) - 1)]; //-
                    h4 = h[(j + i) - collumns] - q[2 * ((j + i) - collumns) + 1]; //-
                    height = (h1 + h2 + h3 + h4) / 4.0;


                    if ((j + i - 2 * collumns) >= 0) { // pokud jsme v poli
                        h[(j + i - 2 * collumns)] = buffer[mod];
                    }
                    buffer[mod] = height;
                    mod++;

                }


                if ((j + collumns - 1 - 2 * collumns) >= 0) { // pokud jsme v poli
                    h[(j + collumns - 1 - 2 * collumns)] = buffer[mod];
                }

                buffer[mod] = h[(j + collumns - 2)];
                mod++;
                if (mod == 2 * collumns) {
                    mod = 0;
                }


            }

            // SPODNI RADKA

            if ((size - collumns - 2 * collumns) >= 0) { // pokud jsme v poli
                h[(size - collumns - 2 * collumns)] = buffer[mod];
            }

            buffer[mod] = h[(size - 2 * collumns + 1)];
            mod++;

            //spodni radek
            for (int i = size - collumns + 1; i < size - 1; i++) {

                if ((i - 2 * collumns) >= 0) { // pokud jsme v poli
                    h[(i - 2 * collumns)] = buffer[mod];
                }

                buffer[mod] = h[i - collumns];
                mod++;

            }

            //pravy spodni roh


            if ((size - 1 - 2 * collumns) >= 0) { // pokud jsme v poli
                h[(size - 1 - 2 * collumns)] = buffer[mod];
            }


            buffer[mod] = h[(size - 2) - collumns];
            mod++;
            if (mod == 2 * collumns) {
                mod = 0;
            }
            for (int i = (size - 2 * collumns); i < size; i++) {
                h[i] = buffer[mod];
                mod++;
                if (mod == 2 * collumns) {
                    mod = 0;
                }
            }


            // HORNI RADKA
            //levy horni roh
            h[0] = h[1 + collumns];
            //horni radka
            for (int i = 1; i < collumns - 1; i++) {
                h[i] = h[i + collumns];
            }
            // pravy horni roh
            h[collumns - 1] = h[collumns - 2 + collumns];
            // SPODNI RADKA
            //levy spodni roh
            h[(size - collumns)] = h[(size - 2 * collumns + 1)];
            //spodni radek
            for (int i = size - collumns + 1; i < size - 1; i++) {
                h[i] = h[i - collumns];
            }
            //pravy spodni roh
            h[size - 1] = h[(size - 2) - collumns];

            //boky
            //TELO
            for (int j = collumns; j < size - collumns; j += collumns) { // projizdime radky
                //levy prvek
                h[j] = h[j + 1];
                //pravy prvek
                h[(j + collumns - 1)] = h[(j + collumns - 2)];
            }

            mod = 0;
        }

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double range;
        for (int i = 0; i < size; i++) {

            if (h[i] > max) {
                max = h[i];
            }
            if (h[i] < min) {
                min = h[i];
            }
        }

        if (min < 0) {
            if (max < 0) {
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }

        byte value;
        for (int i = 0; i < size; i++) {
            value = (byte) ((int) (((h[i] - min) / range) * 255.0));
            fr[3 * i + bodyStart] = value;
            fr[3 * i + bodyStart + 1] = value;
            fr[3 * i + bodyStart + 2] = value;
        }

    }

    /**
     * @method relativeHeights() calculates relative
     * heights between vectors.
     * @return double field of relatives height between vectors
     */
    private double[] relativeHeights() {
        int size = collumns * rows;
        double[] relativeHeights = new double[2 * collumns * rows];
        double x_1, x_2, z_1, z_2, y_1, y_2;
        double a, b, c, d;
        double qij;
        double A, B, h_1, h_2, beta, alpha;
        double aEq, bEq, cEq;
        for (int i = (size - collumns - 2); i >= 0; i--) { // stred
            x_1 = normalField[3 * i];
            x_2 = normalField[3 * i + 3];
            z_1 = normalField[3 * i + 2];
            z_2 = normalField[3 * i + 3 + 2];
            // Rovnice pro (x_1,z_1)  a (x_2,z_2)
            a = x_1;
            b = z_1;
            c = x_2;
            d = z_2;

            if (a == 0) {
                a += Double.MIN_VALUE;
            }
            if (c == 0) {
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b / a);
            alpha = Math.toDegrees(alpha);
            if (a < 0.0) {
                alpha += 180d;
            }
            beta = Math.atan(d / c);
            beta = Math.toDegrees(beta);
            if (c < 0) {
                beta += 180d;
            }
            if (beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)) { // mame podobne uhly
                if (alpha < FLAT_ANGLE) { // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if ((180d - alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a / b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b / a) * c + d;

                if (cEq != 0) { // kvadraticka rovnice
                    A = (b / a) * c + d;
                    B = c - (b / a) * d;

                    h_1 = B / (-A) + Math.sqrt(B * B + A * A) / (-A);
                    h_2 = B / (-A) - Math.sqrt(B * B + A * A) / (-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b / a)) / ((b / a) * c - d);
                alpha = (beta * c + 1) / a;

                if (beta * alpha > 0) {
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // ========
            if (qij < -1 * MAX_RELATIVE_HEIGHT) {
                qij = -1 * MAX_RELATIVE_HEIGHT;
            } else if (qij > MAX_RELATIVE_HEIGHT) {
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2 * i] = qij;

            z_2 = normalField[3 * i + 3 * collumns + 2];
            y_1 = normalField[3 * i + 1];
            y_2 = normalField[3 * i + 3 * collumns + 1];

            // Rovnice pro (y_1,z_1)  a (y_2,z_2)
            a = y_1;
            b = z_1;
            c = y_2;
            d = z_2;

            if (a == 0) {
                a += Double.MIN_VALUE;
            }
            if (c == 0) {
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b / a);
            alpha = Math.toDegrees(alpha);
            if (a < 0.0) {
                alpha += 180d;
            }
            beta = Math.atan(d / c);
            beta = Math.toDegrees(beta);
            if (c < 0) {
                beta += 180d;
            }
            if (beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)) { // mame podobne uhly
                if (alpha < FLAT_ANGLE) { // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if ((180d - alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    qij = -(a / b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b / a) * c + d;

                if (cEq != 0) { // kvadraticka rovnice
                    A = (b / a) * c + d;
                    B = c - (b / a) * d;

                    h_1 = B / (-A) + Math.sqrt(B * B + A * A) / (-A);
                    h_2 = B / (-A) - Math.sqrt(B * B + A * A) / (-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b / a)) / ((b / a) * c - d);
                alpha = (beta * c + 1) / a;

                if (beta * alpha > 0) {
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if (qij < -1 * MAX_RELATIVE_HEIGHT) {
                qij = -1 * MAX_RELATIVE_HEIGHT;
            } else if (qij > MAX_RELATIVE_HEIGHT) {
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2 * i + 1] = qij;
        }
        for (int i = size - 2; i > (size - collumns - 1); i--) { //spodni radka
            x_1 = normalField[3 * i];
            x_2 = normalField[3 * i + 3];
            z_1 = normalField[3 * i + 2];
            z_2 = normalField[3 * i + 3 + 2];

            // Rovnice pro (x_1,z_1)  a (x_2,z_2)
            a = x_1;
            b = z_1;
            c = x_2;
            d = z_2;

            if (a == 0) {
                a += Double.MIN_VALUE;
            }
            if (c == 0) {
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b / a);
            alpha = Math.toDegrees(alpha);
            if (a < 0.0) {
                alpha += 180d;
            }
            beta = Math.atan(d / c);
            beta = Math.toDegrees(beta);
            if (c < 0) {
                beta += 180d;
            }

            if (beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)) { // mame podobne uhly
                if (alpha < FLAT_ANGLE) { // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if ((180d - alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku

                    qij = -(a / b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b / a) * c + d;
                if (cEq != 0) { // kvadraticka rovnice
                    A = (b / a) * c + d;
                    B = c - (b / a) * d;

                    h_1 = B / (-A) + Math.sqrt(B * B + A * A) / (-A);
                    h_2 = B / (-A) - Math.sqrt(B * B + A * A) / (-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }


                beta = (h_1 - (b / a)) / ((b / a) * c - d);
                alpha = (beta * c + 1) / a;

                if (beta * alpha > 0) {
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if (qij < -1 * MAX_RELATIVE_HEIGHT) {
                qij = -1 * MAX_RELATIVE_HEIGHT;
            } else if (qij > MAX_RELATIVE_HEIGHT) {
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2 * i] = qij;
        }
        for (int i = (size - collumns - 1); i >= collumns; i -= collumns) { //pravy sloupec
            z_1 = normalField[3 * i + 2];
            z_2 = normalField[3 * i + 3 * collumns + 2];
            y_1 = normalField[3 * i + 1];
            y_2 = normalField[3 * i + 3 * collumns + 1];
            // Rovnice pro (y_1,z_1)  a (y_2,z_2)
            a = y_1;
            b = z_1;
            c = y_2;
            d = z_2;

            if (a == 0) {
                a += Double.MIN_VALUE;
            }
            if (c == 0) {
                c += Double.MIN_VALUE;
            }
            // uhly vektoru
            alpha = Math.atan(b / a);
            alpha = Math.toDegrees(alpha);
            if (a < 0.0) {
                alpha += 180d;
            }
            beta = Math.atan(d / c);
            beta = Math.toDegrees(beta);
            if (c < 0) {
                beta += 180d;
            }
            if (beta > (alpha - ROUND_ANGLE) && beta < (alpha + ROUND_ANGLE)) { // mame podobne uhly
                if (alpha < FLAT_ANGLE) { // skoro kolmice
                    qij = -MAX_RELATIVE_HEIGHT;
                } else if ((180d - alpha) < FLAT_ANGLE) {
                    qij = MAX_RELATIVE_HEIGHT;
                } else { // prolozime primku
                    //qij = -(b/a);
                    qij = -(a / b);
                }
            } else { // mame rozdilne uhly -> kruznice
                cEq = (b / a) * c + d;

                if (cEq != 0) { // kvadraticka rovnice
                    A = (b / a) * c + d;
                    B = c - (b / a) * d;

                    h_1 = B / (-A) + Math.sqrt(B * B + A * A) / (-A);
                    h_2 = B / (-A) - Math.sqrt(B * B + A * A) / (-A);
                } else { // rovnice ma tvar h*() = 0 takze h = 0
                    h_1 = 0;
                    h_2 = 0;
                }

                beta = (h_1 - (b / a)) / ((b / a) * c - d);
                alpha = (beta * c + 1) / a;

                if (beta * alpha > 0) {
                    qij = h_1;
                } else {
                    qij = h_2;
                }

            }
            // =========
            if (qij < -1 * MAX_RELATIVE_HEIGHT) {
                qij = -1 * MAX_RELATIVE_HEIGHT;
            } else if (qij > MAX_RELATIVE_HEIGHT) {
                qij = MAX_RELATIVE_HEIGHT;
            }
            relativeHeights[2 * i + 1] = qij;
        }
        normalField = null;


        // normalizace
        double range;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < relativeHeights.length; i++) {
            if (relativeHeights[i] < min) {
                min = relativeHeights[i];
            }
            if (relativeHeights[i] > max) {
                max = relativeHeights[i];
            }
        }

        if (min < 0) {
            if (max < 0) {
                range = Math.abs(min) + max;
            } else {
                range = max - min;
            }
        } else {
            range = max - min;
        }

        for (int i = 0; i < relativeHeights.length; i++) {
            relativeHeights[i] = (((relativeHeights[i] - min) / range) - 0.5) * 2;
        }

        //otoceni do spravnehos smeru
        for (int i = 0; i < size; i++) {
            relativeHeights[2 * i + 1] = -relativeHeights[2 * i + 1];
        }


        return relativeHeights;
    }

    /**
     * @method write() writes byte array of the picture
     * to the file specificated in the path.
     * @param picture input byte array
     * @param path path to the file
     */
    public void write(byte[] picture, String path) {
        FileOutputStream fos;

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

    /**
     * @method read() reads file to the byte array
     * from input path.
     * @param path input path of the file
     * @return byte field of the file
     */
    public byte[] read(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @method getDepthMapVEC() calculates the initial normal vector
     * estimation. Equation E2 in the algorithm thesis.
     * @return field of normal vectors
     */
    private double[] getDepthMapVEC() {
        // 4. step
        int size = collumns * rows;
        double[] n = new double[3 * size];
        double[] e = interpolatedNormalEstimation();

        System.arraycopy(e, 0, n, 0, e.length);
        boolean skip = false;
        for (int i = 0; i < size; i++) {

            if (!skip) {
                n[3 * i] = 0;
                n[3 * i + 1] = 0;
                n[3 * i + 2] = 1;
            }
            skip = false;
        }

        int mod = 0;
        double[] buffer = new double[2 * 3 * collumns];

        // matice s neighbours = nei

        int neighbourSize;

        double len;

        double newValue_x;
        double newValue_y;
        double newValue_z;

        double sumX;
        double sumY;
        double sumZ;
        double al4;
        double be4;
        double de4;
        double ga4;

        de4 = (lightX * lightY) / (lightX * lightX + lm * 4);

        al4 = (lightY * lightX) / (lightY * lightY + lm * 4 - (lightX * lightY) * de4);


        be4 = (lightY * lightZ) / (lightY * lightY + lm * 4 - (lightX * lightY) * de4);


        ga4 = (lightX * lightZ) / (lightX * lightX + lm * 4);


        for (int g = 0; g < 25; g++) { // hlavni loop = pocet kroku gauss-siedela 25
            mod += 3;
            //horni radka

            for (int i = 1; i < collumns - 1; i++) { // bereme x,y,z najednou
                mod += 3;
            }

            // pixel vpravo nahore
            mod += 3;

            // po radcich telo
            for (int i = collumns; i < size - collumns; i += collumns) {
                //prvek vlevo

                if ((i - 2 * collumns) >= 0) { // pokud jsme v poli
                    n[3 * (i - 2 * collumns)] = buffer[mod];
                    n[3 * (i - 2 * collumns) + 1] = buffer[mod + 1];
                    n[3 * (i - 2 * collumns) + 2] = buffer[mod + 2];
                }
                mod += 3;

                //prostredek
                neighbourSize = 4;

                for (int j = 1; j < collumns - 1; j++) { // vnitrek radku

                    sumX = n[3 * (i + j) - 3 * collumns] + n[3 * (i + j) + 3 * collumns] + n[3 * (i + j) + 3] + n[3 * (i + j) - 3] /*+ e[3*(i+j)]*/;
                    sumY = n[3 * (i + j) - 3 * collumns + 1] + n[3 * (i + j) + 3 * collumns + 1] + n[3 * (i + j) + 4] + n[3 * (i + j) - 2] /*+ e[3*(i+j)+1]*/;
                    sumZ = n[3 * (i + j) - 3 * collumns + 2] + n[3 * (i + j) + 3 * collumns + 2] + n[3 * (i + j) + 5] + n[3 * (i + j) - 1] /*+ e[3*(i+j)+2]*/;

                    newValue_z =
                            ((1 / q) * grayscale[(i + j)] * (lightZ - lightX * ga4 + lightY * ga4 * al4 - lightY * be4 - lightX * de4 * ga4 * al4 + lightX * de4 * be4) + lm * (sumX * de4 * be4 - sumY * be4 - sumX * ga4 + sumY * ga4 * al4 - sumX * de4 * ga4 * al4 + sumZ) + deltaE * (e[3 * (i + j)] * de4 * be4 - e[3 * (i + j) + 1] * be4 - e[3 * (i + j)] * ga4 + e[3 * (i + j) + 1] * ga4 * al4 - e[3 * (i + j)] * de4 * ga4 * al4 + e[3 * (i + j) + 2]))
                                    /
                                    (lightZ * lightZ + lm * neighbourSize + deltaE - lightZ * lightY * be4 + lightZ * lightX * de4 * be4 - lightZ * lightX * ga4 + lightZ * lightY * al4 * ga4 - lightZ * lightX * al4 * de4 * ga4);

                    newValue_y =
                            ((1 / q) * grayscale[(i + j)] * (lightY - lightX * de4) + newValue_z * lightZ * (lightX * de4 - lightY) + lm * (sumY - sumX * de4) + deltaE * (e[3 * (i + j) + 1] - e[3 * (i + j)] * de4))
                                    /
                                    (lightY * lightY + lm * neighbourSize + deltaE - lightY * lightX * de4);

                    newValue_x =
                            ((1 / q) * grayscale[(i + j)] * lightX - newValue_y * lightY * lightX - newValue_z * lightZ * lightX + lm * sumX + deltaE * e[3 * (i + j)])
                                    /
                                    (lightX * lightX + lm * neighbourSize + deltaE);

                    if (newValue_z < 0) {
                        newValue_z = -newValue_z;
                    }

                    if (((i + j) - 2 * collumns) >= 0) { // pokud jsme v poli
                        n[3 * ((i + j) - 2 * collumns)] = buffer[mod];
                        n[3 * ((i + j) - 2 * collumns) + 1] = buffer[mod + 1];
                        n[3 * ((i + j) - 2 * collumns) + 2] = buffer[mod + 2];
                    }

                    len = Math.sqrt(newValue_x * newValue_x + newValue_y * newValue_y + newValue_z * newValue_z); // length
                    buffer[mod] = newValue_x / len;
                    buffer[mod + 1] = newValue_y / len;
                    buffer[mod + 2] = newValue_z / len;
                    mod += 3;
                }

                //prvek vpravo
                if (((i + collumns - 1) - 2 * collumns) >= 0) { // pokud jsme v poli
                    n[3 * ((i + collumns - 1) - 2 * collumns)] = buffer[mod];
                    n[3 * ((i + collumns - 1) - 2 * collumns) + 1] = buffer[mod + 1];
                    n[3 * ((i + collumns - 1) - 2 * collumns) + 2] = buffer[mod + 2];
                }
                mod += 3;
                if (mod == 2 * 3 * collumns) {
                    mod = 0;
                }

            }

            // pixel vlevo dole
            if (((size - collumns) - 2 * collumns) >= 0) { // pokud jsme v poli
                n[3 * ((size - collumns) - 2 * collumns)] = buffer[mod];
                n[3 * ((size - collumns) - 2 * collumns) + 1] = buffer[mod + 1];
                n[3 * ((size - collumns) - 2 * collumns) + 2] = buffer[mod + 2];
            }
            mod += 3;

            //spodni radka
            for (int i = size - collumns + 1; i < size - 1; i++) { // bereme x,y,z najednou
                if ((i - 2 * collumns) >= 0) { // pokud jsme v poli
                    n[3 * (i - 2 * collumns)] = buffer[mod];
                    n[3 * (i - 2 * collumns) + 1] = buffer[mod + 1];
                    n[3 * (i - 2 * collumns) + 2] = buffer[mod + 2];
                }
                mod += 3;
            }

            // pixel vpravo dole
            if (((size - 1) - 2 * collumns) >= 0) { // pokud jsme v poli
                n[3 * ((size - 1) - 2 * collumns)] = buffer[mod];
                n[3 * ((size - 1) - 2 * collumns) + 1] = buffer[mod + 1];
                n[3 * ((size - 1) - 2 * collumns) + 2] = buffer[mod + 2];
            }
            mod += 3;
            if (mod == 2 * 3 * collumns) {
                mod = 0;
            }
            for (int i = (size - 2 * collumns); i < size; i++) {
                n[3 * i] = buffer[mod];
                n[3 * i + 1] = buffer[mod + 1];
                n[3 * i + 2] = buffer[mod + 2];
                mod += 3;
                if (mod == 2 * 3 * collumns) {
                    mod = 0;
                }
            }
            mod = 0;

            /**
             *
             *  OPISOVANI DO OKRAJU
             *
             */
            // HORNI RADKA
            //levy horni roh
            n[0] = n[3 * (1 + collumns)];
            n[1] = n[3 * (1 + collumns) + 1];
            n[2] = n[3 * (1 + collumns) + 2];
            //horni radka
            for (int i = 1; i < collumns - 1; i++) {
                n[3 * i] = n[3 * (i + collumns)];
                n[3 * i + 1] = n[3 * (i + collumns) + 1];
                n[3 * i + 2] = n[3 * (i + collumns) + 2];
            }
            // pravy horni roh
            n[3 * (collumns - 1)] = n[3 * (collumns - 2 + collumns)];
            n[3 * (collumns - 1) + 1] = n[3 * (collumns - 2 + collumns) + 1];
            n[3 * (collumns - 1) + 2] = n[3 * (collumns - 2 + collumns) + 2];
            // SPODNI RADKA
            //levy spodni roh
            n[3 * (size - collumns)] = n[3 * (size - 2 * collumns + 1)];
            n[3 * (size - collumns) + 1] = n[3 * (size - 2 * collumns + 1) + 1];
            n[3 * (size - collumns) + 2] = n[3 * (size - 2 * collumns + 1) + 2];
            //spodni radek
            for (int i = size - collumns + 1; i < size - 1; i++) {
                n[3 * i] = n[3 * (i - collumns)];
                n[3 * i + 1] = n[3 * (i - collumns) + 1];
                n[3 * i + 2] = n[3 * (i - collumns) + 2];
            }
            //pravy spodni roh
            n[3 * (size - 1)] = n[3 * ((size - 2) - collumns)];
            n[3 * (size - 1) + 1] = n[3 * ((size - 2) - collumns) + 1];
            n[3 * (size - 1) + 2] = n[3 * ((size - 2) - collumns) + 2];

            //boky
            //TELO
            for (int j = collumns; j < size - collumns; j += collumns) { // projizdime radky
                //levy prvek
                n[3 * j] = n[3 * (j + 1)];
                n[3 * j + 1] = n[3 * (j + 1) + 1];
                n[3 * j + 2] = n[3 * (j + 1) + 2];
                //pravy prvek
                n[3 * (j + collumns - 1)] = n[3 * (j + collumns - 2)];
                n[3 * (j + collumns - 1) + 1] = n[3 * (j + collumns - 2) + 1];
                n[3 * (j + collumns - 1) + 2] = n[3 * (j + collumns - 2) + 2];
            }


        }

        return n;
    }

    /**
     * @method getLightSource() calculates light vector. Equation E1 in the algorithm thesis.
     */
    private void getLightSource() {
        double[][] valsA;
        double[][] valsB;

        valsA = new double[markers.size()][3];
        valsB = new double[markers.size()][1];

        Marker m;
        for (int i = 0; i < 3; i++) {
            m = markers.get(i);

            valsA[i] = new double[]{(m.getX() - 127.5), (m.getY() - 127.5), m.getZ() - 127.5};
            valsB[i] = new double[]{((int) (m.getPosX() * (collumns - 1)) + (int) (m.getPosY() * ((rows - 1))) * (collumns))};
        }

        Matrix A = new Matrix(valsA);
        Matrix b = new Matrix(valsB);
        Matrix x = A.solve(b);

        lightX = x.get(0, 0);
        lightY = x.get(1, 0);
        lightZ = x.get(2, 0);

        double size = Math.sqrt((lightX * lightX) + lightY * lightY + lightZ * lightZ);
        lightX = -lightX / size;
        lightY = lightY / size;
        lightZ = lightZ / size;

    }

    /**
     * @method getLightDirection() calculates light vector and creates message for GUI.
     * Equation E1 in the algorithm thesis.
     * @param markers list of input markers
     * @param collumns width of input image
     * @param rows height of input image
     * @return message for GUI: Light vector ...
     */
    public static String getLightDirection(java.util.List<Marker> markers, int collumns, int rows) {
        double[][] valsA;
        double[][] valsB;

        valsA = new double[markers.size()][3];
        valsB = new double[markers.size()][1];

        Marker m;
        for (int i = 0; i < 3; i++) {
            m = markers.get(i);
            valsA[i] = new double[]{(m.getX() - 127.5), (m.getY() - 127.5), m.getZ() - 127.5};
            valsB[i] = new double[]{((int) (m.getPosX() * (collumns - 1)) + (int) (m.getPosY() * ((rows - 1))) * (collumns))};
        }

        Matrix A = new Matrix(valsA);
        Matrix b = new Matrix(valsB);
        Matrix x;
        try{
            x = A.solve(b);
            DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
            double lightX = x.get(0, 0);
            double lightY = x.get(1, 0);
            double lightZ = x.get(2, 0);

            double size = Math.sqrt((lightX * lightX) + lightY * lightY + lightZ * lightZ);
            lightX = -lightX / size;
            lightY = lightY / size;
            lightZ = lightZ / size;

            return "Light vector = (" + decimalFormat.format(lightX) + " , " + decimalFormat.format(lightY) + " , " + decimalFormat.format(lightZ) + ");";
        } catch (Exception e){
            return "Singular values. Rearrange your markers.";
        }



    }

    /**
     * @method getLightMessage() returns light vector message for GUI with proper object parameters
     * @return message for GUI: Light vector ...
     */
    public String getLightMessage() {
        return getLightDirection(markers, collumns, rows);
    }

    /**
     * @method getGrayscale() fill grayscale variable with gray scale values of the
     * fr variable
     */
    private void getGrayscale() {
        int off; // offset in array
        double[] out;


        int i = 3;
        StringBuilder stb = new StringBuilder();
        while (true) {
            if (fr[i] == '#') {
                i++;
                while (fr[i] != '\n') i++;
                while (fr[i] == '\n') i++;
            } else break;

        }
        off = i;
        while (fr[off] != 10 && fr[off] != ' ') off++;
        while (i < off) {
            stb.append((char) fr[i]);
            i++;
        }
        collumns = Integer.parseInt(stb.toString());

        off++;
        i = off;
        while (fr[off] != 10 && fr[off] != ' ') off++;
        stb = new StringBuilder();
        while (i < off) {
            stb.append((char) fr[i]);
            i++;
        }
        rows = Integer.parseInt(stb.toString());
        out = new double[collumns * rows];
        off += 5;
        bodyStart = off;
        int val;
        for (i = 0; i < out.length; i++) {
            val = (int) (0.2126 * (fr[i * 3 + off] & 0xFF) + 0.7152 * (fr[i * 3 + 1 + off] & 0xFF) + 0.0722 * (fr[i * 3 + 2 + off] & 0xFF));
            if (val < 0) val = 0;
            else if (val > 255) val = 255;
            out[i] = ((val / 127.5) - 1);

        }
        grayscale = out;

    }

    /**
     *
     * @param albedo is not currently used in GUY
     */
    public void setAlbedo(double albedo) {
        this.q = albedo;
    }

    /**
     *
     * @param regularization variable can be set from Smoothness slider in GUI
     */
    public void setRegularization(double regularization) {
        this.lm = regularization;
    }

    /**
     *
     * @param deltaE variable can be set from Markers Effect slider in GUI
     */
    public void setDeltaE(double deltaE) {
        this.deltaE = deltaE;
    }

    /**
     * @method invert() inverts color from input image and returns inverted image
     * @param fr byte field image in ppm format
     * @return byte field image in ppm format with inverted colors
     */
    public byte[] invert(byte[] fr) {
        int off = 3; // offset in array

        while (fr[off] != 10) off++;
        int i = 3;
        while (true) {
            if (fr[i] == '#') {
                i++;
                while (fr[i] != '\n') i++;
                while (fr[i] == '\n') i++;
            } else break;

        }
        off = i;
        while (fr[off] != 10 && fr[off] != ' ') off++;

        off++;
        while (fr[off] != 10 && fr[off] != ' ') off++;
        off += 5;

        for (i = off; i < fr.length; i++) {
            fr[i] = (byte) (255 - (fr[i] & 0xFF));
        }
        return fr;
    }
}
