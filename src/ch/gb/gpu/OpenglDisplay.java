/*******************************************************************************
 *     <A simple gameboy emulator>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
package ch.gb.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Disposable;

/**
 * Bufferformat is as follows int[][] with 256 x 240 each int is as follows int
 * b1 b2 b3 b4 b4 =blue b3 =green b2 =red
 * 
 * @author bluew
 * 
 */
public class OpenglDisplay implements Disposable {
	public Pixmap screen;
	public Texture texture;

	private final int[][] bbuffer;

	private final Pixmap[] renderPixmaps;
	private final PixmapUpdate[] changedPixmaps;

	private final int bwidth;
	private final int bheight;
	private int tile = 8;// default
	private final boolean firstBind = true;
	ByteBuffer interBuffer;
	
	private class PixmapUpdate {
		boolean updateNeeded = false;
		int x;
		int y;

		PixmapUpdate() {

		}
	}

	/**
	 * 
	 * @param bwidth
	 *            width of the buffer (actual screen)
	 * @param bheight
	 *            height of the buffer (actual screen)
	 * @param tpowerof2
	 *            sizes of the underlying texture (larger or equal to buffer)
	 * @param fsize
	 *            size of the tiles the textures is split up (8 or 16 or 32)
	 */
	public OpenglDisplay(int bwidth, int bheight, int tpowerof2, int fsize) {
		this.bwidth = bwidth;
		this.bheight = bheight;
		bbuffer = new int[bwidth][bheight];
		this.tile = fsize;

		if (tpowerof2 < bwidth || tpowerof2 < bheight) {
			throw new RuntimeException("Texture too small (smaller than buffer)");
		}

		screen = new Pixmap(tpowerof2, tpowerof2, Format.RGBA8888);

		texture = new Texture(new PixmapTextureData(screen, screen.getFormat(), false, true));

		// System.out.println("t_height=" + texture.getHeight());
		// System.out.println("t_width=" + texture.getWidth());

		int numPixmaps = ((bwidth / tile) * (bheight / tile));
		// System.out.println("numpix:" + numPixmaps);
		renderPixmaps = new Pixmap[numPixmaps];

		changedPixmaps = new PixmapUpdate[numPixmaps];

		for (int i = 0; i < renderPixmaps.length; i++) {
			renderPixmaps[i] = new Pixmap(tile, tile, Format.RGBA8888);
		}
		for (int i = 0; i < changedPixmaps.length; i++) {
			changedPixmaps[i] = new PixmapUpdate();
		}

		// disable unused features
		Gdx.gl.glDisable(GL10.GL_ALPHA_TEST);
		Gdx.gl.glDisable(GL10.GL_BLEND);
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		// Gdx.gl.glDisable(GL30.GL_POLYGON_SMOOTH);
		Gdx.gl.glDisable(GL10.GL_STENCIL_TEST);

		// enable useful features
		Gdx.gl.glEnable(GL10.GL_DITHER);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);

		//Allocate intermediate buffer
		interBuffer = ByteBuffer.allocateDirect(bwidth*bheight*4);
		interBuffer.order(ByteOrder.LITTLE_ENDIAN);
		// disable unnecessary stuff
		// Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		// Gdx.gl.glDisable(GL11.GL_SMOOTH);
		// Gdx.gl.glDisable(GL10.GL_DITHER);
		// Gdx.gl.glDisable(GL10.GL_STENCIL_TEST);
		// Gdx.gl.glDisable(GL10.GL_BLEND);
		// Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		// Gdx.gl.glDisable(GL10.GL_ALPHA_TEST);

		// Gdx.gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
	}

	/**
	 * Rebinds texture to match new size
	 */
	public void resize() {

	}

	/**
	 * just updates whole texture, for possible shader usage
	 * receives RGBA data
	 * @param data
	 */
	public void refresh2(int data[][]) {
		//need to rebind because libgdx does probably bind textures on his own (fonts etc..)
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureObjectHandle());

		interBuffer.clear();
		for (int y = 0; y < data[0].length; y++) {
			for (int x = 0; x < data.length; x++) {
				int tmp = data[x][y];
				//RGBA
				interBuffer.put((byte)(tmp>>24 & 0xff));
				interBuffer.put((byte)(tmp>>16 & 0xff));
				interBuffer.put((byte)(tmp>>8  & 0xff));
				interBuffer.put((byte)(tmp>>0 & 0xff));
			}
		}
		interBuffer.flip();
		Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, bwidth, bheight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,interBuffer);
		Gdx.gl.glFlush();
	}

	/**
	 * feeds new screen data as linear(not) array
	 */
	public void refresh(int data[][]) {

		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureObjectHandle());
		
		Pixmap.setBlending(Blending.None);

		// compare data in tile x tile chunks
		int offset = 0;
		for (int y = 0; y < data[0].length / tile; y++) {
			for (int x = 0; x < data.length / tile; x++) {
				// scan tiles of tile x tile
				boolean changed = false;
				// System.out.println(offset);
				for (int yy = 0; yy < tile; yy++) {
					for (int xx = 0; xx < tile; xx++) {
						int px = x * tile + xx;
						int py = y * tile + yy;

						if (data[px][py] != bbuffer[px][py]) {
							renderPixmaps[offset].setColor(data[px][py]);
							renderPixmaps[offset].drawPixel(xx, yy);
							changed = true;

						}
					}
				}
				if (changed) {
					changedPixmaps[offset].updateNeeded = true;
					changedPixmaps[offset].x = x * tile;
					changedPixmaps[offset].y = y * tile;
				}
				offset++;
			}
		}

		// update all changed pixmap to texture
		for (int i = 0; i < changedPixmaps.length; i++) {
			if (changedPixmaps[i].updateNeeded) {
				// System.out.println("updating Texture");
				// changedPixmap8[i].updateNeeded = false;
				// System.out.println("X=" + changedPixmap8[i].x);
				// System.out.println("Y=" + changedPixmap8[i].y);
				// works
				Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, changedPixmaps[i].x, changedPixmaps[i].y,
						renderPixmaps[i].getWidth(), renderPixmaps[i].getHeight(), renderPixmaps[i].getGLFormat(),
						renderPixmaps[i].getGLType(), renderPixmaps[i].getPixels());
				// texture.draw(renderPixmap8[0],changedPixmap8[i].x,
				// changedPixmap8[i].y);
			}
		}
		// buffer last frame
		for (int x = 0; x < bwidth; x++) {
			System.arraycopy(data[x], 0, bbuffer[x], 0, bheight);
		}

	}

	public void drawStraight(SpriteBatch batch, float x, float y, float originX, float originY, float width,
			float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight) {
		batch.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth,
				srcHeight, false, false);

	}

	public void requestFullUpdate() {
		// just clear buffer to enforce complete update
	}

	@Override
	public void dispose() {
		texture.dispose();
		for (int i = 0; i < renderPixmaps.length; i++) {
			renderPixmaps[i].dispose();
		}

	}
}
