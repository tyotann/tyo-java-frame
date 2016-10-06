package com.ihidea.core.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * @author TYOTANN
 */
@Controller
public abstract class CoreController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
}
