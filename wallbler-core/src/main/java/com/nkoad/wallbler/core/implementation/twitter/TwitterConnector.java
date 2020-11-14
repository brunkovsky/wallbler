package com.nkoad.wallbler.core.implementation.twitter;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.Connector;
import com.nkoad.wallbler.core.WallblerItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.util.*;

public class TwitterConnector extends Connector<TwitterValidator> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterConnector.class);
    private final static String TWITTER_PUBLIC_URL = "https://twitter.com/";
    private final static int MAX_ITEMS_TO_LOAD = 35; // if 'Apply filters' is ON then MAX could be 100
    private static Map<Boolean, FeedType> feedMap = new HashMap<>(); // TODO : enum instead of Boolean

    public TwitterConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
        validator = new TwitterValidator(accountProperties);
        validator.isAccountValid();
        feedMap.put(false, () -> validator.twitter.getUserTimeline(new Paging(1, MAX_ITEMS_TO_LOAD)));
        feedMap.put(true, () -> validator.twitter.search(buildQuery()).getTweets());
    }

    @Override
    public void loadData() {
        try {
            Date lastRefreshDate = new Date();
            FeedType feedType = feedMap.get(getFeedPropertyAsBoolean("config.applyFilters"));
            List<Status> tweets = feedType.buildTweets();
            Set<WallblerItem> wallblerItems = new HashSet<>();
            for (Status status : tweets) {
                String text = status.getText().replaceAll("#(\\w+)", "<a href='http://twitter.com/search?q=%23$1&src=hash' target='_blank'>#$1</a>");
                if (status.getURLEntities().length > 0) {
                    for (URLEntity entity : status.getURLEntities()) {
                        text = text.replace(entity.getURL(), "<a href='" + entity.getURL() + "' target='_blank'>" + entity.getDisplayURL() + "</a>");
                    }
                }
                if (status.getUserMentionEntities().length > 0) {
                    for (UserMentionEntity entity : status.getUserMentionEntities()) {
                        text = text.replace("@" + entity.getScreenName(), "<a href='http://twitter.com/" + entity.getScreenName() + "' target='_blank'>@" + entity.getScreenName() + "</a>");
                    }
                }
                TwitterWallblerItem item = new TwitterWallblerItem(feedProperties);
                item.setLastRefreshDate(lastRefreshDate);
                item.setUrl(TWITTER_PUBLIC_URL + validator.screenName);
                item.setLinkToSMPage(TWITTER_PUBLIC_URL + validator.screenName + "/status/" + status.getId());
                item.setDescription(text);
                item.setTitle("@" + status.getUser().getName());
                item.setDate(status.getCreatedAt());
                item.setSharedCount(status.getRetweetCount());
                item.setLikedCount(status.getFavoriteCount());
                if (status.isRetweet() && (status.getRetweetedStatus() != null)) {
                    item.setLikedCount(status.getRetweetedStatus().getFavoriteCount());
                }
                if ((status.getMediaEntities() != null) && (status.getMediaEntities().length > 0)) {
                    item.setThumbnailUrl(status.getMediaEntities()[0].getMediaURLHttps());
                }
                wallblerItems.add(item);
            }
            cache.add(wallblerItems);
        } catch (Exception e) {
            LOGGER.error("Can't get tweets, feed: " + feedProperties.get("service.pid"), e);
        }
    }

    private Query buildQuery() {
        Query query = new Query();
        query.setCount(MAX_ITEMS_TO_LOAD);
        StringBuilder stringBuilder = new StringBuilder();

        if (getFeedPropertyAsBoolean("config.safe")) {
            stringBuilder.append("-filter:safe "); //@todo  is sign "-" needed ?
        }
        if (getFeedPropertyAsBoolean("config.retweets")) {
            stringBuilder.append("-filter:retweets ");
        }

        //Hashtag
        if (isNotEmpty(getFeedPropertyAsString("config.hashtag"))) {
            StringTokenizer tk = new StringTokenizer(getFeedPropertyAsString("config.hashtag"));
            while (tk.hasMoreTokens()) {
                String token = tk.nextToken();
                stringBuilder.append("#").append(token).append(" ");
            }
            //stringBuilder = stringBuilder + "#" + feedConfig.getHashtag() + " ";
        }

        //mediaType
        if (isNotEmpty(getFeedPropertyAsString("config.mediaType"))) {
            stringBuilder.append("filter:").append(getFeedPropertyAsString("config.mediaType")).append(" ");
        }

        //date
        if (isNotEmpty(getFeedPropertyAsString("config.dateFrom"))) {
            stringBuilder.append("since:").append(getFeedPropertyAsString("config.dateFrom")).append(" ");
        }
        if (isNotEmpty(getFeedPropertyAsString("config.dateTo"))) {
            stringBuilder.append("until:").append(getFeedPropertyAsString("config.dateTo")).append(" ");
        }

        //account
        if (isNotEmpty(getFeedPropertyAsString("config.accountFrom"))) {
            stringBuilder.append("from:").append(getFeedPropertyAsString("config.accountFrom")).append(" ");
        }
        if (isNotEmpty(getFeedPropertyAsString("config.accountTo"))) {
            stringBuilder.append("to:").append(getFeedPropertyAsString("config.accountTo")).append(" ");
        }
        if (isNotEmpty(getFeedPropertyAsString("config.accountMention"))) {
            stringBuilder.append("@").append(getFeedPropertyAsString("config.accountMention")).append(" ");
        }

        if (isNotEmpty(getFeedPropertyAsString("config.query"))) {
            stringBuilder.append(getFeedPropertyAsString("config.query")).append(" ");
        }

        if (stringBuilder.length() == 0) { //if only 'ApplyFilters' checked
            stringBuilder.append(validator.screenName);
        }

        query.setQuery(stringBuilder.toString());
        return query;
    }

    private static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    interface FeedType {
        List<Status> buildTweets() throws TwitterException;
    }
}
