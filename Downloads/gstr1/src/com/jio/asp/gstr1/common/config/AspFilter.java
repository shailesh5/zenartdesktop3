package com.jio.asp.gstr1.common.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.jio.asp.gstr1.v30.constant.Gstr1Constants;

/**
 * 
 * [Introduce a new class for Session Mgmt feature]
 * Class is extending servlet filter and responsible to modify the request header before
 * calling of common interceptor. 
 * 
 * Introduce a new header ip-user which will be use for logging purpose and will contain the ip address
 * sent by the user.
 * 
 * Introduce a constant ip address in property file. "ip-usr" header will modify with constant ip address
 * which will use for auth process.
 */

@Configuration
@PropertySource(value = {"${GSTR1_APP_PROP_PATH_EXT}"})
public class AspFilter implements javax.servlet.Filter{
	
	@Autowired
	private Environment environment;
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {
	
		ServletRequestWrapper httpReq = new ServletRequestWrapper((HttpServletRequest)req);
		HttpServletResponse httpRes = (HttpServletResponse)res;
		
		if(httpReq.getHeader(Gstr1Constants.HEADER_IP_USR) == null){
			httpReq.addHeader(Gstr1Constants.HEADER_IP_USR,"");
			httpReq.addHeader(Gstr1Constants.HEADER_IP_USER, httpReq.getHeader(Gstr1Constants.HEADER_IP_USR));
		}else{
			httpReq.addHeader(Gstr1Constants.HEADER_IP_USER, httpReq.getHeader(Gstr1Constants.HEADER_IP_USR));
			httpReq.addHeader(Gstr1Constants.HEADER_IP_USR, environment.getProperty(Gstr1Constants.GSTN_REQ_IP));
		}
		
		filterChain.doFilter(httpReq, httpRes);
		
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());      
	}
}
