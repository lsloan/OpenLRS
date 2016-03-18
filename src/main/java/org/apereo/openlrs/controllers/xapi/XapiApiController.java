/**
 * 
 */
package org.apereo.openlrs.controllers.xapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.KeyManager;
import org.apereo.openlrs.Tenant;
import org.apereo.openlrs.exceptions.xapi.InvalidXAPIRequestException;
import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.model.xapi.Statement;
import org.apereo.openlrs.storage.v2.Writer;
import org.apereo.openlrs.utils.AuthorizationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
  @Autowired private Writer writer;
  @Autowired KeyManager keyManager;
  
  @RequestMapping(value = { "", "/" }, 
      method = RequestMethod.POST, 
      consumes = "application/json", produces = "application/json;charset=utf-8")
  public List<String> postStatement(@RequestBody String json, @RequestHeader(value="Authorization") String authorizationHeader)
      throws InvalidXAPIRequestException {
    List<String> ids = null;
    String key = AuthorizationUtils.getKeyFromHeader(authorizationHeader);
    
    if (StringUtils.isNotBlank(key)) {
      Tenant tenant = keyManager.getTenantForKey(key);  
      if (tenant != null) { 
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
              Event event = xapiToCaliperConversionService.fromXapi(statement);
              logger.debug("{}",event);
              if (StringUtils.isBlank(event.getId())) {
                event.setId(UUID.randomUUID().toString());
              }

              ids.add(writer.save(event, String.valueOf(tenant.getId())).getId());
            }
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          throw new InvalidXAPIRequestException(e.getMessage(), e);
        }
      }
      else {
        // TODO throw exception
      }
    }
    else {
      // TODO throw exception
    }

    return ids;
  }
  

}
