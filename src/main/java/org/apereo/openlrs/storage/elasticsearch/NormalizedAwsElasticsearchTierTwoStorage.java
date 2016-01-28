/**
 * Copyright 2016 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.apereo.openlrs.storage.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apereo.openlrs.model.OpenLRSEntity;
import org.apereo.openlrs.model.event.Event;
import org.apereo.openlrs.storage.TierTwoStorage;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name="openlrs.tierTwoStorage", havingValue="NormalizedAwsElasticsearchTierTwoStorage")
@Component("NormalizedAwsElasticsearchTierTwoStorage")
public class NormalizedAwsElasticsearchTierTwoStorage implements TierTwoStorage<OpenLRSEntity> {

  private Logger log = LoggerFactory.getLogger(NormalizedAwsElasticsearchTierTwoStorage.class);
  @Autowired JestClient jestClient;
  
  private static final String INDEX = "openlrsevents";
  private static final String TYPE = "event";
  
  private List<OpenLRSEntity> doSearch(String query, Integer offset, Integer size) {
    List<OpenLRSEntity> events = null;
    
    if (offset == null) {
      offset = 0;
    }
    
    if (size == null) {
      size = 1000;
    }
    
    Search search = new Search.Builder(query)
      .addIndex(INDEX)
      .addType(TYPE)
      .setParameter("from", offset)
      .setParameter(Parameters.SIZE, size)
      .build();
    
    try {
      SearchResult result = jestClient.execute(search);
      List<SearchResult.Hit<Event, Void>> results = result.getHits(Event.class);
      
      if (results != null && !results.isEmpty()) {
        events = new ArrayList<OpenLRSEntity>();
        for (SearchResult.Hit<Event, Void> r : results) {
          events.add(r.source);
        }
      }
    
    } 
    catch (IOException e) {
      log.error(e.getMessage(),e);
    }
    
    return events;
  }

  @Override
  public List<OpenLRSEntity> findAll() {
    Page<OpenLRSEntity> page = findAll(null);
    if (page != null) {
      return page.getContent();
    }
    
    return null;
  }

  @Override
  public Page<OpenLRSEntity> findAll(Pageable pageable) {
    Page<OpenLRSEntity> page = null;
    
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    
    int from = 0;
    int size = 1000;
    
    if (pageable != null) {
      from = pageable.getOffset();
      size = pageable.getPageSize();
    }
    
    List<OpenLRSEntity> all = doSearch(searchSourceBuilder.toString(), from, size);
    if (all != null && !all.isEmpty()) {
      page = new PageImpl<OpenLRSEntity>(all);
    }
    
    return page;
  }

  @Override
  public Event findById(String id) {
    Event event = null;
    Get get = new Get.Builder(INDEX, id).type(TYPE).build();
    try {
        JestResult result = jestClient.execute(get);
        if (result != null) {
          event = result.getSourceAsObject(Event.class);
        }
    } 
    catch (IOException e) {
        log.error(e.getMessage(),e);
    }
    return event;
  }

  @Override
  public List<OpenLRSEntity> findWithFilters(Map<String, String> filters) {
    throw new UnsupportedOperationException("NormalizedAwsElasticsearchTierTwoStorage.findWithFilters not supported");
  }

  @Override
  public Page<OpenLRSEntity> findWithFilters(Map<String, String> filters, Pageable pageable) {
    throw new UnsupportedOperationException("NormalizedAwsElasticsearchTierTwoStorage.findWithFilters not supported");
  }

  @Override
  public Event save(OpenLRSEntity entity) {
    throw new UnsupportedOperationException("NormalizedAwsElasticsearchTierTwoStorage.save not supported");
  }

  @Override
  public List<OpenLRSEntity> saveAll(Collection<OpenLRSEntity> entities) {
    throw new UnsupportedOperationException("NormalizedAwsElasticsearchTierTwoStorage.saveAll not supported");
  }

  @Override
  public Page<OpenLRSEntity> findByContext(String context, Pageable pageable) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchQuery("context", context));
    List<OpenLRSEntity> events = doSearch(searchSourceBuilder.toString(), pageable.getOffset(), pageable.getPageSize());
    
    if (events != null && !events.isEmpty()) {
      return new PageImpl<OpenLRSEntity>(events);
    }
    
    return null;
  }

  @Override
  public Page<OpenLRSEntity> findByUser(String user, Pageable pageable) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchQuery("actor", user));
    List<OpenLRSEntity> events = doSearch(searchSourceBuilder.toString(), pageable.getOffset(), pageable.getPageSize());
    
    if (events != null && !events.isEmpty()) {
      return new PageImpl<OpenLRSEntity>(events);
    }
    
    return null;
  }

  @Override
  public Page<OpenLRSEntity> findByContextAndUser(String context, String user, Pageable pageable) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("actor", user)).must(QueryBuilders.matchQuery("context", context)));
    List<OpenLRSEntity> events = doSearch(searchSourceBuilder.toString(), pageable.getOffset(), pageable.getPageSize());
    
    if (events != null && !events.isEmpty()) {
      return new PageImpl<OpenLRSEntity>(events);
    }
    
    return null;
  }

}
