package com.ihidea.component.datastore.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ihidea.component.datastore.dao.model.TCptDataInfo;
import com.ihidea.component.datastore.dao.model.TCptDataInfoCriteria;
import com.ihidea.core.base.CoreDao;

public interface TCptDataInfoMapper extends CoreDao {

	int countByExample(TCptDataInfoCriteria example);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int deleteByExample(TCptDataInfoCriteria example);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int deleteByPrimaryKey(String id);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int insert(TCptDataInfo record);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int insertSelective(TCptDataInfo record);

	List<TCptDataInfo> selectByExample(TCptDataInfoCriteria example);

	TCptDataInfo selectByPrimaryKey(String id);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int updateByExampleSelective(@Param("record")
	TCptDataInfo record, @Param("example")
	TCptDataInfoCriteria example);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int updateByExample(@Param("record")
	TCptDataInfo record, @Param("example")
	TCptDataInfoCriteria example);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int updateByPrimaryKeySelective(TCptDataInfo record);

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	int updateByPrimaryKey(TCptDataInfo record);
}