package com.ihidea.component.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.component.datastore.fileio.FileIoEntity;
import com.ihidea.component.datastore.fileio.IFileInputStream;
import com.ihidea.core.base.CoreController;
import com.ihidea.core.support.cache.CacheSupport;
import com.ihidea.core.support.servlet.ServletHolderFilter;
import com.ihidea.core.util.FileUtilsEx;
import com.ihidea.core.util.ImageUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;

@Controller
public class FileController extends CoreController {

	@Autowired
	private FileSupportService service;

	private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

	private static Map<String, String> contentTypeMap = new HashMap<String, String>();

	static {

		contentTypeMap.put("html", "text/html");
		contentTypeMap.put("htm", "text/html");
		contentTypeMap.put("shtml", "text/html");
		contentTypeMap.put("apk", "application/vnd.android.package-archive");
		contentTypeMap.put("sis", "application/vnd.symbian.install");
		contentTypeMap.put("sisx", "application/vnd.symbian.install");
		contentTypeMap.put("exe", "application/x-msdownload");
		contentTypeMap.put("msi", "application/x-msdownload");
		contentTypeMap.put("css", "text/css");
		contentTypeMap.put("xml", "text/xml");
		contentTypeMap.put("gif", "image/gif");
		contentTypeMap.put("jpeg", "image/jpeg");
		contentTypeMap.put("jpg", "image/jpeg");
		contentTypeMap.put("js", "application/x-javascript");
		contentTypeMap.put("atom", "application/atom+xml");
		contentTypeMap.put("rss", "application/rss+xml");
		contentTypeMap.put("mml", "text/mathml");
		contentTypeMap.put("txt", "text/plain");
		contentTypeMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
		contentTypeMap.put("wml", "text/vnd.wap.wml");
		contentTypeMap.put("htc", "text/x-component");
		contentTypeMap.put("png", "image/png");
		contentTypeMap.put("tif", "image/tiff");
		contentTypeMap.put("tiff", "image/tiff");
		contentTypeMap.put("wbmp", "image/vnd.wap.wbmp");
		contentTypeMap.put("ico", "image/x-icon");
		contentTypeMap.put("jng", "image/x-jng");
		contentTypeMap.put("bmp", "image/x-ms-bmp");
		contentTypeMap.put("svg", "image/svg+xml");
		contentTypeMap.put("jar", "application/java-archive");
		contentTypeMap.put("war", "application/java-archive");
		contentTypeMap.put("ear", "application/java-archive");
		contentTypeMap.put("doc", "application/msword");
		contentTypeMap.put("pdf", "application/pdf");
		contentTypeMap.put("rtf", "application/rtf");
		contentTypeMap.put("xls", "application/vnd.ms-excel");
		contentTypeMap.put("ppt", "application/vnd.ms-powerpoint");
		contentTypeMap.put("7z", "application/x-7z-compressed");
		contentTypeMap.put("rar", "application/x-rar-compressed");
		contentTypeMap.put("swf", "application/x-shockwave-flash");
		contentTypeMap.put("rpm", "application/x-redhat-package-manager");
		contentTypeMap.put("der", "application/x-x509-ca-cert");
		contentTypeMap.put("pem", "application/x-x509-ca-cert");
		contentTypeMap.put("crt", "application/x-x509-ca-cert");
		contentTypeMap.put("xhtml", "application/xhtml+xml");
		contentTypeMap.put("zip", "application/zip");
		contentTypeMap.put("mid", "audio/midi");
		contentTypeMap.put("midi", "audio/midi");
		contentTypeMap.put("kar", "audio/midi");
		contentTypeMap.put("mp3", "audio/mpeg");
		contentTypeMap.put("ogg", "audio/ogg");
		contentTypeMap.put("m4a", "audio/x-m4a");
		contentTypeMap.put("ra", "audio/x-realaudio");
		contentTypeMap.put("3gpp", "video/3gpp");
		contentTypeMap.put("3gp", "video/3gpp");
		contentTypeMap.put("mp4", "video/mp4");
		contentTypeMap.put("mpeg", "video/mpeg");
		contentTypeMap.put("mpg", "video/mpeg");
		contentTypeMap.put("mov", "video/quicktime");
		contentTypeMap.put("flv", "video/x-flv");
		contentTypeMap.put("m4v", "video/x-m4v");
		contentTypeMap.put("mng", "video/x-mng");
		contentTypeMap.put("asx", "video/x-ms-asf");
		contentTypeMap.put("asf", "video/x-ms-asf");
		contentTypeMap.put("wmv", "video/x-ms-wmv");
		contentTypeMap.put("avi", "video/x-msvideo");
	}

