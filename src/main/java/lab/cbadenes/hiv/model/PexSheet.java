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

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PexSheet {

    private static final Logger LOG = LoggerFactory.getLogger(PexSheet.class);

    Set<String> pexList = new TreeSet<>();

    public PexSheet(String filePath) throws IOException, InvalidFormatException {
        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(filePath));


        workbook.forEach( sheet -> {
            DataFormatter dataFormatter = new DataFormatter();
            sheet.forEach( row -> {
                int rowNum = row.getRowNum();
                if (rowNum == 0)return;
                Cell c1 = row.getCell(0);
                if (c1 != null){
                    pexList.add(dataFormatter.formatCellValue(c1).toLowerCase());
                }

                if (rowNum % 1000 == 0) LOG.info(rowNum + " rows processed");
            });
        });

        workbook.close();
    }

    public List<String> getPexList() {
        return new ArrayList<>(pexList);
    }

    public boolean isPex(String candidate){
        return pexList.contains(candidate.toLowerCase());
    }
}
