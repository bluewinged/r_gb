/*
=======
/* 
 * Copyright (C) 2017 bluew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.input;

import ch.gb.io.IJoypad;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import org.lwjgl.glfw.GLFWKeyCallback;

/**
 *
 * @author bluew
 */
public class GLFWInput extends GLFWKeyCallback {

    private IJoypad joypad;

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
       
        if (joypad == null) {
            return;
        }
        if (action == GLFW_PRESS) {          
            joypad.keyDown(key);
        } else if (action == GLFW_RELEASE) {
            joypad.keyUp(key);
        }

    }

    public GLFWInput register(IJoypad joypad) {
        this.joypad = joypad;
        return this;
    }

}
