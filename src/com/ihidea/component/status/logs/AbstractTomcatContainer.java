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
package com.ihidea.component.status.logs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.naming.ContextBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction layer to implement some functionality, which is common between different container
 * adapters.
 */
public abstract class AbstractTomcatContainer implements TomcatContainer {

  /** The logger. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** The host. */
  protected Host host;

  /** The connectors. */
  protected Connector[] connectors;

  /** The deployer o name. */
  protected ObjectName deployerOName;

  /** The mbean server. */
  protected MBeanServer mbeanServer;

  @Override
  public File getAppBase() {
    File base = new File(host.getAppBase());
    if (!base.isAbsolute()) {
      base = new File(System.getProperty("catalina.base"), host.getAppBase());
    }
    return base;
  }

  @Override
  public String getConfigBase() {
    File configBase = new File(System.getProperty("catalina.base"), "conf");
    Container baseHost = null;
    Container thisContainer = host;
    while (thisContainer != null) {
      if (thisContainer instanceof Host) {
        baseHost = thisContainer;
      }
      thisContainer = thisContainer.getParent();
    }
    if (baseHost != null) {
      configBase = new File(configBase, baseHost.getName());
    }
    return configBase.getAbsolutePath();
  }

  @Override
  public String getHostName() {
    return host.getName();
  }

  @Override
  public String getName() {
    return host.getParent().getName();
  }

  @Override
  public List<Context> findContexts() {
    List<Context> results = new ArrayList<>();
    for (Container child : host.findChildren()) {
      if (child instanceof Context) {
        results.add((Context) child);
      }
    }
    return results;
  }

  @Override
  public List<Connector> findConnectors() {
    return Collections.unmodifiableList(Arrays.asList(connectors));
  }

  @Override
  public boolean installContext(String contextName) throws Exception {
    contextName = formatContextName(contextName);
    String contextFilename = formatContextFilename(contextName);
    File contextFile = new File(getConfigBase(), contextFilename + ".xml");
    installContextInternal(contextName, contextFile);
    return findContext(contextName) != null;
  }

  @Override
  public void stop(String name) throws Exception {
    Context ctx = findContext(name);
    if (ctx != null) {
      ctx.stop();
    }
  }

  @Override
  public void start(String name) throws Exception {
    Context ctx = findContext(name);
    if (ctx != null) {
      ctx.start();
    }
  }

  @Override
  public void remove(String name) throws Exception {
    name = formatContextName(name);
    Context ctx = findContext(name);

    if (ctx != null) {

      try {
        stop(name);
      } catch (Exception e) {
        logger.info("Stopping '{}' threw this exception:", name, e);
      }

      File appDir;
      File docBase = new File(ctx.getDocBase());

      if (!docBase.isAbsolute()) {
        appDir = new File(getAppBase(), ctx.getDocBase());
      } else {
        appDir = docBase;
      }

      logger.debug("Deleting '{}'", appDir.getAbsolutePath());
      Utils.delete(appDir);

      String warFilename = formatContextFilename(name);
      File warFile = new File(getAppBase(), warFilename + ".war");
      logger.debug("Deleting '{}'", warFile.getAbsolutePath());
      Utils.delete(warFile);

      File configFile = getConfigFile(ctx);
      if (configFile != null) {
        logger.debug("Deleting " + configFile.getAbsolutePath());
        Utils.delete(configFile);
      }

      removeInternal(name);
    }
  }

  /**
   * Removes the internal.
   *
   * @param name the name
   * @throws Exception the exception
   */
  private void removeInternal(String name) throws Exception {
    checkChanges(name);
  }

  @Override
  public void installWar(String name, URL url) throws Exception {
    checkChanges(name);
  }

  /**
   * Install context internal.
   *
   * @param name the name
   * @param config the config
   * @throws Exception the exception
   */
  private void installContextInternal(String name, File config) throws Exception {
    checkChanges(name);
  }

  @Override
  public Context findContext(String name) {
    String safeName = formatContextName(name);
    if (safeName == null) {
      return null;
    }
    Context result = findContextInternal(safeName);
    if (result == null && "".equals(safeName)) {
      result = findContextInternal("/");
    }
    return result;
  }

  @Override
  public String formatContextName(String name) {
    if (name == null) {
      return null;
    }
    String result = name.trim();
    if (!result.startsWith("/")) {
      result = "/" + result;
    }
    if ("/".equals(result) || "/ROOT".equals(result)) {
      result = "";
    }
    return result;
  }

  @Override
  public String formatContextFilename(String contextName) {
    if (contextName == null) {
      return null;
    } else if ("".equals(contextName)) {
      return "ROOT";
    } else if (contextName.startsWith("/")) {
      return contextName.substring(1);
    } else {
      return contextName;
    }
  }

  @Override
  public void discardWorkDir(Context context) {
    if (context instanceof StandardContext) {
      StandardContext standardContext = (StandardContext) context;
      logger.info("Discarding '{}'", standardContext.getWorkPath());
      Utils.delete(new File(standardContext.getWorkPath(), "org"));
    } else {
      logger.error("context '{}' is not an instance of '{}', expected StandardContext",
          context.getName(), context.getClass().getName());
    }
  }

  @Override
  public boolean getAvailable(Context context) {
    return context.getState().isAvailable();
  }

  @Override
  public File getConfigFile(Context context) {
    URL configUrl = context.getConfigFile();
    if (configUrl != null) {
      try {
        URI configUri = configUrl.toURI();
        if ("file".equals(configUri.getScheme())) {
          return new File(configUri.getPath());
        }
      } catch (URISyntaxException ex) {
        logger.error("Could not convert URL to URI: '{}'", configUrl, ex);
      }
    }
    return null;
  }

  @Override
  public void bindToContext(Context context) throws NamingException {
    changeContextBinding(context, true);
  }

  @Override
  public void unbindFromContext(Context context) throws NamingException {
    changeContextBinding(context, false);
  }

  /**
   * Change context binding.
   *
   * @param context the context
   * @param bind the bind
   * @throws NamingException the naming exception
   */
  private void changeContextBinding(Context context, boolean bind) throws NamingException {
    Object token = getNamingToken(context);
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (bind) {
      ContextBindings.bindClassLoader(context, token, loader);
    } else {
      ContextBindings.unbindClassLoader(context, token, loader);
    }
  }

  /**
   * Find context internal.
   *
   * @param name the context name
   * @return the context
   */
  protected Context findContextInternal(String name) {
    return (Context) host.findChild(name);
  }

  /**
   * Check changes.
   *
   * @param name the name
   * @throws Exception the exception
   */
  protected void checkChanges(String name) throws Exception {
    Boolean result =
        (Boolean) mbeanServer.invoke(deployerOName, "isServiced", new String[] {name},
            new String[] {"java.lang.String"});
    if (!result) {
      mbeanServer.invoke(deployerOName, "addServiced", new String[] {name},
          new String[] {"java.lang.String"});
      try {
        mbeanServer.invoke(deployerOName, "check", new String[] {name},
            new String[] {"java.lang.String"});
      } finally {
        mbeanServer.invoke(deployerOName, "removeServiced", new String[] {name},
            new String[] {"java.lang.String"});
      }
    }
  }

  /**
   * Returns the security token required to bind to a naming context.
   *
   * @param context the catalina context
   * @return the security token for use with <code>ContextBindings</code>
   */
  protected abstract Object getNamingToken(Context context);

  /**
   * Creates the valve.
   *
   * @return the valve
   */
  protected abstract Valve createValve();

}
