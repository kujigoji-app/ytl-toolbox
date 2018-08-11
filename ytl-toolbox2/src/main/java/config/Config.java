package config;

import lombok.Data;

@Data
public class Config {
	private String lastVersionCheck = "1970/01/01";
	private String language = "ja";
	private Vote vote = new Vote();

	public Config() {
	}

}
