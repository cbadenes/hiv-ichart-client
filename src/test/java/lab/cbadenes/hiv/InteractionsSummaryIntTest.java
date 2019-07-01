package lab.cbadenes.hiv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class InteractionsSummaryIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(InteractionsSummaryIntTest.class);


    @Test
    public void execute() throws IOException {

        String s1 = "output/interactions.jsonl";
        String s2 = "output/interactions-error.jsonl";


        BufferedReader reader1 = new BufferedReader(new FileReader(s1));
        BufferedReader reader2 = new BufferedReader(new FileReader(s2));

        ObjectMapper jsonMapper = new ObjectMapper();
        String row;


        Set<String> primaryList = new TreeSet<>();
        Set<String> comedList   = new TreeSet<>();

        Map<String,Boolean> interactions = new HashMap<>();

        while ((row = reader1.readLine()) != null){
            JsonNode json = jsonMapper.readTree(row);

            String primary      = json.get("primary").asText();
            primaryList.add(primary);
            String comedication = json.get("comedication").asText();
            comedList.add(comedication);

            interactions.put(primary+"-"+comedication,true);
        }

        while ((row = reader2.readLine()) != null){
            JsonNode json = jsonMapper.readTree(row);

            String primary      = json.get("primary").asText();
            primaryList.add(primary);
            String comedication = json.get("comedication").asText();
            comedList.add(comedication);

            interactions.put(primary+"-"+comedication,false);
        }

        LOG.info("Total Interactions: " + interactions.size());
        LOG.info("Total Primary: " + primaryList.size());
        LOG.info("Total Comeds: " + comedList.size());
        LOG.info("Mixed Comeds: " + comedList.stream().filter(f -> primaryList.contains(f)).count());


        BufferedWriter w1 = new BufferedWriter(new FileWriter("output/primary-summary.csv"));
        primaryList.forEach(p -> {
            try {
                w1.write(p + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        w1.close();

        BufferedWriter w2 = new BufferedWriter(new FileWriter("output/comed-summary.csv"));
        comedList.forEach(p -> {
            try {
                w2.write(p + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        w2.close();


        BufferedWriter writer = new BufferedWriter(new FileWriter("output/interactions-summary.csv"));
        writer.write("primary, comedication, available\n");
        interactions.entrySet().stream().sorted( (a,b) -> a.getKey().compareTo(b.getKey())).forEach(entry -> {
            try {
                String primary      = StringUtils.substringBefore(entry.getKey(), "-");
                String comedication = StringUtils.substringAfter(entry.getKey(), "-");
                writer.write(primary+","+comedication+","+entry.getValue()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.close();

    }



}
