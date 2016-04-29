/**
 * 
 */
package org.apereo.openlrs.storage.v2;

import java.util.Collection;
import java.util.List;

import org.apereo.openlrs.model.event.v2.Event;

/**
 * @author ggilbert
 *
 */
public interface Writer {
  Event save(Event event, String tenantId);
  List<Event> saveAll(Collection<Event> events, String tenantId);
}
