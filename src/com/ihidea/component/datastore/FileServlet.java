package com.ihidea.component.datastore;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.support.SpringContextLoader;

public class FileServlet extends HttpServlet {

	protected Log logger = LogFactory.getLog(FileServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String id = request.getPathInfo().substring(1);

		String fileId = id, fileImgSize = null, downloadFlag = null, mineType = null;

		if (StringUtils.isNotBlank(id)) {

			// 兼容编码后的参数,主要是七牛云
			// ae1f7c89-7e7e-4a29-a284-1c8c284b1e0e?fileImgSize=20x20
			if (id.indexOf("?") > 0) {
				fileId = id.substring(0, id.indexOf("?"));

				int _i = -1;
				if ((_i = id.indexOf("fileImgSize=")) > 0) {
					int _j = id.indexOf("&", _i);
					fileImgSize = id.substring(_i + "fileImgSize=".length(), _j == -1 ? id.length() : _j);
				}

				if ((_i = id.indexOf("downloadFlag=")) > 0) {
					int _j = id.indexOf("&", _i);
					downloadFlag = id.substring(_i + "downloadFlag=".length(), _j == -1 ? id.length() : _j);
				}

				if ((_i = id.indexOf("mineType=")) > 0) {
					int _j = id.indexOf("&", _i);
					mineType = id.substring(_i + "mineType=".length(), _j == -1 ? id.length() : _j);
				}

			} else {

				fileImgSize = request.getParameter("fileImgSize");

				downloadFlag = request.getParameter("downloadFlag");

				mineType = request.getParameter("mineType");
			}

		} else {
			response.sendError(404);
			return;
		}

		FileController fileController = SpringContextLoader.getBean(FileController.class);

		try {
			fileController.download(request, response, fileId, downloadFlag, fileImgSize, mineType);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

	public void init() throws ServletException {
	}

}
