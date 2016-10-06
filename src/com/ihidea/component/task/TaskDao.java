package com.ihidea.component.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.SystemUtilsEx;

@Repository
public class TaskDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void changeTaskStatus(String taskId, BigDecimal taskSeq, String status) {
		jdbcTemplate.update("update cpt_task set status=?  where task_id = ? and seq = ?",
				new Object[] { status, taskId, taskSeq.intValue() });
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public TaskEntity lockAndUpdateTaskStatus(String taskId, BigDecimal taskSeq, Date taskNextTime) {

		TaskEntity task = null;

		try {
			task = jdbcTemplate.queryForObject("select * from cpt_task where task_id = ? and seq = ? and status=0 for update",
					new Object[] { taskId, taskSeq }, ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));

			// 修改任务的taskSeq
			jdbcTemplate.update(
					"update cpt_task set seq = ?, task_last_time = sysdate, task_next_time = ?, status=1, host_id=?  where task_id = ?",
					new Object[] { taskSeq.intValue() + 1, taskNextTime, SystemUtilsEx.getHostId(), taskId });

			task.setTaskCfg(jdbcTemplate.queryForObject("select * from cpt_task_quartz where task_id = ?", new Object[] { taskId },
					ParameterizedBeanPropertyRowMapper.newInstance(TaskCfgEntity.class)));

		} catch (EmptyResultDataAccessException e) {
		}

