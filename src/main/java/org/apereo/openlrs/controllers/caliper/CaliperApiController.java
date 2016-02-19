/**
 * 
 */
package org.apereo.openlrs.controllers.caliper;

import java.io.IOException;
import java.util.List;

import org.apereo.openlrs.exceptions.caliper.InvalidCaliperFormatException;
import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.model.event.v2.EventEnvelope;
import org.apereo.openlrs.storage.v2.Writer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
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
  @Autowired private Writer writer;
  
  @RequestMapping(value = { "", "/" },
      method = RequestMethod.POST,
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public void postHandler(@RequestBody String json)
        throws JsonProcessingException, IOException, InvalidCaliperFormatException {
    try {
      EventEnvelope ee = objectMapper.readValue(json,
          new TypeReference<EventEnvelope>() {});
      
      if (ee != null) {
        List<Event> events = ee.getData();
        if (events != null && !events.isEmpty()) {
          for (Event e : events) {
            e.setTenantId("openlrs");
            writer.save(e);
          }
        }
      }
      
    } 
    catch (Exception e) {
      e.printStackTrace();
    }

  }

}
