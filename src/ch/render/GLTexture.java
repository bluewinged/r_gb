/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.render;

import ch.render.Bitmap.Format;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 *
 * @author bluew
 */
public class GLTexture {

    private final int ref;
    private final int originalWidth;
    private final int originalHeight;
    private final int numBytes;
    private ByteBuffer uploadBuffer;

    public GLTexture(Bitmap bitmap) {
        this(bitmap, GL_LINEAR, GL_CLAMP_TO_EDGE);
    }

    public GLTexture(Bitmap bitmap, int minMagFilter, int wrapMode) {
        if (bitmap.format() != Format.RGBA) {
            throw new RuntimeException("Bitmap is not in RGBA format, rejected");
        }
        originalWidth = bitmap.width();
        originalHeight = bitmap.height();
        ref = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, ref);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minMagFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, minMagFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);
        //for lwjgl ALWAYS allocate memory with BufferUtils!
        numBytes = bitmap.raw().length;
        ByteBuffer data = BufferUtils.createByteBuffer(numBytes);
        data.put(bitmap.raw()).flip();
        glTexImage2D(GL_TEXTURE_2D,
                0,
                bitmapformatToOpenglformat(bitmap.format()),
                bitmap.width(),
                bitmap.height(),
                0,
                bitmapformatToOpenglformat(bitmap.format()),
                GL_UNSIGNED_BYTE,
                data);
        glBindTexture(GL_TEXTURE_2D, 0);//unbind texture;

        uploadBuffer = BufferUtils.createByteBuffer(numBytes);
    }

    public void upload(byte[] pixels) {
        if (pixels.length != numBytes) {
            throw new RuntimeException("Wrong amount of pixel bytes");
        }
        uploadBuffer.clear();
        uploadBuffer.put(pixels).flip();
        glBindTexture(GL_TEXTURE_2D, ref);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, originalWidth, originalHeight, GL_RGBA, GL_UNSIGNED_BYTE, uploadBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);

    }

    public void upload(int[][] pixels) {
        uploadBuffer.clear();
        for (int y = 0; y < pixels[0].length; y++) {
            for (int x = 0; x < pixels.length; x++) {
                int tmp = pixels[x][y];
                //RGBA
                //System.err.println((tmp >> 0 & 0xff));
                uploadBuffer.put((byte) (tmp >> 24 & 0xff));
                uploadBuffer.put((byte) (tmp >> 16 & 0xff));
                uploadBuffer.put((byte) (tmp >> 8 & 0xff));
                uploadBuffer.put((byte) (tmp >> 0 & 0xff));
            }
        }
        uploadBuffer.flip();
        glBindTexture(GL_TEXTURE_2D, ref);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, originalWidth, originalHeight, GL_RGBA, GL_UNSIGNED_BYTE, uploadBuffer);
        glFlush();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static int bitmapformatToOpenglformat(Format f) {
        //GL_LUMINANCE and GL_LUMINANCE_ALPHA were replaced in opengl 4
        switch (f) {
            case Grayscale:
                return GL_RED;
            case GrayscaleAlpha:
                return GL_RG;
            case RGB:
                return GL_RGB;
            case RGBA:
                return GL_RGBA;
            default:
                throw new RuntimeException("bad Format, not recognized");
        }

    }

    public void release() {
        glDeleteTextures(ref);
    }

    public int object() {
        return ref;
    }

    public int originalWidth() {
        return originalWidth;
    }

    public int originalHeight() {
        return originalHeight;
    }
}
