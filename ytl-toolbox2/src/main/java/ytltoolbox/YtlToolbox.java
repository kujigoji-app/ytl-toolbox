package ytltoolbox;

import config.Config;
import config.ConfigSerializable;
import gui.MainPane;
import gui.delaysetup.DelaySetupManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class YtlToolbox extends Application {
	public static boolean IS_DEMO = false;
	private static Config config = null;
	public static ObservableList<Color> commonColors = FXCollections.observableArrayList();

	public YtlToolbox() {
	}

	@Override
	public void start(Stage stage) {
		loadConfigFile();
		Messages.init(config.getLanguage());

		MainPane mainPane = new MainPane(stage);

		/*==================================================================================================*
		 * scene
		 *==================================================================================================*/
		Scene scene = new Scene(mainPane);
		stage.setMinHeight(400);
		stage.setMinWidth(830);
		stage.setTitle(Consts.APP_TITLE + (IS_DEMO ? " - [demo]" : ""));
		stage.getIcons().add(new Image(this.getClass().getResourceAsStream(Consts.PATH_IMG_LOGO)));
		stage.setScene(scene);
		stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> DelaySetupManager.setupAll(stage));
		stage.showingProperty().addListener((obs, o, n) -> {
			try {
				if (!o && n) {
					ConfigSerializable.loadAll(config);
				}
				if (o && !n) {
					ConfigSerializable.saveAll(config);
					new Yaml().dump(config, new FileWriter(Consts.CONFIG_FILE_NAME));
				}
			} catch (IOException e) {
				log.error("config dump error", e);
			}
		});
		stage.show();

	}

	private void loadConfigFile() {
		if (new File(Consts.CONFIG_FILE_NAME).exists()) {
			try {
				Config loaded = new Yaml().loadAs(
						new InputStreamReader(new FileInputStream(Consts.CONFIG_FILE_NAME), StandardCharsets.UTF_8),
						Config.class);
				config = new Config();
				MyUtil.copy(loaded, config);
			} catch (FileNotFoundException | IllegalArgumentException | IntrospectionException |
					IllegalAccessException | InvocationTargetException e) {
				// can't reach
				e.printStackTrace();
			}
		} else {
			config = new Config();
		}
	}

	public static void main(String[] args) {
		IS_DEMO = args.length > 0 && args[0].equals(Consts.ARG_DEMO);

		launch(args);
	}

}
