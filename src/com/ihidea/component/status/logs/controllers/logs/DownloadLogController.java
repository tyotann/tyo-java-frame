/**
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package com.ihidea.component.status.logs.controllers.logs;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ihidea.component.status.logs.Utils;
import com.ihidea.component.status.logs.tools.logging.LogDestination;

/**
 * 下载日志
 */
@Controller
public class DownloadLogController extends AbstractLogHandlerController {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(DownloadLogController.class);

	@RequestMapping("/serverstatus/logs/download.do")
	public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequestInternal(request, response);
	}

	@Override
	protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
			LogDestination logDest) throws Exception {

		boolean compressed = "true".equals(ServletRequestUtils.getStringParameter(request, "compressed"));

		File file = logDest.getFile();
		logger.info("Sending {}{} to {} ({})", file, compressed ? " compressed" : "", request.getRemoteAddr(),
				request.getRemoteUser());
		if (compressed) {
			Utils.sendCompressedFile(request, response, file);
		} else {
			Utils.sendFile(request, response, file);
		}
		return null;
	}
}
