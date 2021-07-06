package org.thingsboard.server.dft.services.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.thingsboard.server.dft.controllers.web.users.dtos.ExcelDto;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelService {

    ByteArrayInputStream writeExcel(List<ExcelDto> logs);

    Workbook getWorkbook(String filePath);

    boolean writeHeader(Sheet sheet, int rowIndex);

    void writeData(ExcelDto log, Row row);

    CellStyle createStyleForHeader(Sheet sheet);

    boolean writeFooter(Sheet sheet, int rowIndex);

    boolean autoResizeColumn(Sheet sheet, int lastColumn);

    ByteArrayInputStream createOutput(Workbook workbook);
}
