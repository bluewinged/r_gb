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

import ch.gb.Component;
import ch.gb.GB;
import ch.gb.GBComponents;
import ch.gb.cpu.CPU;
import ch.gb.mem.Memory;
import ch.gb.utils.Utils;

public class GPU implements Component {

    public static final int LCD_C = 0xFF40;
    public static final int STAT = 0xFF41;

    public static final int SCY = 0xFF42;
    public static final int SCX = 0xFF43;

    public static final int LY = 0xFF44;
    public static final int LYC = 0xFF45;

    public static final int WY = 0xFF4A;
    public static final int WX = 0xFF4B;

    public static final int BGP = 0xFF47;
    public static final int OBP0 = 0xFF48;
    public static final int OBP1 = 0xFF49;

    private byte lcdc;
    private boolean lcdEnabled;
    private int winTilemap;
    private boolean winEnable;
    private int bgWinTiledata;
    private int bgTilemap;
    private boolean spr8x16;
    private boolean sprEnable;
    private boolean bgEnable;

    private byte stat;
    private int mode;
    private int coincidence;

    private int scy;
    private int scx;

    private int ly;// scanlinecounter
    private int lyc;

    private int wx;
    private int wy;

    private byte bgpraw;
    private byte obp0raw;
    private byte obp1raw;
    private final byte[] bgp = {0, 0, 0, 0};
    private final byte[][] obp = {{0, 0, 0, 0}, {0, 0, 0, 0}};
    // FORMAT: RGBA 10er hex
    // dark green: 015;056;015 -> 0xF , 0x38, 0xF
    // green : 048;098;048 -> 0x30, 0x62, 0x30
    // bright grn: 139;172;015 -> 0x8B, 0xAC, 0xF
    // brgter grn: 155;188;015 -> 0x9B, 0xBC, 0xF
    // brighter, bright, green, darkgreen
    // private final int[] palette = { 0x9BBC0FFF, 0x8BAC0FFF,
    // 0x306230FF,0x0F380FFF };//wiki
        private final int[] palette = {0xE0f8d0ff, 0x88C070ff, 0x346856,
        0x081820ff};//BGB
    ///private final int[] palette = {0xE8E8E8FF, 0xA0A0A0FF, 0x585858FF, 0x101010FF};// b/w
    private int lcdClock = 456;

    private final GB gb;
    private Memory mem;

    public int[][] videobuffer;
    private boolean masterPriority;

    public GPU(GB gb) {
        this.gb = gb;
        videobuffer = new int[160][144];
    }

    @Override
    public void reset() {
        write(LCD_C, (byte) 0);
        stat = 0;
        mode = 0;
        coincidence = 0;
        scy = 0;
        scx = 0;
        ly = 0;
        lyc = 0;
        wx = 0;
        wy = 0;
        bgpraw = 0;
        obp0raw = 0;
        obp1raw = 0;
        for (int i = 0; i < 4; i++) {
            bgp[i] = 0;
            obp[0][i] = 0;
            obp[1][i] = 0;
        }
        for (int y = 0; y < 144; y++) {
            for (int x = 0; x < 160; x++) {
                videobuffer[x][y] = 0;
            }
        }
    }

    @Override
    public void connect(GBComponents comps) {
        this.mem = comps.mem;

    }

    public void write(int add, byte b) {
        if (add == LCD_C) {
            lcdc = b;
            lcdEnabled = (b & 0x80) == 0x80;
            winTilemap = (b & 0x40) == 0x40 ? 0x9C00 : 0x9800;// nametable
            winEnable = (b & 0x20) == 0x20;

            bgWinTiledata = (b & 0x10) == 0x10 ? 0x8000 : 0x9000;// patterns (0 is unsigned addressing)

            bgTilemap = (b & 8) == 8 ? 0x9C00 : 0x9800;// nametable //one 0 to much caused the whole bug...
            spr8x16 = (b & 4) == 4;
            sprEnable = (b & 2) == 2;
            bgEnable = (b & 1) == 1; //dmg background becomes white
            masterPriority = (b & 1) ==1; //TODO: cgb bg and window master priority

        } else if (add == STAT) {
            stat = (byte) (b & 0x78); // clear lower 3 bits and 7 ( those are read only)
        } else if (add == SCX) {
            scx = b & 0xff;
        } else if (add == SCY) {
            scy = b & 0xff;
        } else if (add == LY) {
            ly = 0;
        } else if (add == LYC) {
            lyc = b & 0xff;
        } else if (add == WY) {
            wy = b & 0xff;
        } else if (add == WX) {
            wx = (b & 0xff);
        } else if (add == BGP) {
            bgpraw = b;
            bgp[0] = (byte) (b & 3);
            bgp[1] = (byte) (b >> 2 & 3);
            bgp[2] = (byte) (b >> 4 & 3);
            bgp[3] = (byte) (b >> 6 & 3);
        } else if (add == OBP0) {
            obp0raw = b;
            obp[0][0] = (byte) (b & 3); //not used because transparent
            obp[0][1] = (byte) (b >> 2 & 3);
            obp[0][2] = (byte) (b >> 4 & 3);
            obp[0][3] = (byte) (b >> 6 & 3);
        } else if (add == OBP1) {
            obp1raw = b;
            obp[1][0] = (byte) (b & 3); //not used because transparent
            obp[1][1] = (byte) (b >> 2 & 3);
            obp[1][2] = (byte) (b >> 4 & 3);
            obp[1][3] = (byte) (b >> 6 & 3);
        }

    }

