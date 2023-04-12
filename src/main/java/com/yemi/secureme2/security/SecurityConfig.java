package com.yemi.secureme2.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.yemi.secureme2.repositories.UserRepository;

@Configuration
public class SecurityConfig {
    private UserRepository userRepository;

    JwtTokenFilter jwtTokenFilter;

    @Autowired
    public SecurityConfig(UserRepository userRepository, JwtTokenFilter jwtTokenFilter) {
        this.userRepository = userRepository;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.setUsersByUsernameQuery("select email, password, enabled from users where email=?");

        String query = "select u.email, r.role_name "
                + "from users u join role_user ru "
                + "on u.user_id = ru.user_id join roles r "
                + "on r.role_id = ru.role_id "
                + "where u.email=? ";

        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(query);
        // jdbcUserDetailsManager.setRolePrefix("ROLE");

        return jdbcUserDetailsManager;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                cfgr -> cfgr.requestMatchers(HttpMethod.GET, "/home/hello").hasRole("ADMIN"));

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // http.httpBasic();

        http.csrf().disable();

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
        // http.authorizeHttpRequests(configurer ->
        // configurer.requestMatchers(HttpMethod.GET, "/users").hasRole("EMPLOYEE"))
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService(){
           
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                UserDetails user = userRepository.findByEmail(username);
                return user;
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authentication(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
