package com.ihidea.core.support.orm.mybatis3.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.base.CoreDao;
import com.ihidea.core.base.CoreEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.util.BeanUtilsEx;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

public class IbatisServiceUtils {

	private static Log logger = LogFactory.getLog(IbatisServiceUtils.class);

	public static Integer findCnt(CoreEntity entity, CoreDao dao, String orderString, boolean withLobs) {
		PageLimitHolderFilter.getContext().setOnlyGetTotalCnt(true);

		find(entity, dao, orderString, withLobs);

		return PageLimitHolderFilter.getContext().getTotalCount();
	}

	public static Integer findCnt(CoreEntity entity, CoreDao dao, String orderString) {
		return findCnt(entity, dao, orderString, false);
	}

	public static Integer findCnt(CoreEntity entity, CoreDao dao) {
		return findCnt(entity, dao, null, false);
	}

	public static <T> List<T> find(T entity, CoreDao dao) {
		return find(entity, dao, null, false);
	}

	public static <T> List<T> find(T entity, CoreDao dao, String orderString) {
		return find(entity, dao, orderString, false);
	}

	public static <T> List<T> find(T entity, CoreDao dao, boolean withLobs) {
		return find(entity, dao, null, withLobs);
	}

	/**
	 * <pre>
	 * 根据传入条件检索，此方法只能用于工具自动生成的DAO,MODEL
	 * </pre>
	 * 
	 * @param <T>
	 *            entity
	 * @param entity
	 *            自动生成的model
	 * @param dao
	 *            自动生成的mapper interface
	 * @return 检索后的结果
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> find(T entity, CoreDao dao, String orderString, boolean withLobs) {

		String clsName = entity.getClass().getName();

		List<T> result = null;

		try {

			// entity对象时withBlobs结尾的，为blob查询
			if (clsName.endsWith("WithBLOBs")) {
				withLobs = true;
			}

			// entity对象与Criteria对象在同一个jar包内,可以确保是同一个classloader
			Class exampleCls = Class.forName(
					(clsName.endsWith("WithBLOBs") ? clsName.substring(0, clsName.length() - 9) : clsName) + "Criteria", true,
					entity.getClass().getClassLoader());

			Object example = exampleCls.newInstance();

			Method createCriteria = exampleCls.getDeclaredMethod("createCriteria");

			// createCriteria
			Object criteria = createCriteria.invoke(example);

			Map<String, Object> propertyMap = BeanUtils.describe(entity);

			// 默认查询为非业务逻辑删除记录
			if (propertyMap.containsKey("delFlag") && BeanUtils.getProperty(entity, "delFlag") == null) {
				BeanUtils.setProperty(entity, "delFlag", BigDecimal.ZERO);
			}

			// 默认设置appId
			if (propertyMap.containsKey("appId") && BeanUtils.getProperty(entity, "appId") == null) {
				BeanUtils.setProperty(entity, "appId", LocalAttributeHolder.getContext().get("appid"));
			}

			if (propertyMap.containsKey("appid") && BeanUtils.getProperty(entity, "appid") == null) {
				BeanUtils.setProperty(entity, "appid", LocalAttributeHolder.getContext().get("appid"));
			}

			createCriteriaByEntity(criteria, entity);

			// 默认使用表中的orderId排序orderId排序
			// TODO 后期统一成ORDER_ID
			if (!StringUtils.isBlank(orderString)) {
				BeanUtils.setProperty(example, "orderByClause", orderString);
			} else if (propertyMap.containsKey("orderId")) {
				BeanUtils.setProperty(example, "orderByClause", "ORDER_ID DESC");
			} else if (propertyMap.containsKey("orderIndex")) {
				BeanUtils.setProperty(example, "orderByClause", "ORDER_INDEX DESC");
			} else if (propertyMap.containsKey("createDate")) {
				BeanUtils.setProperty(example, "orderByClause", "CREATE_DATE DESC");
			}

			Method selectByExampleM = null;

			if (withLobs) {
				selectByExampleM = dao.getClass().getDeclaredMethod("selectByExampleWithBLOBs", example.getClass());
			} else {
				selectByExampleM = dao.getClass().getDeclaredMethod("selectByExample", example.getClass());
			}

			result = (List<T>) selectByExampleM.invoke(dao, example);
		} catch (Exception e) {
			throw new ServiceException(e);
		}

		return result;
	}

	public static <T> T get(T entity, CoreDao dao) {
		return IbatisServiceUtils.get(entity, dao, false);
	}

	public static <T> T get(T entity, CoreDao dao, boolean withLobs) {

		List<T> result = find(entity, dao, withLobs);

		// 用apache的CollectionUtils.isEmpty有问题
		if (result != null && result.size() > 0) {
			if (result.size() > 1) {
				logger.info("以上[" + entity.getClass().getName() + "]查出" + result.size() + "笔记录，与预期不符！");
				throw new ServiceException("查出多笔记录");
			} else {
				return result.get(0);
			}
		} else {
			return null;
		}
	}

	/**
	 * <pre>
	 * 根据传入的参数,来生成criteria
	 * 查询，删除，更新如果传入对象属性是""的都不作为查询条件
	 * </pre>
	 * 
	 * @param <T>
	 * @param criteria
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createCriteriaByEntity(T criteria, Object entity) throws Exception {

		Map<String, Object> propertyMap = BeanUtils.describe(entity);

		Map<String, List<Method>> methodList = ClassUtilsEx.getClassMethodMap(criteria.getClass().getSuperclass());

		for (Iterator<String> i = propertyMap.keySet().iterator(); i.hasNext();) {

			String attrName = i.next();

			Object propertyValue = propertyMap.get(attrName);

			// 做检索条件时,""不作为检索条件
			if ("".equals(propertyValue)) {
				propertyValue = null;
			}

			// 如果传入属性值不为null，或者是做检索条件且不为""
			if (propertyValue != null) {

				String methodName = null;

				// 如果传入的为字符且头尾中存在%,则为模糊查询
				if (propertyValue instanceof String) {
					String strPropertyValue = (String) propertyValue;
					if (strPropertyValue.startsWith("%") || strPropertyValue.endsWith("%")) {
						methodName = getCriteriaMethodLikeName(attrName);
					}
				}

				if (methodName == null) {
					methodName = getCriteriaMethodName(attrName);
				}

				// TODO like
				// String methodName = null;
				//
				// // 如果属性名中含有name字段,则使用like方式
				// if (attrName.toLowerCase().indexOf("name") == -1) {
				// methodName = getCriteriaMethodName(attrName);
				// } else {
				// methodName = getCriteriaMethodLikeName(attrName);
				// propertyValue = "%" + String.valueOf(propertyValue) + "%";
				// }

				if (methodList.containsKey(methodName)) {

					// 默认取第一个
					Method equal = methodList.get(methodName).get(0);

					equal.invoke(criteria, BeanUtilsEx.convert(propertyValue, equal.getParameterTypes()[0]));
				}
			}
		}

		return criteria;
	}

	/**
	 * <pre>
	 * 新增
	 * </pre>
	 * 
	 * @param <T>
	 * @param entity
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	public static <T> int insert(T entity, CoreDao dao) throws Exception {

		Method insertM = null;
		try {

			// 设置主键值
			{
				PropertyDescriptor pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(entity.getClass(), "id");

				if (pd != null && String.class.equals(pd.getPropertyType()) && StringUtils.isBlank(BeanUtils.getProperty(entity, "id"))) {
					BeanUtils.setProperty(entity, "id", StringUtilsEx.getUUID());
				}
			}

			// 设置app_id
			{
				PropertyDescriptor pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(entity.getClass(), "appId");

				if (pd != null && String.class.equals(pd.getPropertyType())
						&& StringUtils.isBlank(BeanUtils.getProperty(entity, "appId"))) {
					BeanUtils.setProperty(entity, "appId", LocalAttributeHolder.getContext().get("appid"));
				}

				PropertyDescriptor pid = org.springframework.beans.BeanUtils.getPropertyDescriptor(entity.getClass(), "appid");

				if (pid != null && String.class.equals(pid.getPropertyType())
						&& StringUtils.isBlank(BeanUtils.getProperty(entity, "appid"))) {
					BeanUtils.setProperty(entity, "appid", LocalAttributeHolder.getContext().get("appid"));
				}
			}

			// 设置排序id
			{
				PropertyDescriptor pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(entity.getClass(), "orderId");

				if ((pd != null) && (StringUtils.isBlank(BeanUtils.getProperty(entity, "orderId")))) {

					if (BigDecimal.class.equals(pd.getPropertyType())) {
						BeanUtils.setProperty(entity, "orderId", BigDecimal.valueOf(new Date().getTime()));
					} else if (Date.class.equals(pd.getPropertyType())) {
						BeanUtils.setProperty(entity, "orderId", new Date());
					}
				}
			}

			insertM = dao.getClass().getDeclaredMethod("insert", entity.getClass());
		} catch (NoSuchMethodException e) {
			throw new ServiceException("调用的dao中没有insert方法,请查看对应的mapper.xml!");
		}
		return ((Integer) insertM.invoke(dao, addInsertProperty(entity))).intValue();
	}

	@SuppressWarnings("unchecked")
	private static <T> T addInsertProperty(T entity) throws Exception {

		Map<String, Object> propertyMap = BeanUtils.describe(entity);

		if (propertyMap.containsKey("createTime") && BeanUtils.getProperty(entity, "createTime") == null) {
			BeanUtils.setProperty(entity, "createTime", DateUtilsEx.getSysDate());
		}

		if (propertyMap.containsKey("createDate") && BeanUtils.getProperty(entity, "createDate") == null) {
			BeanUtils.setProperty(entity, "createDate", DateUtilsEx.getSysDate());
		}

		if (propertyMap.containsKey("createAccount") && BeanUtils.getProperty(entity, "createAccount") == null) {
			if (SessionContext.getSessionInfo() != null) {
				BeanUtils.setProperty(entity, "createAccount", SessionContext.getSessionInfo().getUserId());
			} else if (LocalAttributeHolder.getContext().get("userid") != null) {
				BeanUtils.setProperty(entity, "createAccount", (String) LocalAttributeHolder.getContext().get("userid"));
			}
		}

		if ((propertyMap.containsKey("userId")) && (org.apache.commons.beanutils.BeanUtils.getProperty(entity, "userId") == null)) {
			BeanUtils.setProperty(entity, "userId",
					StringUtils.defaultIfEmpty((String) LocalAttributeHolder.getContext().get("userid"), ""));
		}

		if (propertyMap.containsKey("delFlag") && BeanUtils.getProperty(entity, "delFlag") == null) {
			BeanUtils.setProperty(entity, "delFlag", BigDecimal.ZERO);
		}

		return entity;
	}

	/**
	 * <pre>
	 * 根据条件更新
	 * </pre>
	 * 
	 * @param <T>
	 * @param criteriaEntity
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> int update(T record, T criteriaEntity, CoreDao dao) throws Exception {

		String clsName = criteriaEntity.getClass().getName();

		// entity对象与Criteria对象在同一个jar包内,可以确保是同一个classloader
		Class exampleCls = Class.forName(
				(clsName.endsWith("WithBLOBs") ? clsName.substring(0, clsName.length() - 9) : clsName) + "Criteria", true,
				criteriaEntity.getClass().getClassLoader());

		Object example = exampleCls.newInstance();

		Method createCriteria = exampleCls.getDeclaredMethod("createCriteria");

		// createCriteria
		Object criteria = createCriteria.invoke(example);

		createCriteriaByEntity(criteria, criteriaEntity);

		Method updateByExampleM = dao.getClass().getDeclaredMethod("updateByExampleSelective", record.getClass(), example.getClass());
		return (Integer) updateByExampleM.invoke(dao, addUpdateProperty(record), example);
	}

	@SuppressWarnings("unchecked")
	private static <T> T addUpdateProperty(T entity) throws Exception {

		Map<String, Object> propertyMap = BeanUtils.describe(entity);

		if (propertyMap.containsKey("appid") && BeanUtils.getProperty(entity, "appid") == null) {
			BeanUtils.setProperty(entity, "appid", LocalAttributeHolder.getContext().get("appid"));
		}

		if (propertyMap.containsKey("updateTime") && BeanUtils.getProperty(entity, "updateTime") == null) {
			BeanUtils.setProperty(entity, "updateTime", DateUtilsEx.getSysDate());
		}

		if (propertyMap.containsKey("modifyTime") && BeanUtils.getProperty(entity, "modifyTime") == null) {
			BeanUtils.setProperty(entity, "modifyTime", DateUtilsEx.getSysDate());
		}

		if (propertyMap.containsKey("modifyAccount") && BeanUtils.getProperty(entity, "modifyAccount") == null) {
			if (SessionContext.getSessionInfo() != null) {
				BeanUtils.setProperty(entity, "modifyAccount", SessionContext.getSessionInfo().getUserId());
			} else if (LocalAttributeHolder.getContext().get("userid") != null) {
				BeanUtils.setProperty(entity, "modifyAccount", (String) LocalAttributeHolder.getContext().get("userid"));
			}
		}

		if (propertyMap.containsKey("updateUser") && BeanUtils.getProperty(entity, "updateUser") == null
				&& SessionContext.getSessionInfo() != null) {
			BeanUtils.setProperty(entity, "updateUser", SessionContext.getSessionInfo().getUserId());
		}

		return entity;
	}

	/**
	 * <pre>
	 * 根据主键更新，所更新的表必须有主键
	 * </pre>
	 * 
	 * @param <T>
	 * @param entity
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	public static <T> int updateByPk(T entity, CoreDao dao) {

		Method updateByPrimaryKeySelectiveM = null;

		int cnt = -1;

		try {
			updateByPrimaryKeySelectiveM = dao.getClass().getDeclaredMethod("updateByPrimaryKeySelective", entity.getClass());
		} catch (NoSuchMethodException e) {
			throw new ServiceException("请查看此dao对应的表中是否有主键!");
		}

		try {
			cnt = ((Integer) updateByPrimaryKeySelectiveM.invoke(dao, addUpdateProperty(entity))).intValue();
		} catch (Exception e) {
			throw new ServiceException(e);
		}

		return cnt;
	}

	/**
	 * <pre>
	 * 根据主键删除
	 * </pre>
	 * 
	 * @param id
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	public static int deleteByPk(Object id, CoreDao dao) throws Exception {

		Method deleteByPrimaryKeyM = null;

		boolean hasMethod = false;

		for (Method method : dao.getClass().getDeclaredMethods()) {

			if ("deleteByPrimaryKey".equals(method.getName())) {
				deleteByPrimaryKeyM = method;
				hasMethod = true;
				break;
			}
		}

		if (!hasMethod) {
			throw new ServiceException("调用的dao中没有deleteByPrimaryKey方法,请查看对应的mapper.xml!");
		}

		return ((Integer) deleteByPrimaryKeyM.invoke(dao, BeanUtilsEx.convert(id, deleteByPrimaryKeyM.getParameterTypes()[0]))).intValue();
	}

	/**
	 * <pre>
	 * 根据条件删除
	 * </pre>
	 * 
	 * @param id
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> int delete(T entity, CoreDao dao) throws Exception {

		String clsName = entity.getClass().getName();

		// entity对象与Criteria对象在同一个jar包内,可以确保是同一个classloader
		Class exampleCls = Class.forName(
				(clsName.endsWith("WithBLOBs") ? clsName.substring(0, clsName.length() - 9) : clsName) + "Criteria", true,
				entity.getClass().getClassLoader());

		Object example = exampleCls.newInstance();

		Method createCriteria = exampleCls.getDeclaredMethod("createCriteria");

		// createCriteria
		Object criteria = createCriteria.invoke(example);

		createCriteriaByEntity(criteria, entity);

		Method delByExampleM = dao.getClass().getDeclaredMethod("deleteByExample", example.getClass());
		return (Integer) delByExampleM.invoke(dao, example);

	}

	/**
	 * 批量插入
	 * 
	 * @param statementName
	 * @param list
	 */
	public static <T> int batchInsert(String statementName, T entity, CoreDao dao) {
		return 0;
	}

	/**
	 * <pre>
	 * ibatis自动生成的代码中的名称为and(属性名)EqualTo
	 * e.g: xx ---- andXxEqualTo
	 * </pre>
	 * 
	 * @param attrName
	 *            属性名
	 * @return
	 */
	private static String getCriteriaMethodName(String attrName) {
		return "and" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1) + "EqualTo";
	}

	private static String getCriteriaMethodLikeName(String attrName) {
		return "and" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1) + "Like";
	}

}
