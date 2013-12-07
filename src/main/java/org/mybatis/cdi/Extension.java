/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.cdi;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

public class Extension implements javax.enterprise.inject.spi.Extension {

  private final Map<Class, MapperBean> mappers = new HashMap<Class, MapperBean>();

  public <X> void processAnnotatedType(@Observes ProcessInjectionTarget<X> event, BeanManager beanManager) {
    InjectionTarget<X> it = event.getInjectionTarget();
    for (InjectionPoint ip : it.getInjectionPoints()) {
      Mapper annotation = ip.getAnnotated().getAnnotation(Mapper.class);
      if (annotation != null) {
        Class cls = (Class) ip.getAnnotated().getBaseType();
        if (!mappers.containsKey(cls)) {
          String sessionName = "".equals(annotation.manager()) ? null : annotation.manager();
          mappers.put(cls, new MapperBean(cls, sessionName, beanManager));
        }
      }
    }
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
    final Logger logger = Logger.getLogger(getClass().getName());
    logger.log(Level.INFO, "MyBatis CDI Module - Activated");
    for (Bean bean : mappers.values()) {
      logger.log(Level.INFO, "MyBatis CDI Module - Mapper dependency discovered: {0}", bean.getName());
      abd.addBean(bean);
    }
    mappers.clear();
  }

}