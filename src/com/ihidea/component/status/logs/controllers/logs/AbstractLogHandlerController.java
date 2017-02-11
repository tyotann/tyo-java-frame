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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.ihidea.component.status.logs.beans.LogResolverBean;
import com.ihidea.component.status.logs.tools.logging.LogDestination;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * The Class AbstractLogHandlerController.
 */
public abstract class AbstractLogHandlerController extends ParameterizableViewController {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(AbstractLogHandlerController.class);

	/** The log resolver. */
	@Bean
	public LogResolverBean logResolver() {
        return new LogResolverBean();
    }
	//private LogResolverBean logResolver;


	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String logType = ServletRequestUtils.getStringParameter(request, "logType");
		String webapp = ServletRequestUtils.getStringParameter(request, "webapp");
		boolean context = ServletRequestUtils.getBooleanParameter(request, "context", false);
		boolean root = ServletRequestUtils.getBooleanParameter(request, "root", false);
		String logName = ServletRequestUtils.getStringParameter(request, "logName");
		String logIndex = ServletRequestUtils.getStringParameter(request, "logIndex");

		LogDestination dest = logResolver().getLogDestination(logType, webapp, context, root, logName, logIndex);

		ModelAndView modelAndView = null;
		boolean logFound = false;
		if (dest != null) {
			if (dest.getFile() != null && dest.getFile().exists()) {
				modelAndView = handleLogFile(request, response, dest);
				logFound = true;
			} else {
				logger.error("{}: file not found", dest.getFile());
			}
		} else {
			logger.error("{}{} log{} not found", logType, root ? " root" : "", root ? "" : " '" + logName + "'");
		}
		if (!logFound) {
			response.sendError(404);
		}
		return modelAndView;
	}

	/**
	 * Handle log file.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param logDest
	 *            the log dest
	 * @return the model and view
	 * @throws Exception
	 *             the exception
	 */
	protected abstract ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
			LogDestination logDest) throws Exception;
	
	/**
	 * 
	 * @描述	
	 * @author liuyulong
	 * @param 
	 * @return void
	 * @throws IOException 
	 * @date 2017-02-06 09:01:59
	 */
	protected void sendJson(HttpServletResponse response, Object obj) throws IOException {
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write(JSONUtilsEx.serialize(obj));
		writer.flush();
		writer.close();
	}

}
