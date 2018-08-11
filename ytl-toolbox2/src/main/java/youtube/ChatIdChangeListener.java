package youtube;

@SuppressWarnings("ALL")
public interface ChatIdChangeListener {
	void handle(String oldValue, String newValue);
}
