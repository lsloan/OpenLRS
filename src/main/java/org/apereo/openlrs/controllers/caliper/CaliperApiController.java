/**
 * 
 */
package org.apereo.openlrs.controllers.caliper;

import java.io.IOException;

import org.apereo.openlrs.exceptions.caliper.InvalidCaliperFormatException;
import org.apereo.openlrs.model.event.v2.EventEnvelope;
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
@RequestMapping("/v1")
public class CaliperApiController {
  
  @Autowired private ObjectMapper objectMapper;
  
  @RequestMapping(value = {"/caliper"}, method = RequestMethod.POST,
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public void postHandler(@RequestBody String json)
        throws JsonProcessingException, IOException, InvalidCaliperFormatException {
    try {
      EventEnvelope ee = objectMapper.readValue(json,
          new TypeReference<EventEnvelope>() {});
      
      System.out.println(ee);
    } 
    catch (Exception e) {
      e.printStackTrace();
    }

  }

}
