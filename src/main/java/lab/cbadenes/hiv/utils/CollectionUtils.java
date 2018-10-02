package lab.cbadenes.hiv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CollectionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionUtils.class);

    public static void toFile(List<String> values, String fileName) throws IOException {
        File outputFile = new File(fileName);
        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fileName, false))));

        try{
            values.parallelStream().forEach(value -> {
                try {
                    writer.write(value + "\n");
                } catch (Exception e) {
                    LOG.error("Error writing on file: " + fileName,e);
                }
            });

        }finally{
            writer.close();
        }

    }

}
