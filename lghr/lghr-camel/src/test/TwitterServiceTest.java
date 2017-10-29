import camel.TwitterService;
import camel.processor.JsonHelper;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import twitter4j.Status;
import twitter4j.User;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

/***
 * Created by Lin Cheng on 2017/9/25.
 */
public class TwitterServiceTest {

    String consumerKey = "lgjwlqVncQb8ZAy1EjdDRPIMJ";
    String consumerSecret = "lzV68NbXKe6wK3vqhjmJOGxm6koGlxOD0mkUbSF8bdU4W5jsi6";
    String accessToken = "904211533822955520-1bUicMh60bxTwu0NyOZHPHrNo5tRRDz";
    String accessTokenSecret = "QKeebkymJpKXCIthgFNgy6LOKkbz3Y7mJsXE0bGQhWYuD";
    TwitterService service = TwitterService.instance(
            consumerKey, consumerSecret, accessToken, accessTokenSecret );
    @Test
    public void jsonTest() throws Exception {
        JsonHelper helper = new JsonHelper();
        JSONObject info = new JSONObject( helper.stringToJson( "hello") );
        assertEquals( info.getString("value"), "hello" );
        System.out.println("stringToJson passed");
        info = new JSONObject( helper.stringToJson( "iserror") );
        assertEquals( info.getString("error"), "iserror" );
        System.out.println("errorToJson passed");
    }

    @Test
    //put everything here since they should be in the same instance
    public void all() throws Exception {
        Timer timer = new Timer();
        final CountDownLatch latch = new CountDownLatch(1);
        timer.schedule(new TimerTask() {
                           public void run() {
                               try {
                                   assertEquals("camel", service.getKeyword());
                                   assertEquals("BarackObama", service.getTimeline());

                                   service.setKeyword("trump");
                                   service.setTimeline("realDonaldTrump");

                                   assertEquals("trump", service.getKeyword());
                                   assertEquals("realDonaldTrump", service.getTimeline());

                                   Thread.sleep(5000);

                                   service.stop();
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                               latch.countDown();
                           }
                       },
                (5000));

        service.start();

        //cannot be here if stop failed
        Assert.assertTrue(true);
    }

    @Test
    //just for start server, will never end
    public void startOnly() throws Exception {
        service.start();
    }

}