package org.apereo.openlrs.storage.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author jasonbrown
 *
 */
@ConditionalOnProperty(name="openlrs.tierTwoStorage", havingValue="XApiOnlyAwsElasticsearchTierTwoStorage")
@Configuration
public class XApiOnlyAwsElasticsearchConfig {
    private Logger log = LoggerFactory.getLogger(XApiOnlyAwsElasticsearchConfig.class);
    
    @Value("${aws.es.connectionUrl}")
    private String connectionUrl;
    
    @Value("${aws.es.statement.index.name:openlrsstatement}")
    private String statementIndexName;
    
    @Value("${aws.es.statementmetadata.index.name:openlrsstatementmetadata}")
    private String statementMetadataIndexName;
    
    @Bean
    public JestClient jestClient() {

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(connectionUrl)
                .multiThreaded(true)
                .discoveryFrequency(60, TimeUnit.SECONDS)
                .build());
        return factory.getObject();
    }
    
    /**
     * By elasticsearch design if a mapping already exists then it will not update with a new mapping.
     * It is also by design of the app that we do not want to delete a mapping that already exists, 
     * as multiple apps can hit the same elasticsearch endpoint. 
     */
    @PostConstruct
    public void initStatement() throws IOException, ParseException {
        jestClient().execute(new CreateIndex.Builder(statementIndexName).build());
        
        URL loadedResource = this.getClass().getClassLoader().getResource("es/statement.mapping");
        InputStream inputStream = loadedResource.openStream();
        InputStreamReader statementFileReader = new InputStreamReader(inputStream);
        
        JSONParser jsonParser = new JSONParser();
        JSONObject statementJSONObject = (JSONObject) jsonParser.parse(statementFileReader);
        log.debug("statment {}",  statementJSONObject.toJSONString());
        
        PutMapping statmentMapping = 
            new PutMapping.Builder(
                "openlrsstatement",
                "statement",
                statementJSONObject.toJSONString()
        ).build();

        jestClient().execute(statmentMapping);
    }

    @PostConstruct
    public void initMetadata() throws IOException, ParseException {
        jestClient().execute(new CreateIndex.Builder(statementMetadataIndexName).build());
        
        URL loadedResource = this.getClass().getClassLoader().getResource("es/metadata.mapping");
        InputStream inputStream = loadedResource.openStream();
        InputStreamReader metadataFileReader = new InputStreamReader(inputStream);
        
        JSONParser jsonParser = new JSONParser();
        JSONObject metadataJSONObject = (JSONObject) jsonParser.parse(metadataFileReader);
        
        PutMapping metadataMapping = 
            new PutMapping.Builder(
                "openlrsstatementmetadata",
                "statement_metadata",
                metadataJSONObject.toJSONString()
        ).build();

        jestClient().execute(metadataMapping);
    }
}