		return task;
	}

	/**
	 * 更新任务下次执行时间
	 * @param taskId
	 * @param taskNextTime
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void updateTaskNextTime(String taskId, Date taskNextTime) {
		jdbcTemplate.update("update cpt_task set task_next_time = ? where task_id = ?", new Object[] { taskNextTime, taskId });
	}

	/**
	 * 得到任务信息
	 * @param taskId
	 * @return
	 */
	public TaskEntity getTaskInfo(String taskId) {

		TaskEntity task = jdbcTemplate.queryForObject("select * from cpt_task where task_id = ?", new Object[] { taskId },
				ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));

		task.setTaskCfg(jdbcTemplate.queryForObject("select * from cpt_task_quartz where task_id = ?", new Object[] { taskId },
				ParameterizedBeanPropertyRowMapper.newInstance(TaskCfgEntity.class)));

		return task;
	}

	/**
	 * 得到所有任务信息
	 * @return
	 */
	public List<TaskEntity> getAllTask() {

		List<TaskEntity> result = jdbcTemplate.query("select * from cpt_task  order by task_id",
				ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));

		// 得到任务配置信息
		for (TaskEntity task : result) {
			task.setTaskCfg(jdbcTemplate.queryForObject("select * from cpt_task_quartz where task_id = ?",
					new Object[] { task.getTaskId() }, ParameterizedBeanPropertyRowMapper.newInstance(TaskCfgEntity.class)));
		}

		return result;
	}

	/**
	 * 得到所有可用任务信息
	 * @return
	 */
	public List<TaskEntity> getAllActiveTask() {
		List<TaskEntity> result = jdbcTemplate.query("select * from cpt_task where del_flag=0 order by task_id",
				ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));

		// 得到任务配置信息
		for (TaskEntity task : result) {
			task.setTaskCfg(jdbcTemplate.queryForObject("select * from cpt_task_quartz where task_id = ?",
					new Object[] { task.getTaskId() }, ParameterizedBeanPropertyRowMapper.newInstance(TaskCfgEntity.class)));
		}

		return result;
	}

	/**
	 * 得到所有可用任务信息
	 * @return
	 */
	public List<TaskEntity> getAllUnActiveTask() {

		List<TaskEntity> result = jdbcTemplate.query("select * from cpt_task where del_flag=1 order by task_id",
				ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));

		// 得到任务配置信息
		for (TaskEntity task : result) {
			task.setTaskCfg(jdbcTemplate.queryForObject("select * from cpt_task_quartz where task_id = ?",
					new Object[] { task.getTaskId() }, ParameterizedBeanPropertyRowMapper.newInstance(TaskCfgEntity.class)));
		}

		return result;
	}

	/**
	 * 得到所有超时任务
	 * @return
	 */
	public List<TaskEntity> getTimeOutTask() {
		return jdbcTemplate
				.query("select * from cpt_task where task_max_time is not null and status=1 and host_id is not null and (task_last_time + task_max_time/24/60/60) < sysdate",
						ParameterizedBeanPropertyRowMapper.newInstance(TaskEntity.class));
	}

	/**
	 * 增加任务日志
	 * @param taskId
	 * @param seq
	 * @param status
	 * @param message
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void addTaskLog(String taskId, BigDecimal seq, BigDecimal status, String message) {

		jdbcTemplate.update(
				"insert into cpt_task_log (id, seq, create_date, status, text, host_id) values (?, ?, current_timestamp, ?, ?, ?)",
				new Object[] { taskId, seq, status, message, SystemUtilsEx.getHostId() });
	}

	/**
	 * 获取任务调度日志
	 * @return
	 */
	public List<Map<String, Object>> getTaskLog(Map<String, Object> param) {
		return jdbcTemplate
				.queryForList(
						"select * from ( "
								+ "select  rownum rum, a.* from "
								+ "(select distinct seq, id, decode(min(status),-1,'异常','正常') as status, to_char(min(create_date),'yyyy.MM.dd hh24:mi:ss') as create_date  from cpt_task_log "
								+ "where id = ? and to_char(create_date,'yyyy.MM.dd') = ? group by id, seq order by seq desc) a) "
								+ "where rum >= ? and rum <= ?", new Object[] { param.get("id").toString(),
								param.get("creatDate").toString(), param.get("startResult"), param.get("endResult") });
	}

	/**
	 * 获取任务调度日志
	 * @return
	 */
	public List<Map<String, Object>> getLogInfo(String id, String seq) {
		return jdbcTemplate
				.queryForList(
						" select to_char(create_date,'yyyy.MM.dd hh24:mi:ss.ff3') as create_date,decode(status,-1,'异常',0,'正常','未知') as status,text,host_id from cpt_task_log where id = ? and seq=? order by create_date",
						new Object[] { id, seq });
	}

	/**
	 * <pre>
	 * 修改任务配置
	 * </pre>
	 */
	public void updateTasklog(Map<String, String> param) {
		String task_id = param.get("task_id") == null ? "" : param.get("task_id");

		if (StringUtils.isBlank(task_id)) {
			throw new ServiceException("任务配置ID未明确！");
		}

		Map<String, Object> taskInfo = jdbcTemplate
				.queryForMap("select task_id, task_name, to_char(task_next_time,'yyyy.MM.dd') task_next_time, type, seq, status, del_flag, task_max_time from cpt_task where task_id = "
						+ task_id);

		if (taskInfo == null) {
			throw new ServiceException("未找到编号为【" + task_id + "】的任务状态配置信息!");
		}

		Map<String, Object> taskQuartz = jdbcTemplate.queryForMap("select * from cpt_task_quartz where task_id = " + task_id);

		if (taskQuartz == null) {
			throw new ServiceException("未找到编号为【" + task_id + "】的任务配置信息!");
		}

		try {
			jdbcTemplate
					.update("update cpt_task set task_name = ?, task_next_time = to_date(?,'yyyy.MM.dd'), type = ?, seq = ?,status = ?,del_flag = ?, task_max_time = ? where task_id = ?",
							new Object[] {
									param.get("task_name") == null || StringUtils.isBlank(param.get("task_name")) ? taskInfo
											.get("task_name") : param.get("task_name"),
									param.get("task_next_time") == null || StringUtils.isBlank(param.get("task_next_time")) ? taskInfo
											.get("task_next_time") : param.get("task_next_time"),
									param.get("type") == null ? taskInfo.get("type") : param.get("type"),
									param.get("seq") == null ? taskInfo.get("seq") : param.get("seq"),
									param.get("status") == null ? taskInfo.get("status") : param.get("status"),
									param.get("del_flag") == null ? taskInfo.get("del_flag") : param.get("del_flag"),
									param.get("task_max_time") == null || StringUtils.isBlank(param.get("task_max_time")) ? taskInfo
											.get("task_max_time") : param.get("task_max_time"), task_id });

			jdbcTemplate.update(
					"update cpt_task_quartz set call_type = ?, call = ?, cron = ?, call_param = ? where task_id = ?",
					new Object[] {
							param.get("call_type") == null || StringUtils.isBlank(param.get("call_type")) ? taskQuartz.get("call_type")
									: param.get("call_type"),
							param.get("call") == null || StringUtils.isBlank(param.get("call")) ? taskQuartz.get("call") : param
									.get("call"),
							param.get("cron") == null || StringUtils.isBlank(param.get("cron")) ? taskQuartz.get("cron") : param
									.get("cron"),
							param.get("call_param") == null || StringUtils.isBlank(param.get("call_param")) ? taskQuartz.get("call_param")
									: param.get("call_param"), task_id });
		} catch (Exception e) {
			throw new ServiceException("修改任务配置信息出错：" + e.getMessage());
		}

	}
}
