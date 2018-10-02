package lab.cbadenes.hiv.model;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PatientsPexRecetaSheet {

    private static final Logger LOG = LoggerFactory.getLogger(PatientsPexRecetaSheet.class);

    Map<String,List<String>> patientPex = new HashMap<>();
    Map<String,List<String>> patientReceta = new HashMap<>();


    public PatientsPexRecetaSheet(String filePath) throws IOException, InvalidFormatException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(filePath));


        Sheet pexSheet      = workbook.getSheet("Detalle PEX_ARV-Paciente");
        loadMapFromSheet(pexSheet, patientPex);
        LOG.info("Total patients/PEX = " + patientPex.size());

        Sheet recetaSheet   = workbook.getSheet("Detalle RECETA_Co-Meds-Paciente");
        loadMapFromSheet(recetaSheet, patientReceta);
        LOG.info("Total patients/RECETA = " + patientReceta.size());

        workbook.close();
    }

    private void loadMapFromSheet(Sheet sheet, Map<String,List<String>> map){
        DataFormatter dataFormatter = new DataFormatter();
        sheet.forEach( row -> {
            int rowNum = row.getRowNum();
            if (rowNum == 0)return;
            Cell c1 = row.getCell(0);
            String patient = dataFormatter.formatCellValue(c1);
            if (c1 != null){
                List<String> pexList = new ArrayList<String>();
                if (map.containsKey(patient)) pexList = map.get(patient);
                Cell c2 = row.getCell(1);
                String pex = dataFormatter.formatCellValue(c2);
                pexList.add(pex);
                map.put(patient,pexList);
            }

            if (rowNum % 100 == 0) LOG.info(rowNum + " rows processed from sheet '" + sheet.getSheetName() +"'");
        });
    }

    public Map<String, List<String>> getPatientPex() {
        return patientPex;
    }

    public Map<String, List<String>> getPatientReceta() {
        return patientReceta;
    }
}
