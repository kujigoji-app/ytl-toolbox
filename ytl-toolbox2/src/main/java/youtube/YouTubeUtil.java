/*
 * Copyright (c) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import ytltoolbox.Messages;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("ALL")
@Slf4j
public class YouTubeUtil {

	public static final String URL_PATTERN_LIVE = "^https://(www|gaming).youtube.com/channel/[0-9A-Za-z_-]{24}/live";
	public static final String URL_PATTERN_WATCH = "^https://(www|gaming).youtube.com/watch\\?(([^v].+)?=.+?&)*?v=[0-9A-Za-z_-]{11}.*";

	public static LiveBean getLive(YouTube youtube, String url) {
		LiveBean bean = new LiveBean();
		bean.setStreamURL(url);
		// エラー初期化
		bean.setErrorMessage(""); //$NON-NLS-1$
		if ("".equals(url)) { //$NON-NLS-1$
			String message = Messages.getString("YouTubeUtil.error.noUrl"); //$NON-NLS-1$
			log.error(message);
			bean.setErrorMessage(message);
			return bean;
		}
		if (youtube == null) {
			String message = Messages.getString("YouTubeUtil.error.auth"); //$NON-NLS-1$
			log.error(message);
			bean.setErrorMessage(message);
			return bean;
		}

		// 対応するURL形式。wwwはgamingに置換可能
		// https://www.youtube.com/watch?v=IOOp_kvXEnA
		// https://www.youtube.com/channel/UCspv01oxUFf_MTSipURRhkA/live
		// channelID -> videoID -> liveChatID の順で取得
		if (url.matches(URL_PATTERN_WATCH)) { //$NON-NLS-1$
			String[] split = url.split("[\\?&]"); //$NON-NLS-1$
			for (int i = 1; i < split.length; i++) {
				String[] par = split[i].split("="); //$NON-NLS-1$
				if ("v".equals(par[0])) { //$NON-NLS-1$
					bean.setVideoID(par[1]);
					break;
				}
			}
			if (bean.getVideoID() == null) {
				String message = Messages.getString("YouTubeUtil.error.invalidUrl"); //$NON-NLS-1$
				log.error(message);
				bean.setErrorMessage(message);
				return bean;
			}
			searchLiveChatByVideoID(youtube, bean);
			if (bean.hasError()) {
				return bean;
			}
			searchChannelByChannelID(youtube, bean);

		} else if (url.matches(URL_PATTERN_LIVE)) { //$NON-NLS-1$
			String[] split = url.split("channel/"); //$NON-NLS-1$
			bean.setChannelID(split[1].substring(0, 24));
			searchChannelByChannelID(youtube, bean);
			if (bean.hasError()) {
				return bean;
			}
			searchVideoByChannelID(youtube, bean);
			if (bean.hasError()) {
				return bean;
			}
			searchLiveChatByVideoID(youtube, bean);
		} else {
			String message = Messages.getString("YouTubeUtil.error.invalidUrl"); //$NON-NLS-1$
			log.error(message);
			bean.setErrorMessage(message);
			return bean;
		}
		return bean;
	}

	public static void searchLiveChatByVideoID(YouTube youtube, LiveBean bean) {

		try {
			YouTube.Videos.List videoList = youtube.videos()
					.list("snippet,liveStreamingDetails") //$NON-NLS-1$
					.setFields("items(snippet(title,thumbnails/medium/url,channelId)," //$NON-NLS-1$
							+ "liveStreamingDetails(activeLiveChatId,actualStartTime))") //$NON-NLS-1$
					.setId(bean.getVideoID());
			VideoListResponse response = videoList.execute();
			for (Video v : response.getItems()) {
				bean.setVideoTitle(v.getSnippet().getTitle());
				bean.setVideoThumbURL(v.getSnippet().getThumbnails().getMedium().getUrl());
				bean.setLiveChatID(v.getLiveStreamingDetails().getActiveLiveChatId());
				bean.setStartTime(v.getLiveStreamingDetails().getActualStartTime());
				bean.setChannelID(v.getSnippet().getChannelId());
			}
		} catch (IOException e) {
			String message = Messages.getString("YouTubeUtil.error.getChatId"); //$NON-NLS-1$
			bean.setErrorMessage(message);
		}

	}

	public static void searchVideoByChannelID(YouTube youtube, LiveBean bean) {
		try {
			String channelID = bean.getChannelID();

			String fields = "items(id/videoId,snippet/channelId)"; //$NON-NLS-1$
			YouTube.Search.List search = youtube.search().list("id,snippet") //$NON-NLS-1$
					.setChannelId(channelID)
					.setEventType("live") //$NON-NLS-1$
					.setType("video").setFields(fields); //$NON-NLS-1$
			SearchListResponse response = search.execute();
			for (SearchResult e : response.getItems()) {
				bean.setVideoID(e.getId().getVideoId());
				bean.setChannelID(e.getSnippet().getChannelId());
				break;
			}
		} catch (IOException e) {
			String message = Messages.getString("YouTubeUtil.error.getVideoId"); //$NON-NLS-1$
			log.error(message, e);
			bean.setErrorMessage(message);
		}
	}

	public static void searchChannelByChannelID(YouTube youtube, LiveBean bean) {
		try {
			String channelID = bean.getChannelID();

			String fields = "items(snippet(title,thumbnails/medium/url)," + //$NON-NLS-1$
					"brandingSettings/image/bannerImageUrl)"; //$NON-NLS-1$
			YouTube.Channels.List channels = youtube.channels().list("snippet,brandingSettings") //$NON-NLS-1$
					.setId(channelID).setFields(fields);
			ChannelListResponse response = channels.execute();
			for (Channel e : response.getItems()) {
				bean.setChannelTitle(e.getSnippet().getTitle());
				bean.setChannelThumbURL(e.getSnippet().getThumbnails().getMedium().getUrl());
				bean.setChannelBannerURL(e.getBrandingSettings().getImage().getBannerImageUrl());
				break;
			}
		} catch (IOException e) {
			String message = Messages.getString("YouTubeUtil.error.getChannel"); //$NON-NLS-1$
			log.error(message, e);
			bean.setErrorMessage(message);
		}
	}

	/**
	 * @return YouTube object
	 * @throws IOException IOException
	 */
	public static YouTube getYoutube() throws IOException {
		YouTube youtube;
		// This OAuth 2.0 access scope allows for read-only access to the
		// authenticated user's account, but not other types of account access.
		List<String> scopes = Lists.newArrayList(YouTubeScopes.YOUTUBE_READONLY);
		// Authorize the request.
		Credential credential = Auth.authorize(scopes, "listlivechatmessages"); //$NON-NLS-1$

		// This object is used to make YouTube Data API requests.
		youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
				.setApplicationName("ytl-toolbox-client").build(); //$NON-NLS-1$
		return youtube;
	}
}
