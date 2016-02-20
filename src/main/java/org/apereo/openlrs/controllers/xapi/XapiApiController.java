/**
 * 
 */
package org.apereo.openlrs.controllers.xapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.exceptions.xapi.InvalidXAPIRequestException;
import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.model.xapi.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ggilbert
 *
 */
@RestController
@RequestMapping("/v1/xAPI/statements")
public class XapiApiController {
  private final Logger logger = LoggerFactory.getLogger(XapiApiController.class);
  
  @Autowired private ObjectMapper objectMapper;
  @Autowired private Validator validator;
  @Autowired private XapiToCaliperConversionService xapiToCaliperConversionService;
  
  @RequestMapping(value = { "", "/" }, 
      method = RequestMethod.POST, 
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public List<String> postStatement(@RequestBody String json)
      throws InvalidXAPIRequestException {

    List<String> ids = null;
    try {
      if (json != null && StringUtils.isNotBlank(json)) {
        ids = new ArrayList<String>();

        List<Statement> statements = null;
        try {
          statements = objectMapper.readValue(json,
              new TypeReference<List<Statement>>() {
              });
        } catch (Exception e) {
          throw new InvalidXAPIRequestException(e);
        }

        for (Statement statement : statements) {
          Set<ConstraintViolation<Statement>> violations = validator
              .validate(statement);
          if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<Statement> cv : violations) {
              msg.append(cv.getMessage() + ", ");
            }
            throw new InvalidXAPIRequestException(msg.toString());
          }
          logger.debug(
              "Statement POST request received with input statement: {}",
              statement);
          Event event = xapiToCaliperConversionService.fromXapi(statement, null);
          logger.debug("{}",event);
          //ids.addAll();
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new InvalidXAPIRequestException(e.getMessage(), e);
    }
    return ids;
  }
  

}
