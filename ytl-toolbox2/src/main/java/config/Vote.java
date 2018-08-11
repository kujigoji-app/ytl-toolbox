package config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Data
public class Vote {
	private int optionSize = 4;
	private int labelText = 0;
	private int labelNumber = 1;
	private boolean showTotal = false;
	private boolean voteOnce = false;
	private List<String> optionTexts = new ArrayList<String>() {{
		IntStream.range(0, 8).forEach(i -> add(""));
	}};
	private String optionBackground = "white";

	public Vote() {
	}
}