package com.ihidea.component.oauth2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ihidea.component.oauth2.dao.OauthUserMapper;
import com.ihidea.component.oauth2.dao.model.OauthUser;
import com.ihidea.component.oauth2.dao.model.OauthUserCriteria;
import com.ihidea.component.oauth2.model.OAuth2Info;
import com.ihidea.component.oauth2.model.OAuth2UserInfo;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.orm.mybatis3.util.IbatisServiceUtils;
import com.ihidea.core.util.BeanUtilsEx;
import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

@Service
public class OAuth2ClientService {

	private Log logger = LogFactory.getLog(OAuth2ClientService.class);

	@Autowired
	private OauthUserMapper oauthUserDao;

	@SuppressWarnings({ "unchecked" })
	public OAuth2Info getOAuthInfo(String hostUrl, String accessToken, String refreshToken) throws Exception {

		OAuth2Info result = getOAuthInfoByAccessToken(hostUrl, accessToken, refreshToken);

		// accessToken失效的话，再请求accessToken
		if (result == null && StringUtils.isNotBlank(refreshToken)) {

			Map<String, String> params = new HashMap<String, String>();

			params.put("accessToken", accessToken);
			params.put("refreshToken", refreshToken);

			String oauthUrl = StringUtils.isBlank(CoreConstants.OAUTH_SERVER_URL) ? hostUrl + "oauth/" : CoreConstants.OAUTH_SERVER_URL;

			String accessTokenResponse = HttpClientUtils.post(oauthUrl + "refreshToken.do", params);

			// 如果没有请求到accessToken,则退出
			if (StringUtils.isBlank(accessTokenResponse) || !"{".equals(accessTokenResponse.substring(0, 1))) {
				return null;
			}

			ResultEntity resultEntity = JSONUtilsEx.deserialize(accessTokenResponse, ResultEntity.class);

			if ("0".equals(resultEntity.getCode())) {
				accessToken = ((Map<String, String>) resultEntity.getData()).get("accessToken");
				result = getOAuthInfoByAccessToken(hostUrl, accessToken, refreshToken);
			} else {
				result = new OAuth2Info();
				result.setCode(resultEntity.getCode());
				result.setText(resultEntity.getText());
			}
		} else {
			logger.debug("accessToken:[" + accessToken + "]已失效");
		}

		return result;
	}

	/**
	 * 得到OAuth信息
	 * @param accessToken
	 * @param refreshToken
	 * @throws Exception
	 */
	public OAuth2Info getOAuthInfo(HttpServletRequest request) throws Exception {

		String accessToken = request.getParameter("accessToken");

		String refreshToken = request.getParameter("refreshToken");

		return getOAuthInfo(ServletUtilsEx.getHostURL(request), accessToken, refreshToken);
	}

	private OAuth2Info getOAuthInfoByAccessToken(String hostUrl, String accessToken, String refreshToken) throws Exception {

		OAuth2Info oauthInfo = new OAuth2Info();

		Map<String, String> params = new HashMap<String, String>();

		params.put("accessToken", accessToken);
		params.put("refreshToken", refreshToken);

		String oauthUrl = StringUtils.isBlank(CoreConstants.OAUTH_SERVER_URL) ? hostUrl + "oauth/" : CoreConstants.OAUTH_SERVER_URL;

		// 通过accessToken得到用户信息
		String result = HttpClientUtils.post(oauthUrl + "access.do", params);

		ResultEntity resultEntity = JSONUtilsEx.deserialize(result, ResultEntity.class);

		// null的话为请求失败，-2为accessToken失效
		if (resultEntity == null || "-2".equals(resultEntity.getCode())) {
			return null;
		} else if ("0".equals(resultEntity.getCode())) {

			oauthInfo.setCode("0");

			oauthInfo.setRefreshToken(refreshToken);
			oauthInfo.setAccessToken(accessToken);

			OAuth2UserInfo userInfo = new OAuth2UserInfo();
			BeanUtilsEx.copyProperties(resultEntity.getData(), userInfo);

			if (userInfo.getStatus().intValue() != 1) {
				oauthInfo.setCode("-1");
				oauthInfo.setText("用户未激活!");
			} else {
				oauthInfo.setUserInfo(userInfo);
			}
		} else {
			oauthInfo.setCode("-1");
			oauthInfo.setText(resultEntity.getText());
		}

		return oauthInfo;
	}

