/**
 * 
 */
package org.apereo.openlrs.storage.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apereo.openlrs.model.event.v2.Event;
import org.apereo.openlrs.storage.v2.Reader;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name="openlrs.reader", havingValue="AwsElasticsearchReader")
@Component("AwsElasticsearchReader")
public class AwsElasticsearchReader implements Reader {
  
  private Logger log = LoggerFactory.getLogger(AwsElasticsearchReader.class);
  
  @Value("${aws.es.connectionUrl}")
  private String connectionUrl;
  
  private JestClient jestClient;
  
  @PostConstruct
  public void init() throws IOException, ParseException {

      JestClientFactory factory = new JestClientFactory();
      factory.setHttpClientConfig(new HttpClientConfig
              .Builder(connectionUrl)
              .multiThreaded(true)
              .discoveryFrequency(60, TimeUnit.SECONDS)
              .build());
      jestClient = factory.getObject();
      
      jestClient.execute(new CreateIndex.Builder("openlrs_events_v2").build());
      
      URL loadedResource = this.getClass().getClassLoader().getResource("es/event.mapping");
      InputStream inputStream = loadedResource.openStream();
      InputStreamReader fileReader = new InputStreamReader(inputStream);
      
      JSONParser jsonParser = new JSONParser();
      JSONObject json = (JSONObject) jsonParser.parse(fileReader);
      log.debug("mapping json {}",  json.toJSONString());
      
      PutMapping mapping = 
          new PutMapping.Builder(
              "openlrs_events_v2",
              "event",
              json.toJSONString()
      ).build();

      jestClient.execute(mapping);

  }  


  @Override
  public Page<Event> findByTenantId(String tenantId, Pageable pageable) {
    
    Page<Event> page = null;
    
    int offset = (pageable == null) ? 0 : pageable.getOffset();
    int pagesize = (pageable == null) ? 100 : pageable.getPageSize();
    
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("tenantId", tenantId)));

    Search search = new Search.Builder(searchSourceBuilder.toString())
    .addIndex("openlrs_events_v2")
    .setParameter("from", offset)
    .setParameter(Parameters.SIZE, pagesize)
    .build();
    
    try {
      SearchResult result = jestClient.execute(search);
      if (result != null) {
        List<Hit<Event, Void>> hits = result.getHits(Event.class);
        if (hits != null && !hits.isEmpty()) {
          List<Event> events = new LinkedList<Event>();
          for (Hit<Event,Void> hit : hits) {
            events.add(hit.source);
          }
          
          page = new PageImpl<Event>(events, pageable, events.size());
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return page;
  }

}
