package com.ihidea.core.invoke.controller.call.esb;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.model.CptDataStore;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * 使用文件方式,调用ESB请求
 * @author TYOTANN
 */
@Component
public class CallEsbFileController extends AbstractCallEsbController {

	@Autowired
	private DataStoreService dataStoreService;

	@SuppressWarnings("unchecked")
	@Override
	protected ResultEntity request(Map<String, String> requestParam) throws Exception {

		ResultEntity result = null;

		String fileName = requestParam.get("FRAMEesbSeqno");

		// 目录夹=定义的ds_esb_file+日期
		CptDataStore dataStore = dataStoreService.getInfoByName("ds_esb_file");

		if (dataStore == null) {
			throw new ServiceException("请在'组件管理-文件存储'页面,配置ESB文件协议[ds_esb_file]!");
		}

		String dir = dataStore.getPath() + DateUtilsEx.formatToString(new Date(), DateUtilsEx.DATE_FORMAT_DAY) + File.separator;

		// 创建文件
		File requestFile = new File(dir + fileName + ".request");

		// 结果文件
		File responseFile = new File(dir + fileName + ".response");

		if (requestFile.exists() || responseFile.exists()) {
			throw new ServiceException("ESB请求时交易号:" + fileName + "发现重复!");
		}

		// 写入File协议的请求文件
		FileUtils.writeStringToFile(requestFile, JSONUtilsEx.serialize(requestParam), "UTF-8");

		long startDate = (new Date()).getTime();

		// 设置ESB请求超时时间 20秒
		while ((new Date()).getTime() - startDate < 20000) {

			if (responseFile.exists()) {

				String esbResult = FileUtils.readFileToString(responseFile, "UTF-8");

				Map<String, Object> esbResultMap = JSONUtilsEx.deserialize(esbResult, Map.class);

				// 设置结果
				result = new ResultEntity((String) esbResultMap.get("code"), (String) esbResultMap.get("text"), esbResultMap.get("data"));

				// 设置分页信息
				if (PageLimitHolderFilter.getContext() != null && PageLimitHolderFilter.getContext().limited()) {
					PageLimitHolderFilter.getContext().setTotalCount(
							(Integer) (((Map<String, Object>) esbResultMap.get("pageLimit")).get("totalCount")));
				}

				return result;
			}

			// 1s每次轮训
			Thread.sleep(1000);
		}

		return new ResultEntity("-1", "ESB请求超时!请重试!");
	}
}
