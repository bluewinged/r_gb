package ch.gb;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import ch.gb.utils.InputStub;
import ch.gb.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class GUI extends InputStub {
	private final GB gb;

	private final Skin skin;
	private final Stage stage;

	private Window window;

	public GUI(GB gb) {
		this.gb = gb;
		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	public void build() {
		window = new Window("Menu", skin.getStyle(WindowStyle.class), "menu");
		window.x = 0;
		window.y = 0;
		window.width = 200;
		window.height = 300;
		window.visible = false;

		final Button buttonLoad = new TextButton("Load ROM", skin.getStyle(TextButtonStyle.class), "loadbutton");
		final Button buttonInfo = new TextButton("ROM informations", skin.getStyle(TextButtonStyle.class), "infobutton");
		final Button buttonOptions = new TextButton("Options", skin.getStyle(TextButtonStyle.class), "optionsbutton");
		final Button buttonReset = new TextButton("Reset", skin.getStyle(TextButtonStyle.class), "resetbutton");
		final Button buttonExit = new TextButton("Exit", skin.getStyle(TextButtonStyle.class), "exitbutton");
		final Button buttonAbout = new TextButton("About", skin.getStyle(TextButtonStyle.class), "aboutbutton");

		window.align(Align.TOP);
		window.row().fill().expand();
		window.add(buttonLoad);
		window.row().fill().expand();
		window.add(buttonInfo);
		window.row().fill().expand();
		window.add(buttonOptions);
		window.row().fill().expand();
		window.add(buttonAbout);
		window.row().fill().expand();
		window.add(buttonReset);
		window.row().fill().expand();
		window.add(buttonExit);
		window.setMovable(false);

		// OPEN DIALOG---------------------------------------------------
		final Window opendialog = new Window("Load Roms", skin.getStyle(WindowStyle.class), "opendialog");
		final Table layout = new Table();
		final Table entries = new Table();
		final ScrollPane scrollPane = new ScrollPane(entries, skin.getStyle(ScrollPaneStyle.class), "scroll");
		layout.add(scrollPane).minWidth(320).maxHeight(288).colspan(2);
		layout.row();
		final TextButton load = new TextButton("Load", skin.getStyle(TextButtonStyle.class), "load");
		final TextButton close = new TextButton("Close", skin.getStyle(TextButtonStyle.class), "close");
		layout.add(load).fill().expandX();
		layout.add(close).fill().expandX();

		opendialog.add(layout);
		opendialog.width = 320;
		opendialog.height = 288;
		opendialog.x = Gdx.graphics.getWidth() / 2 - opendialog.width / 2;
		opendialog.y = Gdx.graphics.getHeight() / 2 - opendialog.height / 2;
		opendialog.visible = false;
		opendialog.setMovable(false);

		buttonLoad.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				// open load dialog
				final File f = new File("roms/");// standard files where the rom
				opendialog.visible = true;
				window.visible = false;
				File[] dirs = f.listFiles(new DirFilter());
				String[] data = f.list(new GBFilter());
				String[] tmp = new String[dirs.length];
				for(int i =0; i<dirs.length;i++){
					tmp[i] = dirs[i].toString()+" (DIRECTORY)";
				}
				String[] combined = Utils.addArrays(tmp,data);

				final List list = new List(combined, skin.getStyle(ListStyle.class), "list");
				scrollPane.setWidget(list);
				
				load.setClickListener(new ClickListener() {
					@Override
					public void click(Actor actor, float x, float y) {
						if (list != null) {
							gb.reset();
							gb.loadRom(f.getPath() + File.separatorChar + list.getSelection());
							gb.runGameboy();
							opendialog.visible = false;
						}
					}
				});
			}
		});
		close.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				opendialog.visible = false;
			}
		});

		// OPTIONS-DIALOG------------------------------------------------
		final Window optionsdialog = new Window("Options", skin.getStyle(WindowStyle.class), "options");
		optionsdialog.width = 320;
		optionsdialog.height = 288;
		optionsdialog.x = Gdx.graphics.getWidth() / 2 - optionsdialog.width / 2;
		optionsdialog.y = Gdx.graphics.getHeight() / 2 - optionsdialog.height / 2;
		optionsdialog.visible = false;
		optionsdialog.setMovable(false);
		optionsdialog.debug();// !
		final Table optionslayout = new Table();
		final CheckBox fpscheckbox = new CheckBox("Show frames per second", skin.getStyle(CheckBoxStyle.class),
				"showfps");
		final CheckBox showRomInfo = new CheckBox("Show rom info at startup", skin.getStyle(CheckBoxStyle.class),
				"showrominfo");
		final SelectBox selectSound = new SelectBox(new String[] { "JavaSound", "OpenAL" },
				skin.getStyle(SelectBoxStyle.class), "selectsound");
		final SelectBox resampler = new SelectBox(new String[] { "None", "Chain Sinc"},
				skin.getStyle(SelectBoxStyle.class), "resampler");
		final Label selectSoundLabel = new Label("Sound rendering", skin.getStyle(LabelStyle.class), "selectsoundlabel");
		final Label selectResampler = new Label("Resampler", skin.getStyle(LabelStyle.class), "selectresampler");
		
		final TextButton applyoptions = new TextButton("Apply", skin.getStyle(TextButtonStyle.class), "apply");
		final TextButton closeoptions = new TextButton("Close", skin.getStyle(TextButtonStyle.class), "closeoptions");
		optionslayout.debug();// !
		optionslayout.width(320);
		optionslayout.height(288);
		optionslayout.add(fpscheckbox.align(Align.LEFT).pad(10)).fill().colspan(2).expandX();
		optionslayout.row();
		optionslayout.add(showRomInfo.align(Align.LEFT).pad(10)).fill().colspan(2).expandX();
		optionslayout.row();
		optionslayout.add(selectSoundLabel).padLeft(10).fill().colspan(2);
		optionslayout.row();
		optionslayout.add(selectSound).pad(10).fill().colspan(2);
		optionslayout.row();
		optionslayout.add(selectResampler).padLeft(10).fill().colspan(2);
		optionslayout.row();
		optionslayout.add(resampler).pad(10).fill().colspan(2);
		optionslayout.row();
		optionslayout.add(applyoptions).fill().expandX();
		optionslayout.add(closeoptions).fill().expandX();
		optionsdialog.add(optionslayout);

		buttonOptions.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				optionsdialog.visible = true;
				window.visible = false;
			}
		});
		closeoptions.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				optionsdialog.visible = false;
				// window.visible=false;
			}
		});
		applyoptions.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				optionsdialog.visible = false;
				gb.setShowFps(fpscheckbox.isChecked());
				gb.setShowRomInfo(showRomInfo.isChecked());
				gb.runGameboy();
			}
		});
		// INFO BUtton--------------------------------------------------
		final Window infodialog = new Window("Rom Informations", skin.getStyle(WindowStyle.class), "rominfos");
		infodialog.width = 320;
		infodialog.height = 288;
		infodialog.x = Gdx.graphics.getWidth() / 2 - infodialog.width / 2;
		infodialog.y = Gdx.graphics.getHeight() / 2 - infodialog.height / 2;
		infodialog.visible = false;
		infodialog.setMovable(false);

		final TextButton closeinfo = new TextButton("Close", skin.getStyle(TextButtonStyle.class), "closeinfo");

		buttonInfo.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				infodialog.visible = true;
				window.visible = false;
				infodialog.clear();
				String info = gb.getRomInfo().trim();
				Button buttonMulti = new TextButton(info.equals("") ? "No Rom loaded..." : info, skin.getStyle(
						"toggle", TextButtonStyle.class), "multilineinfo");
				infodialog.add(buttonMulti);
				infodialog.row();
				infodialog.add(closeinfo.align(Align.BOTTOM)).fill().expandX();
			}
		});
		closeinfo.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				infodialog.visible = false;
				gb.runGameboy();
			}
		});
		// ABOUT BUTTON
		// ------------------------------------------------------------------------
		final Window aboutdialog = new Window("About", skin.getStyle(WindowStyle.class), "aboutdialog");
		aboutdialog.width = 320;
		aboutdialog.height = 288;
		aboutdialog.x = Gdx.graphics.getWidth() / 2 - aboutdialog.width / 2;
		aboutdialog.y = Gdx.graphics.getHeight() / 2 - aboutdialog.height / 2;
		aboutdialog.visible = false;
		aboutdialog.setMovable(false);
		final Button buttonMulti = new TextButton(
				"bluew, 2012 \n none \n Uses Libgdx \n http://code.google.com/p/libgdx/",
				skin.getStyle("toggle", TextButtonStyle.class), "button-ml-tgl");
		final TextButton closeabout = new TextButton("Close", skin.getStyle(TextButtonStyle.class), "closeinfo");
		aboutdialog.add(buttonMulti);
		aboutdialog.row();
		aboutdialog.add(closeabout.align(Align.BOTTOM)).fill().expandX();
		buttonAbout.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				aboutdialog.visible = true;
				window.visible = false;
			}
		});
		closeabout.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				aboutdialog.visible = false;
				gb.runGameboy();
			}
		});
		// -------------------------------------------------------------
		buttonReset.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				String oldpath = gb.currentRomPath();
				gb.reset();
				if (oldpath != null) {
					gb.loadRom(oldpath);
				}
				window.visible = false;
				gb.runGameboy();
			}
		});
		buttonExit.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				Gdx.app.exit();
			}
		});

		stage.addActor(window);
		stage.addActor(opendialog);
		stage.addActor(optionsdialog);
		stage.addActor(infodialog);
		stage.addActor(aboutdialog);
	}

	public void draw() {
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		//Table.drawDebug(stage);
	}

	public InputProcessor getInputProcessor1() {
		return stage;
	}

	public InputProcessor getInputProcessor2() {
		return this;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (Input.Buttons.RIGHT == button) {
			window.visible = true;
			window.x = Gdx.graphics.getWidth() / 2 - window.width / 2;
			window.y = Gdx.graphics.getHeight() / 2 - window.height / 2;
			gb.stopGameboy();
		} else if (Input.Buttons.LEFT == button) {
			if (window.visible) {
				gb.runGameboy();
			}
			window.visible = false;

		}
		return true;
	}
	private class GBFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".gb")||name.endsWith(".gbc");
		}
		
	}
	private class DirFilter implements FileFilter{
	    @Override
		public boolean accept(File file) {
	        return file.isDirectory();
	    }
	}

}
