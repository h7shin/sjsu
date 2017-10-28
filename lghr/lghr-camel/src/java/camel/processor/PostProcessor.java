package camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/***
 *Created by Lin Cheng
 */
abstract class PostProcessor implements Processor {

    protected String body = "";

    @Override
    public void process(Exchange exchange) throws Exception {
        InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                body += line;
            }
            body = body.trim();

            String resp = handle();

            exchange.getOut().setBody(resp);
            exchange.getOut().setHeader("Access-Control-Allow-Origin","*");

        } catch (Exception e) {
            e.printStackTrace();
            exchange.getOut().setBody(e.getMessage());
        } finally {
            inputStream.close();
        }
    }

    abstract String handle() throws Exception;
}


