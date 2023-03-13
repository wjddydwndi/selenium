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

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.example.selenium.repository.biztalk", sqlSessionFactoryRef = "biztalkSqlSessionFactory")
public class BiztalkDatabaseConfig {
    @Value("${selenium.biztalk.datasource.url}")
    private String url;

    @Value("${selenium.biztalk.datasource.username}")
    private String username;

    @Value("${selenium.biztalk.datasource.password}")
    private String password;

    @Value("${selenium.biztalk.datasource.driver-class-name}")
    private String driver;

    @Value(value = "${selenium.biztalk.datasource.pool-size}")
    private int poolsize;

    @Bean(name = "biztalkDataSource")
    public DataSource biztalkDataSource() {
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

    @Bean(name = "biztalkSqlSessionFactory")
    public SqlSessionFactory biztalkSqlSessionFactory(@Qualifier("biztalkDataSource") DataSource biztalkDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(biztalkDataSource);
        sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis/mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mybatis/mapper/biztalk/*Mapper.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "biztalkSqlSessionTemplate")
    public SqlSessionTemplate biztalkSqlSessionTemplate(@Qualifier("biztalkSqlSessionFactory") SqlSessionFactory biztalkSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(biztalkSqlSessionFactory);
    }




}
