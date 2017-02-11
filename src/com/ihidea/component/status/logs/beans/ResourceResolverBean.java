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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.component.status.logs.model.ApplicationResource;
import com.ihidea.component.status.logs.model.DataSourceInfo;

/**
 * The Class ResourceResolverBean.
 */
public class ResourceResolverBean implements ResourceResolver {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ResourceResolverBean.class);

  /**
   * The default resource prefix for JNDI objects in the global scope: <code>java:</code>.
   */
  public static final String DEFAULT_GLOBAL_RESOURCE_PREFIX = "";

  /**
   * The default resource prefix for objects in a private application scope:
   * <code>java:comp/env/</code>.
   */
  public static final String DEFAULT_RESOURCE_PREFIX = DEFAULT_GLOBAL_RESOURCE_PREFIX
      + "java:comp/env/";

  /** The datasource mappers. */
  private List<DatasourceAccessor> datasourceMappers;

  /**
   * Gets the datasource mappers.
   *
   * @return the datasource mappers
   */
  public List<DatasourceAccessor> getDatasourceMappers() {
    return datasourceMappers;
  }

  /**
   * Sets the datasource mappers.
   *
   * @param datasourceMappers the new datasource mappers
   */
  public void setDatasourceMappers(List<DatasourceAccessor> datasourceMappers) {
    this.datasourceMappers = datasourceMappers;
  }

  @Override
  public boolean supportsPrivateResources() {
    return true;
  }

  @Override
  public boolean supportsGlobalResources() {
    return true;
  }

  @Override
  public boolean supportsDataSourceLookup() {
    return true;
  }

  /**
   * Resolves a JNDI resource name by prepending the scope-appropriate prefix.
   *
   * @param name the JNDI name of the resource
   * @param global whether to use the global prefix
   * @return the JNDI resource name with the prefix appended
   * @see #DEFAULT_GLOBAL_RESOURCE_PREFIX
   * @see #DEFAULT_RESOURCE_PREFIX
   */
  protected static String resolveJndiName(String name, boolean global) {
    return (global ? DEFAULT_GLOBAL_RESOURCE_PREFIX : DEFAULT_RESOURCE_PREFIX) + name;
  }

  /**
   * Gets the string attribute.
   *
   * @param server the server
   * @param objectName the object name
   * @param attributeName the attribute name
   * @return the string attribute
   */
  private String getStringAttribute(MBeanServer server, ObjectName objectName, String attributeName) {

    try {
      return (String) server.getAttribute(objectName, attributeName);
    } catch (Exception e) {
      logger.error("Error getting attribute '{}' from '{}'", attributeName, objectName, e);
      return null;
    }
  }


}
