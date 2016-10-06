package com.ihidea.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.support.session.SessionInfo;

/**
 * 涉及业务的共通
 * @author TYOTANN
 */
public class CommonUtilsEx {

	/**
	 * json转化成存储过程调用XML参数
	 * @param jsonStr
	 * @return
	 */
	public static String toXml(String jsonStr) throws Exception {

		String xml = XMLUtilsEx.serialize(JSONUtilsEx.deserialize(jsonStr, jsonStr.trim().indexOf("[") == 0 ? ArrayList.class
				: HashMap.class));

		// 加入分页
		PageLimit pl = PageLimitHolderFilter.getContext();

		if (pl != null) {
			xml = xml + "<limitEnable>" + String.valueOf(pl.limited()) + "</limitEnable>";

			if (pl.limited()) {
				xml = xml + "<startResult>" + String.valueOf(pl.getStartRowNo()) + "</startResult>";
				xml = xml + "<endResult>" + String.valueOf(pl.getEndRowNo()) + "</endResult>";
			}
		}

		xml = "<ywsj>" + xml + getSessionInfoXml() + "</ywsj>";
		return StringUtilsEx.filterSQL(xml);
	}

	/**
	 * map转化成存储过程调用XML参数
	 * @param map
	 * @return
	 */
	public static String toXml(Map<String, Object> param) throws Exception {

		// 加入分页
		PageLimit pl = PageLimitHolderFilter.getContext();

		if (pl != null) {
			param.put("limitEnable", String.valueOf(pl.limited()));

			if (pl.limited()) {
				param.put("startResult", String.valueOf(pl.getStartRowNo()));
				param.put("endResult", String.valueOf(pl.getEndRowNo()));
			}
		}

		return StringUtilsEx.filterSQL("<ywsj>" + XMLUtilsEx.serialize(param) + getSessionInfoXml() + "</ywsj>");
	}

	private static String getSessionInfoXml() {

		SessionInfo session = SessionContext.getSessionInfo();

		return session != null ? ("<userid>" + session.getUserId() + "</userid><appid>" + CoreConstants.appId + "</appid>") : "<appid>"
				+ CoreConstants.appId + "</appid>";
	}

}
