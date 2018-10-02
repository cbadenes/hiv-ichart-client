package lab.cbadenes.hiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.cbadenes.hiv.model.Interaction;
import lab.cbadenes.hiv.model.PatientsPexRecetaSheet;
import lab.cbadenes.hiv.model.PatientsSheet;
import lab.cbadenes.hiv.model.PexSheet;
import lab.cbadenes.hiv.utils.CollectionUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PatientsStudyIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(PatientsStudyIntTest.class);


    public static final String SEPARATOR = ";";

    @Test
    public void execute() throws IOException, InvalidFormatException {

        String input                    = "src/main/resources/4. Pacientes HIV enero a junio 2017_PEX-RECETA_Interacciones_PACIENTES_Carlos.xlsx";
        String inputInteractions        = "output/interactions.jsonl";
        String inputComedPex            = "output/comedications-pex.csv";
        String output                   = "output/patients-study.csv";
        String outputStatus             = "output/patients-status.csv";
        String outputAtcComed           = "output/patients-per-atc-comeds.csv";
        String outputAtcArv             = "output/patients-per-atc-arv.csv";


        File outputFile = new File(output);
        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, false)));

        File outputFile2 = new File(outputStatus);
        if (outputFile2.exists()) outputFile2.delete();
        else outputFile2.getParentFile().mkdirs();
        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputStatus, false)));

        File outputFile3 = new File(outputAtcComed);
        if (outputFile3.exists()) outputFile3.delete();
        else outputFile3.getParentFile().mkdirs();
        BufferedWriter writer3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputAtcComed, false)));

        File outputFile4 = new File(outputAtcArv);
        if (outputFile4.exists()) outputFile4.delete();
        else outputFile4.getParentFile().mkdirs();
        BufferedWriter writer4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputAtcArv, false)));

        List<String> stopComeds = new ArrayList<>();
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(inputComedPex)));
        String rowLine = null;
        while ((rowLine = reader1.readLine()) != null){
            stopComeds.add(rowLine.toLowerCase());
        }
        reader1.close();


        PatientsPexRecetaSheet sheet = new PatientsPexRecetaSheet(input);
        Map<String, List<String>> pexPatients       = sheet.getPatientPex();
        Map<String, List<String>> recetaPatients    = sheet.getPatientReceta();


        Map<String,List<String>> interactionsPatients    = new HashMap<>();

        AtomicInteger index = new AtomicInteger();
        Set<String> patients = new TreeSet<>();
        Map<Integer,List<String>> patientsPerInteractionStatus = new HashMap<>();
        for(String patient : pexPatients.keySet()){

            LOG.info("analyzing patient '" + patient +"'  [" + index.incrementAndGet() + "/" + pexPatients.size() +"]");
            if (!recetaPatients.containsKey(patient)) continue;

            List<String> pexList    = pexPatients.get(patient);
            List<String> recetaList = recetaPatients.get(patient);

            for(String pex: pexList){
                for(String receta: recetaList){
                    if (stopComeds.contains(pex.toLowerCase()) || stopComeds.contains(receta.toLowerCase())) continue;
                    patients.add(patient);
                    String interaction = pex+"-"+receta;
                    List<String> patientList= new ArrayList<>();
                    if (interactionsPatients.containsKey(interaction)) patientList = interactionsPatients.get(interaction);
                    patientList.add(patient);
                    interactionsPatients.put(interaction,patientList);

                }
            }
        }

        LOG.info("Num Patients with Interactions: " + patients);

        StringBuilder header = new StringBuilder();
        header.append("interaction_status").append(SEPARATOR);
        header.append("primary_drug_name").append(SEPARATOR);
        header.append("primary_drug_atc_code").append(SEPARATOR);
        header.append("co_drug_name").append(SEPARATOR);
        header.append("co_drug_atc_code").append(SEPARATOR);
        header.append("evidence_grade").append(SEPARATOR);
        header.append("summary").append(SEPARATOR);
