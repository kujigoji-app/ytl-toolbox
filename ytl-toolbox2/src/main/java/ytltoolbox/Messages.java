package ytltoolbox;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String RESOURCE_DIR = "/message";
	private static final String BUNDLE_NAME = "message.messages"; //$NON-NLS-1$
	private static ResourceBundle RESOURCE_BUNDLE;
	private static String LANGUAGE;

	private Messages() {
	}

	public static void init(String language) {
		//
		//		URLClassLoader urlLoader = new URLClassLoader(
		//				new URL[] { new File(RESOURCE_DIR).toURI().toURL() });
		LANGUAGE = language;
		try {
			if (language != null && !language.isEmpty()) {
				RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(LANGUAGE));
			} else {
				RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
			}
		} catch (MissingResourceException e) {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
		}
	}

	public static String getString(String key) {
		return getString(key, (String[]) null);
	}

	public static String getString(String key, String... replace) {
		try {
			String string = RESOURCE_BUNDLE.getString(key);
			if (replace != null) {
				for (int i = 0; i < replace.length; i++) {
					string = string.replaceAll("\\{" + i + "\\}", replace[i]);
				}
			}
			return string;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	public static String getLanguage() {
		return LANGUAGE;
	}
}
