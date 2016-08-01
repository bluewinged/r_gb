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
package ch.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 *
 * @author bluew
 */
public class GLShader {

    private String code = null;
    private int ref = 0;

    // private int shaderType = -1;

    public GLShader(String code, int shaderType) {
        this.code = code;
        ref = glCreateShader(shaderType);
        if (ref == 0) {
            throw new RuntimeException("glCreateShader failed");
        }

        glShaderSource(ref, code);
        glCompileShader(ref);
        if (glGetShaderi(ref, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't compile shader");
        }
    }
    public int object(){
        return ref;
    }
    public void release(){
        glDeleteShader(ref);
    }
}
