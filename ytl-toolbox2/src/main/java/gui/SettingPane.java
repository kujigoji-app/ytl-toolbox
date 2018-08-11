package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import ytltoolbox.Consts;

@Getter
public class SettingPane extends AnchorPane {

	@FXML
	private TextField liveUrlField;

	public SettingPane() {
		GuiUtil.loadFXML(this, Consts.PATH_FXML_SETTING);

	}
}
