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
    String idsToJson(IDs ids) {
        JSONObject info = new JSONObject();
        JSONObject tweetInfo = new JSONObject();
        long[] idList = ids.getIDs();
        for ( int i = 0; i < idList.length; i++) {
            tweetInfo.put(String.valueOf(i), idList[i] );
        }
        info.put( "error", "" );
        info.put( "value", tweetInfo );
        return info.toString();
    }

    String statusesToJson(List<Status> statuses) {
        JSONObject info = new JSONObject();
        JSONObject tweetInfo = new JSONObject();
        for ( Status status : statuses) {
            tweetInfo.put( status.getUser().getScreenName(), status.getText() );
        }
        info.put( "error", "" );
        info.put( "value", tweetInfo );
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
        String accessToken = "904211533822955520-1bUicMh60bxTwu0NyOZHPHrNo5tRRDz";
        String accessTokenSecret = "QKeebkymJpKXCIthgFNgy6LOKkbz3Y7mJsXE0bGQhWYuD";
        Twitter twitter = twitterHandle( consumerKey, consumerSecret, accessToken, accessTokenSecret );
        System.out.println( "Body is '" + body + "'");
        JSONObject info = new JSONObject(body);
        JSONObject data = info.getJSONObject("data");
        String action = info.getString("action");
        String response;
        System.out.println( "action is " + action );
        System.out.println( "data is " + data.toString() );
        body = "";
        double SanJoseLatitude = 37.3382;
        double SanJoseLongitude = -121.8863;
        double latitude, longitude;
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
            case "favorites":
                response = statusesToJson( twitter.getFavorites() );
                break;
            case "name":
                response = stringToJson( twitter.getAccountSettings().getScreenName());
                break;
            case "friends":
                try {
                    String name = twitter.getAccountSettings().getScreenName();
                    response = idsToJson(twitter.getFriendsIDs( name, -1 ));
                } catch (Exception e) {
                    response = errorToJson( e.toString() );
                }
                break;
            default:
                response = errorToJson("improper action");
        }
        return response;
    }
}
