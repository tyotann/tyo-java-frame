package com.ihidea.core.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.support.exception.ServiceException;

public class FileUtilsEx {

	private static Log logger = LogFactory.getLog(FileUtilsEx.class);

	private static Set<String> imageSuffix = new HashSet<String>();

	static {
		imageSuffix.add("BMP");
		imageSuffix.add("DUB");
		imageSuffix.add("GIF");
		imageSuffix.add("JFIF");
		imageSuffix.add("JPE");
		imageSuffix.add("JPEG");
		imageSuffix.add("JPG");
		imageSuffix.add("PNG");
		imageSuffix.add("TIF");
		imageSuffix.add("TIFF");
		imageSuffix.add("ICO");
	}

	public static void export(List<Map<String, Object>> lists, OutputStream os, String type, String charsplit) throws WriteException {

		if ("txt".equals(type)) {
			exportTxt(lists, os, charsplit);
		} else if ("csv".equals(type)) {
			exportCsv(lists, os);
		} else if ("xls".equals(type)) {
			exportXls(lists, os);
		} else {
			throw new ServiceException("不支持类型为:" + type + "的导出功能!");
		}
	}

	/**
	 * 导出txt类型
	 * @param rs
	 * @param os
	 */
	public static void exportTxt(List<Map<String, Object>> lists, OutputStream os, String charsplit) {
		BufferedOutputStream bos = new BufferedOutputStream(os);

		if (charsplit == null || "t".equals(charsplit)) {
			charsplit = "	";
		}
		StringBuffer sbcols = new StringBuffer();
		for (Map<String, Object> mp : lists) {
			Set<String> key = mp.keySet();
			for (Iterator<String> it = key.iterator(); it.hasNext();) {
				sbcols.append(mp.get(it.next()));
				sbcols.append(charsplit);
			}
			sbcols.append("\r\n");
		}
		try {
			bos.write(sbcols.toString().getBytes("GBK"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 导出csv类型
	 * @param rs
	 * @param os
	 */
	public static void exportCsv(List<Map<String, Object>> lists, OutputStream os) {
		exportTxt(lists, os, null);
	}

	/**
	 * 导出xls类型
	 * @param rs
	 * @param os
	 * @throws WriteException
	 */
	public static void exportXls(List<Map<String, Object>> lists, OutputStream os) throws WriteException {
		ExcelUtilsEx eu = null;
		try {
			eu = ExcelUtilsEx.getWInstance(os);
			// 设置格式
			WritableCellFormat cellFormat = new WritableCellFormat(new WritableFont(WritableFont.createFont("宋体"), 10));
			cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			int row = 0;
			for (Map<String, Object> map : lists) {
				int column = 0;
				for (Object obj : map.values()) {
					eu.write(column++, row, obj.toString(), cellFormat);
				}
				row++;
			}
		} finally {
			eu.close();
		}

		/*
		 * WritableWorkbook rwb = null; try { rwb = Workbook.createWorkbook(os); WritableSheet sheet = rwb.createSheet("sheet1", 0);
		 * jxl.write.Label label = null; int t = 0; for (Map<String, Object> mp : lists) { Set<String> key = mp.keySet(); int i = 0; for
		 * (Iterator<String> it = key.iterator(); it.hasNext();) { Object o = mp.get(it.next()); if (o == null) o = ""; label = new
		 * jxl.write.Label(i, t, o.toString()); sheet.addCell(label); i++; } t++; } rwb.write(); } catch (Exception e) {
		 * e.printStackTrace(); if (os != null) { try { os.close(); } catch (IOException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); } } } finally { if (rwb != null) { try { rwb.close(); } catch (WriteException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } } if (os !=
		 * null) { try { os.close(); } catch (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); } } }
		 */
	}

	/**
	 * 从路径中得到文件名(文件上传取文件名用)
	 * @param path
	 * @return
	 */
	public static String getFileNameByPath(String path) {

		if (path.lastIndexOf("/") > -1) {
			path = path.substring(path.lastIndexOf("/") + 1);
		}

		if (path.lastIndexOf("\\") > -1) {
			path = path.substring(path.lastIndexOf("\\") + 1);
		}

		return path;
	}

	/**
	 * 过滤文件路径的分隔符
	 * @param path
	 * @return
	 */
	public static String filterPath(String path) {

		if (!StringUtils.isBlank(path) && !path.endsWith(File.separator)) {

			if (path.endsWith("\\") || path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			path = path + File.separator;
		}

		return path;
	}

	/**
	 * 得到文件后缀名
	 * @param fileName 文件名
	 * @return 后缀名
	 */
	public static String getSuffix(String fileName) {

		if (fileName.indexOf(".") == -1) {
			return StringUtils.EMPTY;
		}
		return String.valueOf(fileName.substring(fileName.lastIndexOf(".") + 1)).toUpperCase();
	}

	/**
	 * 将InputStream转换成byte数组
	 * @param in InputStream
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] is2byte(InputStream in) throws IOException {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[10240];
		int count = -1;
		while ((count = in.read(data, 0, 10240)) != -1) {
			outStream.write(data, 0, count);
		}
		data = null;
		return outStream.toByteArray();
	}

	public static boolean isImage(String fileName) {
		boolean isImage = false;

		if (!StringUtils.isBlank(fileName)) {
			String fileSuffix = getSuffix(fileName);

			isImage = imageSuffix.contains(fileSuffix);
		}

		return isImage;
	}

	public static byte[] zip(List<FileEntity> fileList) throws Exception {

		byte[] result = null;

		if (fileList != null && fileList.size() > 0) {

			ByteArrayOutputStream baos = null;
			ZipOutputStream zout = null;
			try {
				baos = new ByteArrayOutputStream();

				try {
					zout = new ZipOutputStream(baos);

					for (FileEntity file : fileList) {
						ZipEntry entry = new ZipEntry(file.getFileName());
						entry.setSize(file.getContent().length);
						zout.putNextEntry(entry);
						zout.write(file.getContent());
					}
				} finally {
					if (zout != null) {
						zout.close();
					}
				}

				result = baos.toByteArray();
			} finally {
				if (baos != null) {
					baos.close();
				}
			}

		}

		return result;
	}

	static public class FileEntity {

		private String fileName;

		private byte[] content;

		public FileEntity(String fileName, byte[] content) {
			this.fileName = fileName;
			this.content = content;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}
	}

}
