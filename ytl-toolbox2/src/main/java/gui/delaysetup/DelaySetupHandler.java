package gui.delaysetup;

import javafx.stage.Stage;

@FunctionalInterface
interface DelaySetupHandler {
	void setup(Stage stage);
}