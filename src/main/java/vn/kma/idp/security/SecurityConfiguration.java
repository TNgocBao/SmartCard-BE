package vn.kma.idp.security;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean SecurityFilterChain security(HttpSecurity http,SessionGuard guard,RateLimitFilter limiter,RequestTraceFilter trace) throws Exception {
        return http.csrf(csrf -> csrf.disable()) // Desktop bearer-only API; no ambient authentication cookies.
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.requestMatchers("/api/v1/status","/api/v1/health","/actuator/health","/oauth2/jwks","/api/v1/auth/**").permitAll().anyRequest().authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> vn.kma.idp.shared.ErrorResponses.write(req,res,401,"AUTHENTICATION_REQUIRED")).accessDeniedHandler((req,res,ex) -> vn.kma.idp.shared.ErrorResponses.write(req,res,403,"ACCESS_DENIED")))
        .addFilterBefore(guard,org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(limiter,SessionGuard.class)
        .addFilterBefore(trace,RateLimitFilter.class)
        .headers(h -> h.cacheControl(c -> {
        }
        ).frameOptions(f -> f.deny()))
        .build();
    }
    @Bean FilterRegistrationBean<RequestTraceFilter> disableTraceAutoRegistration(RequestTraceFilter filter){var bean=new FilterRegistrationBean<>(filter);bean.setEnabled(false);return bean;}
    @Bean FilterRegistrationBean<SessionGuard> disableGuardAutoRegistration(SessionGuard filter) {
        var bean=new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
    @Bean FilterRegistrationBean<RateLimitFilter> disableLimiterAutoRegistration(RateLimitFilter filter) {
        var bean=new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
}
