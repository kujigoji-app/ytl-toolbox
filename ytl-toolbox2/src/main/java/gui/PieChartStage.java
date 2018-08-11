package gui;

import config.Config;
import config.ConfigSerializable;
import gui.tooltip.TooltipBehavior;
import javafx.beans.binding.Bindings;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class PieChartStage extends TransparentStage implements ConfigSerializable {

	public PieChartStage(VoteModel model) {

		final TooltipBehavior behavior = new TooltipBehavior() {{
			//マウスが乗ってから0.1秒後に表示
			setOpenDuration(new Duration(100));
			//ずっと表示
			setHideDuration(Duration.INDEFINITE);
			//マウスが放れてから0.3秒後に非表示
			setLeftDuration(new Duration(100));
		}};


		PieChart pieChart = new PieChart() {{
			setLegendVisible(false);
			setStartAngle(90);

			model.getVoteData().forEach(vd -> {
				Data d = new Data("", 0);
				d.nameProperty().bind(vd.nameProperty());
				d.pieValueProperty().bind(Bindings.when(model.isVoteOnceProperty())
						.then(vd.singleCountProperty()).otherwise(vd.multiCountProperty()));
				this.getData().add(d);
				// pie tooltip
				Tooltip tooltip = new Tooltip("") {{
					textProperty().bind(d.nameProperty());
					behavior.install(d.getNode(), this);
				}};
			});

		}};

		Circle circle = new Circle(50);
		StackPane stackPane = new StackPane() {{
			getChildren().addAll(pieChart, circle);
		}};
		setContent(stackPane);

	}

	@Override
	public void loadConfig(Config config) {

	}

	@Override
	public void saveConfig(Config config) {

	}
}
