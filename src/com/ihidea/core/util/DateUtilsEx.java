package com.ihidea.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.orm.mybatis3.interceptor.dialect.DialectInterceptor;

/**
 * 日期共通方法
 * 
 * @author TYOTANN
 */
public final class DateUtilsEx {

	// 时间格式到年
	public static final String DATE_FORMAT_YEAR = "yyyy";

	// 时间格式到月
	public static final String DATE_FORMAT_MONTH = "yyyy.MM";

	// 时间格式到天
	public static final String DATE_FORMAT_DAY = "yyyy.MM.dd";

	// 时间格式到天（中文版）
	public static final String DATE_FORMAT_DAY_C = "yyyy年MM月dd日";

	// 时间格式到秒
	public static final String DATE_FORMAT_SEC = "yyyy.MM.dd HH:mm:ss";

	// 时间格式到秒,没有任何断隔符号,纯数字
	public static final String DATE_FORMAT_SEC_U = "yyyyMMddHHmmss";

	/**
	 * <pre>
	 * 得到当前系统时间
	 * 如果应用服务器与数据库服务器时间同步的话,可以直接new Date()
	 * </pre>
	 * 
	 * @return
	 */
	public static Date getSysDate() {

		JdbcTemplate jdbcTemplate = SpringContextLoader.getBean(JdbcTemplate.class);

		return jdbcTemplate.queryForObject(DialectInterceptor.getDialect().getSysdateString(), Date.class);
	}

	/**
	 * 得到传入日期的当月第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getFDayInMonth(Date date) {
		return DateUtils.truncate(date, Calendar.MONTH);
	}

	/**
	 * 得到传入日期的当年的第一天
	 * 
	 * @return
	 */
	public static Date getFDayInCurrentYear(Date date) {
		return DateUtils.truncate(date, Calendar.YEAR);
	}

	/**
	 * 日期格式化成字符
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatToString(Date date, String format) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 字符格式化成时间
	 * 
	 * @param dateString
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date formatToDate(String dateString, String format) throws ParseException {

		if (StringUtils.isBlank(dateString)) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.parse(dateString);
	}

	/**
	 * 得到当天的星期。
	 * 
	 * @return
	 */
	public static String getDayofWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String weekday = "";
		int week_day = calendar.get(Calendar.DAY_OF_WEEK);
		if (week_day == 1) {
			weekday = "星期天";
		} else if (week_day == 2) {
			weekday = "星期一";
		} else if (week_day == 3) {
			weekday = "星期二";
		} else if (week_day == 4) {
			weekday = "星期三";
		} else if (week_day == 5) {
			weekday = "星期四";
		} else if (week_day == 6) {
			weekday = "星期五";
		} else if (week_day == 7) {
			weekday = "星期六";
		}
		return weekday;
	}

	/**
	 * 得到当年的最后一天。
	 * 
	 * @return
	 */
	public static Date getEDayInCurrentYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), 11, 31);
		return calendar.getTime();
	}

	/**
	 * 得到下一年。
	 * 
	 * @return
	 */
	public static String getNextYear() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 得到当前年。
	 * 
	 * @return
	 */
	public static String getCurrYear() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 得到当月。
	 * 
	 * @return
	 */
	public static String getCurrMothe() {
		DateFormat dateFormat = new SimpleDateFormat("MM");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 得到上一年。
	 * 
	 * @return
	 */
	public static String getHistoryYear() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 得到日期的+-月份
	 * 
	 * @return
	 */
	public static Date addMonths(Date date, int month) {
		return DateUtils.addMonths(date, month);
	}

	/**
	 * 得到下一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getNextDate(Date date) {
		return DateUtils.addDays(date, 1);
	}

	public static Date getPreDate(Date date) {
		return DateUtils.addDays(date, -1);
	}

	public static String getDateString(int year, int month, int data, int num) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_DAY);
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, data);
		calendar.add(Calendar.MONTH, num);
		return dateFormat.format(calendar.getTime());
	}

	public static String getDateString(int year, int month, int data) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_DAY);
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, data);

		return dateFormat.format(calendar.getTime());
	}

	public static int getDayBetween(Date startDate, Date endDate) {

		long to = startDate.getTime();
		long end = endDate.getTime();
		return new BigDecimal((end - to) / (1000 * 60 * 60 * 24)).setScale(0, RoundingMode.DOWN).intValue();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getNextDate(new Date()));

	}
}
