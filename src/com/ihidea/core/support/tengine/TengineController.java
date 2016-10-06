package com.ihidea.core.support.tengine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.core.util.ServletUtilsEx;

/**
 * 集群的监控检查
 * @author TYOTANN
 */
@Controller
public class TengineController {

	@RequestMapping("/healthCheck.do")
	public void uploadReportEx(HttpServletRequest resquest, HttpServletResponse response) throws Exception {

		resquest.getSession().invalidate();
		ServletUtilsEx.renderText(response, "success");
	}

}