    public byte read(int add) {
        if (add == LCD_C) {
            return lcdc;
        } else if (add == STAT) {
            return (byte) (stat | (coincidence << 2 & 4) | mode & 3);
        } else if (add == SCX) {
            return (byte) scx;
        } else if (add == SCY) {
            return (byte) scy;
        } else if (add == LY) {
            return (byte) ly;
        } else if (add == LYC) {
            return (byte) lyc;
        } else if (add == WY) {
            return (byte) wy;
        } else if (add == WX) {
            return (byte) wx;
        } else if (add == BGP) {
            return bgpraw;
        } else if (add == OBP0) {
            return obp0raw;
        } else if (add == OBP1) {
            return obp1raw;
        } else {
            throw new RuntimeException("GPU->couldnt decode address:" + Utils.dumpHex(add) + " (Read)");
        }
    }

    /**
     * 160x144 pixels to draw
     *
     * @param cycles
     */
    public void clock(int cycles) {

        if (!lcdEnabled) {
            lcdClock = 456;
            ly = 0;
            mode = 1;
        } else {
            // mode 0: 204 cycles
            // mode 1:4560 cycles
            // mode 2: 80 cycles
            // mode 3: 172 cycles
            //complete screen refresh 70224 = 144 * 456 + 4560

            if (ly >= 144) { //ly = 144 ... 153
                if (mode != 1) {
                    mode = 1;//VBLANK 
                    if ((stat & 0x10) == 0x10) {
                        mem.requestInterrupt(CPU.LCD_IR);
                    }
                    mem.requestInterrupt(CPU.VBLANK_IR);
                    gb.signalVblank();//signal that screen is fully rendered
                }
            } else if (lcdClock >= 456 - 80) { // counting downwards!
                if (mode != 2) {
                    mode = 2; //Searching OAM
                    if ((stat & 0x20) == 0x20) {
                        mem.requestInterrupt(CPU.LCD_IR); //TODO: check
                    }
                }
            } else if (lcdClock >= 456 - 80 - 172) {
                mode = 3; //data transfer to LCD
            } else {
                if (mode != 0) { //HBLANK
                    mode = 0;

                    if ((stat & 0x8) == 0x8) {
                        mem.requestInterrupt(CPU.LCD_IR);
                    }

                    if (bgEnable) { //background
                        drawBgScanline();
                    }
                    if (winEnable) { //TODO: window
                        drawWinScanline();
                    }

                    if (sprEnable) { //sprites
                        drawSprScanline();
                    }
                }
            }
        }
        if (!lcdEnabled) {
            return;
        }

        lcdClock -= cycles;
        if (lcdClock <= 0) {
            lcdClock = 456 + lcdClock; // adjust if taken too many
            ly++;
            if (ly > 153) {
                ly = 0;
            }
            scanlineCompare();
        }
    }

    public void scanlineCompare() {
        if (ly == lyc) {
            coincidence = 1;
            if ((stat & 0x40) == 0x40) {
                mem.requestInterrupt(CPU.LCD_IR);
            }
        } else {
            coincidence = 0;
        }
    }

    public void drawBgScanline() { // bg and window scanline
        int y = ly + scy;// which scanline in the tilemap
        y = y % 256;// wrap around bg map

        int bgInTileY = y % 8 * 2;

        boolean signed = bgWinTiledata == 0x9000;
        int bgEntry = bgTilemap + y / 8 * 32;// 32 tiles per nametablerow

        int targetTilemap = bgEntry;
        int targetTileY = bgInTileY;

        for (int x = 0; x < 160; x++) {//

            int tx = (x + scx) & 0xff;

            // fetch namtable byte
            byte tileId = mem.readByte(targetTilemap + tx / 8);

            int tileLoc = bgWinTiledata + (signed ? (int) tileId : tileId & 0xff) * 16;

            // fetch tile pattern
            byte lo = mem.readByte(tileLoc + targetTileY);
            byte hi = mem.readByte(tileLoc + targetTileY + 1);

            int inTileX = tx % 8;

            int color = palette[bgp[(lo >> (7 - inTileX) & 1) | ((hi >> (7 - inTileX) & 1) << 1)]];
            videobuffer[x][ly] = color;
        }

    }

