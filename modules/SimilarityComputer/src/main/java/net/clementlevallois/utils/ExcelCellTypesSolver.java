/*
 * Copyright 2017 Clément Levallois
 * http://wwww.clementlevallois.net
 */
package net.clementlevallois.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 *
 * @author LEVALLOIS
 */
public class ExcelCellTypesSolver {

    public static String anyCellToString(Cell cell) {

        if (isCellEmpty(cell)) {
            return null;
        }
        switch (cell.getCellType()) {


            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }

            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case Cell.CELL_TYPE_FORMULA:
                return String.valueOf(cell.getCellFormula());

            default:
                return cell.getRichStringCellValue().getString();
        }
    }

    /**
     * Checks if the value of a given {@link XSSFCell} is empty.
     *
     * @param cell The {@link XSSFCell}.
     * @return {@code true} if the {@link XSSFCell} is empty. {@code false}
     * otherwise.
     */
    private static boolean isCellEmpty(Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return true;
        }

        if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty()) {
            return true;
        }

        return false;
    }

}
