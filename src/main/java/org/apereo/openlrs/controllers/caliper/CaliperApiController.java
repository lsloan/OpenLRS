/**
 * 
 */
package org.apereo.openlrs.controllers.caliper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.KeyManager;
import org.apereo.openlrs.Tenant;
import org.apereo.openlrs.exceptions.caliper.InvalidCaliperFormatException;
import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.model.event.v2.EventEnvelope;
import org.apereo.openlrs.utils.AuthorizationUtils;
import org.apereo.openlrs.storage.v2.Writer;
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
@RequestMapping("/v1/caliper")
public class CaliperApiController {
  
  @Autowired private ObjectMapper objectMapper;
  @Autowired KeyManager keyManager;

  @Autowired private Writer writer;
  
  @RequestMapping(value = { "", "/" },
      method = RequestMethod.POST,
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public List<String> postHandler(@RequestBody String json, @RequestHeader(value="Authorization") String authorizationHeader)
        throws JsonProcessingException, IOException, InvalidCaliperFormatException {
    List<String> ids = null;
    String key = AuthorizationUtils.getKeyFromHeader(authorizationHeader);
    
    if (StringUtils.isNotBlank(key)) {
      Tenant tenant = keyManager.getTenantForKey(key);       

      if (tenant != null) {
        EventEnvelope ee = objectMapper.readValue(json,
            new TypeReference<EventEnvelope>() {});
        
        if (ee != null) {
          List<Event> events = ee.getData();
          if (events != null && !events.isEmpty()) {
            ids = new ArrayList<String>();
            for (Event e : events) {
              if (StringUtils.isBlank(e.getId())) {
                e.setId(UUID.randomUUID().toString());
              }
              ids.add(writer.save(e, String.valueOf(tenant.getId())).getId());
            }
          }
        }
      }
      else {
        // TODO exception
      }
    }
    else {
      // TODO exception
    }
    return ids;
  }

}
