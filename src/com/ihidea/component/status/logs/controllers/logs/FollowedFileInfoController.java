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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ihidea.component.status.logs.tools.logging.LogDestination;

/**
 * 获取日志文件的信息
 */
@Controller
public class FollowedFileInfoController extends AbstractLogHandlerController {

	@RequestMapping(value = "/serverstatus/logs/fileInfo.do", produces = { "application/json;charset=UTF-8" })
	public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequestInternal(request, response);
	}

	@Override
	protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
			LogDestination logDest) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("path", logDest.getFile().getAbsolutePath());
		result.put("size", logDest.getSize());
		result.put("lastModified", logDest.getLastModified());
		sendJson(response, result);
		return null;
	}

}