	/**
	 * 添加用户
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public String addUser(OAuth2UserInfo userInfo) throws Exception {

		if (StringUtils.isBlank(userInfo.getEmail())) {
			throw new ServiceException("邮箱不能为空,请输入正确的邮箱地址!");
		}

		// 检查账号唯一性
		checkLoginAccount(userInfo.getLoginName(), userInfo.getEmail());

		OauthUser entity = new OauthUser();
		entity.setLoginName(userInfo.getLoginName());
		entity.setOpenId(StringUtilsEx.getUUID());

		entity.setPasswordSalt(StringUtilsEx.getUUID());
		entity.setLoginPassword(StringUtilsEx.md5(userInfo.getLoginPwd(), entity.getPasswordSalt()));

		entity.setUserName(userInfo.getUserName());
		entity.setNickname(userInfo.getNickname());

		entity.setPhone(userInfo.getPhone());
		entity.setEmail(userInfo.getEmail());

		entity.setPosition(userInfo.getPosition());

		// tyotann 20130911 添加QQ，生日，所在地，所属公司字段
		entity.setQq(userInfo.getQq());
		entity.setCompany(userInfo.getCompany());
		entity.setBirthday(userInfo.getBirthday());
		entity.setAddress(userInfo.getAddress());

		// 新增用户的话直接激活
		entity.setStatus(BigDecimal.ONE);

		IbatisServiceUtils.insert(entity, oauthUserDao);

		return entity.getOpenId();
	}

	/**
	 * 添加用户
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public void updateUserInfo(OAuth2UserInfo userInfo) throws Exception {

		if (StringUtils.isBlank(userInfo.getOpenId())) {
			throw new ServiceException("更新用户信息时,用户openId不能为空!");
		}

		OauthUser whereEntity = new OauthUser();
		whereEntity.setOpenId(userInfo.getOpenId());

		OauthUser entity = new OauthUser();

		// 修改登录名
		if (StringUtils.isNotBlank(userInfo.getLoginName())) {

			if (userInfo.getLoginName().indexOf("@") > -1) {
				throw new ServiceException("用户登录名中不能出现@符号!");
			}

			if (!accountExists(userInfo.getLoginName(), userInfo.getOpenId())) {
				entity.setLoginName(userInfo.getLoginName());
			} else {
				throw new ServiceException("用户登录名已存在!");
			}
		}

		// 修改用户名
		if (StringUtils.isNotBlank(userInfo.getUserName())) {
			entity.setUserName(userInfo.getUserName());
		} else {
			entity.setUserName(StringUtils.EMPTY);
		}

		// 修改昵称
		if (StringUtils.isNotBlank(userInfo.getNickname())) {
			entity.setNickname(userInfo.getNickname());
		} else {
			entity.setNickname(StringUtils.EMPTY);
		}

		// 修改联系电话
		if (StringUtils.isNotBlank(userInfo.getPhone())) {
			entity.setPhone(userInfo.getPhone());
		} else {
			entity.setPhone(StringUtils.EMPTY);
		}

		// 修改职位
		if (StringUtils.isNotBlank(userInfo.getPosition())) {
			entity.setPosition(userInfo.getPosition());
		} else {
			entity.setPosition(StringUtils.EMPTY);
		}

		// 修改QQ
		if (StringUtils.isNotBlank(userInfo.getQq())) {
			entity.setQq(userInfo.getQq());
		} else {
			entity.setQq(StringUtils.EMPTY);
		}

		// 修改生日
		if (StringUtils.isNotBlank(userInfo.getBirthday())) {
			entity.setBirthday(userInfo.getBirthday());
		} else {
			entity.setBirthday(StringUtils.EMPTY);
		}

		// 修改所在地
		if (StringUtils.isNotBlank(userInfo.getAddress())) {
			entity.setAddress(userInfo.getAddress());
		} else {
			entity.setAddress(StringUtils.EMPTY);
		}

		// 修改所属公司
		if (StringUtils.isNotBlank(userInfo.getCompany())) {
			entity.setCompany(userInfo.getCompany());
		} else {
			entity.setCompany(StringUtils.EMPTY);
		}

		IbatisServiceUtils.update(entity, whereEntity, oauthUserDao);
	}

	/**
	 * 判断登录名(邮箱或登录名)是否存在
	 * @param loginName
	 * @return
	 */
	public void checkLoginAccount(String loginName, String email) {

		if (StringUtils.isNotBlank(loginName) && loginName.indexOf("@") > -1) {
			throw new ServiceException("登录名:[" + loginName + "]不能存在@符号!");
		}

		OauthUser entity = null;
		if (StringUtils.isNotBlank(loginName)) {

			entity = new OauthUser();

			entity.setLoginName(loginName);

			List<OauthUser> userList = IbatisServiceUtils.find(entity, oauthUserDao);

			if (userList != null && userList.size() > 0) {
				throw new ServiceException("登录名:[" + loginName + "]已存在!");
			}
		}

		entity = new OauthUser();
		entity.setEmail(email);

		List<OauthUser> userList = IbatisServiceUtils.find(entity, oauthUserDao);

		if (userList != null && userList.size() > 0) {
			throw new ServiceException("邮箱名:[" + email + "]已存在!");
		}
	}

	/**
	 * 判断账号是否唯一
	 * @param account
	 * @return
	 */
	public boolean accountExists(String account) {
		return accountExists(account, null);
	}

