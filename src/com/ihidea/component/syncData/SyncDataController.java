package com.ihidea.component.syncData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.support.exception.ServiceWarn;
import com.ihidea.core.util.ServletUtilsEx;

@Controller
public class SyncDataController {

	@Autowired
	private SyncDataService service;

	@SuppressWarnings("unchecked")
	@RequestMapping("/syncData.do")
	public void sycData(HttpServletRequest request, HttpServletResponse response) {

		ResultEntity resultEntity = new ResultEntity();

		try {
			service.syncData(request.getParameterMap());
			resultEntity.setCode("0");

		} catch (ServiceWarn e) {
			resultEntity.setCode("9");
			resultEntity.setText("同步出现警告：" + e.getMessage());
		} catch (Exception e) {
			resultEntity.setCode("-1");
			resultEntity.setText("同步出现异常：" + e.getMessage());
		}

		ServletUtilsEx.renderJson(response, resultEntity);
	}
}