    public void drawWinScanline() {
        //int winInTileY = ((ly - wy) % 8) * 2;
        //int winEntry = winTilemap + (ly - wy) / 8 * 32;

        int winInTileY = ((ly - wy) % 8) * 2;
        int winEntry = winTilemap + (ly - wy) / 8 * 32;
        // first check wether this can be a window scanline
        if ((ly < wy) || wx >= 160) {
            return;
        }
        boolean signed = bgWinTiledata == 0x9000;
        int targetTilemap = winEntry;
        int targetTileY = winInTileY;

        for (int x = 0; x < 160; x++) {//

            int tx = (x + wx - 7) & 0xff;

            // fetch namtable byte
            byte tileId = mem.readByte(targetTilemap + tx / 8);

            int tileLoc = bgWinTiledata + (signed ? (int) tileId : tileId & 0xff) * 16;

            // fetch tile pattern
            byte lo = mem.readByte(tileLoc + targetTileY);
            byte hi = mem.readByte(tileLoc + targetTileY + 1);

            int inTileX = tx % 8;

            int color = palette[bgp[(lo >> (7 - inTileX) & 1) | ((hi >> (7 - inTileX) & 1) << 1)]];
            videobuffer[x][ly] = color;
        }
    }

    public void drawSprScanline() {
        // counting backwards ensures that $fe00 has a higher priority compared to
        //$fe00+x //fixes a wario land bug which caused the 4 blocks in the start screen
        //to be rendered incorrectly
        
        //TODO: order sprites according to their x coordinates and then render them
        //from the highest x value to the lowest
        for (int i = 39; i >= 0; i--) {
            int ypos = (mem.readByte(0xFE00 + i * 4) & 0xff) - 16;
            int xpos = (mem.readByte(0xFE00 + i * 4 + 1) & 0xff) - 8;
            int tileid = (mem.readByte(0xFE00 + i * 4 + 2) & 0xff);
            byte attr = mem.readByte(0xFE00 + i * 4 + 3);

            // 8x8 mode
            int priority = (attr >> 7) & 1;
            int yflip = (attr >> 6) & 1;
            int xflip = (attr >> 5) & 1;

            int pal = (attr >> 4) & 1;
            int size = spr8x16 ? 16 : 8; // 16 mode works

            // clipping
            if (ly >= ypos && ly < (ypos + size)) {
                int line = (ly - ypos);
                line = yflip == 1 ? (spr8x16 ? 15 : 7) - line : line;

                int patternentry = 0x8000 + tileid * 16 + line * 2;
                byte lo = mem.readByte(patternentry);
                byte hi = mem.readByte(patternentry + 1);
                
                //draw the 8x1 pixels
                for (int x = 0; x < 8; x++) {

                    int newx = (xflip == 1 ? x : 7 - x);
                    int tx = xpos + x;
                    
                    if (tx < 0 || tx >= 160) {
                        continue;
                    }
                    //TODO: there is still a priority bug, see wario land when wario enters a pipe
                    //TODO: display max 10 sprites per scanline
                    if (priority == 1&& videobuffer[tx][ly] != palette[bgp[0]] ) {
                        continue;
                    }

                    int color = (lo >> (newx)) & 1 | ((hi >> (newx)) & 1) << 1;
                    int palcolor = obp[pal][color];
                    if (color != 0)// sprite transparency
                    {
                        videobuffer[tx][ly] = palette[palcolor];
                    }

                }
            }
        }
    }

    public int[] get8spr(int line, byte sprid, byte attr) {
        int priority = (attr >> 7) & 1;
        int yflip = (attr >> 6) & 1;
        int xflip = (attr >> 5) & 1;
        int pal = (attr >> 4) & 1;

        line = line % 8;
        line = yflip == 1 ? 8 - line : line;

        int table = 0x8000;
        int tile = sprid & 0xff;
        int patternentry = table + tile * 16 + line * 2;

        byte lo = mem.readByte(patternentry);
        byte hi = mem.readByte(patternentry + 1);

        int[] ib = new int[8];
        for (int i = 0; i < 8; i++) {
            int shift = seq[xflip][i];
            ib[i] = palette[obp[pal][(lo >> (shift)) & 1 | ((hi >> (shift)) & 1) << 1]];
        }
        return ib;
    }

    /**
     * Debug method, gets 8 pixel from the pattern table
     */
    public int[] get8bg(int line, byte tile, int table) {
        line = line % 8;
        // int target = table == 0 ? 0x8000 : 0x9000;
        int target = bgWinTiledata;
        boolean signed = target == 0x9000;
        int realtile = (signed ? (int) tile : tile & 0xff);
        int patternentry = target + realtile * 16 + line * 2;
        byte lo = mem.readByte(patternentry);
        byte hi = mem.readByte(patternentry + 1);
        int[] ib = new int[8];
        for (int i = 0; i < 8; i++) {
            ib[i] = palette[bgp[(lo >> (7 - i)) & 1 | ((hi >> (7 - i)) & 1) << 1]];
        }
        return ib;
    }

    private final int[][] seq = {{7, 6, 5, 4, 3, 2, 1, 0}, {0, 1, 2, 3, 4, 5, 6, 7}};

}