	/**
	 * 上传文件
	 * 
	 * @param request
	 * @param response
	 * @param resultFlag
	 *            返回结果，2：手机端上传，返回手机端需要格式
	 * @param filePath
	 *            文件保存路径+文件名，如：stcharge/userid213123/sdf.jpg
	 *
	 **/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/uploadFile.do")
	public void upload(HttpServletRequest request, HttpServletResponse response, String fileImgSize, String storeName, Integer resultFlag, String filePath) {

		Map<String, Object> result = new HashMap<String, Object>();

		try {
			Map<String, Object> param = ServletHolderFilter.getContext().getParamMap();

			if (param.containsKey("resultFlag")) {
				resultFlag = Integer.valueOf(String.valueOf(param.get("resultFlag")));
			}

			List<Object[]> fileList = new ArrayList<Object[]>();

			// 根据里面参数自动判断
			for (String nameKey : param.keySet()) {

				Object _obj = param.get(nameKey);

				if (_obj != null && _obj instanceof List && ((List) _obj).size() > 0) {

					Object fileItem = ((List) _obj).get(0);

					if (fileItem instanceof DiskFileItem) {

						// 如果是servlet2上传的文件
						List<DiskFileItem> diskFileItemList = (List<DiskFileItem>) _obj;

						for (DiskFileItem file : diskFileItemList) {

							String fileName = FileUtilsEx.getFileNameByPath(file.getName());

							byte[] fileContent = file.get();

							fileList.add(new Object[] { fileName, fileContent });
						}
					} else if (fileItem instanceof Object[] && ((Object[]) fileItem).length == 2
							&& ((Object[]) fileItem)[0] instanceof String && ((Object[]) fileItem)[1] instanceof byte[]) {

						// 如果是servlet3上传的文件
						List<Object[]> diskFileItemList = (List<Object[]>) _obj;

						for (Object[] file : diskFileItemList) {
							fileList.add(file);
						}
					}
				}
			}

			if (fileList.size() > 0) {
				List<String> fileIdList = new ArrayList<String>();

				for (Object[] file : fileList) {
					fileIdList.add(service.add((String) file[0], (byte[]) file[1], StringUtils.isBlank(storeName) ? "ds_upload" : storeName,
							fileImgSize, filePath));
				}

				// 返回文件id
				result.put("fileIdList", fileIdList);

				// TODO 如果是kindeditor,则需要返回以下2个参数,后期考虑分离开
				if (resultFlag != null && resultFlag == 3) {
					result.put("error", 0); // 只能用数字
					result.put("url", "download/" + fileIdList.get(0));
				}
			} else {
				logger.error("上传文件没有接受到文件!");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("errorMsg", e.getMessage());
		}

		// TODO 后面这里需要统一
		if (resultFlag == null) {
			ServletUtilsEx.renderJson(response, result);
		} else if (resultFlag == 2) {
			ServletUtilsEx.renderText(response, "{\"code\":\"200\",\"text\":null,\"data\":" + JSONUtilsEx.serialize(result) + "}");
		} else if (resultFlag == 3) {
			ServletUtilsEx.renderJson(response, result);
		}

	}

	/**
	 * 删除文件
	 * 
	 * @param fileId
	 */
	@RequestMapping(value = "/deleteFile.do")
	public void delete(String fileId) {
		service.remove(fileId);
	}

	/**
	 * <pre>
	 * 文件下载页面
	 * 
	 * 使用方式：
	 * 页面定义<a href="downloadFile.do?id="+文件id  target="blank">文件名</a>
	 * 
	 * </pre>
	 * 
	 * @param id
	 *            blob表id
	 * @param downloadFlag
	 *            默认下载
	 * @param response
	 * @throws Exception
	 */
	@Deprecated
	@RequestMapping("/downloadFile.do")
	public void downloadFile(HttpServletRequest request, String id, String downloadFlag, HttpServletResponse response, String fileImgSize,
			String mineType) throws Exception {
		download(request, response, id, downloadFlag, fileImgSize, mineType);
	}

	private List<Range> getRange(HttpServletRequest request, HttpServletResponse response, FileIoEntity file) throws IOException {

		long length = file.getDataInfo().getFileSize().longValue();

		long lastModified = file.getDataInfo().getCreateDate().getTime();

		String eTag = file.getDataInfo().getFileName() + "_" + length + "_" + lastModified;

		// Prepare some variables. The full Range represents the complete file.
		Range full = new Range(0, length - 1, length);
		List<Range> ranges = new ArrayList<Range>();

		// Validate and process Range and If-Range headers.
		String range = request.getHeader("Range");
		if (range != null) {

			// Range header should match format "bytes=n-n,n-n,n-n...". If not,
			// then return 416.
			if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
				response.setHeader("Content-Range", "bytes */" + length); // Required
																			// in
																			// 416.
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return null;
			}

			// If-Range header should either match ETag or be greater then
			// LastModified. If not,
			// then return full file.
			String ifRange = request.getHeader("If-Range");
			if (ifRange != null && !ifRange.equals(eTag)) {
				try {
					long ifRangeTime = request.getDateHeader("If-Range"); // Throws
																			// IAE
																			// if
																			// invalid.
					if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
						ranges.add(full);
					}
				} catch (IllegalArgumentException ignore) {
					ranges.add(full);
				}
			}

			// If any valid If-Range header, then process each part of byte
			// range.
			if (ranges.isEmpty()) {
				for (String part : range.substring(6).split(",")) {
					// Assuming a file with length of 100, the following
					// examples returns bytes at:
					// 50-80 (50 to 80), 40- (40 to length=100), -20
					// (length-20=80 to length=100).
					long start = sublong(part, 0, part.indexOf("-"));
					long end = sublong(part, part.indexOf("-") + 1, part.length());

					if (start == -1) {
						start = length - end;
						end = length - 1;
					} else if (end == -1 || end > length - 1) {
						end = length - 1;
					} else if (start == end) {
						start = 0;
					}

					// Check if Range is syntactically valid. If not, then
					// return 416.
					if (start > end) {
						response.setHeader("Content-Range", "bytes */" + length); // Required
																					// in
																					// 416.
						response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return null;
					}

					// Add range.
					ranges.add(new Range(start, end, length));
				}
			}
		}

