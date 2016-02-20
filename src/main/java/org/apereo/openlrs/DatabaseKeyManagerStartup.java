/**
 * 
 */
package org.apereo.openlrs;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name="openlrs.keyManager", havingValue="DatabaseKeyManager")
@Component
public class DatabaseKeyManagerStartup {
  
  @Autowired private TenantRepository tenantRepository;
  
  @PostConstruct
  public void initIt() throws Exception {
	  
	  Tenant baseTenant = new Tenant();
	  
	  baseTenant.setName("openlrs");	  
	  baseTenant.setConsumerKey("openlrs");
	  baseTenant.setSecret("openlrs");
	  
	  baseTenant.setCreated(new Date());
	  baseTenant.setUpdated(new Date());
	  baseTenant.setActive(true);
	  baseTenant.setId(1);
	  
	  tenantRepository.save(baseTenant);	  
  }

}
