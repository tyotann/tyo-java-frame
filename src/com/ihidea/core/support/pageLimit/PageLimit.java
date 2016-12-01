package com.ihidea.core.support.pageLimit;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.base.CoreEntity;
import com.ihidea.core.support.servlet.ServletHolderFilter;
import com.ihidea.core.util.BeanUtilsEx;

public class PageLimit extends CoreEntity {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String PAGE_COUNT = "limit.count";

	public final static String PAGE_SIZE = "limit.length";

	public final static String PAGE_START = "limit.start";

	public static final String PAGE_ENABLE = "limit.enable";

	// 是否需要分页
	private boolean limited;

	// 是否已经分过页了(一般我们查询，如果有后续管理查询，只做第一个分页)
	private boolean isLimited;

	// 当前页号
	private Integer currentPageNo;

	// 每页长度
	private Integer pageLength;

	// 总数据量
	private Integer totalCount;

	// 查询起始行号(从1开始)
	private Integer startRowNo;

	// 查询起始行号(从1开始)
	private Integer endRowNo;

	// 时候只得到记录数
	private boolean onlyGetTotalCnt;

	public PageLimit() {
	}

	public PageLimit(boolean limited, Integer pageStart, Integer pageSize, Integer totalCount) {

		this.limited = limited;

		if (limited) {

			isLimited = false;

			this.currentPageNo = pageStart;
			this.pageLength = pageSize;
			this.totalCount = totalCount;

			// 算出起始行号,从1开始计数
			if (currentPageNo > 1) {
				startRowNo = (currentPageNo - 1) * pageLength + 1;
			} else {
				startRowNo = currentPageNo;
			}

			// 算出结束行号
			endRowNo = startRowNo + pageLength - 1;
		}
	}

	public PageLimit(HttpServletRequest request) {

		// 有可能没经过servletHolder的过滤
		Map<String, Object> paramMap = ServletHolderFilter.getContext() != null ? ServletHolderFilter.getContext().getParamMap() : null;

		limited = (paramMap != null && paramMap.get(PageLimit.PAGE_ENABLE) != null
				&& new Boolean(String.valueOf(paramMap.get(PageLimit.PAGE_ENABLE))));

		if (limited) {

			isLimited = false;

			if (paramMap != null) {
				this.currentPageNo = BeanUtilsEx.convert(paramMap.get(PageLimit.PAGE_START), Integer.class);
				this.pageLength = BeanUtilsEx.convert(
						ObjectUtils.defaultIfNull(paramMap.get(PageLimit.PAGE_SIZE), CoreConstants.SUPPORT_LIMIT_PAGESIZE), Integer.class);
				this.totalCount = BeanUtilsEx.convert(ObjectUtils.defaultIfNull(paramMap.get(PageLimit.PAGE_COUNT), "0"), Integer.class);
			} else {
				this.currentPageNo = Integer.valueOf(request.getParameter(PageLimit.PAGE_START));
				this.pageLength = Integer.valueOf(
						StringUtils.defaultIfEmpty(request.getParameter(PageLimit.PAGE_SIZE), CoreConstants.SUPPORT_LIMIT_PAGESIZE));
				this.totalCount = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter(PageLimit.PAGE_COUNT), "0"));
			}

			// 算出起始行号,从1开始计数
			if (currentPageNo > 1) {
				startRowNo = (currentPageNo - 1) * pageLength + 1;
			} else {
				startRowNo = currentPageNo;
			}

			// 算出结束行号
			endRowNo = startRowNo + pageLength - 1;
		}

		// 清除参数中的分页信息
		if (paramMap != null) {
			paramMap.remove(PageLimit.PAGE_ENABLE);
			paramMap.remove(PageLimit.PAGE_COUNT);
			paramMap.remove(PageLimit.PAGE_SIZE);
			paramMap.remove(PageLimit.PAGE_START);
		}
	}

	public PageLimit(Integer currentPageNo, Integer pageLength, Integer totalCount) {

		this.limited = true;
		this.isLimited = false;

		this.currentPageNo = (currentPageNo == null ? 1 : currentPageNo);
		this.pageLength = (pageLength == null ? Integer.valueOf(CoreConstants.SUPPORT_LIMIT_PAGESIZE) : pageLength);

		this.totalCount = Integer.valueOf((totalCount == null) ? 0 : totalCount.intValue());

		if (currentPageNo.intValue() > 1)
			this.startRowNo = Integer.valueOf((currentPageNo.intValue() - 1) * pageLength.intValue() + 1);
		else {
			this.startRowNo = currentPageNo;
		}

		this.endRowNo = Integer.valueOf(this.startRowNo.intValue() + pageLength.intValue() - 1);
	}

	/**
	 * <pre>
	 * 得到分页起始行号,从1开始
	 * </pre>
	 * 
	 * @return
	 */
	public Integer getStartRowNo() {
		return startRowNo;
	}

	/**
	 * <pre>
	 * 得到分页起始行号,从1开始
	 * </pre>
	 * 
	 * @return
	 */
	public Integer getEndRowNo() {
		return endRowNo;
	}

	/**
	 * 得到总页号
	 * 
	 * @return
	 */
	@ModelAttribute("totalPageCount")
	public Integer getTotalPageCount() {

		Integer totalPageCount = null;

		if (limited) {
			if (totalCount % pageLength == 0) {
				totalPageCount = totalCount / pageLength;
			} else {
				totalPageCount = totalCount / pageLength + 1;
			}
		}
		return totalPageCount;
	}

	/**
	 * 是否有上一页
	 * 
	 * @return
	 */
	@ModelAttribute("hasPreviousPage")
	public boolean getHasPreviousPage() {
		return currentPageNo > 1;
	}

	/**
	 * 是否有下一页
	 * 
	 * @return
	 */
	@ModelAttribute("hasPreviousPage")
	public boolean getHasNextPage() {
		return currentPageNo < this.getTotalPageCount();
	}

	/**
	 * 判断请求是否需要分页
	 * 
	 * @return
	 */
	public boolean limited() {
		return limited;
	}

	/**
	 * 判断是否已经分过页
	 * 
	 * @return
	 */
	public boolean isLimited() {
		return isLimited;
	}

	/**
	 * 默认调用的第一个语句是分页的
	 * 
	 * @param isLimited
	 */
	public void setLimited(boolean isLimited) {
		this.isLimited = isLimited;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public Integer getCurrentPageNo() {
		return currentPageNo;
	}

	public Integer getPageLength() {
		return pageLength;
	}

	public void setTotalCount(Integer totalCount) {

		// 一旦设置总值,则分页结束～
		this.isLimited = true;
		this.totalCount = totalCount;
	}

	public boolean isOnlyGetTotalCnt() {
		return this.onlyGetTotalCnt;
	}

	public void setOnlyGetTotalCnt(boolean onlyGetTotalCnt) {
		this.onlyGetTotalCnt = onlyGetTotalCnt;
	}

	public <T> List<T> limitList(List<T> resultList) {

		try {

			if (resultList != null && resultList.size() > 0 && getStartRowNo() != null) {

				// 设置总记录数
				setTotalCount(resultList.size());

				// 如果开始行数>总记录数,返回null
				if (getStartRowNo() > resultList.size()) {
					return null;
				}

				int endRowNo = getEndRowNo();

				// 如果结束行数>总的记录数,则默认使用记录行数
				if (endRowNo > resultList.size()) {
					endRowNo = resultList.size();
				}

				resultList = resultList.subList(startRowNo - 1, endRowNo);
			}

			return resultList;
		} finally {

			// 分页后设置已经分过页
			setLimited(true);
		}
	}

}
