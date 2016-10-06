package com.ihidea.component.datastore.archive;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.component.datastore.FileSupportService;
import com.ihidea.component.datastore.fileio.FileIoEntity;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.servlet.ServletHolderFilter;
import com.ihidea.core.util.ImageUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 收件扫描
 * @author TYOTANN
 */
@Controller
public class ArchiveController {

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired
	private FileSupportService fileSupportService;

	@Autowired
	private ArchiveService archiveService;

	@RequestMapping("/twain.do")
	public String doTwain(ModelMap model, HttpServletRequest request) {

		String twainHttp = CoreConstants.twainHost;
		model.addAttribute("path", request.getParameter("path"));
		model.addAttribute("params", request.getParameter("cs"));
		model.addAttribute("picname", request.getParameter("picname"));
		model.addAttribute("frame", request.getParameter("frame"));
		model.addAttribute("HostIP", twainHttp.split(":")[0]);
		model.addAttribute("HTTPPort", twainHttp.split(":")[1]);
		model.addAttribute("storeName", "ds_archive");
		model.addAttribute("storeType", "2");
		return "archive/twain";
	}

	@RequestMapping("/editPicture.do")
	public String doEditPicture(ModelMap model, HttpServletRequest request) {
		model.addAttribute("ywid", request.getParameter("ywid"));
		model.addAttribute("img", request.getParameter("img"));
		model.addAttribute("name", request.getParameter("name"));
		return "aie/imageeditor";
	}

