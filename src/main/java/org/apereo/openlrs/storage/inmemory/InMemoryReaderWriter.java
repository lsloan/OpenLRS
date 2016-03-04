/**
 * 
 */
package org.apereo.openlrs.storage.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.storage.v2.Reader;
import org.apereo.openlrs.storage.v2.Writer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name={"openlrs.writer","openlrs.reader"}, havingValue="InMemoryReaderWriter")
@Component("InMemoryReaderWriter")
public class InMemoryReaderWriter implements Writer, Reader {

  private static Map<String, List<Event>> store = new HashMap<String, List<Event>>();

  @Override
  public Page<Event> findByTenantId(String tenantId, Pageable pageable) {
    List<Event> events = store.get(tenantId);
    if (events == null) return null;
    
    return new PageImpl<>(events, pageable, events.size());
  }

  @Override
  public Event save(Event event, String tenantId) {
    
    if (event == null || StringUtils.isBlank(tenantId)) {
      throw new IllegalArgumentException("Event or Tenant cannot be null");
    }
    
    List<Event> events = store.get(tenantId);
    
    if (events == null) {
      events = new LinkedList<Event>();
    }
    events.add(event);
    store.put(tenantId, events);
    
    return event;
  }

  @Override
  public List<Event> saveAll(Collection<Event> events, String tenantId) {
    List<Event> savedEvents = null;
    if (events != null && !events.isEmpty()) {
      savedEvents = new ArrayList<Event>();
      for (Event e : events) {
        savedEvents.add(this.save(e, tenantId));
      }
    }
    return savedEvents;
  }

}
