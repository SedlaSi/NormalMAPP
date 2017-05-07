package image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;

/**
 * Created by sedlasi1 on 14.7.16.
 *
 * Image class is data container for all main input / output files.
 *
 * Image class contains:
 *      input image -> BufferedImage originalMap
 *      height map image -> BufferedImage heightMap
 *      normal map image -> BufferedImage normalMap
 *      reference to the input file -> File sourceFile
 *
 */
public class Image {

    private File sourceFile;
    private BufferedImage originalMap;
    private BufferedImage heightMap;
    private BufferedImage normalMap;

    public Image(File sourceFile, BufferedImage originalMap) {
        this.sourceFile = sourceFile;
        this.originalMap = originalMap;
    }

    public void setHeightMap(BufferedImage heightMap) {
        this.heightMap = heightMap;
    }

    public void setNormalMap(BufferedImage normalMap) {
        this.normalMap = normalMap;
    }

    public BufferedImage getNormalMap() {
        return normalMap;
    }

    public BufferedImage getHeightMap() {
        return heightMap;
    }

    public BufferedImage getOriginalMap() {
        return originalMap;
    }

}
