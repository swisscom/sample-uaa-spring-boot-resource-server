package com.swisscom.cloud.apc.sample.sampleuaaspringbootresourceserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {
    private static final String RESOURCE_ID = "openid";

    @Override
    public void configure(ResourceServerSecurityConfigurer security) {
        security.resourceId(RESOURCE_ID);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/**").authenticated();
                //.antMatchers("/**").access("#oauth2.hasScope('message.read') or hasRole('CLIENT') or hasRole('MESSAGING_CLIENT')");
    }
}
