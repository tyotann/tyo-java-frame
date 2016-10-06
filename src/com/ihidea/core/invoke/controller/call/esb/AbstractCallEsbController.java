package com.ihidea.core.invoke.controller.call.esb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.invoke.controller.call.CallController;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.support.servlet.ServletHolderFilter;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.support.session.SessionInfo;
import com.ihidea.core.util.JSONUtilsEx;

public abstract class AbstractCallEsbController implements CallController {

	private Log logger = LogFactory.getLog(getClass());

	protected abstract ResultEntity request(Map<String, String> requestParam) throws Exception;

	/**
	 * 调用JAVA服务
	 * @param serviceName 服务名
	 * @param params 参数
	 * @return ResultEntity结果集
	 * @throws Exception 异常
	 */
	public ResultEntity call(String serviceName, Map<String, Object> params) throws Exception {

		ResultEntity result = null;

		// 去除服务名后的@ESB.xxx
		serviceName = StringUtils.substringBefore(serviceName, "@");

		// esb请求序号,必须存在
		String esbSeqno = ServletHolderFilter.getContext().getFrameParamMap().get("esbSeqno");

		if (StringUtils.isBlank(esbSeqno) && params.containsKey("esbSeqno")) {
			esbSeqno = String.valueOf(params.get("esbSeqno"));
		}

		if (StringUtils.isBlank(esbSeqno)) {
			throw new ServiceException("ESB请求时交易号不能为空!查看web调用时是否使用AE.ServiceEx,grid标准方式调用,或java调用时是否传入参数中还有esbSeqno!");
		}

		esbSeqno = CoreConstants.appToken + "#" + esbSeqno;

		try {

			Map<String, String> requestParam = new HashMap<String, String>();

			SessionInfo session = SessionContext.getSessionInfo();

			// 加入当前用户,appId,appToken
			{
				if (!params.containsKey("userid")) {
					params.put("userid", session.getUserId());
				}
				params.put("appid", CoreConstants.appId);
				params.put("appToken", CoreConstants.appToken);
			}

			// 加入分页,分页信息会被过滤器排除掉
			{
				PageLimit pl = PageLimitHolderFilter.getContext();

				if (pl != null && pl.limited()) {
					params.put("limit.count", String.valueOf(pl.getTotalCount()));
					params.put("limit.length", String.valueOf(pl.getPageLength()));
					params.put("limit.start", String.valueOf(pl.getCurrentPageNo()));
					params.put("limit.enable", true);
				}
			}

			// 合并CLOB
			{
				params.putAll(ServletHolderFilter.getContext().getClobParamMap());
			}

			// 加入ESB请求的交易号(APP_TOKEN#业务交易号[自身系统内唯一]),交易类别
			requestParam.put("FRAMEesbSeqno", esbSeqno);

			requestParam.put("FRAMEserviceName", serviceName);
			requestParam.put("FRAMEparams", JSONUtilsEx.serialize(params));

			logger.debug("请求ESB平台,交易号:[" + esbSeqno + "],服务名:[" + serviceName + "]开始,参数:[" + requestParam.get("FRAMEparams") + "]");

			result = request(requestParam);

			logger.debug("请求ESB平台,交易号:[" + esbSeqno + "],服务名:[" + serviceName + "]成功,结果代码:[" + result.getCode() + "]");

		} catch (Exception e) {
			logger.error("请求ESB平台交易号:[" + esbSeqno + "]发生系统异常", e);
			result = new ResultEntity(ResultEntity.RESULT_EXCEPTION, "请求ESB平台时发生系统异常:" + ExceptionUtils.getMessage(e));
		}

		logger.debug("请求ESB平台交易号:[" + esbSeqno + "]正常结束");

		return result;
	}

}
