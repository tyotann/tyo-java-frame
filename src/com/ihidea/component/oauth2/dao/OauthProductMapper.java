package com.ihidea.component.oauth2.dao;

import com.ihidea.component.oauth2.dao.model.OauthProduct;
import com.ihidea.component.oauth2.dao.model.OauthProductCriteria;
import com.ihidea.core.base.CoreDao;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OauthProductMapper extends CoreDao {
    int countByExample(OauthProductCriteria example);

    int deleteByExample(OauthProductCriteria example);

    int deleteByPrimaryKey(String id);

    int insert(OauthProduct record);

    int insertSelective(OauthProduct record);

    List<OauthProduct> selectByExample(OauthProductCriteria example);

    OauthProduct selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OauthProduct record, @Param("example") OauthProductCriteria example);

    int updateByExample(@Param("record") OauthProduct record, @Param("example") OauthProductCriteria example);

    int updateByPrimaryKeySelective(OauthProduct record);

    int updateByPrimaryKey(OauthProduct record);
}