	@RequestMapping("/showPicture.do")
	public void showPicture(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		OutputStream os = null;

		String id = request.getParameter("id");

		FileIoEntity file = fileSupportService.get(id);
		byte[] b = file.getContent();
		response.setContentType("image/png");
		try {
			os = response.getOutputStream();
			FileCopyUtils.copy(b, os);
		} catch (IOException e) {
			logger.debug("出错啦!", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.debug("出错啦!", e);
				}
			}
		}
	}

	/**
	 * 扫描收件获取图片信息
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("/doPictureData.do")
	public void doPictureData(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String tid = request.getParameter("id");
		String spcode = request.getParameter("spcode");
		String sncode = request.getParameter("sncode");
		String typeid = request.getParameter("typeid");
		String dm = request.getParameter("dm");
		String dwyq = request.getParameter("dwyq");
		if (tid == null || tid.trim().equals("") || tid.trim().equals("null")) {
			tid = "-1";
		}
		List<Map<String, Object>> pList = null;
		// 根据sncode或spcode查询所有收件
		if (StringUtils.isBlank(spcode) && StringUtils.isBlank(sncode)) {
			pList = archiveService.getPicture(Integer.valueOf(tid));
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("spcode", spcode);
			params.put("sncode", sncode);
			params.put("typeid", typeid);
			params.put("dm", dm);
			pList = archiveService.getPicture("09100E", params);
		}
		// 查询单位印签只需要最后一张
		List<Map<String, Object>> picList = new ArrayList<Map<String, Object>>();
		if (!StringUtils.isBlank(dwyq)) {
			for (int i = 0; i < pList.size(); i++) {
				Map<String, Object> pic = pList.get(i);
				if (dwyq.equals(pic.get("ino").toString())) {
					picList.add(pic);
					break;
				}
			}
		} else {
			picList = pList;
		}

		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
		map.put("images", picList);
		ServletUtilsEx.renderJson(response, map);
	}

	@SuppressWarnings({ "unchecked" })
	@RequestMapping("/uploadDataFile.do")
	public String uploadFjToFile(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		String ino = null;

		int id = request.getParameter("id") == null ? 0 : Integer.valueOf(request.getParameter("id"));

		String strFileName = request.getParameter("filename");

		Map<String, Object> paramMap = ServletHolderFilter.getContext().getParamMap();

		FileItem cfile = null;
		// 得到上传的文件
		for (String key : paramMap.keySet()) {

			if (paramMap.get(key) instanceof List) {
				if (((List) paramMap.get(key)).get(0) instanceof FileItem)
					cfile = ((List<FileItem>) paramMap.get(key)).get(0);
				break;
			}
		}

		ino = fileSupportService.add(cfile.getName(), cfile.get(), "ds_archive");
		fileSupportService.submit(ino, "收件扫描-扫描");

		// 保存到收件明细表ARCSJMX
		archiveService.savePicture(id, ino, strFileName, "");

		return null;
	}

	/**
	 * 扫描收件时上传图片
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping("/uploadUnScanDataFile.do")
	public String uploadUnScanFjToFile(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		Map<String, Object> paramMap = ServletHolderFilter.getContext().getParamMap();

		int id = paramMap.get("id") == null ? 0 : Integer.valueOf(String.valueOf(paramMap.get("id")));

		String strFileName = paramMap.get("filename") == null ? StringUtils.EMPTY : String.valueOf(paramMap.get("filename"));

		String storeName = "ds_archive";

		FileOutputStream fos = null;
		String ino = null;

		try {
			List<FileItem> remoteFile = (List<FileItem>) paramMap.get("uploadFile");
			FileItem cfile = remoteFile.get(0);

			String cfileOrjgName = cfile.getName();
			String cfileName = cfileOrjgName.substring(0, cfileOrjgName.lastIndexOf("."));

			ino = fileSupportService.add(cfile.getName(), cfile.get(), "ds_archive");
			fileSupportService.submit(ino, "收件扫描-本地上传");

			if (strFileName != null) {
				cfileName = strFileName;
			}

			// 保存到收件明细表ARCSJMX
			archiveService.savePicture(id, ino, cfileName, "");
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		} finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}

		model.addAttribute("isSubmit", "1");
		model.addAttribute("params", id);
		model.addAttribute("path", "");
		model.addAttribute("picname", strFileName);
		model.addAttribute("storeName", storeName);
		model.addAttribute("success", "上传成功!");
		model.addAttribute("ino", ino);
		return "archive/twain";
	}

	/**
	 * 扫描收件时查看
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/doPicture.do")
	public String doPicture(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String img = request.getParameter("img");
		String ywid = request.getParameter("ywid");
		model.addAttribute("img", img);
		model.addAttribute("ywid", ywid);
		model.addAttribute("storeName", "ds_archive");
		return "aie/showPicture";
	}

	/**
	 * 下载图片
	 * @param request
	 * @param response
	 * @throws Exception
	 */

	@RequestMapping("/downloadFj.do")
	public void downloadFj(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String id = request.getParameter("id") == null ? "" : String.valueOf(request.getParameter("id"));

		if (StringUtils.isBlank(id)) {
			id = request.getParameter("file") == null ? "" : String.valueOf(request.getParameter("file"));
		}

		ServletOutputStream fos = null;

		try {
			fos = response.getOutputStream();
			response.setContentType("application/octet-stream");

			FileIoEntity file = fileSupportService.get(id);
			FileCopyUtils.copy(file.getContent(), fos);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}

	@RequestMapping("/changePicture.do")
	public void changePicture(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		String type = request.getParameter("type");

		String path = request.getParameter("imagepath");

		// 旋转
		if (type.equals("rotate")) {
			String aktion = request.getParameter("aktion");

			// 角度
			if (aktion.equals("rotieren")) {
				int degree = Integer.valueOf(request.getParameter("degree"));
				try {

					Image imageOriginal = ImageIO.read(new ByteArrayInputStream(fileSupportService.get(path).getContent()));

					// if (uploadType.equals("file")) { // 文件方式存储
					// File tfile = new File(path);
					// if (!tfile.exists()) {
					// return;
					// }
					// imageOriginal = ImageIO.read(tfile);
					// } else { // 数据库方式存储
					// String errfilename =
					// this.getClass().getResource("/").getPath()
					// .replace("WEB-INF/classes",
					// "resources/images/sjerr.png");
					// ByteArrayOutputStream os = new ByteArrayOutputStream();
					// archiveService.getPictureFromDB(request.getParameter("imagepath"),
					// errfilename, os);
					// byte[] data = os.toByteArray();
					// InputStream is = new ByteArrayInputStream(data);
					// imageOriginal = ImageIO.read(is);
					// }
					int widthOriginal = imageOriginal.getWidth(null);
					int heightOriginal = imageOriginal.getHeight(null);

					BufferedImage bi = new BufferedImage(widthOriginal, heightOriginal, BufferedImage.TYPE_3BYTE_BGR);
					Graphics2D g2d = bi.createGraphics();
					g2d.drawImage(imageOriginal, 0, 0, null);
					BufferedImage bu = ImageUtilsEx.rotateImage(bi, degree);

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
					encoder.encode(bu);
					byte[] data = bos.toByteArray();

					// 更新文件内容
					fileSupportService.updateContent(path, data);

					// if (uploadType.equals("file")) { // 文件方式存储
					// FileOutputStream fos = new FileOutputStream(path);
					// JPEGImageEncoder encoder =
					// JPEGCodec.createJPEGEncoder(fos);
					// encoder.encode(bu);
					//
					// fos.flush();
					// fos.close();
					// fos = null;
					// } else { // 数据库方式存储
					// ByteArrayOutputStream bos = new ByteArrayOutputStream();
					// JPEGImageEncoder encoder =
					// JPEGCodec.createJPEGEncoder(bos);
					// encoder.encode(bu);
					// byte[] data = bos.toByteArray();
					// InputStream is = new ByteArrayInputStream(data);
					// archiveService.updatePictureToDB(request.getParameter("imagepath"),
					// is, data.length);
					//
					// bos.flush();
					// bos.close();
					// bos = null;
					//
					// is.close();
					// is = null;
					// }
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("错误：" + e.getMessage());
				}

				// 翻转
			} else {
				String degree = request.getParameter("degree");

				try {
					Image imageOriginal = ImageIO.read(new ByteArrayInputStream(fileSupportService.get(path).getContent()));

					// if (uploadType.equals("file")) { // 文件方式存储
					// File tfile = new File(path);
					// if (!tfile.exists()) {
					// return;
					// }
					// imageOriginal = ImageIO.read(tfile);
					// } else { // 数据库方式存储
					// String errfilename =
					// this.getClass().getResource("/").getPath()
					// .replace("WEB-INF/classes",
					// "resources/images/sjerr.png");
					// ByteArrayOutputStream os = new ByteArrayOutputStream();
					// archiveService.getPictureFromDB(request.getParameter("imagepath"),
					// errfilename, os);
					// byte[] data = os.toByteArray();
					// InputStream is = new ByteArrayInputStream(data);
					// imageOriginal = ImageIO.read(is);
					// }
					int widthOriginal = imageOriginal.getWidth(null);
					int heightOriginal = imageOriginal.getHeight(null);

					BufferedImage bi = new BufferedImage(widthOriginal, heightOriginal, BufferedImage.TYPE_3BYTE_BGR);
					Graphics2D g2d = bi.createGraphics();
					g2d.drawImage(imageOriginal, 0, 0, null);
					BufferedImage bu = null;
					if (degree.equals("flip")) {
						bu = ImageUtilsEx.flipImage(bi);
					} else {
						bu = ImageUtilsEx.flopImage(bi);
					}

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
					encoder.encode(bu);
					byte[] data = bos.toByteArray();

					// 更新文件内容
					fileSupportService.updateContent(path, data);

					// if (uploadType.equals("file")) { // 文件方式存储
					// FileOutputStream fos = new FileOutputStream(path);
					// JPEGImageEncoder encoder =
					// JPEGCodec.createJPEGEncoder(fos);
					// encoder.encode(bu);
					//
					// fos.flush();
					// fos.close();
					// fos = null;
					// } else { // 数据库方式存储
					// ByteArrayOutputStream bos = new ByteArrayOutputStream();
					// JPEGImageEncoder encoder =
					// JPEGCodec.createJPEGEncoder(bos);
					// encoder.encode(bu);
					// byte[] data = bos.toByteArray();
					// InputStream is = new ByteArrayInputStream(data);
					// archiveService.updatePictureToDB(request.getParameter("imagepath"),
					// is, data.length);
					//
					// bos.flush();
					// bos.close();
					// bos = null;
					//
					// is.close();
					// is = null;
					// }
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("错误：" + e.getMessage());
				}
			}
		}
	}

	// 公共方法
	// 下载附件
	@RequestMapping("/arcDownloadFj.do")
	public void arcDownloadFj(HttpServletRequest request, HttpServletResponse response) {
		int id = Integer.parseInt(request.getParameter("id"));
		Map<String, Object> map = this.archiveService.getArcfj(id);
		if (map == null) {
			return;
		}
		ServletOutputStream fos = null;
		FileInputStream fis = null;
		BufferedInputStream bfis = null;
		BufferedOutputStream bfos = null;

		try {
			File file = new File(map.get("filepath").toString());
			if (!file.exists()) {
				response.setCharacterEncoding("GBK");
				response.getWriter().write("<script>alert('文件不存在');window.history.back();</script>");
				return;
			}

			fos = response.getOutputStream();
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ new String(map.get("filename").toString().getBytes("GBK"), "ISO8859_1") + "\"");

			fis = new FileInputStream(file);
			bfis = new BufferedInputStream(fis);
			bfos = new BufferedOutputStream(fos);

			FileCopyUtils.copy(bfis, bfos);

		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (bfis != null) {
					bfis.close();
				}
				if (bfos != null) {
					bfos.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {

			}
		}
	}
}
