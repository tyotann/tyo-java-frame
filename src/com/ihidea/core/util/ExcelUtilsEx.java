package com.ihidea.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.support.exception.ServiceException;

/**
 * 适用于导出,不适用于本身有多sheet 使用临时文件,避免内存溢出
 * @author TYOTANN
 */
public class ExcelUtilsEx {

	private static Log logger = LogFactory.getLog(ExcelUtilsEx.class);

	private WritableWorkbook wwb;

	private Workbook rwb;

	private boolean modify;

	private List<WritableSheet> sheetList = new ArrayList<WritableSheet>();

	// 每个sheet最大行数
	private int maxRow = 65536;

	private List<Integer> cellByteCnt = new ArrayList<Integer>();

	private ExcelUtilsEx(WritableWorkbook rwb, boolean modify) {
		this.wwb = rwb;
		this.modify = modify;
		if (this.modify) {
			sheetList.add(rwb.getSheet(0));
		} else {
			sheetList.add(rwb.createSheet("第" + (sheetList.size() + 1) + "页", sheetList.size()));
		}
	}

	private ExcelUtilsEx(Workbook wb) {
		this.rwb = wb;
	}

	public static ExcelUtilsEx getWInstance(OutputStream os) {

		WorkbookSettings wbSetting = new WorkbookSettings();
		wbSetting.setUseTemporaryFileDuringWrite(true);
		WritableWorkbook rwb = null;
		try {
			rwb = Workbook.createWorkbook(os, wbSetting);
		} catch (Exception e) {
			logger.error("excel创建失败", e);
		}

		return new ExcelUtilsEx(rwb, false);
	}

	public static ExcelUtilsEx getWInstance(File file) {

		WorkbookSettings wbSetting = new WorkbookSettings();
		wbSetting.setUseTemporaryFileDuringWrite(true);
		WritableWorkbook rwb = null;
		try {
			rwb = Workbook.createWorkbook(file, wbSetting);
		} catch (Exception e) {
			logger.error("excel创建失败", e);
		}

		return new ExcelUtilsEx(rwb, false);
	}

	public static ExcelUtilsEx getWInstance(OutputStream os, File templeteFile) {

		WorkbookSettings wbSetting = new WorkbookSettings();
		wbSetting.setUseTemporaryFileDuringWrite(true);
		WritableWorkbook rwb = null;
		try {
			Workbook wb = Workbook.getWorkbook(templeteFile);
			rwb = Workbook.createWorkbook(os, wb, wbSetting);
		} catch (Exception e) {
			logger.error("excel创建失败", e);
		}

		return new ExcelUtilsEx(rwb, true);
	}

	public static ExcelUtilsEx getRInstance(byte[] bytes) {
		return getRInstance(new ByteArrayInputStream(bytes));
	}

	public static ExcelUtilsEx getRInstance(File file) {
		try {
			return getRInstance(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error("excel读取失败", e);
			throw new ServiceException("excel读取失败:" + e.getMessage());
		}
	}

	public static ExcelUtilsEx getRInstance(InputStream is) {

		Workbook wb = null;
		try {
			wb = Workbook.getWorkbook(is);
		} catch (Exception e) {
			logger.error("excel读取失败", e);
			throw new ServiceException("excel读取失败:" + e.getMessage());
		}

		return new ExcelUtilsEx(wb);
	}

	public Integer getSheetRow(int sheet) {
		return rwb.getSheet(sheet).getRows();
	}

	public String read(int sheet, int column, int row) {

		try {
			return rwb.getSheet(sheet).getCell(column, row).getContents();
		} catch (Exception e) {
			logger.error("excel读取失败:", e);
			throw new ServiceException("excel读取失败:" + e.getMessage());
		}

	}

	public void write(int column, int row, String value) {
		write(column, row, value, null);
	}

	public void write(int column, int row, String value, CellFormat cellFormat) {

		int currentSheet = row / maxRow;

		if (currentSheet >= sheetList.size()) {
			sheetList.add(wwb.createSheet("第" + (sheetList.size() + 1) + "页", sheetList.size()));
		}

		// 每个单元格最大字节数(用于计算单元格宽度)
		if (cellByteCnt.size() <= column) {
			cellByteCnt.add(value.getBytes().length);
		} else {
			if (value.getBytes().length > cellByteCnt.get(column)) {
				cellByteCnt.set(column, value.getBytes().length);
			}
		}

		try {

			if (cellFormat == null) {
				cellFormat = sheetList.get(currentSheet).getCell(column, row).getCellFormat();
			}

			sheetList.get(currentSheet).addCell(new Label(column, row % maxRow, value, cellFormat));
		} catch (Exception e) {
			logger.error("excel写入单元格失败", e);
		}
	}

	public void close() {

		if (wwb != null) {

			// 自适应单元格宽
			if (!modify) {
				for (WritableSheet sheet : sheetList) {
					for (int i = 0; i < sheet.getColumns(); i++) {
						sheet.setColumnView(i, cellByteCnt.get(i) > 30 ? 30 : cellByteCnt.get(i));
					}
				}
			}

			try {
				wwb.write();
				wwb.close();
			} catch (Exception e) {
				logger.error("excel保存关闭失败", e);
			}
		}

		if (rwb != null) {
			try {
				rwb.close();
			} catch (Exception e) {
				logger.error("excel关闭失败", e);
			}
		}
	}

}
