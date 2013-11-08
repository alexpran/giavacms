/*
 * Copyright 2013 twiliofaces.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.base.listener;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.giavacms.base.service.CacheService;
import org.giavacms.common.filter.MappingFilter;

@WebListener
public class CacheListener implements ServletContextListener
{

   Logger logger = Logger.getLogger(getClass().getName());

   @Inject
   CacheService cacheService;

   @Override
   public void contextInitialized(ServletContextEvent sce)
   {
      cacheService.writeAll(sce.getServletContext().getInitParameter(MappingFilter.PAGES_PATH_PARAM_NAME));
   }

   @Override
   public void contextDestroyed(ServletContextEvent arg0)
   {
   }

}
