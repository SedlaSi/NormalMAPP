package algorithms;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class NormalMap implements Algorithm {

    private static final int STEPS = 5;

    public byte[] read(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    private byte[] getGrayscale(byte[] fr) {
        int collumns;
        int rows;
        int off; // offset in array
        byte[] out;

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
        out = new byte[collumns * rows];
        off += 5;
        int val;
        for (i = 0; i < out.length; i++) {
            val = (int) (((fr[i * 3 + off] & 0xFF) + (fr[i * 3 + 1 + off] & 0xFF) + (fr[i * 3 + 2 + off] & 0xFF)) / 3);
            if (val < 0) val = 0;
            else if (val > 255) val = 255;
            out[i] = (byte) val;
        }

        return out;
    }

    public byte[] normalMap(byte[] fr, double angle, double height) {
        byte[] gray = getGrayscale(fr);
        byte[] out = Arrays.copyOf(fr, fr.length);

        int collumns;
        int rows;
        int off = 3; // offset in array

        while (fr[off] != 10) off++;
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

        off += 5;
        int readen_lines = 1;
        double valX;
        double valY;
        double valZ = height;
        double length;

        while (readen_lines < rows - 1) {
            for (i = 1; i < collumns - 1; i++) {
                valX = -(gray[(readen_lines - 1) * collumns + i - 1] & 0xFF) + (gray[(readen_lines - 1) * collumns + i + 1] & 0xFF)
                        - 2 * (gray[readen_lines * collumns + i - 1] & 0xFF) + 2 * (gray[readen_lines * collumns + i + 1] & 0xFF)
                        - (gray[(readen_lines + 1) * collumns + i - 1] & 0xFF) + (gray[(readen_lines + 1) * collumns + i + 1] & 0xFF);
                valY = -(gray[(readen_lines - 1) * collumns + i - 1] & 0xFF) - 2 * (gray[(readen_lines - 1) * collumns + i] & 0xFF) - (gray[(readen_lines - 1) * collumns + i + 1] & 0xFF)
                        + (gray[(readen_lines + 1) * collumns + i - 1] & 0xFF) + 2 * (gray[(readen_lines + 1) * collumns + i] & 0xFF) + (gray[(readen_lines + 1) * collumns + i + 1] & 0xFF);
                double radAngle = Math.toRadians(angle);
                valX = ((valX * Math.cos(radAngle)) - (valY * Math.sin(radAngle))) / 255.0;
                valY = ((valY * Math.cos(radAngle)) + (valX * Math.sin(radAngle))) / 255.0;

                length = Math.sqrt((valX * valX) + (valY * valY) + (valZ * valZ));

                valX = ((valX / length + 1.0) * (255.0 / 2.0));
                valY = ((valY / length + 1.0) * (255.0 / 2.0));
                out[off + (readen_lines * collumns * 3) + i * 3] = (byte) (int) valX;

                out[off + (readen_lines * collumns * 3) + 1 + i * 3] = (byte) (int) valY;
                out[off + (readen_lines * collumns * 3) + 2 + i * 3] = (byte) (int) ((valZ / length + 1.0) * (255.0 / 2.0));

            }
            readen_lines++;
        }

        // kopirovani hodnot do kraju
        out[off] = out[off + 3 * (1 + collumns)];
        out[off + 1] = out[off + 3 * (1 + collumns) + 1];
        out[off + 2] = out[off + 3 * (1 + collumns) + 2];

        // horni radka
        for (i = off + 3; i < off + 3 * (collumns - 1); i++) {
            out[i] = out[i + 3 * collumns];
        }

        int size = collumns * rows;

        out[off + 3 * (collumns - 1)] = out[off + 3 * (collumns - 1) + 3 * (collumns - 1)];
        out[off + 1 + 3 * (collumns - 1)] = out[off + 3 * (collumns - 1) + 3 * (collumns - 1) + 1];
        out[off + 2 + 3 * (collumns - 1)] = out[off + 3 * (collumns - 1) + 3 * (collumns - 1) + 2];

        for (i = off + 3 * (collumns); i < off + 3 * (size - collumns); i += 3 * collumns) {
            // levy prvek
            out[i] = out[i + 3];
            out[i + 1] = out[i + 4];
            out[i + 2] = out[i + 5];
            // pravy prvek
            out[i + 3 * (collumns - 1)] = out[i + 3 * (collumns - 1) - 3];
            out[i + 3 * (collumns - 1) + 1] = out[i + 3 * (collumns - 1) - 2];
            out[i + 3 * (collumns - 1) + 2] = out[i + 3 * (collumns - 1) - 1];
        }

        out[off + 3 * (size - collumns)] = out[off + 3 * (size - collumns) - 3 * (collumns - 1)];
        out[off + 3 * (size - collumns) + 1] = out[off + 3 * (size - collumns) - 3 * (collumns - 1) + 1];
        out[off + 3 * (size - collumns) + 2] = out[off + 3 * (size - collumns) - 3 * (collumns - 1) + 2];

        // spodni radka
        for (i = off + 3 * (size - collumns + 1); i < off + 3 * (size - 1); i++) {
            out[i] = out[i - 3 * collumns];
        }

        out[off + 3 * (size - 1)] = out[off + 3 * (size - 2)];
        out[off + 3 * (size - 1) + 1] = out[off + 3 * (size - 2) + 1];
        out[off + 3 * (size - 1) + 2] = out[off + 3 * (size - 2) + 2];


        return out;
    }

    public byte[] convolution(byte[] fr) {
        int collumns;
        int rows;
        int off = 3; // offset in array
        byte[] out = Arrays.copyOf(fr, fr.length);

        int upper, middle, lower;


        System.out.println("P = " + fr[0]);
        System.out.println("6 = " + fr[1]);
        System.out.println("/n = " + fr[2]);
        while (fr[off] != 10) off++;
        int i = 3;
        StringBuilder stb = new StringBuilder();
        while (true) {
            if (fr[i] == '#') {
                i++;
                while (fr[i] != '\n') i++;
            } else break;
        }
        i++;
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
        off += 5;
        upper = off;
        middle = off + 3 * collumns;
        lower = off + 6 * collumns;
        int readen_lines = 3;
        int valR;
        int valG;
        int valB;

        while (readen_lines <= rows) {
            for (i = 3; i < collumns * 3 - 3; i += 3) {

                valR = (5 * (fr[middle + i] & 0xFF) - (fr[middle + i - 3] & 0xFF) - (fr[middle + i + 3] & 0xFF) - (fr[upper + i] & 0xFF) - (fr[lower + i] & 0xFF)); // R

                if (valR > 255) {
                    valR = 255;
                } else if (valR < 0) {
                    valR = 0;
                }

                out[middle + i] = (byte) valR;

                valG = (5 * (fr[middle + i + 1] & 0xFF) - (fr[middle + i - 2] & 0xFF) - (fr[middle + i + 4] & 0xFF) - (fr[upper + i + 1] & 0xFF) - (fr[lower + i + 1] & 0xFF)); // G
                if (valG > 255) {
                    valG = 255;
                } else if (valG < 0) {
                    valG = 0;
                }
                out[middle + i + 1] = (byte) valG;

                valB = (5 * (fr[middle + i + 2] & 0xFF) - (fr[middle + i - 1] & 0xFF) - (fr[middle + i + 5] & 0xFF) - (fr[upper + i + 2] & 0xFF) - (fr[lower + i + 2] & 0xFF)); // B
                if (valB > 255) {
                    valB = 255;
                } else if (valB < 0) {
                    valB = 0;
                }
                out[middle + i + 2] = (byte) valB;
            }

            upper = middle;
            middle = lower;
            lower += 3 * collumns;
            readen_lines++;
        }

        return out;
    }

    @Override
    public int getSteps() {
        return STEPS;
    }

}
