package config;

import java.util.ArrayList;
import java.util.List;

public interface ConfigSerializable {
	List<ConfigSerializable> serializables = new ArrayList<>();

	void loadConfig(Config config);

	void saveConfig(Config config);

	static void add(ConfigSerializable c) {
		serializables.add(c);
	}

	static void remove(ConfigSerializable c) {
		serializables.remove(c);
	}

	static void loadAll(Config config) {
		if (config != null) {
			serializables.forEach(s -> s.loadConfig(config));
		}
	}

	static void saveAll(Config config) {
		if (config != null) {
			serializables.forEach(s -> s.saveConfig(config));
		}
	}
}
