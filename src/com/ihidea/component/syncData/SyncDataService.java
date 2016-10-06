package com.ihidea.component.syncData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.exception.ServiceWarn;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

/**
 * 必需项:tableName;sycId;sycType[0:新增，修改；1:删除]<br>
 * 插件类必需标注@SyncPlugin 注解<br>
 * @author TYOTANN
 */
@Service
public class SyncDataService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static Set<String> tableNameSet = new HashSet<String>();

	static {

		for (int i = 0; i < CoreConstants.SYNCDATA_TABLENAME.length; i++) {
			tableNameSet.add(CoreConstants.SYNCDATA_TABLENAME[i].trim().toLowerCase());
		}
	}

	@RequestMapping("/syncData.do")
	public void syncData(Map<String, String[]> param) throws Exception {

		if (param.get("tableName") == null || StringUtils.isBlank(((String[]) param.get("tableName"))[0])) {
			throw new ServiceException("请传入同步表名参数:tableName");
		}

		String tableName = ((String[]) param.get("tableName"))[0];

		if (!tableNameSet.contains(tableName.trim().toLowerCase())) {
			throw new ServiceException("此表名不允许被同步:" + tableName);
		}

		// 0：数据需要更新，1：数据需要删除
		if (param.get("sycType") == null || StringUtils.isBlank(((String[]) param.get("sycType"))[0])) {
			throw new ServiceException("请传入同步类型参数:sycType");
		}

		String sycType = ((String[]) param.get("sycType"))[0];

		if (param.get("syc_id") == null || StringUtils.isBlank(((String[]) param.get("syc_id"))[0])) {
			throw new ServiceException("请传入同步业务主键参数:sycId");
		}

		String sycId = ((String[]) param.get("syc_id"))[0];

		if ("0".equals(sycType)) {

			Integer rowCnt = jdbcTemplate.queryForObject("select count(1) from " + tableName + " where syc_id = ?", Integer.class, sycId);

			StringBuffer sql = new StringBuffer();

			List<String> paramList = new ArrayList<String>();

			// 新增或更新
			if (rowCnt == 0) {

				sql.append("insert into " + tableName + "(");

				StringBuffer values = new StringBuffer();

				for (Iterator<String> i = param.keySet().iterator(); i.hasNext();) {

					String columnName = i.next();

					if (!"tableName".equals(columnName) && !"sycType".equals(columnName)) {
						sql.append(columnName.toLowerCase()).append(",");
						values.append("?").append(",");
						paramList.add(((String[]) param.get(columnName))[0]);
					}
				}

				// 设置主键
				sql.append("id").append(",");
				values.append("?").append(",");
				paramList.add(StringUtilsEx.getUUID());

				sql.deleteCharAt(sql.length() - 1).append(") values (").append(values.deleteCharAt(values.length() - 1)).append(")");
			} else {

				sql.append("update " + tableName + " set ");

				for (Iterator<String> i = param.keySet().iterator(); i.hasNext();) {

					String columnName = i.next();

					if (!"tableName".equals(columnName) && !"sycType".equals(columnName) && !"syc_id".equals(columnName)) {
						sql.append(columnName.toLowerCase()).append("=?,");
						paramList.add(((String[]) param.get(columnName))[0]);
					}
				}

				sql.deleteCharAt(sql.length() - 1).append(" where syc_id = ?");
				paramList.add(sycId);
			}

			jdbcTemplate.update(sql.toString(), paramList.toArray());

		} else if ("1".equals(sycType)) {
			int delCnt = jdbcTemplate.update("delete from " + tableName + " where syc_id = ?", sycId);

			if (delCnt == 0) {
				throw new ServiceWarn("没有匹配到需要删除的记录:" + sycId);
			}
		}

		// 执行插件
		try {
			Map<String, Object> syncBeans = SpringContextLoader.getBeansWithAnnotation(SyncPlugin.class);

			if (syncBeans != null) {
				for (Object serviceClz : syncBeans.values()) {

					List<Method> methodList = ClassUtilsEx.getClassMethodByAnnotation(serviceClz.getClass(), SyncPluginMethod.class);

					for (Method method : methodList) {

						SyncPluginMethod methodAnno = method.getAnnotation(SyncPluginMethod.class);

						if (tableName.equalsIgnoreCase(methodAnno.tableName())) {

							Map<String, Object> params = new HashMap<String, Object>();

							params.put("id", sycId);
							params.put("type", sycType);

							method.invoke(serviceClz, sycId, sycType);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceException("同步数据执行插件时报错:" + e.getMessage(), e);
		}

	}
}
