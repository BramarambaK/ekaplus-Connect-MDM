package com.eka.mdm.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Component
public class HandlerInterceptor {

	@Order(1)
	@Bean
	@Autowired
	public MappedInterceptor getPropertyInterceptor(PropertyInterceptor propertyInterceptor) {
		return new MappedInterceptor(new String[] {}, new String[] {"/logger/**","/common/getManifestInfo"}, propertyInterceptor);
	}

	/*@Order(2)
	@Bean
	@Autowired
	public MappedInterceptor getMappedInterceptor(RequestValidatorInterceptor requestValidatorInterceptor) {
		return new MappedInterceptor(new String[] {}, new String[] {"/logger/**","/common/getManifestInfo"}, requestValidatorInterceptor);
	}
	*/
	@Order(2)
	@Bean
	@Autowired
	public MappedInterceptor getServiceKeyMapInterceptor(ServiceKeyMapInterceptor serviceKeyMapInterceptor) {
		return new MappedInterceptor(new String[] {}, new String[] {"/logger/**","/common/getManifestInfo"}, serviceKeyMapInterceptor);
	}

}
