package ch.gb.mem;

import java.io.IOException;
import java.io.InputStream;

import ch.gb.utils.RessourceLoader;
import ch.gb.utils.Utils;

public class Rom {
	private final byte[] header;
	private boolean headerchunk=true;
	private byte[][] rom;

	private String title;
	private byte cgbflag;
	private int sgbflag;
	private int cartridgetype;
	private int romsize;
	private int ramsize;
	private int destination;
	private byte oldlicensee;
	private byte new1;
	private byte new2;
	private int maskRomVersionNumber;
	private byte headerchecksum;
	private int globalchecksum;
	private int banks16kB;

	String path = "";

	private String cartridgeAsString;
	private final String nl;

	public Rom(String path) {
		this.path = path;
		header = new byte[0x50 + 0x100];
		nl = System.getProperty("line.separator");
		load(path);
	}

	private void load(String path) {
		// FileHandle file = Gdx.files.internal(path);
		InputStream is = RessourceLoader.load(path);// file.read();
		byte[] tmp = new byte[0x10];
		// get header
		// file.readBytes(header, 0x0, 0x150);
		try {
			for (int i = 0; i < 0x150; i++) {
				header[i] = (byte) is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//is = RessourceLoader.load(path);// reload to reset IS

		System.arraycopy(header, 0x134, tmp, 0, 0x10);

		title = Utils.decASCII(tmp);
		// 013F-0142 -Manufacturere Code
		cgbflag = header[0x143];
		// 0144-0145 - New Licensee Code
		new1 = header[0x144];
		new2 = header[0x145];
		sgbflag = header[0x146];
		cartridgetype = header[0x147];
		romsize = 0x8000 << (header[0x148] & 0xff);
		banks16kB = romsize / 0x8000 * 2;
		ramsize = header[0x149] == 0 ? 0 : (header[0x149] == 1 ? 2048 : (header[0x149] == 2 ? 8192 : 32768));
		destination = header[0x14A];
		oldlicensee = header[0x14B];
		maskRomVersionNumber = header[0x14C];
		headerchecksum = header[0x14D];
		globalchecksum = (header[0x14E] & 0xff) << 8 | (header[0x14F] & 0xff);

		cartridgeAsString = "Title:" + title + nl + "CGB flag:" + getGameboyType(cgbflag) + nl + "New licensee code:"
				+ getNewLicensee(new1, new2) + nl + "SGB support:" + (sgbflag == 3) + nl + "Cartridgetype:"
				+ getCartridgeType(cartridgetype) + nl + "Rom size:" + romsize + " in 16kB:" + banks16kB + nl
				+ "Ram size:" + ramsize + nl + "Destination:" + getDestination(destination) + nl + "Old licensee code:"
				+ getOldLicensee(oldlicensee) + nl + "Mask rom version num:" + maskRomVersionNumber + nl
				+ "Header checksum:" + Utils.dumpHex(headerchecksum) + "  " + headerchksum(headerchecksum) + nl
				+ "Global checksum:" + Utils.dumpHex(globalchecksum);

		// load in 16kB chunks
		try {
			rom = new byte[banks16kB][0x4000];
			for (int i = 0; i < banks16kB; i++) {
				for (int x = 0; x < 0x4000; x++) {
					if(headerchunk){
						x=0x150;
						System.arraycopy(header, 0, rom[i],0, 0x150);
						headerchunk = false;
					}
					rom[i][x] = (byte) is.read();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		cartridgeAsString += "  " + globalchksum(globalchecksum);
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getLoadPath() {
		return path;
	}

	public String getInformation() {
		return cartridgeAsString;
	}

	public int getType() {
		return cartridgetype;
	}

	public int get16kRomNum() {
		return banks16kB;
	}

	public byte[] get16kRomBank(int bank) {
		return rom[bank];
	}

	/**
	 * Nope.avi
	 * 
	 * @param bank
	 * @return
	 */
	public byte[] get2kRamBank(int bank) {
		return null;
	}

	public int getRamSize() {
		return ramsize;
	}

	private String getCartridgeType(int type) {
		//@formatter:off
		String t = "UNKNOWN";
		switch (type){
		 case 0x00 : t="ROM ONLY                " ;break;
		 case 0x01 : t="MBC1                    " ;break;
		 case 0x02 : t="MBC1+RAM                " ;break;
		 case 0x03 : t="MBC1+RAM+BATTERY        " ;break;
		 case 0x05 : t="MBC2                    " ;break;
		 case 0x06 : t="MBC2+BATTERY            " ;break;
		 case 0x08 : t="ROM+RAM                 " ;break;
		 case 0x09 : t="ROM+RAM+BATTERY         " ;break;
		 case 0x0B : t="MMM01                   " ;break;
		 case 0x0C : t="MMM01+RAM               " ;break;
		 case 0x0D : t="MMM01+RAM+BATTERY       " ;break;
		 case 0x0F : t="MBC3+TIMER+BATTERY      " ;break;
		 case 0x10 : t="MBC3+TIMER+RAM+BATTERY  " ;break;
		 case 0x11 : t="MBC3                    " ;break;
		 case 0x12 : t="MBC3+RAM                " ;break;
		 case 0x13 : t="MBC3+RAM+BATTERY        " ;break;      
		 case 0x15 : t="MBC4                    " ;break;      
		 case 0x16 : t="MBC4+RAM                " ;break;      
		 case 0x17 : t="MBC4+RAM+BATTERY        " ;break;      
		 case 0x19 : t="MBC5                    " ;break;      
		 case 0x1A : t="MBC5+RAM                " ;break;      
		 case 0x1B : t="MBC5+RAM+BATTERY        " ;break;      
		 case 0x1C : t="MBC5+RUMBLE             " ;break;      
		 case 0x1D : t="MBC5+RUMBLE+RAM         " ;break;      
		 case 0x1E : t="MBC5+RUMBLE+RAM+BATTERY " ;break;      
		 case 0xFC : t="POCKET CAMERA           " ;break;      
		 case 0xFD : t="BANDAI TAMA5            " ;break;
		 case 0xFE : t="HuC3                    " ;break;
		 case 0xFF : t="HuC1+RAM+BATTERY        " ;break;
		}  
		return t;
	}
	private String getOldLicensee(byte b){
		String t="unknown (error?)";
		switch(b&0xff){
		case 0x00:t="none              ";break;case 0x01:t="nintendo          " ;break;case 0x08:t="capcom                ";break;
		case 0x09:t="hot:b             ";break;case 0x0A:t="jaleco            " ;break;case 0x0B:t="coconuts              ";break;
		case 0x0C:t="elite systems     ";break;case 0x13:t="electronic arts   " ;break;case 0x18:t="hudsonsoft            ";break;
		case 0x19:t="itc entertainment ";break;case 0x1A:t="yanoman           " ;break;case 0x1D:t="clary                 ";break;
		case 0x1F:t="virgin            ";break;case 0x24:t="pcm complete      " ;break;case 0x25:t="san:x                 ";break;
		case 0x28:t="kotobuki systems  ";break;case 0x29:t="seta              " ;break;case 0x30:t="infogrames            ";break;
		case 0x31:t="nintendo          ";break;case 0x32:t="bandai            " ;break;case 0x33:t="see new licensee code ";break;
		case 0x34:t="konami            ";break;case 0x35:t="hector            " ;break;case 0x38:t="capcom                ";break;
		case 0x39:t="banpresto         ";break;case 0x3C:t="*entertainment i  " ;break;case 0x3E:t="gremlin               ";break;
		case 0x41:t="ubi soft          ";break;case 0x42:t="atlus             " ;break;case 0x44:t="malibu                ";break;
		case 0x46:t="angel             ";break;case 0x47:t="spectrum holoby   " ;break;case 0x49:t="irem                  ";break;
		case 0x4A:t="virgin            ";break;case 0x4D:t="malibu            " ;break;case 0x4F:t="u.s. gold             ";break;
		case 0x50:t="absolute          ";break;case 0x51:t="acclaim           " ;break;case 0x52:t="activision            ";break;
		case 0x53:t="american sammy    ";break;case 0x54:t="gametek           " ;break;case 0x55:t="park place            ";break;
		case 0x56:t="ljn               ";break;case 0x57:t="matchbox          " ;break;case 0x59:t="milton bradley        ";break;
		case 0x5A:t="mindscape         ";break;case 0x5B:t="romstar           " ;break;case 0x5C:t="naxat soft            ";break;
		case 0x5D:t="tradewest         ";break;case 0x60:t="titus             " ;break;case 0x61:t="virgin                ";break;
		case 0x67:t="ocean             ";break;case 0x69:t="electronic arts   " ;break;case 0x6E:t="elite systems         ";break;
		case 0x6F:t="electro brain     ";break;case 0x70:t="infogrames        " ;break;case 0x71:t="interplay             ";break;
		case 0x72:t="broderbund        ";break;case 0x73:t="sculptered soft   " ;break;case 0x75:t="the sales curve       ";break;
		case 0x78:t="t*hq              ";break;case 0x79:t="accolade          " ;break;case 0x7A:t="triffix entertainment ";break;
		case 0x7C:t="microprose        ";break;case 0x7F:t="kemco             " ;break;case 0x80:t="misawa entertainment  ";break;
		case 0x83:t="lozc              ";break;case 0x86:t="*tokuma shoten i  " ;break;case 0x8B:t="bullet:proof software ";break;
		case 0x8C:t="vic tokai         ";break;case 0x8E:t="ape               " ;break;case 0x8F:t="i'max                 ";break;
		case 0x91:t="chun soft         ";break;case 0x92:t="video system      " ;break;case 0x93:t="tsuburava             ";break;
		case 0x95:t="varie             ";break;case 0x96:t="yonezawa/s'pal    " ;break;case 0x97:t="kaneko                ";break;
		case 0x99:t="arc               ";break;case 0x9A:t="nihon bussan      " ;break;case 0x9B:t="tecmo                 ";break;
		case 0x9C:t="imagineer         ";break;case 0x9D:t="banpresto         " ;break;case 0x9F:t="nova                  ";break;
		case 0xA1:t="hori electric     ";break;case 0xA2:t="bandai            " ;break;case 0xA4:t="konami                ";break;
		case 0xA6:t="kawada            ";break;case 0xA7:t="takara            " ;break;case 0xA9:t="technos japan         ";break;
		case 0xAA:t="broderbund        ";break;case 0xAC:t="toei animation    " ;break;case 0xAD:t="toho                  ";break;
		case 0xAF:t="namco             ";break;case 0xB0:t="acclaim           " ;break;case 0xB1:t="ascii or nexoft       ";break;
		case 0xB2:t="bandai            ";break;case 0xB4:t="enix              " ;break;case 0xB6:t="hal                   ";break;
		case 0xB7:t="snk               ";break;case 0xB9:t="pony canyon       " ;break;case 0xBA:t="*culture brain o      ";break;
		case 0xBB:t="sunsoft           ";break;case 0xBD:t="sony imagesoft    " ;break;case 0xBF:t="sammy                 ";break;
		case 0xC0:t="taito             ";break;case 0xC2:t="kemco             " ;break;case 0xC3:t="squaresoft            ";break;
		case 0xC4:t="*tokuma shoten i  ";break;case 0xC5:t="data east         " ;break;case 0xC6:t="tonkin house          ";break;
		case 0xC8:t="koei              ";break;case 0xC9:t="ufl               " ;break;case 0xCA:t="ultra                 ";break;
		case 0xCB:t="vap               ";break;case 0xCC:t="use               " ;break;case 0xCD:t="meldac                ";break;
		case 0xCE:t="*pony canyon or   ";break;case 0xCF:t="angel             " ;break;case 0xD0:t="taito                 ";break;
		case 0xD1:t="sofel             ";break;case 0xD2:t="quest             " ;break;case 0xD3:t="sigma enterprises     ";break;
		case 0xD4:t="ask kodansha      ";break;case 0xD6:t="naxat soft        " ;break;case 0xD7:t="copya systems         ";break;
		case 0xD9:t="banpresto         ";break;case 0xDA:t="tomy              " ;break;case 0xDB:t="ljn                   ";break;
		case 0xDD:t="ncs               ";break;case 0xDE:t="human             " ;break;case 0xDF:t="altron                ";break;
		case 0xE0:t="jaleco            ";break;case 0xE1:t="towachiki         " ;break;case 0xE2:t="uutaka                ";break;
		case 0xE3:t="varie             ";break;case 0xE5:t="epoch             " ;break;case 0xE7:t="athena                ";break;
		case 0xE8:t="asmik             ";break;case 0xE9:t="natsume           " ;break;case 0xEA:t="king records          ";break;
		case 0xEB:t="atlus             ";break;case 0xEC:t="epic/sony records " ;break;case 0xEE:t="igs                   ";break;
		case 0xF0:t="a wave            ";break;case 0xF3:t="extreme entertainment"; break;case 0xFF: t="ljn					";break;
		}
		return t;
	}
	
	private String getNewLicensee(byte b1,byte b2){
		byte[] tmp = new byte[2];
		tmp[0]=b1;
		tmp[1]=b2;
		
		String num = Utils.decASCII(tmp);
		String t="";
		int nb =0;
		try{
		 nb = Integer.parseInt(num);
		}catch(Exception ex){//supress exception
		}
		switch(nb){
		case 00:t="none            " ;break;      case 1:t="nintendo      ";break;   case 8:t="capcom                ";break;
		case 13:t="electronic arts " ;break;      case 18:t="hudsonsoft    ";break;   case 19:t="b:ai                  ";break;
		case 20:t="kss             " ;break;      case 22:t="pow           ";break;   case 24:t="pcm complete          ";break;
		case 25:t="san:x           " ;break;      case 28:t="kemco japan   ";break;   case 29:t="seta                  ";break;
		case 30:t="viacom          " ;break;      case 31:t="nintendo      ";break;   case 32:t="bandia                ";break;
		case 33:t="ocean/acclaim   " ;break;      case 34:t="konami        ";break;   case 35:t="hector                ";break;
		case 37:t="taito           " ;break;      case 38:t="hudson        ";break;   case 39:t="banpresto             ";break;
		case 41:t="ubi soft        " ;break;      case 42:t="atlus         ";break;   case 44:t="malibu                ";break;
		case 46:t="angel           " ;break;      case 47:t="pullet:proof  ";break;   case 49:t="irem                  ";break;
		case 50:t="absolute        " ;break;      case 51:t="acclaim       ";break;   case 52:t="activision            ";break;
		case 53:t="american sammy  " ;break;      case 54:t="konami        ";break;   case 55:t="hi tech entertainment ";break;
		case 56:t="ljn             " ;break;      case 57:t="matchbox      ";break;   case 58:t="mattel                ";break;
		case 59:t="milton bradley  " ;break;      case 60:t="titus         ";break;   case 61:t="virgin                ";break;
		case 64:t="lucasarts       " ;break;      case 67:t="ocean         ";break;   case 69:t="electronic arts       ";break;
		case 70:t="infogrames      " ;break;      case 71:t="interplay     ";break;   case 72:t="broderbund            ";break;
		case 73:t="sculptured      " ;break;      case 75:t="sci           ";break;   case 78:t="t*hq                  ";break;
		case 79:t="accolade        " ;break;      case 80:t="misawa        ";break;   case 83:t="lozc                  ";break;
		case 86:t="tokuma shoten i*" ;break;      case 87:t="tsukuda ori*  ";break;   case 91:t="chun soft             ";break;
		case 92:t="video system    " ;break;      case 93:t="ocean/acclaim ";break;   case 95:t="varie                 ";break;
		case 96:t="yonezawas'pal   " ;break;      case 97:t="kaneko        ";break;   case 99:t="pack in soft          ";break;
		}
		return t;
	}
	private String getGameboyType(byte b){
	  return b==0x80? "Gameboy Color":"Not Gameboy Color";
	}
	private String getDestination(int b){
		return b==0?"Japan":"Non-Japan";
	}
	private String headerchksum(byte b){
		int x =0;
		//System.out.println(Utils.dumpHex(b));
		for(int i = 0x134;i<0x14C+1;i++){
			x= x-header[i]-1;
		}
		//System.out.println(Utils.dumpHex((byte)x));
		return (b&0xff)==(x&0xff)?"(CORRECT)":"(FAILED)";
	}
	private String globalchksum(int d){
		int add=0;
		for(int i =0; i<romsize;i++){
			add += (rom[i/0x4000][i%0x4000]&0xff);
		}
		add-=header[0x14E];
		add-=header[0x14F];
		//System.out.println(Utils.dumpHex(d&0xffff));
		//System.out.println(Utils.dumpHex(add&0xffff));
		return (add&0xffff)==(d&0xffff)?"(CORRECT) - not reliable":"(FAILED) - not reliable";
	}
	
}
