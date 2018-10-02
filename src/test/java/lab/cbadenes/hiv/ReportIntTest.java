package lab.cbadenes.hiv;

import lab.cbadenes.hiv.utils.CollectionUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ReportIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(ReportIntTest.class);


    @Test
    public void error() throws IOException {

        String outputInteractionsError  = "output/interactions-error.jsonl.gz";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(outputInteractionsError))));

        Set<String> comeds = new TreeSet<>();
        try{
            String line;
            while((line = reader.readLine()) != null){

                JSONObject jsonObject = new JSONObject(line);
                comeds.add(jsonObject.getString("comedication"));

            }
            LOG.info("Total Comeds: " + comeds.size());
            CollectionUtils.toFile(new ArrayList<>(comeds),"output/missing-comeds.csv.gz");

        }catch (Exception e){
            LOG.error("Error reading file",e);
        }

    }
}
