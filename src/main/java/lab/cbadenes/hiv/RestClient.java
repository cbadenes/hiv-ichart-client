package lab.cbadenes.hiv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lab.cbadenes.hiv.model.InteractionQuery;
import lab.cbadenes.hiv.model.InteractionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class RestClient {

    /**
     * API Documentation: https://hivdrugs.docs.apiary.io/
     */

    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    private static final String uri     = "http://www.hiv-druginteractions.org/api";

    private static final String secure_uri = "https://www.hiv-druginteractions.org/api";

    //private static final String api_key = "191c1421bc3b02585f7bba8aa7ab6556";
    private static final String api_key = "b40845709167784512475ecd4e047c99";

    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;

    public RestClient(){

        jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Unirest.setObjectMapper(new ObjectMapper() {


            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            } };


            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            Unirest.setHttpClient(httpclient);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void listPrimaryDrugs() throws UnirestException {
        LOG.debug("-> 'list of primary drugs'");
        HttpResponse<JsonNode> response = Unirest.get(uri+"/hiv_drugs")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-API-Key",api_key)
                .asJson();

        LOG.debug("response status: " + response.getStatus());
        LOG.debug("response: " + response.getBody());
    }


    public String checkInteraction(List<String> primary, List<String> comeds) throws UnirestException, IOException {

        LOG.debug("-> 'atc interactions'");

        InteractionQuery query = new InteractionQuery();
        query.setPrimary_atc_codes(primary);
        query.setCo_medication_atc_codes(comeds);

        InteractionRequest request = new InteractionRequest(query);

        LOG.debug("request: " + jacksonObjectMapper.writeValueAsString(request));

        HttpResponse<JsonNode> response = Unirest.post(secure_uri+"/atc_interactions")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-API-Key",api_key)
                .body(request)
                .asJson();


//        response.getHeaders().entrySet().forEach(entry -> LOG.info("response header: " + entry.getKey() + " : " + entry.getValue()));
        LOG.debug("response status: " + response.getStatus());

        if (response.getStatus() != 200){
            throw new RuntimeException(response.getBody().toString());
        }


        Object jsonObject = jacksonObjectMapper.readValue(response.getBody().toString(), Object.class);
        String prettyJson = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        LOG.debug("response: " + prettyJson);

//        LOG.info("response: " + response.getBody());

        return response.getBody().toString();
    }



    public static void main(String[] args) {

        //List<String> primary = Arrays.asList(new String[]{"J05AE03","J05AE10","J05AR03"});
        List<String> primary = Arrays.asList(new String[]{"J05AE03"});

        //List<String> comeds  = Arrays.asList(new String[]{"B01AC06","C08CA01","C10AA05","C10BA05","C07AG02","B01AC04","N03AX12","C01EB17","C09BA03","N02BB02","A02BA02","R03AC02","C01EB15","C01DA02","C01DA52"});
        List<String> comeds  = Arrays.asList(new String[]{"A02BA02"});

        RestClient client = new RestClient();

        try {
            System.out.println(client.checkInteraction(primary,comeds));

        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
