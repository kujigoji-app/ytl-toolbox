package youtube;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Strings;
import lombok.Data;

@Data
public class LiveBean {
	private String errorMessage = null;
	//	private YouTube youtube;
	private String streamURL;
	private String channelTitle;
	private String channelID;
	private String channelThumbURL;
	private String videoTitle;
	private String videoID;
	private String videoThumbURL;
	private String liveChatID;
	private DateTime startTime;
	private String channelBannerURL;

	public LiveBean() {
	}

	public boolean hasError() {
		return !Strings.isNullOrEmpty(this.getErrorMessage());
	}

}
