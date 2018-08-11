package youtube;

import com.google.api.client.util.Strings;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageListResponse;
import javafx.animation.AnimationTimer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import static youtube.ChatPollerState.*;

@SuppressWarnings("ALL")
@Slf4j()
public class ChatPoller extends AnimationTimer {

	private final YouTube youtube;
	private String liveChatId;

	/**
	 * Common fields to retrieve for chat messages
	 */
	private static final String LIVE_CHAT_FIELDS = "items(authorDetails(channelId,displayName,channelUrl,isChatModerator,isChatOwner,isChatSponsor,"
			+ "profileImageUrl),snippet(displayMessage,superChatDetails,publishedAt)),"
			+ "nextPageToken,pollingIntervalMillis";
	private final String nextPageToken;
	private long delayMs;
	private boolean isDemo;
	private final Collection<ChatArrivedListner> arrivedListners = new CopyOnWriteArraySet<>();
	private final Collection<ChatShutdownListener> shutdownListners = new CopyOnWriteArraySet<>();
	private final Collection<ChatIdChangeListener> chatIdChangeListeners = new CopyOnWriteArraySet<>();
	private long last = 0;

	private ChatPollerState state = IDLE;

	public ChatPoller(YouTube youtube) {
		this(youtube, null);
	}

	public ChatPoller(YouTube youtube, final String liveChatId) {
		this.youtube = youtube;
		this.liveChatId = liveChatId;
		this.state = IDLE;
		this.nextPageToken = null;
		this.delayMs = 1000;

	}

	//	public void reset() {
	//		this.state = IDLE;
	//		this.nextPageToken = null;
	//		this.delayMs = 0;
	//	}

	@Override
	public void handle(long now) {
		try {
			if (this.last == 0) {
				this.state = RUNNING;
				this.last = now;
			}
			if (now - this.last > this.delayMs * 1000000) {
				if (Strings.isNullOrEmpty(this.liveChatId)) {
					this.delayMs = 100;
					return; // continue
				}
				LiveChatMessageListResponse response = this.youtube
						.liveChatMessages()
						.list(this.liveChatId, "snippet, authorDetails")
						.setPageToken(this.nextPageToken)
						.setFields(LIVE_CHAT_FIELDS)
						.execute();
				if (response == null || response.getItems() == null) {
					this.delayMs = 100;
					return; // continue
				}
				List<LiveChatMessage> items = response.getItems();
				for (LiveChatMessage lcm : items) {
					for (ChatArrivedListner cal : this.arrivedListners) {
						cal.handle(lcm);
					}
				}
				if (this.state == ChatPollerState.SHUTDOWN) {
					for (ChatShutdownListener csl : this.shutdownListners) {
						csl.handle();
					}
					this.state = ChatPollerState.STOPPED;
					this.stop();
				}
				this.last = now;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		if (this.state != ChatPollerState.STOPPED) {
			this.state = SHUTDOWN;
			log.info("shutting down.");
		}
	}

	public boolean isStopped() {
		return this.state == STOPPED;
	}

	public boolean isDemo() {
		return this.isDemo;
	}

	public void setDemo(boolean isDemo) {
		this.isDemo = isDemo;
	}

	public boolean addArrivedListner(ChatArrivedListner e) {
		return this.arrivedListners.add(e);
	}

	public boolean removeArrivedListner(Object o) {
		return this.arrivedListners.remove(o);
	}

	public void clearArrivedListner() {
		this.arrivedListners.clear();
	}

	public boolean addChatShutdownListener(ChatShutdownListener e) {
		return this.shutdownListners.add(e);
	}

	public boolean removeChatShutdownListener(Object o) {
		return this.shutdownListners.remove(o);
	}

	public void clearChatShutdownListener() {
		this.shutdownListners.clear();
	}

	public boolean addChatIdChangeListener(ChatIdChangeListener e) {
		return this.chatIdChangeListeners.add(e);
	}

	public boolean removeChatIdChangeListener(Object o) {
		return this.chatIdChangeListeners.remove(o);
	}

	public void clearChatIdChangeListener() {
		this.chatIdChangeListeners.clear();
	}

	public void setLiveChatId(String liveChatId) {
		if (String.valueOf(liveChatId).equals(this.liveChatId)) {
			for (ChatIdChangeListener cicl : this.chatIdChangeListeners) {
				cicl.handle(this.liveChatId, liveChatId);
			}
		}
		this.liveChatId = liveChatId;
	}

}
