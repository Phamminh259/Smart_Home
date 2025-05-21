package com.ftn.uns.ac.rs.smarthome.config;

import com.ftn.uns.ac.rs.smarthome.auth.RestAuthenticationEntryPoint;
import com.ftn.uns.ac.rs.smarthome.auth.TokenAuthenticationFilter;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import com.ftn.uns.ac.rs.smarthome.utils.TokenUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// Bảo mật toàn bộ hệ thống
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

	private final IUserService userService;
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final TokenUtils tokenUtils;

	public WebSecurityConfig(IUserService userService, RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                             TokenUtils tokenUtils){
		this.tokenUtils = tokenUtils;
		this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
		this.userService = userService;
	}
//Mã hóa mật khẩu
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

 	@Bean
 	public DaoAuthenticationProvider authenticationProvider() {
 	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
 	    authProvider.setUserDetailsService(userService);
 	    authProvider.setPasswordEncoder(passwordEncoder());
 	    return authProvider;
 	}
 

 	@Bean
 	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
 	    return authConfig.getAuthenticationManager();
 	}
//Kiểm tra JWT từ header request
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);
    	http.authorizeRequests()
				.antMatchers(HttpMethod.POST, "/api/user/register").permitAll()
				.antMatchers(HttpMethod.GET, "/images/**").permitAll()

				.antMatchers(HttpMethod.POST, "/api/user/sendPasswordResetEmail").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/passwordReset").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/registerAdmin").hasAnyRole("SUPERADMIN")
				.antMatchers(HttpMethod.POST, "/api/user/login").permitAll()
				.antMatchers(HttpMethod.GET, "/api/user/activate/*").permitAll()
				.antMatchers(HttpMethod.GET, "/api/devices/ownerAll").hasAnyRole("USER")
				.antMatchers(HttpMethod.GET, "/api/devices/measurements").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/devices/commands").hasAnyRole("USER")
				.antMatchers(HttpMethod.POST, "/api/property/registerProperty").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/property/approvedProperties/*").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/property/allProperties").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/property/unapprovedProperties").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/property/deny/*").hasAnyRole("USER", "SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/property/approve/*").hasAnyRole("SUPERADMIN", "ADMIN")
				.antMatchers(HttpMethod.GET, "/api/user/info").hasAnyRole("ADMIN", "SUPERADMIN","USER")
				.antMatchers(HttpMethod.GET, "/api/user/info/*").hasAnyRole("USER")
				.antMatchers(HttpMethod.PUT, "/api/devices/shareControl/*").hasAnyRole("USER")
				.antMatchers(HttpMethod.PUT, "/api/devices/shareControl/property/*").hasAnyRole("USER")
				.antMatchers(HttpMethod.GET, "/api/devices/shareControl/get/*").hasAnyRole("USER")
				.antMatchers(HttpMethod.GET, "/api/devices/shareControl/get/property/*").hasAnyRole("USER")
				.antMatchers(HttpMethod.GET, "/api/devices/shared").hasAnyRole("USER")
				.anyRequest().authenticated().and()
				.cors().and()
				.addFilterBefore(new TokenAuthenticationFilter(tokenUtils,  userService), BasicAuthenticationFilter.class);
		http.csrf().disable();
		http.headers().frameOptions().disable();

        http.authenticationProvider(authenticationProvider());
		return http.build();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//    	return (web) -> web.ignoring().antMatchers(HttpMethod.POST,"/socket/**").antMatchers(HttpMethod.GET,"/swagger-ui/**",
//				"/v3/**")
//    			.antMatchers(HttpMethod.GET, "/", "/api/image/**", "/webjars/**", "/*.html", "favicon.ico",
//    			"/**/*.html", "/**/*.css", "/**/*.js","/socket/**")
//				.antMatchers(HttpMethod.DELETE, "/api/image/**")
//				.antMatchers(HttpMethod.POST, "/api/image/**");
//    }



	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
				.antMatchers(HttpMethod.GET, "/images/**") // 💥 RẤT QUAN TRỌNG: Bỏ qua security check cho ảnh
				.antMatchers(HttpMethod.POST,"/socket/**")
				.antMatchers(HttpMethod.GET,"/swagger-ui/**", "/v3/**")
				.antMatchers(HttpMethod.GET, "/", "/api/image/**", "/webjars/**", "/*.html", "favicon.ico",
						"/**/*.html", "/**/*.css", "/**/*.js", "/socket/**")
				.antMatchers(HttpMethod.DELETE, "/api/image/**")
				.antMatchers(HttpMethod.POST, "/api/image/**");
	}






//	@Bean
//	public CorsConfigurationSource corsConfigurationSource() {
//		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // React dev server
//		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		configuration.setAllowedHeaders(Arrays.asList("*"));
//		configuration.setAllowCredentials(true); // Nếu frontend gửi cookie/auth
//
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", configuration);
//		return source;
//	}


}
