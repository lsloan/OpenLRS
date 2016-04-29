/**
 * 
 */
package org.apereo.openlrs.storage.v2;

import org.apereo.openlrs.model.event.v2.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author ggilbert
 *
 */
public interface Reader {
  Page<Event> findByTenantId(String tenantId, Pageable pageable);
}
