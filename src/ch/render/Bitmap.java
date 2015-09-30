/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.render;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author bluew
 */
public class Bitmap {

    public enum Format {

        Grayscale, GrayscaleAlpha, RGB, RGBA
    }
    private final int width;
    private final int height;
    private final Format format;
    private byte[] pixels;

    Bitmap(int width, int height, Format format) {
        this.width = width;
        this.height = height;
        this.format = format;

        pixels = new byte[width * height * 4]; //creates an empty RGBA bitmap
    }

    Bitmap(int width, int height, Format format, byte[] pixels) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.pixels = pixels;
    }

    public static Bitmap bitmapFromFile(String path) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ex) {
            Logger.getLogger(Bitmap.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (img == null) {
            throw new RuntimeException("Image is null");
        }
        WritableRaster raster = img.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
        ColorSpace cs = img.getColorModel().getColorSpace();
        boolean hasAlpha = img.getColorModel().hasAlpha();
        System.out.println(img.toString());
        System.out.println(img.getColorModel().getPixelSize());

        if (img.getColorModel().getPixelSize() / raster.getNumDataElements() != 8) {
            throw new RuntimeException("Only 8 bit per Color/Alpha component supported");
        }
        Format format = null;
        if (cs.getType() == ColorSpace.TYPE_GRAY) {
            format = hasAlpha ? Format.GrayscaleAlpha : Format.Grayscale;
        } else if (cs.getType() == ColorSpace.TYPE_RGB) {
            format = hasAlpha ? Format.RGBA : Format.RGB;
        }

        return new Bitmap(img.getWidth(), img.getHeight(), format, data.getData());
    }

    public static Bitmap bitmapFromFileAsRGBA8(String path) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ex) {
            Logger.getLogger(Bitmap.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (img == null) {
            throw new RuntimeException("Image is null");
        }
        BufferedImage newcopy = copyAs(img, BufferedImage.TYPE_4BYTE_ABGR);

// Flip the image vertically
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -newcopy.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        newcopy = op.filter(newcopy, null);

        DataBufferByte data = (DataBufferByte) newcopy.getRaster().getDataBuffer();
        byte[] abgr = data.getData();
        byte[] rgba = new byte[abgr.length];
        //convert abgr to rgba
        for (int i = 0; i < abgr.length; i = i + 4) {
            rgba[i] = abgr[i + 3];
            rgba[i + 1] = abgr[i + 2];
            rgba[i + 2] = abgr[i + 1];
            rgba[i + 3] = rgba[i];
        }
        return new Bitmap(img.getWidth(), img.getHeight(), Format.RGBA, rgba);
    }

    public static BufferedImage copyAs(BufferedImage image, int type) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), type);
        if (copy.getColorModel().hasAlpha()) {
            Graphics2D g = copy.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, copy.getWidth(), copy.getHeight());
            g.dispose();
        }
        // copy.getGraphics().

        copy.getGraphics().drawImage(image, 0, 0, null);
        return copy;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Format format() {
        return format;
    }

    public byte[] raw() {
        return pixels;
    }

    public void flipVertically() {
        if (pixels == null) {
            throw new RuntimeException("No bitmap loaded can't flip verticall");
        }
        if (format != Format.RGBA) {
            throw new RuntimeException("Only RGBA is supported");
        }
        byte[] tmp = new byte[pixels.length];
        for (int y = 0; y < height; y++) {
            int yoff = y * width * 4;
            for (int x = 0; x < width; x++) {
                tmp[yoff + 4 * x] = pixels[yoff + (width - 1) * 4 - 4 * x];
                tmp[yoff + 4 * x + 1] = pixels[yoff + (width - 1) * 4 - 4 * x + 1];
                tmp[yoff + 4 * x + 2] = pixels[yoff + (width - 1) * 4 - 4 * x + 2];
                tmp[yoff + 4 * x + 3] = pixels[yoff + (width - 1) * 4 - 4 * x + 3];
            }
        }
        pixels = tmp;
    }

    public void rotate90cc() {

    }

    @Override
    public String toString() {
        return Arrays.toString(pixels);
    }
}
