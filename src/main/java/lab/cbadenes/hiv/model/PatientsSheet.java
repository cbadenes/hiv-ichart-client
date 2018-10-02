package lab.cbadenes.hiv.model;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PatientsSheet {

    private static final Logger LOG = LoggerFactory.getLogger(PatientsSheet.class);

    Set<String> pexATCList = new TreeSet<>();
    AtomicInteger pexCounter = new AtomicInteger();

    Set<String> prescriptionATCList = new TreeSet<>();
    AtomicInteger prescriptionsCounter = new AtomicInteger();

    public PatientsSheet(String filePath, Integer maxRows) throws IOException, InvalidFormatException {
        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(filePath));


        workbook.forEach( sheet -> {
            DataFormatter dataFormatter = new DataFormatter();
            sheet.forEach( row -> {
                int rowNum = row.getRowNum();
                if (rowNum == 0)return;
                if (maxRows >0 && rowNum > maxRows) return;
                Cell c1 = row.getCell(0);
                if (c1 != null){
                    pexATCList.add(dataFormatter.formatCellValue(c1));
                    pexCounter.incrementAndGet();
                }

                Cell c2 = row.getCell(1);
                if (c2 != null){
                    prescriptionATCList.add(dataFormatter.formatCellValue(c2));
                    prescriptionsCounter.incrementAndGet();
                }
                if (rowNum % 1000 == 0) LOG.info(rowNum + " rows processed");
            });
        });

        workbook.close();
    }

    public List<String> getPexATCList() {
        return new ArrayList<>(pexATCList);
    }

    public List<String> getPrescriptionATCList() {
        return new ArrayList<>(prescriptionATCList);
    }


    @Override
    public String toString() {
        return "PatientsSheet{" +
                "pex=" + pexATCList.size() + "/" + pexCounter.get() +
                " prescriptions=" + prescriptionATCList.size() + "/" + prescriptionsCounter.get() + "}";
    }
}
