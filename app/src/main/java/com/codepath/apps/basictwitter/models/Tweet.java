package com.codepath.apps.basictwitter.models;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by kemo on 9/27/14.
 */
public class Tweet implements Serializable {

    private String body;
    private long tId;
    private String createdAt;
    private User user;
    //private ArrayList<String> replies;

    public static Tweet fromJson(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.tId = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return tweet;
    }

    public static List<Tweet> readTweetsFromDB() {

        List<TweetModel> tweetModels = TweetModel.getAll();
        List<Tweet> tweets = new ArrayList<Tweet>();
        for (TweetModel tweetModel : tweetModels) {
            Tweet tweet = new Tweet();

            tweet.setUid(tweetModel.tId);
            tweet.setBody(tweetModel.body);
            tweet.setCreatedAt(tweetModel.createdAt);
            tweets.add(tweet);
        }
        return tweets;
    }

    public static TweetModel saveTweetfromJsonToDB(JSONObject jsonObject) {
        TweetModel tweetModel = new TweetModel(jsonObject);
        tweetModel.save();
        return tweetModel;
    }

    public static TweetModel saveTweetToDB(Tweet tweet) {
        TweetModel tweetModel = new TweetModel(tweet.gettId()
                ,tweet.getBody()
                ,tweet.getCreatedAt()
                ,tweet.getUser());
        UserModel userModel = new UserModel(tweet.getUser());
        userModel.save();
        tweetModel.save();
        return tweetModel;
    }

    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());
        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = Tweet.fromJson(tweetJson);
            if (tweet !=null) {
                tweets.add(tweet);
                // save to DB
                saveTweetToDB(tweet);
            }
        }
        return tweets;
    }

    public String getBody() {
        return body;
    }

    public long gettId() {
        return tId;
    }

    public String getCreatedAt() {
        return getRelativeTimeAgo(createdAt);
    }

    public User getUser() {
        return user;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUid(long uid) {
        this.tId = uid;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return getBody() + " - " + getUser().getScreenName();
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate){
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, String> replaceMappings = new HashMap<String, String>();
        replaceMappings.put(" hours ago", "h");
        replaceMappings.put(" hour ago", "h");
        replaceMappings.put(" min ago", "m");
        replaceMappings.put(" mins ago", "m");
        replaceMappings.put(" seconds ago", "s");
        replaceMappings.put(" second ago", "s");
        replaceMappings.put(" day ago", "d");
        replaceMappings.put(" days ago", "d");
        for (String suffixKey: replaceMappings.keySet()) {
            if (relativeDate.endsWith(suffixKey)) {
                relativeDate = relativeDate.replace(suffixKey, replaceMappings.get(suffixKey));
            }
        }

        return relativeDate;
    }
}
