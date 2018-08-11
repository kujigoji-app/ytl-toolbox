package gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import lombok.extern.slf4j.Slf4j;
import ytltoolbox.Messages;

import java.io.IOException;

@Slf4j
public class GuiUtil {

	private GuiUtil() {
	}

	private static FXMLLoader fxmlLoader(String path) {
		return new FXMLLoader(GuiUtil.class.getResource(path), Messages.getResourceBundle());
	}

	public static void loadFXML(Node node, String path) {
		final FXMLLoader fxmlLoader = GuiUtil.fxmlLoader(path);
		fxmlLoader.setRoot(node);
		fxmlLoader.setController(node);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			log.error(path, e);
		}
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setTopAnchor(node, 0.0);
	}

	public static Image image(String path) {
		return new Image(GuiUtil.class.getResourceAsStream(path));
	}

	public static Blend createMenuItemBlend(ImageView imageView, Image image, Paint color) {
		ImageView clipView = new ImageView(image);
		clipView.setFitHeight(imageView.getFitHeight());
		clipView.setFitWidth(imageView.getFitWidth());
		imageView.setClip(clipView);
		return new Blend(BlendMode.ADD, null,
				new ColorInput(0, 0, imageView.getFitHeight(), imageView.getFitWidth(), color));
	}

}
