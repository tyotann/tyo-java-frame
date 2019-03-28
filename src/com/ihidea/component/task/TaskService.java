package com.ihidea.component.task;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ihidea.core.base.CoreService;
import com.ihidea.core.support.JdbcSupportService;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.DateUtilsEx;

@Service
public class TaskService extends CoreService {

	@Autowired
	private JdbcSupportService jdbcSupportService;

	@Autowired
	private TaskDao taskDao;

	/**
	 * <pre>
	 * 任务配置列表
	 * </pre>
	 */
	public List<Map<String, Object>> getTasklogList(String delFlag) {
		String sql = " from cpt_task c, cpt_task_quartz d,task_info a, task_receive_cfg b where c.task_id = d.task_id and  c.task_id = a.task_id(+) and a.receive_cfg_id = b.receive_id(+) ";

		if (delFlag != null && !StringUtils.isBlank(delFlag)) {
			sql = sql + " and del_flag = " + delFlag;
		}

		sql = "select rownum rum, c.task_id, task_name,type,decode(status,0,'空闲',1,'执行中') status, decode(del_flag, 0,'启用',1,'停用') del_flag,to_char(task_last_time, 'yyyy.MM.dd hh24:mi:ss') task_last_time, to_char(task_next_time, 'yyyy.MM.dd hh24:mi:ss') task_next_time,task_max_time,cron,call_param, call "
				+ sql + " order by rum ";

		return jdbcSupportService.getJdbcTemplate().queryForList(sql);
	}

	/**
	 * <pre>
	 * 任务配置明细
	 * </pre>
	 */
	public Map<String, Object> getTasklogInfo(String taskId) {
		return jdbcSupportService
				.getJdbcTemplate()
				.queryForMap(
						"select a.task_id, task_name, type, seq, status, del_flag, to_char(a.task_next_time, 'yyyy.MM.dd') task_next_time, task_max_time, call_type, call, cron, call_param from cpt_task a, cpt_task_quartz b  where a.task_id = b.task_id and a.task_id = "
								+ taskId);
	}

	/**
	 * <pre>
	 * 查询所有任务
	 * </pre>
	 */
	public List<TaskEntity> getAllTask() {
		return taskDao.getAllTask();
	}

	/**
	 * <pre>
	 * 查询任务日志String id, String creatDate
	 * </pre>
	 */
	public List<Map<String, Object>> getTaskLog(Map<String, Object> param) {
		// 加入分页
		PageLimit pl = PageLimitHolderFilter.getContext();

		if (pl != null) {
			param.put("limitEnable", String.valueOf(pl.limited()));

			if (pl.limited()) {
				pl.setTotalCount(jdbcSupportService.getJdbcTemplate().queryForObject("select count(1) from ( "+
							"select distinct seq, id, decode(min(status),-1,'异常','正常') as status, to_char(min(create_date),'yyyy.MM.dd hh24:mi:ss') as create_date  from cpt_task_log "+
							"where id = ? and to_char(create_date,'yyyy.MM.dd') = ? group by id, seq order by seq desc) a", new Object[] {param.get("id").toString(), param.get("creatDate").toString()}, Integer.class));
				
				param.put("startResult", String.valueOf(pl.getStartRowNo()));
				param.put("endResult", String.valueOf(pl.getEndRowNo()));
			}
		}
		return taskDao.getTaskLog(param);
	}

	/**
	 * <pre>
	 * 查询日志详细
	 * </pre>
	 */
	public List<Map<String, Object>> getLogInfo(String id, String seq) {
		return taskDao.getLogInfo(id, seq);
	}

	/**
	 * <pre>
	 * 数据协议配置明细
	 * </pre>
	 */
	public Map<String, Object> getTaskReceiveInfo(String receiveId) {
		return jdbcSupportService.getJdbcTemplate().queryForMap("select * from task_receive_cfg where receive_id =" + receiveId);
	}

	/**
	 * <pre>
	 * 数据协议配置列表
	 * </pre>
	 */
	public List<Map<String, Object>> getTaskReceiveList(String receiveId) {
		String sql = " from task_receive_cfg b where 1=1 ";

		if (receiveId != null && !StringUtils.isBlank(receiveId)) {
			sql = sql + " and receive_id = " + receiveId;
		}

		sql = "select rownum rum, b.* " + sql + " order by receive_id ";

		return jdbcSupportService.getJdbcTemplate().queryForList(sql);
	}

	/**
	 * <pre>
	 * 修改任务配置
	 * </pre>
	 * @throws Exception
	 */
	public void updateTasklog(Map<String, String> param, String flag) throws Exception {

		if (param == null || param.get("task_id") == null || StringUtils.isBlank(param.get("task_id"))) {
			throw new ServiceException("任务配置ID未明确！");
		}
		// 执行修改任务配置
		taskDao.updateTasklog(param);

		// TODO: 接口尚在调整 待完整实现
		/*
		 * if("start".equals(flag)){ //启用 taskEnginee.start(param.get("task_id")); }else if ("stop".equals(flag)){ //停用
		 * taskEnginee.stop(param.get("task_id")); }else{ //修改 taskEnginee.resetTask(param.get("task_id")); }
		 */
	}

