/**
 * 
 */
package org.apereo.openlrs;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  
  @Value("${databasekeymanager.tenents}")
  private List<String> tenents;

  @PostConstruct
  public void initIt() throws Exception {
	  
	  //Tenant t = new Tenant();
	  
	  //tenantRepository.save(t);
	  System.out.println("Init method after properties are set");
  }

}