	/**
	 * 判断账号是否唯一
	 * @param account
	 * @param excludeOpenId 排除的openId
	 * @return
	 */
	private boolean accountExists(String account, String excludeOpenId) {

		OauthUserCriteria ouc = new OauthUserCriteria();
		OauthUserCriteria.Criteria criteria = ouc.createCriteria();

		if (account.indexOf("@") > -1) {
			criteria.andEmailEqualTo(account);
		} else {
			criteria.andLoginNameEqualTo(account);
		}

		if (StringUtils.isNotBlank(excludeOpenId)) {
			criteria.andOpenIdNotEqualTo(excludeOpenId);
		}

		List<OauthUser> userList = oauthUserDao.selectByExample(ouc);

		return userList != null && userList.size() > 0;
	}

	/**
	 * 根据登录名查找openId
	 * @param loginName
	 * @return
	 */
	public String getOpenId(String loginName) {

		if (StringUtils.isBlank(loginName)) {
			throw new ServiceException("请输入用户登录名或邮箱!");
		}

		OauthUser entity = new OauthUser();

		// 登录名可以为邮箱
		if (loginName.indexOf("@") > -1) {
			entity.setEmail(loginName);
		} else {
			entity.setLoginName(loginName);
		}

		entity = IbatisServiceUtils.get(entity, oauthUserDao);

		if (entity != null) {
			return entity.getOpenId();
		}

		return null;
	}

	/**
	 * 根据用户名或者邮箱获取用户信息
	 */
	public OauthUser getOauthUserInfoByEmail(String email) {
		OauthUser entity = new OauthUser();
		// 判断传过来的是用户名还是邮箱
		if (email.indexOf("@") >= 0) {
			entity.setEmail(email);
		} else {
			entity.setLoginName(email);
		}
		entity = IbatisServiceUtils.get(entity, oauthUserDao);
		return entity;
	}

	/**
	 * 通过openIdList得到用户列表
	 * @param openIdList
	 * @return
	 * @throws Exception
	 */
	public List<OAuth2UserInfo> getUserList(List<String> openIdList) throws Exception {

		OauthUserCriteria criteria = new OauthUserCriteria();
		criteria.createCriteria().andOpenIdIn(openIdList);
		List<OauthUser> userList = oauthUserDao.selectByExample(criteria);

		List<OAuth2UserInfo> resultList = new ArrayList<OAuth2UserInfo>();

		for (OauthUser user : userList) {

			OAuth2UserInfo userInfo = new OAuth2UserInfo();

			BeanUtilsEx.copyProperties(user, userInfo);

			userInfo.setLoginPwd(StringUtils.EMPTY);
			resultList.add(userInfo);
		}

		return resultList;
	}

	/**
	 * 通过openIdList得到单个用户信息
	 * @param openIdList
	 * @return
	 * @throws Exception
	 */
	public OAuth2UserInfo getUserInfo(String openId) throws Exception {

		OauthUser entity = new OauthUser();
		entity.setOpenId(openId);

		entity = IbatisServiceUtils.get(entity, oauthUserDao);

		if (entity == null) {
			return null;
		}

		OAuth2UserInfo result = new OAuth2UserInfo();

		BeanUtilsEx.copyProperties(entity, result);

		return result;
	}

	/**
	 * 修改密码
	 * @param openId
	 * @param oldPwd
	 * @param newPwd
	 */
	public void changePwd(String openId, String oldPwd, String newPwd) {

		OauthUser searchEntity = new OauthUser();
		searchEntity.setOpenId(openId);
		searchEntity = IbatisServiceUtils.get(searchEntity, oauthUserDao);

		if (!searchEntity.getLoginPassword().equals(StringUtilsEx.md5(oldPwd, searchEntity.getPasswordSalt()))) {
			throw new ServiceException("输入旧密码不正确");
		}

		// 修改密码为新的密码
		OauthUser entity = new OauthUser();
		entity.setId(searchEntity.getId());
		entity.setLoginPassword(StringUtilsEx.md5(newPwd, searchEntity.getPasswordSalt()));

		IbatisServiceUtils.updateByPk(entity, oauthUserDao);
	}

	/**
	 * 模拟登录请求
	 * @return
	 * @throws Exception
	 */
	public OAuth2Info simulationLogin(HttpServletRequest request, String openId) throws Exception {
		OAuth2Info oauthInfo = new OAuth2Info();

		Map<String, String> params = new HashMap<String, String>();

		params.put("openId", openId);

		String oauthUrl = StringUtils.isBlank(CoreConstants.OAUTH_SERVER_URL) ? ServletUtilsEx.getHostURL(request) + "oauth/"
				: CoreConstants.OAUTH_SERVER_URL;

		// 通过accessToken得到用户信息
		String result = HttpClientUtils.post(oauthUrl + "simulationLogin.do", params, "UTF-8");

		ResultEntity resultEntity = JSONUtilsEx.deserialize(result, ResultEntity.class);

		// null的话为请求失败.
		if (resultEntity == null) {
			return null;
		} else if ("0".equals(resultEntity.getCode())) {
			oauthInfo = BeanUtilsEx.convert(resultEntity.getData(), OAuth2Info.class);
		} else {
			oauthInfo.setCode("-1");
			oauthInfo.setText(resultEntity.getText());
		}

		return oauthInfo;
	}
}
