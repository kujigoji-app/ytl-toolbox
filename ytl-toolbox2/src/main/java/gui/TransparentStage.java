package gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import ytltoolbox.tuple.Tuple;

public class TransparentStage extends Stage {

	@Getter
	private HBox topLeftHBox;
	@Getter
	private HBox topRightHBox;
	private AnchorPane contentPane;
	@Getter
	private BooleanProperty showTopBarAlways = new SimpleBooleanProperty(false);
	@Getter
	private ColorPicker colorPicker;
	@Getter
	private Node content;

	/**
	 * 親ウィンドウを設定するには、initOwner(ownerStage)を呼び出します。
	 * モーダルウィンドウに設定するには、initModality(Modality.APPLICATION_MODAL)を呼び出します。
	 */
	TransparentStage() {
		super();
		this.setup();
	}

	public TransparentStage(StageStyle style) {
		super();
		this.setup();
	}

	private void setup() {

		VBox root = new VBox() {{
			setSpacing(5);
			setFillWidth(true);
			styleProperty().set("-fx-background-color: white");
		}};


		HBox topBarHBox = new HBox() {
			final Tuple<Double, Double> dragDelta = new Tuple<>();

			{
				setAlignment(Pos.CENTER_LEFT);
				setPadding(new Insets(5));
				setSpacing(5);
				setStyle("-fx-background-color:#f4f4f4");
				// allow the clock background to be used to drag the clock around.


				addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
					dragDelta.setVal1(TransparentStage.this.getX() - e.getScreenX());
					dragDelta.setVal2(TransparentStage.this.getY() - e.getScreenY());
				});
				addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
					TransparentStage.this.setX(e.getScreenX() + dragDelta.getVal1());
					TransparentStage.this.setY(e.getScreenY() + dragDelta.getVal2());
				});

				topLeftHBox = new HBox() {{
					setAlignment(Pos.CENTER_LEFT);
					setSpacing(5);
				}};

				topRightHBox = new HBox() {{
					setAlignment(Pos.CENTER_LEFT);
					setSpacing(5);


					colorPicker = new ColorPicker() {{
						valueProperty().addListener((observable, oldValue, newValue) ->
								root.styleProperty().set("-fx-background-color: #" + newValue.toString().substring(2)));
						setMaxWidth(50);
					}};

					Button closeButton = new Button("✕") {{
						setOnAction(e -> TransparentStage.this.close());
					}};
					getChildren().addAll(colorPicker, closeButton);
				}};

				getChildren().addAll(topLeftHBox, topRightHBox);
				HBox.setHgrow(topLeftHBox, Priority.ALWAYS);
				HBox.setHgrow(topRightHBox, Priority.NEVER);
			}
		};

		this.contentPane = new AnchorPane();

		root.getChildren().addAll(topBarHBox, this.contentPane);
		VBox.setVgrow(topBarHBox, Priority.NEVER);
		VBox.setVgrow(this.contentPane, Priority.ALWAYS);

		// シーン設定

		this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
			if (!showTopBarAlways.get()) {
				double y2 = e.getSceneY();
				if (y2 < 65) {
					topBarHBox.setVisible(true);
				} else {
					topBarHBox.setVisible(false);
				}
			}
		});
		this.setScene(new Scene(root));
		this.setWidth(700);
		this.setHeight(400);
		// 透過設定
		this.initStyle(StageStyle.TRANSPARENT);
		this.getScene().setFill(null);
		ResizeHelper.addResizeListener(this);
	}

	void setContent(Node content) {
		this.content = content;
		contentPane.getChildren().clear();
		contentPane.getChildren().add(content);

		AnchorPane.setTopAnchor(content, 0.0);
		AnchorPane.setRightAnchor(content, 0.0);
		AnchorPane.setBottomAnchor(content, 0.0);
		AnchorPane.setLeftAnchor(content, 0.0);
	}
}
