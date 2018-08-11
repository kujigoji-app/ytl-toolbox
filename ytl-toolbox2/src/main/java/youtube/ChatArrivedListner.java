package youtube;

import com.google.api.services.youtube.model.LiveChatMessage;

@SuppressWarnings("ALL")
public interface ChatArrivedListner {
	void handle(LiveChatMessage message);
}
