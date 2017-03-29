package image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;

/**
 * Created by root on 14.7.16.
 */
public class Image {

    private File sourceFile;
    private BufferedImage originalMap;
    private BufferedImage heightMap;
    private BufferedImage normalMap;
    private BufferedImage preview;

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

    public void setPreview(BufferedImage preview) {
        this.preview = preview;
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

    public BufferedImage getPreview() {
        return preview;
    }

}
