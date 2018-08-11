package gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ytltoolbox.YtlToolbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class VoteModel {
	private static final int OPTION_SIZE_MAX = 8;
	@SuppressWarnings("MalformedFormatString")
	private static final String regex = String.format("^[1-%d]", OPTION_SIZE_MAX);

	private final IntegerProperty optionSize;
	private final Set<String> keys;
	private final IntegerProperty singleTotal;
	private final IntegerProperty multiTotal;
	private final ObservableList<VoteData> voteData;
	//	private final ObservableList<VoteData> voteData;
	private final BooleanProperty isLabelIndex;
	private final BooleanProperty isNumberCount;
	private final BooleanProperty isVoteOnce;


	public VoteModel() {
		optionSize = new SimpleIntegerProperty(4);
		keys = new HashSet<>();
		singleTotal = new SimpleIntegerProperty();
		multiTotal = new SimpleIntegerProperty();
		voteData = FXCollections.observableArrayList(Collections.nCopies(OPTION_SIZE_MAX, new VoteData()));
		isLabelIndex = new SimpleBooleanProperty();
		isNumberCount = new SimpleBooleanProperty();
		isVoteOnce = new SimpleBooleanProperty();

//		ObservableList<VoteData> trimmedVoteData = FXCollections.observableArrayList();

//		voteData.addListener((ListChangeListener<? super VoteData>) c -> {
//			while (c.next()) {
//				for (int i = c.getFrom(); i < c.getTo(); i++) {
//					if (i < trimmedVoteData.size()) {
//						trimmedVoteData.set(i, voteData.get(i));
//					}
//				}
//			}
//		});
//		optionSize.addListener((observable, oldValue, newValue) -> {
//			if (trimmedVoteData.size() < newValue.intValue()) {
//				trimmedVoteData.addAll(voteData.subList(trimmedVoteData.size(), newValue.intValue()));
//			} else if (trimmedVoteData.size() < newValue.intValue()) {
//				trimmedVoteData.remove(newValue.intValue() - 1, trimmedVoteData.size());
//			}
//		});
		/*==================================================================================================*
		 * for getter
		 *==================================================================================================*/
//		voteData = FXCollections.unmodifiableObservableList(trimmedVoteData);

		NumberBinding sTotal = null;
		NumberBinding mTotal = null;
		for (int idx = 0; idx < OPTION_SIZE_MAX; idx++) {
			final int i = idx;
			final VoteData data = voteData.get(i);
			/*==================================================================================================*
			 * total
			 *==================================================================================================*/
			if (i == 0) {
				sTotal = new SimpleIntegerProperty().add(data.singleCountProperty());
				mTotal = new SimpleIntegerProperty().add(data.multiCountProperty());
			} else {
				sTotal = sTotal.add(Bindings.when(optionSize.greaterThan(i))
						.then(data.singleCountProperty()).otherwise(0));
				mTotal = sTotal.add(Bindings.when(optionSize.greaterThan(i))
						.then(data.multiCountProperty()).otherwise(0));
			}
			/*==================================================================================================*
			 * ratio
			 *==================================================================================================*/
			data.bindSingleRatio(data.singleCountProperty().divide(1.0).divide(singleTotal));
			data.bindSingleRatio(data.multiCountProperty().divide(1.0).divide(multiTotal));

			/*==================================================================================================*
			 * label
			 *==================================================================================================*/
			StringBinding labelText = new StringBinding() {
				{
					super.bind(isLabelIndex, data.nameProperty(), isNumberCount, isVoteOnce, data.multiCountProperty());
				}

				@Override
				protected String computeValue() {
					StringBuilder sb = new StringBuilder();
					sb.append("[");
					sb.append(isLabelIndex.get() ? ("" + (i + 1)) : data.getName());
					sb.append("]\n");
					if (isNumberCount.get()) {
						sb.append(isVoteOnce.get() ? data.getSingleCount() : data.getMultiCount());
					} else {
						sb.append(String.format("%.0f", (isVoteOnce() ? data.getSingleRatio() : data.getMultiRatio()) * 100));
						sb.append("%");
					}
					return sb.toString();
				}
			};
			data.bindLabelText(labelText);
		}
		singleTotal.bind(sTotal);
		multiTotal.bind(mTotal);
	}

	public void addVote(String key, String message) {
		int idx = parseIndex(message);
		if (idx > 0) {
			if (keys.add(key)) {
				voteData.get(idx).setSingleCount(voteData.get(idx).getSingleCount() + 1);
			}
			voteData.get(idx).setMultiCount(voteData.get(idx).getMultiCount() + 1);
		}
	}

	private int parseIndex(String message) {
		if (YtlToolbox.IS_DEMO && !message.matches(regex)) {
			message = "" + (1 + message.charAt(0) % 8);
		}
		if (message.matches(regex)) {
			return Integer.parseInt(message) - 1;
		}
		return -1;
	}

	/*==================================================================================================*
	 * accessors
	 *==================================================================================================*/
//	public ObservableList<VoteData> getVoteData() {
//		return voteData;
//	}

	public int getOptionSize() {
		return optionSize.get();
	}

	public IntegerProperty optionSizeProperty() {
		return optionSize;
	}

	public void setOptionSize(int optionSize) {
		this.optionSize.set(optionSize);
	}

	public int getSingleTotal() {
		return singleTotal.get();
	}

	public IntegerProperty singleTotalProperty() {
		return singleTotal;
	}

	public int getMultiTotal() {
		return multiTotal.get();
	}

	public IntegerProperty multiTotalProperty() {
		return multiTotal;
	}

	public ObservableList<VoteData> getVoteData() {
		return voteData;
	}

	public boolean isLabelIndex() {
		return isLabelIndex.get();
	}

	public BooleanProperty isLabelIndexProperty() {
		return isLabelIndex;
	}

	public void setIsLabelIndex(boolean isLabelIndex) {
		this.isLabelIndex.set(isLabelIndex);
	}

	public boolean isNumberCount() {
		return isNumberCount.get();
	}

	public BooleanProperty isNumberCountProperty() {
		return isNumberCount;
	}

	public void setIsNumberCount(boolean isNumberCount) {
		this.isNumberCount.set(isNumberCount);
	}

	private boolean isVoteOnce() {
		return isVoteOnce.get();
	}

	public BooleanProperty isVoteOnceProperty() {
		return isVoteOnce;
	}

	public void setVoteOnce(boolean isVoteOnce) {
		this.isVoteOnce.set(isVoteOnce);
	}
}
