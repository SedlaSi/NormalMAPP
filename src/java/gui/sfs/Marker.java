package gui.sfs;

import java.awt.*;

/**
 * Created by sedlasi1 on 23.10.16.
 *
 * Marker class is data class containing data of
 * normal vector which was set by user.
 *
 */
public class Marker {

    private int x;
    private int y;
    private int z;

    private double posX;
    private double posY;

    private int direction;
    private int angle;

    // not currently in use
    Rectangle square;

    private String name;

    public Marker(String name, int x, int y, int z, double posX, double posY) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.posX = posX;
        this.posY = posY;
    }

    public Marker(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getDirection() {
        return direction;
    }

    public int getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return name;
    }
}