		return ranges;
	}

	private String getContentType(String fileName) {

		String contentType = "application/octet-stream";

		if (fileName.lastIndexOf(".") < 0) {
			return contentType;
		}

		fileName = FileUtilsEx.getSuffix(fileName).toLowerCase();

		if (contentTypeMap.containsKey(fileName)) {
			contentType = contentTypeMap.get(fileName);
		}

		return contentType;
	}

	private static long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	protected class Range {
		long start;

		long end;

		long length;

		long total;

		public Range(long start, long end, long total) {
			this.start = start;
			this.end = end;
			this.length = end - start + 1;
			this.total = total;
		}
	}

	protected void download(final HttpServletRequest request, final HttpServletResponse response, String id, final String downloadFlag,
			final String fileImgSize, final String mineType) throws Exception {

		// 判断如果存在真实地址配置，则重定向至真实地址
		String realPath = service.getRealPathById(id, fileImgSize);
		if(realPath != null){
			
			if(realPath.indexOf("?") < 0){
				// 拼接参数
				String paramsStr = "";
				Map<String, String[]> requestParams = request.getParameterMap();

				for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
					String name = iter.next();
					String[] values = (String[]) requestParams.get(name);
					String valueStr = "";
					for (int i = 0; i < values.length; i++) {
						valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
					}
					paramsStr += "&" + name + "=" + valueStr;
				}
				response.sendRedirect(realPath + (paramsStr == "" ? "" : "?" + paramsStr.substring(1, paramsStr.length())));
				return ;
			}else{
				response.sendRedirect(realPath);
				return ;
			}
		}
		
		service.execute(id, new IFileInputStream() {

			public void execute(FileIoEntity entity, InputStream is) throws Exception {

				response.reset();

				ServletOutputStream os = response.getOutputStream();

				try {

					if (entity != null && is != null) {

						// TODO String previousToken =
						// servletRequest.getHeader("If-None-Match");
						response.setHeader("ETag", entity.getDataInfo().getId() + "_" + entity.getDataInfo().getCreateDate().getTime());

						response.setDateHeader("Last-Modified", entity.getDataInfo().getCreateDate().getTime());

						// 1周失效
						response.setDateHeader("Expires", System.currentTimeMillis() + 604800000L);

						String contentType = StringUtils.isNotBlank(mineType) ? mineType
								: getContentType(entity.getDataInfo().getFileName());

						// 如果没有设置flag或为0，则下载,否则直接打印在窗口上
						String disposition = (StringUtils.isBlank(downloadFlag) || "true".equals(downloadFlag)) ? "attachment" : "";

						response.setHeader("Content-Disposition",
								disposition + ";filename=" + URLEncoder.encode(entity.getDataInfo().getFileName(), "UTF-8"));

						// 支持range
						List<Range> ranges = getRange(request, response, entity);

						try {

							// 如果启用压缩图片,则先从缓存中获取
							if (StringUtils.isNotBlank(fileImgSize)) {

								byte[] imgBytes = CacheSupport.get("imageCache", entity.getDataInfo().getId() + "#" + fileImgSize,
										byte[].class);

								if (ArrayUtils.isEmpty(imgBytes)) {

									String[] sizeArray = fileImgSize.replace(",", "|").replace("x", "|").split("\\|");

									imgBytes = ImageUtilsEx.resizeImage(is, Integer.valueOf(sizeArray[0]), Integer.valueOf(sizeArray[1]),
											FileUtilsEx.getSuffix(entity.getDataInfo().getFileName()));

									CacheSupport.put("imageCache", entity.getDataInfo().getId() + "#" + fileImgSize, imgBytes);
								}

								response.setContentType(contentType);
								response.setHeader("Content-Range",
										"bytes " + 0L + "-" + String.valueOf(imgBytes.length) + "/" + String.valueOf(imgBytes.length));
								response.addHeader("Content-Length", String.valueOf(imgBytes.length));

								IOUtils.write(imgBytes, os);

							} else if (ranges.size() == 0) {
								response.setContentType(contentType);
								response.setHeader("Content-Range", "bytes " + 0L + "-" + entity.getDataInfo().getFileSize().longValue()
										+ "/" + entity.getDataInfo().getFileSize().longValue());
								response.addHeader("Content-Length", String.valueOf(entity.getDataInfo().getFileSize().longValue()));

								byte[] buff = new byte[307200];
								int cnt = 0;
								while ((cnt = is.read(buff)) != -1) {
									IOUtils.write(buff, os);
								}

							} else if (ranges.size() == 1) {

								response.setContentType(contentType);
								Range r = ranges.get(0);
								response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
								response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

								if (r.length < 2147483647L) {
									response.setContentLength((int) r.length);
								} else {
									response.setHeader("Content-Length", String.valueOf(r.length));
								}

								is.skip(r.start);

								byte[] buff = new byte[307200];
								int cnt = 0;
								while ((cnt = is.read(buff)) != -1) {
									IOUtils.write(buff, os);
								}
							} else {

								// TODO 未测试
								response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
								response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

								for (Range r : ranges) {
									os.println();
									os.println("--" + MULTIPART_BOUNDARY);
									os.println("Content-Type: " + contentType);
									os.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

									is.skip(r.start);

									byte[] buff = new byte[307200];
									int cnt = 0;
									while ((cnt = is.read(buff)) != -1) {
										IOUtils.write(buff, os);
									}
								}

								os.println();
								os.println("--" + MULTIPART_BOUNDARY + "--");
							}

							os.flush();
						} catch (Exception e) {
						}
					} else {
						response.sendError(404);
					}
				} catch (Exception e) {
					logger.debug(e.getMessage(), e);
				} catch (Error e) {
					logger.debug(e.getMessage(), e);
				} finally {
					if (os != null) {
						os.close();
					}
				}

			}
		});

	}
}
