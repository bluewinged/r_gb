package ch.gb;

import java.util.HashMap;

import com.badlogic.gdx.Input.Keys;

public class Settings {
	public static HashMap<Integer, Integer> joymap;
	public static String root = "rgbsavegames/";
	static {
		// default Joypad mapping
		// 7: start
		// 6: select
		// 5: button B
		// 4: button A
		// 3: down
		// 2: up
		// 1: left
		// 0: right
		joymap = new HashMap<Integer, Integer>();
		joymap.put(Keys.RIGHT, 0);
		joymap.put(Keys.LEFT, 1);
		joymap.put(Keys.UP, 2);
		joymap.put(Keys.DOWN, 3);

		joymap.put(Keys.S, 4);
		joymap.put(Keys.A, 5);
		joymap.put(Keys.SHIFT_RIGHT, 6);
		joymap.put(Keys.ENTER, 7);
	}
	public static float mastervolume = 1f;// between 0 and 1
	public static int ch1enable = 1;// 0 disabled, 1 enabled,
	public static int ch2enable = 1;
	public static int ch3enable = 1;
	public static int ch4enable = 1;
	public static int speedup = 10;
	
	
	public static boolean checkSettings(){
		return false;
	}
}
