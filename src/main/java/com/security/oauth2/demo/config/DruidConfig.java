package com.security.oauth2.demo.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.support.spring.stat.DruidStatInterceptor;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DruidConfig implements EnvironmentAware {
    private Logger logger = LoggerFactory.getLogger(DruidConfig.class);
    private Environment propertyResolver;
    @Autowired
    WallFilter wallFilter;


    @Bean(name = "wallConfig")
    WallConfig wallFilterConfig() {
        WallConfig wc = new WallConfig();
        wc.setMultiStatementAllow(true);
        return wc;
    }

    @Bean(name = "wallFilter")
    @DependsOn("wallConfig")
    WallFilter wallFilter(WallConfig wallConfig) {
        WallFilter wfilter = new WallFilter();
        wfilter.setConfig(wallConfig);
        return wfilter;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.propertyResolver = environment;

    }

    @Bean
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        List<Filter> filters = new ArrayList<>();
        filters.add(wallFilter);
//        datasource.setProxyFilters(filters);
//        datasource.setUrl(propertyResolver.getProperty("url"));
//        datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));
//        datasource.setUsername(propertyResolver.getProperty("username"));
//        datasource.setPassword(propertyResolver.getProperty("password"));
//        datasource.setInitialSize(Integer.valueOf(propertyResolver.getProperty("initialSize")));
//        datasource.setMinIdle(Integer.valueOf(propertyResolver.getProperty("minIdle")));
//        datasource.setMaxWait(Long.valueOf(propertyResolver.getProperty("maxWait")));
//        datasource.setMaxActive(Integer.valueOf(propertyResolver.getProperty("maxActive")));
//        datasource.setMinEvictableIdleTimeMillis(
//                Long.valueOf(propertyResolver.getProperty("minEvictableIdleTimeMillis")));
        datasource = Binder.get(this.propertyResolver).bind("spring.datasource", Bindable.of(DruidDataSource.class)).get();
        try {
            datasource.setFilters("stat,wall");
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        return datasource;
    }

    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/druid/*");
        Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put("loginUsername", "lin");// 用户名
        initParameters.put("loginPassword", "123");// 密码
        initParameters.put("resetEnable", "false");// 禁用HTML页面上的“Reset All”功能  
        initParameters.put("allow", "127.0.0.1"); // IP白名单 (没有配置或者为空，则允许所有访问)
        // initParameters.put("deny", "192.168.20.38");// IP黑名单  
        // (存在共同时，deny优先于allow)  
        servletRegistrationBean.setInitParameters(initParameters);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

    // 按照BeanId来拦截配置 用来bean的监控  
    @Bean(value = "druid-stat-interceptor")
    public DruidStatInterceptor DruidStatInterceptor() {
        DruidStatInterceptor druidStatInterceptor = new DruidStatInterceptor();
        return druidStatInterceptor;
    }

    @Bean
    public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
        beanNameAutoProxyCreator.setProxyTargetClass(true);
        // 设置要监控的bean的id  
        //beanNameAutoProxyCreator.setBeanNames("sysRoleMapper","loginController");  
        beanNameAutoProxyCreator.setInterceptorNames("druid-stat-interceptor");
        return beanNameAutoProxyCreator;
    }
}
