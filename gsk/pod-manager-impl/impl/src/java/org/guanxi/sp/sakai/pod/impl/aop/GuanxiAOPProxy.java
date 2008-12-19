/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.impl.aop;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.aop.support.Perl5RegexpMethodPointcut;
import org.springframework.aop.ClassFilter;

/**
 * This class sets up any AOP proxies that are required by the GSK.
 *
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiAOPProxy implements BeanPostProcessor {
  /**
   * Spring will call this method after it has loaded a bean but before it has called the bean's
   * init method. We don't use this functionality as we need the beans to be first class citizens.
   *
   * @param bean The class of the loaded Spring bean.
   * @param beanName The name of the bean as defined in a components.xml file or other config file.
   * @return The object to use as an AOP proxy. In this case, we always return the bean unmodified.
   * @throws BeansException if an error occurs.
   */
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  /**
   * Spring will call this method after it has loaded a bean and after it has called the bean's
   * init method.
   *
   * @param bean The class of the loaded and initialised Spring bean.
   * @param beanName The name of the bean as defined in a components.xml file or other config file.
   * @return The object to use as an AOP proxy.
   * @throws BeansException if an error occurs.
   */
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    // Proxy the UsageSessionService
    if (beanName.equals("org.sakaiproject.event.api.UsageSessionService")) {
      ProxyFactory factory = new ProxyFactory(bean);
      DefaultPointcutAdvisor pointCutAdvisor = new DefaultPointcutAdvisor();
      ClassFilter classFilter = new RootClassFilter(bean.getClass());
      Perl5RegexpMethodPointcut pointCut = new Perl5RegexpMethodPointcut();
      pointCut.setClassFilter(classFilter);
      pointCut.setPattern(".*logout.*");
      pointCutAdvisor.setPointcut(pointCut);
      pointCutAdvisor.setAdvice(new GuanxiAOPInterceptor());
      factory.addAdvisor(pointCutAdvisor);
      return factory.getProxy();
    }
    else {
      return bean;
    }
  }
}
