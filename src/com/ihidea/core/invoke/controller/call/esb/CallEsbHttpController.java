package com.ihidea.core.invoke.controller.call.esb;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * 使用HTTP协议调用ESB请求
 * @author TYOTANN
 */
@Component
public class CallEsbHttpController extends AbstractCallEsbController {

	@SuppressWarnings("unchecked")
	@Override
	protected ResultEntity request(Map<String, String> requestParam) throws Exception {

		String esbResult = HttpClientUtils.post(CoreConstants.preESBUrl + "esb.do", requestParam, "UTF-8", "UTF-8");

		// 没有得到JSON数据
		if (!esbResult.startsWith("[") && !esbResult.startsWith("{")) {
			return new ResultEntity("-1", "请求ESB服务异常:" + esbResult);
		}

		Map<String, Object> esbResultMap = JSONUtilsEx.deserialize(esbResult, Map.class);

		// 设置结果
		ResultEntity result = new ResultEntity((String) esbResultMap.get("code"), (String) esbResultMap.get("text"),
				esbResultMap.get("data"));

		// 设置分页信息
		if (PageLimitHolderFilter.getContext() != null && PageLimitHolderFilter.getContext().limited()) {
			PageLimitHolderFilter.getContext().setTotalCount(
					(Integer) (((Map<String, Object>) esbResultMap.get("pageLimit")).get("totalCount")));
		}

		return result;
	}
}
