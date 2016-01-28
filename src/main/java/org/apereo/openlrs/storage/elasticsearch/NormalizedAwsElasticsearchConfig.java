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
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name="openlrs.tierTwoStorage", havingValue="NormalizedAwsElasticsearchTierTwoStorage")
@Configuration
public class NormalizedAwsElasticsearchConfig {
  private Logger log = LoggerFactory.getLogger(XApiOnlyAwsElasticsearchConfig.class);
  
  @Value("${aws.es.connectionUrl}")
  private String connectionUrl;
  
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
}
