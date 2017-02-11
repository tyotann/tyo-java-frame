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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ihidea.component.status.logs.tools.logging.LogDestination;

/**
 * 显示日志详情页面
 */
@Controller
public class DetailLogController extends AbstractLogHandlerController {

	@RequestMapping("/serverstatus/logs/detail.do")
	public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequestInternal(request, response);
	}

	@Override
	protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
			LogDestination logDest) throws Exception {
		return new ModelAndView("/com/ihidea/component/status/logs/resources/detail").addObject("log", logDest);
	}
}
