package lab.cbadenes.hiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.cbadenes.hiv.model.Interaction;
import lab.cbadenes.hiv.model.PatientsSheet;
import lab.cbadenes.hiv.model.PexSheet;
import lab.cbadenes.hiv.utils.CollectionUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BatchIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(BatchIntTest.class);


    @Test
    public void download() throws IOException, InvalidFormatException {

        String input        = "src/main/resources/Pacientes HIV enero a junio 2017_interacciones.xlsx";
        String inputPex     = "src/main/resources/PEX_ARV.xlsx";
        String outputInteractions       = "output/interactions.jsonl.gz";
        String outputInteractionsError  = "output/interactions-error.jsonl.gz";
        String outputPrimaryDrugs       = "output/primary-drugs.csv.gz";
        String outputComedications      = "output/comedications.csv.gz";
        String outputComedicationsPex   = "output/comedications-pex.csv.gz";

        File outputFile = new File(outputInteractions);
        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputInteractions, false))));

        File outputErrorFile = new File(outputInteractionsError);
        if (outputErrorFile.exists()) outputErrorFile.delete();
        else outputFile.getParentFile().mkdirs();
        BufferedWriter writerError = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputInteractionsError, false))));


        PatientsSheet sheet = new PatientsSheet(input, -1);
        LOG.info("" + sheet);
        PexSheet pexSheet   = new PexSheet(inputPex);

        List<String> initialPexList = sheet.getPexATCList();

        List<String> primaryDrugs = initialPexList.parallelStream().filter(pex -> pexSheet.isPex(pex)).collect(Collectors.toList());
        LOG.info("primary-drugs size: " + primaryDrugs.size());
        CollectionUtils.toFile(primaryDrugs, outputPrimaryDrugs);

        List<String> comedicationsPex = initialPexList.parallelStream().filter(pex -> !pexSheet.isPex(pex)).collect(Collectors.toList());
        LOG.info("comedications-pex size: " + comedicationsPex.size());
        CollectionUtils.toFile(comedicationsPex, outputComedicationsPex);


        List<String> comedications = sheet.getPrescriptionATCList();
        LOG.info("comedications size: " + comedications.size());
        CollectionUtils.toFile(comedications, outputComedications);

        comedications.addAll(comedicationsPex);

        primaryDrugs.parallelStream().forEach( pex -> {

            LOG.info("Getting interactions for Pex '" + pex + "' ...");
            RestClient restClient = new RestClient();
            ObjectMapper jsonMapper = new ObjectMapper();
            comedications.forEach( comm -> {

                List<String> primary    = Arrays.asList(new String[]{pex});
                List<String> comeds     = Arrays.asList(new String[]{comm});

                Interaction interaction = new Interaction();
                interaction.setPrimary(pex);
                interaction.setComedication(comm);
                try {
                    String result = restClient.checkInteraction(primary, comeds);
                    interaction.setInteraction(result);
                    writer.write(jsonMapper.writeValueAsString(interaction) + "\n");
                }catch (RuntimeException e){
                    interaction.setInteraction(e.getMessage());
                    try {
                        writerError.write(jsonMapper.writeValueAsString(interaction) + "\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error",e);
                }

            });

            LOG.info("Pex '" + pex + "' completed.");
        });

        writer.close();

        writerError.close();
    }


}
