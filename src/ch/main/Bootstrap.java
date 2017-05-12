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
package ch.main;

import ch.gb.GB;
import ch.render.OGLrenderer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author bluew
 */
public class Bootstrap {

    private static final Options options = new Options();

    public static void main(String[] args) {
        Option rom = Option.builder("f")
                .required(true)
                .hasArg()
                .longOpt("file")
                .desc("load given rom")
                .build();
        Option zoom = Option.builder("z")
                .hasArg()
                .longOpt("zoom")
                .desc("an integer that defines the zoom level")
                .build();

        options.addOption(rom);
        options.addOption(zoom);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            String rompath = line.getOptionValue("f");

            int zoomlvl = 4;
            if (line.hasOption("z")) {
                zoomlvl = Integer.parseInt(line.getOptionValue("z"));
            }

            GB gb = new GB();
            gb.loadRom(rompath);
            new OGLrenderer(gb, zoomlvl).run();

        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
        }
    }
}
