package youtube;

import com.google.api.services.youtube.model.LiveChatMessage;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ALL")
public abstract class VoteManager {

	private final ChatPoller poller;
	final List<LiveChatMessage> messages;

	@Accessors
	private long pollerStart;
	@Accessors
	private long pollerEnd;

	public VoteManager(ChatPoller poller) {
		this.pollerStart = Long.MAX_VALUE;
		this.pollerEnd = 0;
		this.poller = poller;
		this.messages = Collections.synchronizedList(new ArrayList<>());
		poller.addArrivedListner(m -> {
			long at = m.getSnippet().getPublishedAt().getValue();
			if (this.pollerStart < at && at < this.pollerEnd) {
				this.messages.add(m);
			} else if (this.pollerStart < at && this.pollerEnd < at) {
				this.pollerStart = Long.MAX_VALUE;
				this.pollerEnd = 0;
				this.onVoteClosed(this.messages);
			}
		});
		poller.addChatIdChangeListener((o, n) -> {
			this.messages.clear();
		});
	}

	public void open(long second) {
		this.messages.clear();
		this.pollerStart = System.currentTimeMillis();
		this.pollerEnd = this.pollerStart + second * 1000;
	}

	public void close() {
		this.pollerEnd = System.currentTimeMillis();
	}

	public void setLiveChatId(String liveChatId) {
		this.poller.setLiveChatId(liveChatId);
	}

	public boolean addArrivedListner(ChatArrivedListner e) {
		return this.poller.addArrivedListner(e);
	}

	public boolean addChatShutdownListener(ChatShutdownListener e) {
		return this.poller.addChatShutdownListener(e);
	}

	public abstract void onVoteClosed(List<LiveChatMessage> messages);

	public void shutdown() {
		this.poller.shutdown();
	}
}
