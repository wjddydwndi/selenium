package com.example.selenium.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.example.selenium.repository.selenium", sqlSessionFactoryRef = "seleniumSqlSessionFactory")
public class SeleniumDatabaseConfig {
    @Value("${selenium.selenium.datasource.url}")
    private String url;

    @Value("${selenium.selenium.datasource.username}")
    private String username;

    @Value("${selenium.selenium.datasource.password}")
    private String password;

    @Value("${selenium.selenium.datasource.driver-class-name}")
    private String driver;

    @Value(value = "${selenium.selenium.datasource.pool-size}")
    private int poolsize;

    @Bean(name = "seleniumDataSource")
    @Primary
    public DataSource seleniumDataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(poolsize);
        config.setAutoCommit(true);
        config.setConnectionTimeout(3000);
        config.setConnectionTestQuery("select 1");
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return hikariDataSource;
    }

    @Bean(name = "seleniumSqlSessionFactory")
    @Primary
    public SqlSessionFactory seleniumSqlSessionFactory(@Qualifier("seleniumDataSource") DataSource seleniumDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(seleniumDataSource);
        sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis/mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mybatis/mapper/selenium/*Mapper.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "seleniumSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate seleniumSqlSessionTemplate(@Qualifier("seleniumSqlSessionFactory")SqlSessionFactory seleniumSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(seleniumSqlSessionFactory);
    }




}
