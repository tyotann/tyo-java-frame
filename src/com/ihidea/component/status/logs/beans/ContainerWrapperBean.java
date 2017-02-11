/**
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package com.ihidea.component.status.logs.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.component.status.logs.TomcatContainer;
import com.ihidea.component.status.logs.model.ApplicationResource;

/**
 * This class wires support for Tomcat "privileged" context functionality into Spring. If
 * application context is privileged Tomcat would always call servlet.setWrapper method on each
 * request. ContainerWrapperBean wires the passed wrapper to the relevant Tomcat container adapter
 * class, which in turn helps the Probe to interpret the wrapper.
 */
public class ContainerWrapperBean {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ContainerWrapperBean.class);

  /** The tomcat container. */
  private TomcatContainer tomcatContainer;

  /** The lock. */
  private final Object lock = new Object();

  /** List of class names to adapt particular Tomcat implementation to TomcatContainer interface. */
  private List<String> adapterClasses;

  /** The resource resolver. */
  private ResourceResolver resourceResolver;

  /** The force first adapter. */
  private boolean forceFirstAdapter;

  /** The resource resolvers. */
  private Map<String, ResourceResolver> resourceResolvers;

  /**
   * Checks if is force first adapter.
   *
   * @return true, if is force first adapter
   */
  public boolean isForceFirstAdapter() {
    return forceFirstAdapter;
  }

  /**
   * Sets the force first adapter.
   *
   * @param forceFirstAdapter the new force first adapter
   */
  public void setForceFirstAdapter(boolean forceFirstAdapter) {
    this.forceFirstAdapter = forceFirstAdapter;
  }

  /**
   * Gets the tomcat container.
   *
   * @return the tomcat container
   */
  public TomcatContainer getTomcatContainer() {
    return tomcatContainer;
  }

  /**
   * Gets the adapter classes.
   *
   * @return the adapter classes
   */
  public List<String> getAdapterClasses() {
    return adapterClasses;
  }

  /**
   * Sets the adapter classes.
   *
   * @param adapterClasses the new adapter classes
   */
  public void setAdapterClasses(List<String> adapterClasses) {
    this.adapterClasses = adapterClasses;
  }

  /**
   * Gets the resource resolver.
   *
   * @return the resource resolver
   */
  public ResourceResolver getResourceResolver() {
    if (resourceResolver == null) {
      if (System.getProperty("jboss.server.name") != null) {
        resourceResolver = resourceResolvers.get("jboss");
        logger.info("Using JBOSS resource resolver");
      } else {
        resourceResolver = resourceResolvers.get("default");
        logger.info("Using DEFAULT resource resolver");
      }
    }
    return resourceResolver;
  }

  /**
   * Gets the resource resolvers.
   *
   * @return the resource resolvers
   */
  public Map<String, ResourceResolver> getResourceResolvers() {
    return resourceResolvers;
  }

  /**
   * Sets the resource resolvers.
   *
   * @param resourceResolvers the resource resolvers
   */
  public void setResourceResolvers(Map<String, ResourceResolver> resourceResolvers) {
    this.resourceResolvers = resourceResolvers;
  }

  /**
   * Filter data sources.
   *
   * @param resources the resources
   * @param dataSources the data sources
   */
  protected void filterDataSources(List<ApplicationResource> resources,
      List<ApplicationResource> dataSources) {

    for (ApplicationResource res : resources) {
      if (res.getDataSourceInfo() != null) {
        dataSources.add(res);
      }
    }
  }

}
