package camel.processor;


import org.json.JSONObject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


import java.util.List;

/***
 *Created by Hyunwook Shin
 */
public class ServerPostProcessor extends  PostProcessor{
    String placesToJson( List<Place> places ) {
        JSONObject info = new JSONObject();
        JSONObject placesInfo = new JSONObject();
        for ( int i = 0; i < places.size(); i++) {
            JSONObject placeInfo = new JSONObject();
            placeInfo.put("country", places.get(i).getCountry());
            placeInfo.put("name", places.get(i).getName());
            placeInfo.put("type", places.get(i).getPlaceType());
            placesInfo.put(String.valueOf(i), placeInfo);
        }
        info.put( "error", "" );
        info.put( "value", placesInfo );
        return info.toString();
    }

    String errorToJson(String error) {
        JSONObject info = new JSONObject();
        info.put( "error", error );
        info.put( "value", "" );
        return info.toString();
    }

    String stringToJson(String x) {
        JSONObject info = new JSONObject();
        info.put( "value", x );
        info.put( "error", "" );
        return info.toString();
    }
    String usersToJson(List<User> users) {
        JSONObject info = new JSONObject();
        JSONObject usersInfo = new JSONObject();
        for ( int i = 0; i < users.size(); i++) {
            JSONObject userinfo = new JSONObject();
            userinfo.put( "email", users.get(i).getEmail());
            userinfo.put( "id", users.get(i).getId());
            userinfo.put( "location", users.get(i).getLocation());
            userinfo.put( "profilePic", users.get(i).getBiggerProfileImageURL());
            usersInfo.put( users.get(i).getScreenName(), userinfo );
        }
        info.put( "error", "" );
        info.put( "value", usersInfo );
        return info.toString();
    }

    String statusToJson(Status status) {
        JSONObject info = new JSONObject();
        JSONObject tweetInfo = new JSONObject();
        tweetInfo.put( "id", status.getId() );
        tweetInfo.put( "text", status.getText() );
        tweetInfo.put( status.getUser().getScreenName(), tweetInfo );
        info.put( "error", "" );
        info.put( "value", tweetInfo );
        return info.toString();
    }

    String statusesToJson(List<Status> statuses) {
        JSONObject info = new JSONObject();
        JSONObject tweetsInfo = new JSONObject();
        for ( Status status : statuses) {
            JSONObject tweetInfo = new JSONObject();
            tweetInfo.put( "id", status.getId() );
            tweetInfo.put( "text", status.getText() );
            tweetsInfo.put( status.getUser().getScreenName(), tweetInfo );
        }
        info.put( "error", "" );
        info.put( "value", tweetsInfo );
        return info.toString();
    }

    Twitter twitterHandle( String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret ) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        builder.setOAuthAccessToken(accessToken);
        builder.setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory factory = new TwitterFactory(builder.build());
        return factory.getInstance();
    }

    @Override
    String handle() throws Exception {
        /* Takes json object and parses out
           [url], [method], and [data] portion
           and returns response to HTTP [method]
           request to [ur] with [data] as payload
         */
        String consumerKey = "lgjwlqVncQb8ZAy1EjdDRPIMJ";
        String consumerSecret = "lzV68NbXKe6wK3vqhjmJOGxm6koGlxOD0mkUbSF8bdU4W5jsi6";
        String accessToken;
        String accessTokenSecret;
        JSONObject info = new JSONObject(body);
        JSONObject data = info.getJSONObject("data");

        String action = info.getString("action");
        String response;
        String name; // username
        System.out.println( body );
        System.out.println( "action is " + action );
        System.out.println( "data is " + data.toString() );

        /* Get the credentials from "credentials" */
        try {
            JSONObject credentials = info.getJSONObject( "credentials" );
            accessToken = credentials.getString( "accessToken" );
            accessTokenSecret = credentials.getString( "accessTokenSecret");
        } catch (Exception e) {
            return errorToJson( "Invalid authentication format or invalid keys: " + e.toString() );
        }

        Twitter twitter = twitterHandle( consumerKey, consumerSecret, accessToken, accessTokenSecret );

        body = "";
        double SanJoseLatitude = 37.3382;
        double SanJoseLongitude = -121.8863;
        double latitude, longitude;
        try {
            switch (action) {
                case "nearby":
                    try {
                        latitude = data.getDouble("latitude");
                        longitude = data.getDouble("longitude");
                    } catch (Exception e){
                        latitude = SanJoseLatitude;
                        longitude = SanJoseLongitude;
                    }
                    GeoLocation location = new GeoLocation(latitude, longitude);
                    GeoQuery query = new GeoQuery(location);
                    response = placesToJson( twitter.searchPlaces( query ) );
                    break;
                case "tweet":
                    twitter.updateStatus( data.getString( "status" ));
                    response = stringToJson( "updated" );
                    break;
                case "retweet":
                    twitter.retweetStatus( data.getLong( "id"));
                    response = stringToJson("retweeted");
                    break;
                case "search":
                    Query tweetQuery = new Query( data.getString( "keyword" ) );
                    response = statusesToJson( twitter.search( tweetQuery ).getTweets());
                    break;
                case "favorites":
                    response = statusesToJson( twitter.getFavorites() );
                    break;
                case "timeline":
                    response = statusesToJson( twitter.getHomeTimeline() );
                    break;
                case "name":
                    response = stringToJson( twitter.getAccountSettings().getScreenName());
                    break;
                case "follow":
                    twitter.createFriendship(data.getString( "user" ));
                    response = stringToJson("now following");
                    break;
                case "unfollow":
                    twitter.destroyFriendship(data.getString( "user" ));
                    response = stringToJson("unfollowing");
                    break;
                case "friends":
                    name = twitter.getAccountSettings().getScreenName();
                    response = usersToJson(twitter.getFriendsList( name, -1 ));
                    break;
                case "followers":
                    name = twitter.getAccountSettings().getScreenName();
                    response = usersToJson(twitter.getFollowersList( name, -1 ));
                    break;
                default:
                    response = errorToJson("improper action");
            }
        } catch (Exception e) {
            response = errorToJson( e.toString() );
        }
        return response;
    }
}
