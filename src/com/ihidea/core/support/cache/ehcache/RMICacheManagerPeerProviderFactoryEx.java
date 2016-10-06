package com.ihidea.core.support.cache.ehcache;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.MulticastRMICacheManagerPeerProvider;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import net.sf.ehcache.util.PropertyUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author TYOTANN
 */
public class RMICacheManagerPeerProviderFactoryEx extends RMICacheManagerPeerProviderFactory {

	private static final Logger LOG = LoggerFactory.getLogger(RMICacheManagerPeerProviderFactory.class.getName());

	protected CacheManagerPeerProvider createAutomaticallyConfiguredCachePeerProvider(CacheManager cacheManager, Properties properties)
			throws IOException {
		String hostName = PropertyUtil.extractAndLogProperty("hostName", properties);
		InetAddress hostAddress = null;
		if ((hostName != null) && (hostName.length() != 0)) {
			hostAddress = InetAddress.getByName(hostName);
			if (hostName.equals("localhost")) {
				LOG.warn("Explicitly setting the multicast hostname to 'localhost' is not recommended. It will only work if all CacheManager peers are on the same machine.");
			}
		} else {

			// TODO
			InetAddress localhostInetAddress = InetAddress.getLocalHost();
			if (StringUtils.isNotBlank(localhostInetAddress.getHostAddress()) && !"127.0.0.1".equals(localhostInetAddress.getHostAddress())) {
				hostAddress = localhostInetAddress;
			}
		}

		if (hostAddress != null) {
			LOG.info("RMICacheManagerPeerProviderFactoryEx绑定网卡:" + hostAddress);
		}

		String groupAddressString = PropertyUtil.extractAndLogProperty("multicastGroupAddress", properties);
		InetAddress groupAddress = InetAddress.getByName(groupAddressString);
		String multicastPortString = PropertyUtil.extractAndLogProperty("multicastGroupPort", properties);
		Integer multicastPort = Integer.valueOf(multicastPortString);
		String packetTimeToLiveString = PropertyUtil.extractAndLogProperty("timeToLive", properties);
		Integer timeToLive;
		if (packetTimeToLiveString == null) {
			timeToLive = Integer.valueOf(1);
			LOG.debug("No TTL set. Setting it to the default of 1, which means packets are limited to the same subnet.");
		} else {
			timeToLive = Integer.valueOf(packetTimeToLiveString);
			if ((timeToLive.intValue() < 0) || (timeToLive.intValue() > 255)) {
				throw new CacheException("The TTL must be set to a value between 0 and 255");
			}
		}
		return new MulticastRMICacheManagerPeerProvider(cacheManager, groupAddress, multicastPort, timeToLive, hostAddress);
	}
}
