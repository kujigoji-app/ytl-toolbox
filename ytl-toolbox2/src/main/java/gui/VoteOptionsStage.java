package gui;

import config.Config;
import config.ConfigSerializable;
import config.Vote;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import ytltoolbox.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Getter
public class VoteOptionsStage extends TransparentStage implements ConfigSerializable {

	private static final List<String> cssColors = Arrays.asList(
			"-fx-background-color: rgb(255.0, 209.0, 209.0);",
			"-fx-background-color: rgb(255.0, 255.0, 153.0);",
			"-fx-background-color: rgb(203.0, 242.0, 102.0);",
			"-fx-background-color: rgb(180.0, 235.0, 250.0);",
			"-fx-background-color: rgb(237.0, 197.0, 143.0);",
			"-fx-background-color: rgb(135.0, 231.0, 176.0);",
			"-fx-background-color: rgb(199.0, 178.0, 222.0);",
			"-fx-background-color: rgb(200.0, 200.0, 203.0);");
	private static final String cssBackRadius = "-fx-background-radius: 6px;";

	private final VotePane votePane;

	public VoteOptionsStage(VotePane votePane) {
		super();
		ConfigSerializable.add(this);

		/*==================================================================================================*
		 * field
		 *==================================================================================================*/
		this.votePane = votePane;

		/*==================================================================================================*
		 * scene
		 *==================================================================================================*/
		super.setHeight(600);
		super.setWidth(200);

		VBox vbox = new VBox() {{
			this.setSpacing(5.0);
			this.setPadding(new Insets(10.0));
			this.setFillWidth(true);
		}};
		super.setContent(vbox);

		IntStream.range(0, 8).forEach(i -> {
			HBox hbox = new HBox() {{
				this.setStyle(cssColors.get(i) + cssBackRadius);
				this.setMinHeight(Region.USE_COMPUTED_SIZE);
				this.setMinWidth(Region.USE_COMPUTED_SIZE);
				this.setPrefHeight(Region.USE_COMPUTED_SIZE);
				this.setPrefWidth(Region.USE_COMPUTED_SIZE);
				this.setMaxHeight(Region.USE_COMPUTED_SIZE);
				this.setMaxWidth(Region.USE_COMPUTED_SIZE);
				VBox.setVgrow(this, Priority.NEVER);
				this.setPadding(new Insets(5.0));
				Label label = new Label("label") {{
					setWrapText(true);
					textProperty().bind(Bindings.concat("" + (i + 1) + ". ",
							votePane.getModel().getVoteData().get(i).nameProperty()));
				}};

				this.getChildren().add(label);

				this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					TextInputDialog iptDlg = new TextInputDialog();
					iptDlg.setTitle(Messages.getString("gui.voteOptions.dialog.title", "" + (i + 1)));
					iptDlg.setHeaderText(null);
					iptDlg.setContentText(Messages.getString("gui.voteOptions.dialog.text"));
					Optional<String> result = iptDlg.showAndWait();
					result.ifPresent(value -> { //値があった場合
						votePane.getModel().getVoteData().get(i).nameProperty().set(result.get());
					});
				});
				visibleProperty().bind(votePane.getOptionSizeProperty().greaterThan(i));
			}};
			vbox.getChildren().add(hbox);
		});
	}

	@Override
	public void loadConfig(Config config) {

		String optionBackground = config.getVote().getOptionBackground();
		if (optionBackground == null) {
			optionBackground = (new Vote()).getOptionBackground();
		}
		getColorPicker().setValue(Color.valueOf(optionBackground));
	}

	@Override
	public void saveConfig(Config config) {
		config.getVote().setOptionBackground("#" + getColorPicker().getValue().toString().substring(2));
	}
}
