package com.ihidea.core;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.util.PropertyUtils;

public class CoreConstants {

	// 分页常量设置
	public final static String SUPPORT_LIMIT_PAGESIZE = getProperty("support.limit.pageSize");

	// 工作流引擎
	public final static String WORKFLOW_ENGINE = getProperty("workflow.engine");

	// 工作流 任务推送模式
	public final static String WORKFLOW_ASSIGN = getProperty("workflow.assign", "true");

	// EXCEL导出 系统设定行数
	public final static String JXL_EXPORT_ROWS = getProperty("jxl.export.rows", "true");

	// 服务器配置
	public final static String hostprotocol = getProperty("Host.protocol", "http");

	public final static String hostip = getProperty("Host.ip", "localhost");

	public final static String hostport = getProperty("Host.port", "7001");

	// 应用标识
	public final static String appId = getProperty("application.id", "");

	// 单位版标识
	public final static String appType = getProperty("application.type", "");

	// 应用标识
	public final static String appToken = getProperty("application.token", "");

	// 应用名称
	public final static String appName = getProperty("application.name", "");

	// ESB系统集成,路径设置
	public final static String preESBUrl = getProperty("pre.esburl", "esb/");

	// BI系统集成,路径设置
	public final static String preBiUrl = getProperty("pre.biurl", "hfmisbi/");

	// OA系统集成,路径设置
	public final static String preOaUrl = getProperty("pre.oaurl", "hfmisoa/");

	public final static String uploadDir = getProperty("upload.dir", "");

	//
	public final static String preDataUrl = getProperty("pre.dataurl");

	// 扫描控件 TWAINS
	public final static String twainHost = getProperty("twain.host", hostip + ":" + hostport);

	// 交易中间件Tuxedo
	public final static String useTuxedo = getProperty("tuxedo.use", "auto");

	// jcaptcha登录验证码校验
	public final static String jcaptcha = getProperty("jcaptcha.enabled", "true");

	// 是否启用mac地址校验,默认不启用
	public final static String macCheck = getProperty("macCheck.enabled", "false");

	// 用户最大重复登录数
	public final static String usersessionMax = getProperty("usersession.max", "0");

	// 业务删除是否需要授权审批
	public final static String confirmDelete = getProperty("config.confirm.delete", "false");

	// 报表模板文件保存方式
	public final static String reportUploadType = getProperty("report.upload.type", "file");

	// 是否启用系统定时任务
	public final static Boolean componentTaskEnable = "true".equals(getProperty("component.task.enable", "false"));

	// 不检查session的servlet
	public final static String[] SESSION_UNCHECK = getProperty("session.uncheck", "").split(",");

	// session失效跳转页面
	public final static String SESSION_OUTPAGE = getProperty("session.outpage", "logon.do");

	// 同步数据的表名
	public final static String[] SYNCDATA_TABLENAME = getProperty("syncData.tableName", "").split(",");

	// OAUTH认证服务器地址
	public final static String OAUTH_SERVER_URL = getProperty("oauth.server.url", "");

	// 启用cookies
	public final static Boolean SESSION_ENABLE_COOKIES = "true".equals(getProperty("session.enable.cookies", "false"));

	// 启用签名与时间戳检查
	public final static Boolean REQUEST_ENABLE_SIGN = "true".equals(getProperty("request.enable.sign", "false"));

	public final static String REQUEST_ENABLE_SIGN_KEY = getProperty("request.enable.sign.key", StringUtils.EMPTY);

	public static String getProperty(String key, String defaultValue) {
		return PropertyUtils.getProperty(key, defaultValue);
	}

	public static String getProperty(String key) {
		return PropertyUtils.getProperty(key);
	}
}
