package com.ihidea.core.support;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ihidea.core.support.cache.CacheSupport;
import com.ihidea.core.util.SystemUtilsEx;

@Service
public class ClusterSupportService {

	private final static String CLUSTER_CACHE = "clusterCache";

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getClusterNodes() {
		return (List<Map<String, Object>>) (Object) CacheSupport.get(ClusterSupportService.CLUSTER_CACHE, Map.class);
	}

	/**
	 * 得到主机信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getHostInfo() {
		return (Map<String, Object>) CacheSupport.get(ClusterSupportService.CLUSTER_CACHE, SystemUtilsEx.getHostId(), Map.class);
	}

	/**
	 * 增加集群节点
	 * @param hostInfo
	 * @return
	 */
	public boolean addClusterNodes(Map<String, Object> hostInfo) {

		boolean result = true;

		if (CacheSupport.get(ClusterSupportService.CLUSTER_CACHE, (String) hostInfo.get("hostId"), Map.class) == null) {
			CacheSupport.put(ClusterSupportService.CLUSTER_CACHE, (String) hostInfo.get("ip"), hostInfo);
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * 移除集群节点
	 * @param hostInfo
	 * @return
	 */
	public boolean removeClusterNodes(String hostId) {

		boolean result = true;

		if (CacheSupport.get(ClusterSupportService.CLUSTER_CACHE, hostId, Map.class) != null) {
			CacheSupport.remove(ClusterSupportService.CLUSTER_CACHE, SystemUtilsEx.getHostId());
		} else {
			result = false;
		}

		return result;
	}

}
