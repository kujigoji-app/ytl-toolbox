package gui;

import config.Config;
import config.ConfigSerializable;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import ytltoolbox.Consts;
import ytltoolbox.Messages;

@Getter
public class MainPane extends AnchorPane implements ConfigSerializable {
	private final Stage owner;
	@FXML
	private VBox leftMenuBox;
	@FXML
	private AnchorPane contentPane;
	private SettingPane settingPane;
	private VotePane votePane;

	public MainPane(Stage owner) {
		this.owner = owner;
		ConfigSerializable.add(this);


		GuiUtil.loadFXML(this, Consts.PATH_FXML_MAIN);
	}

	@FXML
	public void initialize() {
		LeftMenuItem menuSetting = new LeftMenuItem(Consts.PATH_IMG_GEAR);
		menuSetting.getLabel().setText(Messages.getString("gui.setting"));
		LeftMenuItem menuVote = new LeftMenuItem(Consts.PATH_IMG_CHART);
		menuVote.getLabel().setText(Messages.getString("gui.vote"));

		leftMenuBox.getChildren().add(menuSetting);
		leftMenuBox.getChildren().add(menuVote);

		settingPane = new SettingPane();
		votePane = new VotePane(owner);

		menuSetting.addEventHandler(MouseEvent.MOUSE_CLICKED,
				onMenuItemMouseClicked(menuSetting, settingPane));
		menuVote.addEventHandler(MouseEvent.MOUSE_CLICKED,
				onMenuItemMouseClicked(menuVote, votePane));
	}

	private EventHandler<? super MouseEvent> onMenuItemMouseClicked(LeftMenuItem menuItem, Node pane) {
		return e -> {
			contentPane.getChildren().clear();
			contentPane.getChildren().add(pane);
			leftMenuBox.getChildren().stream().map(c -> (LeftMenuItem) c).forEach(mi -> mi.changeColor(false));
			menuItem.changeColor(true);
			//			menuSetting.setEffect(GuiUtil.createMenuItemBlend(menuSetting.getImageView(), Color.SKYBLUE));
		};
	}

	@Override
	public void loadConfig(Config config) {

	}

	@Override
	public void saveConfig(Config config) {

	}
}
