package ch.gb.io;

import ch.gb.Settings;
import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.InputProcessor;

public class Joypad implements IOport, InputProcessor {
	public final static int P1 = 0xff00;

	private int joy8 = 0xff;// all buttons mapped as 4 and 4 bits
	private byte selection = 0x3F;

	private final MemoryManager mem;

	public Joypad(MemoryManager mem) {
		this.mem = mem;
	}

	@Override
	public void write(int add, byte b) {
		//System.out.println("happens");
		// select buttons or direction pad
		selection &= 0xCF;
		selection |= (b & 0x30);
	}

	@Override
	public byte read(int add) {
		//System.out.println("hthattooappens");
		if ((selection & 0x20) == 0) {
			// select buttons
			// System.out.println(Integer.toBinaryString(selection &
			// (0x30|((joy8 >> 4) & 0xf))));
			return (byte) (selection & (0x30 | ((joy8 >> 4) & 0xf)));

		} else if ((selection & 0x10) == 0) {
			// select direction pad
			// System.out.println("faggy");
			return (byte) (selection & (0x30 | (joy8 & 0xf)));
		}
		return (selection);
	}

	public void tick() {

	}

	// --------Libgdx Input processing ---------------------------
	// 0 = pressed
	// 1 = not pressed
	@Override
	public boolean keyDown(int keycode) {
		if (Settings.joymap.containsKey(keycode)) {
			mem.requestInterrupt(CPU.JOYPAD_IR);
			// clear corresponding bit
			int key = Settings.joymap.get(keycode);
			joy8 &= ~(1 << key);

			if (key > 3 && (selection & 0x20) == 0) { // = buttons
				mem.requestInterrupt(CPU.JOYPAD_IR);
			} else if (!(key > 3) && (selection & 0x10) == 0) {
				mem.requestInterrupt(CPU.JOYPAD_IR);
			}
			// System.out.println("whatever");
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (Settings.joymap.containsKey(keycode)) {
			// set corresponding bit
			joy8 |= (1 << Settings.joymap.get(keycode));

			// System.out.println(Utils.dumpHex(joy8));
		}
		return false;
	}

	// ------ not relevant------------------------
	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {

		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {

		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		return false;
	}
}
