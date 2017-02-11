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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.ihidea.component.status.logs.beans.LogResolverBean;
import com.ihidea.component.status.logs.tools.logging.LogDestination;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * The Class ListLogsController.
 */
@Controller
public class ListLogsController extends ParameterizableViewController {

	/** The error view. */
	private String errorView;

	/** The log resolver. */
	@Bean
	public LogResolverBean logResolver() {
        return new LogResolverBean();
    }
	//private LogResolverBean logResolver;

	/**
	 * Gets the error view.
	 *
	 * @return the error view
	 */
	public String getErrorView() {
		return errorView;
	}

	/**
	 * Sets the error view.
	 *
	 * @param errorView
	 *            the new error view
	 */
	public void setErrorView(String errorView) {
		this.errorView = errorView;
	}

	@RequestMapping("/serverstatus/logs/list.do")
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean showAll = ServletRequestUtils.getBooleanParameter(request, "apps", false);
		List<LogDestination> uniqueList = logResolver().getLogDestinations(showAll);
		if (uniqueList != null) {
			return new ModelAndView("/com/ihidea/component/status/logs/resources/list").addObject("logs", JSONUtilsEx.serialize(uniqueList));
		}
		return new ModelAndView("common/error/500");
	}

}
