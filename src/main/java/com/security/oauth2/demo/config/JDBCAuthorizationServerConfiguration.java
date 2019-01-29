package com.security.oauth2.demo.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.JdbcClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;
@Configuration
@ConditionalOnClass(EnableJDBCAuthorizationServer.class)
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class JDBCAuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter implements AuthorizationServerConfigurer {

    private  AuthenticationManager authenticationManager;
    @Resource
    private  TokenStore tokenStore;
    private  AccessTokenConverter tokenConverter;
    @Resource
    private  AuthorizationServerProperties properties;

    public JDBCAuthorizationServerConfiguration(ObjectProvider<AccessTokenConverter> tokenConverter, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.tokenConverter=tokenConverter.getIfAvailable();
    }

    @Resource
    private DataSource dataSource;
    @Bean
    public TokenStore tokenStore(RedisConnectionFactory redisConnectionFactory){
       return new Boot2RedisTokenStore(redisConnectionFactory);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        JdbcClientDetailsServiceBuilder jdbc = clients.jdbc(dataSource);
        ClientDetailsService build = jdbc.build();
        clients.withClientDetails(build);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        if (this.tokenConverter != null) {
            endpoints.accessTokenConverter(this.tokenConverter);
        }
        if (this.tokenStore != null) {
            endpoints.tokenStore(this.tokenStore);
        }
        if (this.authenticationManager!=null) {
            endpoints.authenticationManager(this.authenticationManager);
        }
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(NoOpPasswordEncoder.getInstance());
        if (this.properties.getCheckTokenAccess() != null) {
            security.checkTokenAccess(this.properties.getCheckTokenAccess());
        }
        if (this.properties.getTokenKeyAccess() != null) {
            security.tokenKeyAccess(this.properties.getTokenKeyAccess());
        }
        if (this.properties.getRealm() != null) {
            security.realm(this.properties.getRealm());
        }
    }

}
