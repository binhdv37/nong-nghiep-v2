package org.thingsboard.server.dft.services.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.controllers.web.users.dtos.ExcelDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {

    public static final int COLUMN_INDEX_TIME = 0;
    public static final int COLUMN_INDEX_ENTITY_TYPE = 1;
    public static final int COLUMN_INDEX_ENTITY_NAME = 2;
    public static final int COLUMN_INDEX_USERNAME = 3;
    public static final int COLUMN_INDEX_ACTION_TYPE = 4;
    public static final int COLUMN_INDEX_ACTION_STATUS = 5;
    private static CellStyle cellStyleFormatNumber = null;
    public static final String FILE_NAME = "excel.xlsx";

    @Override
    public ByteArrayInputStream writeExcel(List<ExcelDto> logs) {
        boolean resultOfWriteHeader;
        boolean resultOfAutoResizeColumn;
        ByteArrayInputStream byteArrayInputStream = null;
        int rowIndex = 0;
        Workbook workbook;
        Sheet sheet;
        try {
            workbook = getWorkbook(FILE_NAME);
            if (workbook != null) {
                sheet = workbook.createSheet("Access history");
                resultOfWriteHeader = writeHeader(sheet, rowIndex);
                if (resultOfWriteHeader) {
                    rowIndex++;
                    for (ExcelDto log: logs) {
                        Row row = sheet.createRow(rowIndex);
                        writeData(log, row);
                        rowIndex++;
                    }
                    int numberOfColumn = sheet.getRow(0).getPhysicalNumberOfCells();
                    resultOfAutoResizeColumn = autoResizeColumn(sheet, numberOfColumn);
                    if (resultOfAutoResizeColumn) {
                        byteArrayInputStream = createOutput(workbook);
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return byteArrayInputStream;
    }

    @Override
    public Workbook getWorkbook(String filePath) {
        try {
            Workbook workbook = null;
            if (filePath.endsWith("xlsx")) {
                workbook = new XSSFWorkbook();
            } else if (filePath.endsWith("xls")) {
                workbook = new HSSFWorkbook();
            } else {
                throw new IllegalArgumentException("The specified file is not Excel file");
            }
            return workbook;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    @Override
    public boolean writeHeader(Sheet sheet, int rowIndex) {
        CellStyle cellStyle;
        Cell cell;
        Row row;
        try {
            cellStyle = createStyleForHeader(sheet);
            row = sheet.createRow(rowIndex);

            cell = row.createCell(COLUMN_INDEX_TIME);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Thời gian");

            cell = row.createCell(COLUMN_INDEX_ENTITY_TYPE);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Loại đối tượng");

            cell = row.createCell(COLUMN_INDEX_ENTITY_NAME);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Đối tượng");

            cell = row.createCell(COLUMN_INDEX_USERNAME);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Tài khoản tác động");

            cell = row.createCell(COLUMN_INDEX_ACTION_TYPE);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Hành động");

            cell = row.createCell(COLUMN_INDEX_ACTION_STATUS);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("Trạng thái");

            return true;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    @Override
    public void writeData(ExcelDto log, Row row) {
        Cell cell;
        try {
            if (cellStyleFormatNumber == null) {
                short format = (short)BuiltinFormats.getBuiltinFormat("#,##0");
                Workbook workbook = row.getSheet().getWorkbook();
                cellStyleFormatNumber = workbook.createCellStyle();
                cellStyleFormatNumber.setDataFormat(format);
            }

            String localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.getCreatedTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss"));
            cell = row.createCell(COLUMN_INDEX_TIME);
            cell.setCellValue(localDateTime);

            cell = row.createCell(COLUMN_INDEX_ENTITY_TYPE);
            cell.setCellValue(log.getEntityType());

            cell = row.createCell(COLUMN_INDEX_ENTITY_NAME);
            cell.setCellValue(log.getEntityName());

            cell = row.createCell(COLUMN_INDEX_USERNAME);
            cell.setCellValue(log.getUserName());

            cell = row.createCell(COLUMN_INDEX_ACTION_TYPE);
            cell.setCellValue(log.getActionType());

            cell = row.createCell(COLUMN_INDEX_ACTION_STATUS);
            cell.setCellValue(log.getActionStatus());

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    @Override
    public CellStyle createStyleForHeader(Sheet sheet) {
        Font font;
        CellStyle cellStyle;
        try {
            font = sheet.getWorkbook().createFont();
            font.setFontName("Times New Roman");
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            font.setColor(IndexedColors.WHITE.getIndex());

            cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setBorderBottom(BorderStyle.THIN);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return cellStyle;
    }

    @Override
    public boolean writeFooter(Sheet sheet, int rowIndex) {
        return false;
    }

    @Override
    public boolean autoResizeColumn(Sheet sheet, int lastColumn) {
        try {
            for (int i = 0; i < lastColumn; i++) {
                sheet.autoSizeColumn(i);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return true;
    }

    @Override
    public ByteArrayInputStream createOutput(Workbook workbook) {
        ByteArrayInputStream in;
        ByteArrayOutputStream out ;
        try {
            out = new ByteArrayOutputStream();
            workbook.write(out);
            in = new ByteArrayInputStream(out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return in;
    }
}
