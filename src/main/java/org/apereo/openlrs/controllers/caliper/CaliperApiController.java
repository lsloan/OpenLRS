/**
 * 
 */
package org.apereo.openlrs.controllers.caliper;

import java.io.IOException;

import org.apereo.openlrs.KeyManager;
import org.apereo.openlrs.Tenant;
import org.apereo.openlrs.exceptions.caliper.InvalidCaliperFormatException;
import org.apereo.openlrs.model.event.v2.EventEnvelope;
import org.apereo.openlrs.utils.AuthorizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ggilbert
 *
 */
@RestController
@RequestMapping("/v1")
public class CaliperApiController {
  
  @Autowired private ObjectMapper objectMapper;
  @Autowired KeyManager keyManager;

  
  @RequestMapping(value = {"/caliper"}, method = RequestMethod.POST,
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public void postHandler(@RequestBody String json, @RequestHeader(value="Authorization") String authorizationHeader)
        throws JsonProcessingException, IOException, InvalidCaliperFormatException {
    try {
    	    
	    String key = AuthorizationUtils.getKeyFromHeader(authorizationHeader);
	    Tenant tenant = keyManager.getTenantForKey(key);
	     
	    //TODO Get do something with the tenant.getName()		    	
	    EventEnvelope ee = objectMapper.readValue(json,
	        new TypeReference<EventEnvelope>() {});

    } 
    catch (Exception e) {
      e.printStackTrace();
    }

  }

}
