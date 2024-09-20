package src.main.java;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Sprite {
    private int[][] result;
    
    public Sprite(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int bits = image.getColorModel().getPixelSize();
            byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
            result = new int[image.getHeight()][image.getWidth()];
            int pixel = 0;
            for (int column = 0; column < image.getHeight(); column++) {
                for (int row = 0; row < image.getWidth(); row++) {
                    if (bits == 24) {
                        result[row][column] = -16777216 + ((int)pixels[pixel] & 0xff) + (((int)pixels[pixel + 1] & 0xff) << 8) + (((int)pixels[pixel + 2] & 0xff) << 16);
                        pixel += 3;
                    }
                    else if (bits == 32) {
                        result[row][column] = (((int)pixels[pixel] & 0xff) << 24) + ((int)pixels[pixel + 1] & 0xff) + (((int)pixels[pixel + 2] & 0xff) << 8) + (((int)pixels[pixel + 3] & 0xff) << 16);
                        pixel += 4;
                    }
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public int getPixelColor(int x, int y) {
        return result[y][x];
    }

    public int lengthX() {
        return result[0].length;
    }

    public int lengthY() {
        return result.length;
    }
}
