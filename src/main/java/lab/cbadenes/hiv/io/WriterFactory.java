package lab.cbadenes.hiv.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WriterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(WriterFactory.class);


    public static BufferedWriter newWriter(String path) throws FileNotFoundException {
        File outputFile = new File(path);
        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false)));
    }

}
