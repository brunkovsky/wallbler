package com.nkoad.wallbler.core.implementation.twitter;

import com.nkoad.wallbler.core.Validator;
import twitter4j.Twitter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;

public class TwitterValidator extends Validator {
    private static final String TWITTER_DEFAULT_API = "https://api.twitter.com/1.1/";
    Twitter twitter;
    String screenName;

    public TwitterValidator(Map<String, Object> accountProperties) {
        super(accountProperties);
    }

    @Override
    public boolean isAccountValid() {
        try {
            Configuration twitterConfig = new ConfigurationBuilder()
                    .setOAuthConsumerKey((String) accountProperties.get("config.oAuthConsumerKey"))
                    .setOAuthConsumerSecret((String) accountProperties.get("config.oAuthConsumerSecret"))
                    .setOAuthAccessToken((String) accountProperties.get("config.oAuthAccessToken"))
                    .setOAuthAccessTokenSecret((String) accountProperties.get("config.oAuthAccessTokenSecret"))
                    .setRestBaseURL(TWITTER_DEFAULT_API)
                    .build();
            twitter = new twitter4j.TwitterFactory(twitterConfig).getInstance();
            screenName = twitter.getAccountSettings().getScreenName();
            LOGGER.info("twitter account is valid. name: " + accountProperties.get("config.name")
                    + ". gotten account name: " + screenName);
            return true;
        } catch (Exception e) {
            LOGGER.warn("twitter account is not valid. account name: " + accountProperties.get("config.name"));
            return false;
        }
    }

}
