package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.Blend;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import ytltoolbox.Consts;

@Getter
public class LeftMenuItem extends VBox {
	private final String imagePath;
	@FXML
	private ImageView imageView;
	@FXML
	private Label label;
	private Blend blend;

	public LeftMenuItem(String imagePath) {
		this.imagePath = imagePath;
		GuiUtil.loadFXML(this, Consts.PATH_FXML_LEFTMENUITEM);
	}

	@FXML
	private void initialize() {
		Image image = GuiUtil.image(imagePath);
		this.imageView.setImage(image);
		this.blend = GuiUtil.createMenuItemBlend(this.imageView, image, Color.SKYBLUE);
	}

	public void changeColor(boolean change) {
		if (change) {
			this.imageView.setEffect(this.blend);
		} else {
			this.imageView.setEffect(null);
		}
	}
}
