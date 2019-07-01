package stats;

import lab.cbadenes.hiv.model.Stats;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class StatsViewerTest {

    private static final Logger LOG = LoggerFactory.getLogger(StatsViewerTest.class);

    @Test
    public void execute() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/edad.csv"));
        Integer offset = 1;
        String row;
        List<Double> ageList = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        while(( row = reader.readLine()) != null){
            if (counter.incrementAndGet()<= offset) continue;
            String[] values = row.split(";");
            String times = values[2].replace(".","");
            Integer limit = Integer.valueOf(times);
            Double index = Double.valueOf(Integer.valueOf(values[0]));
            for(int i=0;i<limit;i++){
                ageList.add(index);
            }
            LOG.info("Added " + index + " value " + limit +" times ");
        }

        Stats stats = new Stats(ageList);
        LOG.info("Age stats: " + stats);
        LOG.info("Age Size: " + ageList.size());
        LOG.info("Q1: " + stats.percentil(25));
        LOG.info("Q2: " + stats.percentil(50));
        LOG.info("Q3: " + stats.percentil(75));
        LOG.info("Q4: " + stats.percentil(100));

    }

}