//        header.append("description").append(SEPARATOR);
        header.append("patients").append(SEPARATOR);
        header.append("\n");
        writer.write(header.toString());


        Map<String,List<String>> patientsPerAtcComedMap = new HashMap<>();
        Map<String,List<String>> patientsPerAtcArvMap = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputInteractions)));
        ObjectMapper jsonMapper = new ObjectMapper();
        String line = null;
        while ((line = reader.readLine()) != null){
            Interaction interactionInfo = jsonMapper.readValue(line, Interaction.class);
            String interactionKey   = interactionInfo.getPrimary()+"-"+interactionInfo.getComedication();
            Integer numPatients     = 0;
            if (interactionsPatients.containsKey(interactionKey)) numPatients = Long.valueOf(interactionsPatients.get(interactionKey).stream().distinct().count()).intValue();

            String res = interactionInfo.getInteraction();
            try{
                JSONArray array = new JSONArray(res);
                if (array.length() <1) continue;
                JSONObject intJson = array.getJSONObject(0);

                int interactionStatus   = intJson.getInt("interaction_status");
                List<String> accPatientList = new ArrayList<>();
                if (patientsPerInteractionStatus.containsKey(interactionStatus)) accPatientList = patientsPerInteractionStatus.get(interactionStatus);
                if (interactionsPatients.containsKey(interactionKey)) accPatientList.addAll(interactionsPatients.get(interactionKey));
                patientsPerInteractionStatus.put(interactionStatus,accPatientList);

                String primaryDrugName  = intJson.getString("primary_drug_name");
                String primaryATCCode   = interactionInfo.getPrimary();

                String coDrugName       = intJson.getString("co_drug_name");
                String coDrugATCCode    = interactionInfo.getComedication();

                int evidenceGrade       = intJson.has("evidence_grade") && !intJson.isNull("evidence_grade")? intJson.getInt("evidence_grade") : 0;

                String summary          = intJson.getString("summary");
                String description      = intJson.getString("description");


                StringBuilder row = new StringBuilder();
                row.append(interactionStatus).append(SEPARATOR);
                row.append(primaryDrugName).append(SEPARATOR);
                row.append(primaryATCCode).append(SEPARATOR);
                row.append(coDrugName).append(SEPARATOR);
                row.append(coDrugATCCode).append(SEPARATOR);
                row.append(evidenceGrade).append(SEPARATOR);
                row.append(summary.replace("\n","").replace(SEPARATOR,".").replace("\r","")).append(SEPARATOR);
//                row.append(description.replace("\n","")).append(SEPARATOR);
                row.append(numPatients);
                row.append("\n");
                writer.write(row.toString());

                // insert into patients per ATC and Comed
                String atcAndComed = interactionStatus +"-"+coDrugATCCode.substring(0,1);
                List<String> patientsAux = new ArrayList<>();
                if (patientsPerAtcComedMap.containsKey(atcAndComed)){
                    patientsAux = patientsPerAtcComedMap.get(atcAndComed);
                }
                if (interactionsPatients.containsKey(interactionKey)){
                    patientsAux.addAll(interactionsPatients.get(interactionKey));
                }
                patientsPerAtcComedMap.put(atcAndComed, patientsAux);

                // insert into patients per ATC and ARV
                String atcAndArv = interactionStatus +"-"+primaryATCCode;
                List<String> patients2Aux = new ArrayList<>();
                if (patientsPerAtcArvMap.containsKey(atcAndArv)){
                    patients2Aux = patientsPerAtcArvMap.get(atcAndArv);
                }
                if (interactionsPatients.containsKey(interactionKey)){
                    patients2Aux.addAll(interactionsPatients.get(interactionKey));
                }
                patientsPerAtcArvMap.put(atcAndArv, patients2Aux);



            }catch (JSONException e){
                LOG.error(e.getMessage() + ": " + res);
                continue;
            }



        }

        writer2.write("Interaction_Status" +SEPARATOR + "Patients" + "\n");

        patientsPerInteractionStatus.entrySet().forEach(entry -> {
            try {
                writer2.write(entry.getKey() + SEPARATOR + entry.getValue().stream().distinct().count() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        // Write patients-per-atc-comeds.csv

        StringBuilder header3 = new StringBuilder();
        header3.append("interaction_status").append(SEPARATOR);
        header3.append("co_drug_atc_code").append(SEPARATOR);
        header3.append("patients").append(SEPARATOR);
        header3.append("\n");
        writer3.write(header3.toString());

        for(Map.Entry<String,List<String>> patientsPerAtc : patientsPerAtcComedMap.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).collect(Collectors.toList())){
            String[] values = patientsPerAtc.getKey().split("-");
            StringBuilder row = new StringBuilder();
            row.append(values[0]).append(SEPARATOR);
            row.append(values[1]).append(SEPARATOR);
            row.append(patientsPerAtc.getValue().stream().distinct().count()).append(SEPARATOR);
            row.append("\n");
            writer3.write(row.toString());
        }

        // Write patients-per-atc-arv.csv

        StringBuilder header4 = new StringBuilder();
        header4.append("interaction_status").append(SEPARATOR);
        header4.append("primary_drug_atc_code").append(SEPARATOR);
        header4.append("patients").append(SEPARATOR);
        header4.append("\n");
        writer4.write(header4.toString());

        for(Map.Entry<String,List<String>> patientsPerAtcArv : patientsPerAtcArvMap.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).collect(Collectors.toList())){
            String[] values = patientsPerAtcArv.getKey().split("-");
            StringBuilder row = new StringBuilder();
            row.append(values[0]).append(SEPARATOR);
            row.append(values[1]).append(SEPARATOR);
            row.append(patientsPerAtcArv.getValue().stream().distinct().count()).append(SEPARATOR);
            row.append("\n");
            writer4.write(row.toString());
        }


        writer.close();
        writer2.close();
        writer3.close();
        writer4.close();

    }


}
