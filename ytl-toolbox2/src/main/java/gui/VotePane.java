package gui;

import config.Config;
import config.ConfigSerializable;
import config.Vote;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.ToggleSwitch;
import ytltoolbox.Consts;
import ytltoolbox.Messages;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
class VotePane extends AnchorPane implements ConfigSerializable {
	private final Stage owner;
	private final VoteModel model;

	public enum State {
		IDLE, RUNNING
	}

	/*==================================================================================================*
	 * Nodes
	 *==================================================================================================*/
	@FXML
	private ImageView voteOptions;
	@FXML
	private ImageView votePieChart;
	@FXML
	private ImageView voteBarChart;
	@FXML
	private ImageView votePanel;
	@FXML
	private TextField secondsField;
	@FXML
	private Button seButton;
	@FXML
	private ProgressBar secondsProgressBar;
	@FXML
	private Label secondsLabel;
	@FXML
	private FlowPane voteSettingPane;
	@FXML
	private SegmentedButton optionSizeSegment;
	@FXML
	private SegmentedButton labelTextSegment;
	@FXML
	private SegmentedButton labelNumberSegment;
	@FXML
	private ToggleSwitch showTotalSwitch;
	@FXML
	private ToggleSwitch voteOnceSwitch;

	/*==================================================================================================*
	 * Properties
	 *==================================================================================================*/
	private final DoubleProperty elapseProperty;
	private final IntegerProperty secondsProperty;
	private final IntegerProperty optionSizeProperty;
	private final IntegerProperty labelTextProperty;
	private final IntegerProperty labelNumberProperty;
	private final BooleanProperty showTotalProperty;
	private final BooleanProperty voteOnceProperty;
	private final ObjectProperty<State> stateProperty;

	/*==================================================================================================*
	 * Toggle index
	 *==================================================================================================*/
	public static final int LABEL_TEXT_INDEX = 0;
	public static final int LABEL_TEXT_TEXT = 1;
	public static final int LABEL_NUMBER_COUNT = 0;
	public static final int LABEL_NUMBER_RATIO = 1;

	/*==================================================================================================*
	 * Other fields
	 *==================================================================================================*/
	public static final int SEC_DEFAULT = 30;
	public static final int SEC_MIN = 30;
	public static final int SEC_MAX = 300;

	VotePane(Stage owner) {
		this.owner = owner;
		ConfigSerializable.add(this);


		elapseProperty = new SimpleDoubleProperty(0);
		secondsProperty = new SimpleIntegerProperty(SEC_DEFAULT);
		optionSizeProperty = new SimpleIntegerProperty();
		labelTextProperty = new SimpleIntegerProperty(LABEL_TEXT_INDEX);
		labelNumberProperty = new SimpleIntegerProperty(LABEL_NUMBER_RATIO);
		showTotalProperty = new SimpleBooleanProperty();//showTotalSwitch.selectedProperty();
		voteOnceProperty = new SimpleBooleanProperty();//voteOnceSwitch.selectedProperty();
		stateProperty = new SimpleObjectProperty<>(State.IDLE);
		model = new VoteModel();

		/*==================================================================================================*
		 * load and initialize
		 *==================================================================================================*/
		GuiUtil.loadFXML(this, Consts.PATH_FXML_VOTE);
	}

