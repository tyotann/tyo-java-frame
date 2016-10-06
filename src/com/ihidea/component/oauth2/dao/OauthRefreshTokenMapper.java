package com.ihidea.component.oauth2.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ihidea.component.oauth2.dao.model.OauthRefreshToken;
import com.ihidea.component.oauth2.dao.model.OauthRefreshTokenCriteria;
import com.ihidea.core.base.CoreDao;

public interface OauthRefreshTokenMapper extends CoreDao {
	int countByExample(OauthRefreshTokenCriteria example);

	int deleteByExample(OauthRefreshTokenCriteria example);

	int insert(OauthRefreshToken record);

	int insertSelective(OauthRefreshToken record);

	List<OauthRefreshToken> selectByExample(OauthRefreshTokenCriteria example);

	int updateByExampleSelective(@Param("record")
	OauthRefreshToken record, @Param("example")
	OauthRefreshTokenCriteria example);

	int updateByExample(@Param("record")
	OauthRefreshToken record, @Param("example")
	OauthRefreshTokenCriteria example);
}