	/**
	 * <pre>
	 * 新增任务配置
	 * </pre>
	 */
	public void addTasklog(Map<String, String> param) {
		try {
			int taskId = jdbcSupportService.getJdbcTemplate().queryForObject("select seq_cpt_task_quartz_id.nextval  from dual", Integer.class);

			jdbcSupportService
					.getJdbcTemplate()
					.update("insert into cpt_task(task_id, task_name, type, seq, status, del_flag, task_next_time, task_max_time) values (?,?,?,?,?,?,to_date(?, 'yyyy.MM.dd'),?)",
							new Object[] {
									taskId,
									param.get("task_name") == null ? "" : param.get("task_name"),
									param.get("type") == null ? 0 : param.get("type"),
									param.get("seq") == null ? 1 : param.get("seq"),
									param.get("status") == null ? 0 : param.get("status"),
									param.get("del_flag") == null ? 0 : param.get("del_flag"),
									param.get("task_next_time") == null || StringUtils.isBlank(param.get("task_next_time")) ? DateUtilsEx
											.formatToString(new Date(), "yyyy.MM.dd") : param.get("task_next_time"),
									param.get("task_max_time") == null || StringUtils.isBlank(param.get("task_max_time")) ? "600" : param
											.get("task_max_time") });

			jdbcSupportService.getJdbcTemplate().update(
					"insert into cpt_task_quartz(task_id, call_type, call, cron, call_param) values (?,?,?,?,?)",
					new Object[] { taskId, param.get("call_type") == null ? 0 : param.get("call_type"),
							param.get("call") == null ? "" : param.get("call"),
							param.get("cron") == null || StringUtils.isBlank(param.get("cron")) ? "0/5 * * * * ?" : param.get("cron"),
							param.get("call_param") == null ? "" : param.get("call_param") });
		} catch (Exception e) {
			throw new ServiceException("新增任务配置信息出错：" + e.getMessage());
		}
	}

	/**
	 * <pre>
	 * 新增数据协议配置
	 * </pre>
	 */
	public void addTaskReceive(Map<String, String> param) {
		String receiveId = param.get("receive_id") == null ? "" : param.get("receive_id");

		if (StringUtils.isBlank(receiveId)) {
			throw new ServiceException("数据协议配置ID未明确！");
		}

		try {

			int cnt = jdbcSupportService.getJdbcTemplate().queryForObject(
					"select count(*)  from task_receive_cfg where receive_id = " + receiveId, Integer.class);

			if (cnt > 0) {
				throw new ServiceException("已存在编号为【" + receiveId + "】的数据协议配置信息!");
			}

			jdbcSupportService.getJdbcTemplate().update(
					"insert into task_receive_cfg(receive_id, receive_type, receive_param) values (?,?,?)",
					new Object[] {
							receiveId,
							param.get("receive_type") == null || StringUtils.isBlank(param.get("receive_type")) ? "HESSIAN" : param
									.get("receive_type"), param.get("receive_param") == null ? "" : param.get("receive_param") });
		} catch (Exception e) {
			throw new ServiceException("新增数据协议配置出错：" + e.getMessage());
		}
	}

	/**
	 * <pre>
	 * 修改数据协议配置
	 * </pre>
	 */
	public void updateTaskReceive(Map<String, String> param) {
		String receiveId = param.get("receive_id") == null ? "" : param.get("receive_id");

		if (StringUtils.isBlank(receiveId)) {
			throw new ServiceException("数据协议配置ID未明确！");
		}

		try {
			Map<String, Object> taskReceive = jdbcSupportService.getJdbcTemplate().queryForMap(
					"select *  from task_receive_cfg where receive_id = " + receiveId);

			if (taskReceive == null) {
				throw new ServiceException("未找到编号为【" + receiveId + "】的数据协议配置信息!");
			}

			jdbcSupportService.getJdbcTemplate().update(
					"update task_receive_cfg set receive_type = ?, receive_param = ?  where receive_id = ?",
					new Object[] {
							param.get("receive_type") == null || StringUtils.isBlank(param.get("receive_type")) ? taskReceive
									.get("receive_type") : param.get("receive_type"),
							param.get("receive_param") == null || StringUtils.isBlank(param.get("receive_type")) ? taskReceive
									.get("receive_param") : param.get("receive_param"), receiveId });
		} catch (Exception e) {
			throw new ServiceException("修改数据协议配置出错：" + e.getMessage());
		}
	}

}