	@FXML
	private void initialize() {
		System.out.println("init");
		/*==================================================================================================*
		 * top images
		 *==================================================================================================*/
		VoteOptionsStage voteOptionsStage = new VoteOptionsStage(this);
		voteOptionsStage.initOwner(owner);
		voteOptionsStage.getShowTopBarAlways().set(true);
		voteOptions.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> voteOptionsStage.show());
		PieChartStage pieChartStage = new PieChartStage(model);
		pieChartStage.initOwner(owner);
		pieChartStage.getShowTopBarAlways().set(true);
		this.votePieChart.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> pieChartStage.show());

		/*==================================================================================================*
		 * 実行
		 *==================================================================================================*/
		this.secondsField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty()) {
				this.secondsProperty.set(SEC_DEFAULT);
			} else if (!newValue.matches("\\d+")) {
				this.secondsProperty.set(1);
			} else {
				this.secondsProperty.set(Integer.parseInt(newValue));
			}
		});
		this.secondsField.disableProperty().bind(this.stateProperty.isNotEqualTo(State.IDLE));
		this.secondsProgressBar.progressProperty().bind(this.elapseProperty.divide(this.secondsProperty));
		this.secondsLabel.textProperty().bind(this.secondsProperty.subtract(this.elapseProperty).asString("%.1fs"));

		Timeline timeLine = new Timeline();

		this.seButton.textProperty().bind(Bindings.when(this.stateProperty.isEqualTo(State.IDLE))
				.then(Messages.getString("gui.vote.se.start"))
				.otherwise(Messages.getString("gui.vote.se.stop")));
		this.seButton.addEventHandler(ActionEvent.ACTION, e -> {
			switch (this.stateProperty.get()) {
				case IDLE:
					int sec = this.secondsProperty.get();
					if (sec < SEC_MIN || SEC_MAX < sec) {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText(Messages.getString("gui.vote.se.secondsNg"));
						alert.show();
						break;
					}
					this.elapseProperty.set(0);
					timeLine.getKeyFrames().clear();
					timeLine.getKeyFrames()
							.add(new KeyFrame(Duration.seconds(sec), new KeyValue(this.elapseProperty, sec)));
					timeLine.setCycleCount(1);
					timeLine.play();
					this.stateProperty.setValue(State.RUNNING);
					break;
				case RUNNING:
					timeLine.stop();
					this.elapseProperty.set(0);
					this.stateProperty.setValue(State.IDLE);
					break;
			}
		});
		/*==================================================================================================*
		 * voteSettingPane
		 *==================================================================================================*/
		this.voteSettingPane.disableProperty().bind(this.stateProperty.isNotEqualTo(State.IDLE));
		/*==================================================================================================*
		 * voteSettingPane>optionSizeSegment
		 *==================================================================================================*/
		IntStream.range(2, 9).mapToObj(i -> new ToggleButton("" + i))
				.forEach(tb -> this.optionSizeSegment.getButtons().add(tb));
		this.optionSizeSegment.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
		this.optionSizeSegment.getToggleGroup().selectedToggleProperty().addListener((obs, o, n) -> {
			if (n != null) {
				System.out.println(((ToggleButton) n).getText());
			} else if (o != null) {
				o.setSelected(true);
			}
		});

		this.optionSizeSegment.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue,
																					  n) -> this.optionSizeProperty.set(n == null ? 4 : Integer.parseInt(((ToggleButton) n).getText())));
		this.optionSizeProperty.addListener((observable, oldValue, newValue) -> this.optionSizeSegment.getButtons()
				.get(newValue.intValue() - 2).setSelected(true));

		/*==================================================================================================*
		 * voteSettingPane>labelTextSegment
		 *==================================================================================================*/
		ToggleButton labelTextIndex = new ToggleButton(Messages.getString("gui.vote.labelText.index"));
		ToggleButton labelTextText = new ToggleButton(Messages.getString("gui.vote.labelText.text"));
		this.labelTextSegment.getButtons().addAll(labelTextIndex, labelTextText);
		this.labelTextSegment.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
		this.labelTextSegment.getToggleGroup().selectedToggleProperty().addListener((obs, o, n) -> {
			if (n != null) {
				System.out.println(((ToggleButton) n).getText());
			} else if (o != null) {
				o.setSelected(true);
			}
		});
		this.labelTextSegment.getToggleGroup().selectedToggleProperty()
				.addListener((observable, oldValue, n) -> this.labelTextProperty
						.set(this.labelTextSegment.getButtons().indexOf(n == null ? LABEL_TEXT_INDEX : n)));
		this.labelTextProperty.addListener((observable, oldValue, newValue) -> this.labelTextSegment.getButtons()
				.get(newValue.intValue()).setSelected(true));

		/*==================================================================================================*
		 * voteSettingPane>labelNumberSegment
		 *==================================================================================================*/
		ToggleButton labelNumberCount = new ToggleButton(Messages.getString("gui.vote.labelNumber.count"));
		ToggleButton labelNumberRatio = new ToggleButton(Messages.getString("gui.vote.labelNumber.ratio"));
		this.labelNumberSegment.getButtons().addAll(labelNumberCount, labelNumberRatio);
		this.labelNumberSegment.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
		this.labelNumberSegment.getToggleGroup().selectedToggleProperty().addListener((obs, o, n) -> {
			if (n != null) {
				System.out.println(((ToggleButton) n).getText());
			} else if (o != null) {
				o.setSelected(true);
			}
		});
		this.labelNumberSegment.getToggleGroup().selectedToggleProperty()
				.addListener((observable, oldValue, n) -> this.labelNumberProperty
						.set(this.labelNumberSegment.getButtons().indexOf(n == null ? LABEL_NUMBER_RATIO : n)));
		this.labelNumberProperty.addListener((observable, oldValue, newValue) -> this.labelNumberSegment.getButtons()
				.get(newValue.intValue()).setSelected(true));

		getShowTotalSwitch().selectedProperty().bindBidirectional(showTotalProperty);
		getVoteOnceSwitch().selectedProperty().bindBidirectional(voteOnceProperty);
	}

	@Override
	public void loadConfig(Config config) {
		Vote vote = config.getVote();
		this.labelNumberSegment.getButtons().get(vote.getLabelNumber()).setSelected(true);
		//		labelNumberProperty.set(vote.getLabelNumber());
		this.labelTextSegment.getButtons().get(vote.getLabelText()).setSelected(true);
		//		labelTextProperty.set(vote.getLabelText());
		this.optionSizeSegment.getButtons().get(vote.getOptionSize() - 2).setSelected(true);
		//		optionSizeProperty.set(vote.getOptionSize());
		this.showTotalProperty.set(vote.isShowTotal());
		this.voteOnceProperty.set(vote.isVoteOnce());
		List<String> texts = vote.getOptionTexts();
		if (texts != null) {
			for (int i = 0; i < texts.size(); i++) {
				model.getVoteData().get(i).nameProperty().set(texts.get(i));
			}
		}
	}

	@Override
	public void saveConfig(Config config) {
		Vote vote = config.getVote();
		vote.setLabelNumber(this.labelNumberProperty.get());
		vote.setLabelText(this.labelTextProperty.get());
		vote.setOptionSize(this.optionSizeProperty.get());
		vote.setShowTotal(this.showTotalProperty.get());
		vote.setVoteOnce(this.voteOnceProperty.get());
		config.getVote().setOptionTexts(model.getVoteData().stream().map(d -> d.nameProperty().get()).collect(Collectors.toList()));
	}

	VoteModel getModel() {
		return model;
	}
}
