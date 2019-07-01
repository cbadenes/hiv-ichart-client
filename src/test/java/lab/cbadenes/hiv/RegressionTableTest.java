package lab.cbadenes.hiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.cbadenes.hiv.io.WriterFactory;
import lab.cbadenes.hiv.model.Interaction;
import lab.cbadenes.hiv.model.PatientActive;
import lab.cbadenes.hiv.model.PatientsPexRecetaSheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class RegressionTableTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegressionTableTest.class);


    public static final String SEPARATOR = ";";


    @Test
    public void generatePartialFiles() throws IOException, InvalidFormatException {

        String input                    = "src/main/resources/4. Pacientes HIV enero a junio 2017_PEX-RECETA_Interacciones_PACIENTES_Carlos.xlsx";


        PatientsPexRecetaSheet sheet = new PatientsPexRecetaSheet(input);
        Map<String, List<String>> pexPatients       = sheet.getPatientPex();
        Map<String, List<String>> recetaPatients    = sheet.getPatientReceta();
        Map<String, Integer> pimList                = loadMap("src/main/resources/pim-list.csv");


        Map<String,String> arvMap = new HashMap<>();
        arvMap.put("J05AE01","bPI");
        arvMap.put("J05AE02","bPI");
        arvMap.put("J05AE03","bPI");
        arvMap.put("J05AE04","bPI");
        arvMap.put("J05AE05","bPI");
        arvMap.put("J05AE07","bPI");
        arvMap.put("J05AE08","bPI");
        arvMap.put("J05AE09","bPI");
        arvMap.put("J05AE10","bPI");
        arvMap.put("J05AR10","bPI");
        arvMap.put("J05AR14","bPI");
        arvMap.put("J05AR15","bPI");
        arvMap.put("J05AR22","bPI");

        arvMap.put("J05AF01","nRTIs");
        arvMap.put("J05AF02","nRTIs");
        arvMap.put("J05AF04","nRTIs");
        arvMap.put("J05AF05","nRTIs");
        arvMap.put("J05AF06","nRTIs");
        arvMap.put("J05AF07","nRTIs");
        arvMap.put("J05AF09","nRTIs");
        arvMap.put("J05AF13","nRTIs");
        arvMap.put("J05AR01","nRTIs");
        arvMap.put("J05AR02","nRTIs");
        arvMap.put("J05AR03","nRTIs");
        arvMap.put("J05AR04","nRTIs");
        arvMap.put("J05AR17","nRTIs");

        arvMap.put("J05AG01","nnRTIs");
        arvMap.put("J05AG03","nnRTIs");
        arvMap.put("J05AG04","nnRTIs");
        arvMap.put("J05AG05","nnRTIs");
        arvMap.put("J05AR06","nnRTIs");
        arvMap.put("J05AR08","nnRTIs");
        arvMap.put("J05AR19","nnRTIs");

        arvMap.put("J05AR09","bINSTI");
        arvMap.put("J05AR18","bINSTI");

        arvMap.put("J05AR13","INSTI");
        arvMap.put("J05AX08","INSTI");
        arvMap.put("J05AX12","INSTI");

        arvMap.put("J05AX09","MVC");

        arvMap.put("J05AX07","ENF");

        Map<String,Set<String>> patientsPerArv = new HashMap<>();

        for(String p: pexPatients.keySet()){

            List<String> arvList = pexPatients.get(p);
            for(String c : arvList){

                if (!arvMap.containsKey(c)){
                    LOG.warn("missing ARV Code: " +c);
                    continue;
                }
                String key = arvMap.get(c);
                if (!patientsPerArv.containsKey(key)){
                    patientsPerArv.put(key, new TreeSet<>());
                }
                patientsPerArv.get(key).add(p);
            }


        }

        for (String arvCode : patientsPerArv.keySet()){
            BufferedWriter w1 = WriterFactory.newWriter(Paths.get("output", "arv", "patients-" + arvCode + "-arv.csv").toFile().getAbsolutePath());
            for(String p: patientsPerArv.get(arvCode)){
                w1.write(p+"\n");
            }
            w1.close();
        }

        Map<String,Set<String>> patientsPerComed    = new HashMap<>();
        Map<String,Set<String>> patientsWithPIM     = new HashMap<>();

        for(String p: recetaPatients.keySet()){

            List<String> comeds = recetaPatients.get(p);
            for(String c : comeds){

                String key = c.substring(0,1).toUpperCase();

                if (!patientsPerComed.containsKey(key)){
                    patientsPerComed.put(key, new TreeSet<>());
                }
                patientsPerComed.get(key).add(p);

                if (pimList.containsKey(c) && !patientsWithPIM.containsKey(p)){
                    patientsWithPIM.put(p, new TreeSet<>());
                }
            }

        }

        BufferedWriter w11 = WriterFactory.newWriter(Paths.get("output","patients-pim.csv").toFile().getAbsolutePath());
        for(String pacPim : patientsWithPIM.keySet()){
            w11.write(pacPim+"\n");
        }
        w11.close();

        for(String comedKey : patientsPerComed.keySet()){
            BufferedWriter w1 = WriterFactory.newWriter(Paths.get("output","comeds", "patients-" + comedKey + "-atc-comeds.csv").toFile().getAbsolutePath());
            for(String p: patientsPerComed.get(comedKey)){
                w1.write(p+"\n");
            }
            w1.close();
        }


    }


    @Test
    public void createTable() throws IOException, InvalidFormatException {

        String input                    = "src/main/resources/patients_edadysexo.csv";
        String output                   = "output/regression.csv";


        BufferedWriter writer = WriterFactory.newWriter(output);

        Map<String, Integer> arv_bINSTI = loadMap("output/arv/patients-bINSTI-arv.csv");
        Map<String, Integer> arv_bPI    = loadMap("output/arv/patients-bPI-arv.csv");
        Map<String, Integer> arv_ENF    = loadMap("output/arv/patients-ENF-arv.csv");
        Map<String, Integer> arv_INSTI  = loadMap("output/arv/patients-INSTI-arv.csv");
        Map<String, Integer> arv_MVC    = loadMap("output/arv/patients-MVC-arv.csv");
        Map<String, Integer> arv_nnRTIs = loadMap("output/arv/patients-nnRTIs-arv.csv");
        Map<String, Integer> arv_nRTIs  = loadMap("output/arv/patients-nRTIs-arv.csv");

        Map<String, Integer> comed_A    = loadMap("output/comeds/patients-A-atc-comeds.csv");
        Map<String, Integer> comed_B    = loadMap("output/comeds/patients-B-atc-comeds.csv");
        Map<String, Integer> comed_C    = loadMap("output/comeds/patients-C-atc-comeds.csv");
        Map<String, Integer> comed_D    = loadMap("output/comeds/patients-D-atc-comeds.csv");
        Map<String, Integer> comed_G    = loadMap("output/comeds/patients-G-atc-comeds.csv");
        Map<String, Integer> comed_H    = loadMap("output/comeds/patients-H-atc-comeds.csv");
        Map<String, Integer> comed_J    = loadMap("output/comeds/patients-J-atc-comeds.csv");
        Map<String, Integer> comed_L    = loadMap("output/comeds/patients-L-atc-comeds.csv");
        Map<String, Integer> comed_M    = loadMap("output/comeds/patients-M-atc-comeds.csv");
        Map<String, Integer> comed_N    = loadMap("output/comeds/patients-N-atc-comeds.csv");
        Map<String, Integer> comed_P    = loadMap("output/comeds/patients-P-atc-comeds.csv");
        Map<String, Integer> comed_R    = loadMap("output/comeds/patients-R-atc-comeds.csv");
        Map<String, Integer> comed_S    = loadMap("output/comeds/patients-S-atc-comeds.csv");
        Map<String, Integer> comed_V    = loadMap("output/comeds/patients-V-atc-comeds.csv");

        Map<String, Integer> status_1   = loadMap("output/status/patients-1-status.csv");
        Map<String, Integer> status_2   = loadMap("output/status/patients-2-status.csv");
        Map<String, Integer> status_3   = loadMap("output/status/patients-3-status.csv");
        Map<String, Integer> status_4   = loadMap("output/status/patients-4-status.csv");
        Map<String, Integer> status_5   = loadMap("output/status/patients-5-status.csv");
        Map<String, Integer> pimList    = loadMap("output/patients-pim.csv");




        writer.write("PATIENT;" +
                "AGE;" +
                "AGE_RANGE;" +
                "GENDER;" +
                "NUM_PA_HIV;" +
                "NUM_PA_RECETA;" +
                "POLYPHARMACY;" +
                "bPI;"+
                "nRTIs;"+
                "nnRTIs;"+
                "bINSTI;"+
                "INSTI;"+
                "MVC;"+
                "ENF;"+
                "COMEDS_A;"+
                "COMEDS_B;"+
                "COMEDS_C;"+
                "COMEDS_D;"+
                "COMEDS_G;"+
                "COMEDS_H;"+
                "COMEDS_J;"+
                "COMEDS_L;"+
                "COMEDS_M;"+
                "COMEDS_N;"+
                "COMEDS_P;"+
                "COMEDS_R;"+
                "COMEDS_S;"+
                "COMEDS_V;"+
                "RED_FLAG;"+        //1
                "ORANGE_FLAG;"+     //2
                "YELLOW_FLAG;"+     //5
                "GREEN_FLAG;"+      //3
                "GREY_FLAG;"+       //4
                "PIM;"+       //4
                "\n"
        );

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
        String rowLine = null;
        reader1.readLine();
        while ((rowLine = reader1.readLine()) != null){
            PatientActive patient = new PatientActive(rowLine);
            if (patient.getGender().equalsIgnoreCase("DESCONOCIDO")) continue;
            writer.write(patient.getId()+";");
            writer.write(patient.getAge()+";");
            writer.write(patient.getAgeRange()+";");
            writer.write(patient.getGender()+";");
            writer.write(patient.getHivActives()+";");
            writer.write(patient.getReceipActives()+";");
            writer.write(patient.getPolypharmacy()+";");
            writer.write(validate(patient.getId(), arv_bPI)+";");
            writer.write(validate(patient.getId(), arv_nRTIs)+";");
            writer.write(validate(patient.getId(), arv_nnRTIs)+";");
            writer.write(validate(patient.getId(), arv_bINSTI)+";");
            writer.write(validate(patient.getId(), arv_INSTI)+";");
            writer.write(validate(patient.getId(), arv_MVC)+";");
            writer.write(validate(patient.getId(), arv_ENF)+";");
            writer.write(validate(patient.getId(), comed_A)+";");
            writer.write(validate(patient.getId(), comed_B)+";");
            writer.write(validate(patient.getId(), comed_C)+";");
            writer.write(validate(patient.getId(), comed_D)+";");
            writer.write(validate(patient.getId(), comed_G)+";");
            writer.write(validate(patient.getId(), comed_H)+";");
            writer.write(validate(patient.getId(), comed_J)+";");
            writer.write(validate(patient.getId(), comed_L)+";");
            writer.write(validate(patient.getId(), comed_M)+";");
            writer.write(validate(patient.getId(), comed_N)+";");
            writer.write(validate(patient.getId(), comed_P)+";");
            writer.write(validate(patient.getId(), comed_R)+";");
            writer.write(validate(patient.getId(), comed_S)+";");
            writer.write(validate(patient.getId(), comed_V)+";");
            writer.write(validate(patient.getId(), status_1)+";");
            writer.write(validate(patient.getId(), status_2)+";");
            writer.write(validate(patient.getId(), status_5)+";");
            writer.write(validate(patient.getId(), status_3)+";");
            writer.write(validate(patient.getId(), status_4)+";");
            writer.write(validate(patient.getId(), pimList)+";");
            writer.write("\n");
        }
        reader1.close();
        writer.close();

    }

    @Test
    public void validate() throws IOException, InvalidFormatException {

        Map<String, Integer> pimPatients = loadMap("output/patients-pim.csv");


        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream("output/regression.csv")));
        String rowLine = null;
        Map<String,Integer> regressionPatients = new HashMap<>();
        while ((rowLine = reader1.readLine()) != null){
            regressionPatients.put(rowLine.split(";")[0],1);
        }
        reader1.close();


        for(String p : pimPatients.keySet()){
            if (!regressionPatients.containsKey(p)){
                LOG.info("missing patient '" +p +"'" );
            }
        }

        String input                    = "src/main/resources/4. Pacientes HIV enero a junio 2017_PEX-RECETA_Interacciones_PACIENTES_Carlos.xlsx";


        PatientsPexRecetaSheet sheet = new PatientsPexRecetaSheet(input);
        Map<String, List<String>> pexPatients       = sheet.getPatientPex();
        Map<String, List<String>> recetaPatients    = sheet.getPatientReceta();

        LOG.info("Patients in Excel and NOT in CSV:");

        for(String p: recetaPatients.keySet()){
            if (!regressionPatients.containsKey(p)){
                LOG.info("missing RECETA patient: " + p);
            }
        }

        for(String p: pexPatients.keySet()){
            if (!regressionPatients.containsKey(p)){
                LOG.info("missing PEX patient: " + p);
            }
        }


    }

    private Map<String,Integer> loadMap(String path) throws IOException {
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String rowLine = null;
        Map<String,Integer> map = new HashMap<>();
        while ((rowLine = reader1.readLine()) != null){
            map.put(rowLine,1);
        }
        reader1.close();
        return map;
    }

    private String validate(String patient, Map<String,Integer> map){
        return map.containsKey(patient)? "1" : "0";
    }

}
