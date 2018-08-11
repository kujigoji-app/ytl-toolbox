package gui;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

class VoteData {
	private final StringProperty name;
	private final IntegerProperty singleCount;
	private final IntegerProperty multiCount;
	private final DoubleProperty singleRatio;
	private final DoubleProperty multiRatio;
	private final StringProperty labelText;

	public VoteData() {
		name = new SimpleStringProperty();
		singleCount = new SimpleIntegerProperty();
		multiCount = new SimpleIntegerProperty();
		singleRatio = new SimpleDoubleProperty();
		multiRatio = new SimpleDoubleProperty();
		labelText = new SimpleStringProperty();
	}


	public String getName() {
		return name.get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public int getSingleCount() {
		return singleCount.get();
	}

	public IntegerProperty singleCountProperty() {
		return singleCount;
	}

	public void setSingleCount(int singleCount) {
		this.singleCount.set(singleCount);
	}

	public int getMultiCount() {
		return multiCount.get();
	}

	public IntegerProperty multiCountProperty() {
		return multiCount;
	}

	public void setMultiCount(int multiCount) {
		this.multiCount.set(multiCount);
	}

	public double getSingleRatio() {
		return singleRatio.get();
	}

	public ReadOnlyDoubleProperty singleRatioProperty() {
		return singleRatio;
	}

	void bindSingleRatio(DoubleBinding binding) {
		singleRatio.bind(binding);
	}

	public double getMultiRatio() {
		return multiRatio.get();
	}

	public ReadOnlyDoubleProperty multiRatioProperty() {
		return multiRatio;
	}

	void bindMultiRatio(DoubleBinding binding) {
		multiRatio.bind(binding);
	}

	public String getLabelText() {
		return labelText.get();
	}

	public ReadOnlyStringProperty labelTextProperty() {
		return labelText;
	}

	void bindLabelText(ObservableValue<String> binding) {
		labelText.bind(binding);
	}

}
