package com.ihidea.core.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 核心服务类
 */
@Service
public abstract class CoreService {

	protected Logger logger = LoggerFactory.getLogger(getClass());
}
