package com.ihidea.component.oauth2.dao;

import com.ihidea.component.oauth2.dao.model.OauthUser;
import com.ihidea.component.oauth2.dao.model.OauthUserCriteria;
import com.ihidea.core.base.CoreDao;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OauthUserMapper extends CoreDao {
    int countByExample(OauthUserCriteria example);

    int deleteByExample(OauthUserCriteria example);

    int deleteByPrimaryKey(String id);

    int insert(OauthUser record);

    int insertSelective(OauthUser record);

    List<OauthUser> selectByExample(OauthUserCriteria example);

    OauthUser selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OauthUser record, @Param("example") OauthUserCriteria example);

    int updateByExample(@Param("record") OauthUser record, @Param("example") OauthUserCriteria example);

    int updateByPrimaryKeySelective(OauthUser record);

    int updateByPrimaryKey(OauthUser record);
}