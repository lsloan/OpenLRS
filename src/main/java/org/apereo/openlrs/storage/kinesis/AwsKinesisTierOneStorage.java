/**
 * Copyright 2015 Unicon (R) Licensed under the
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
package org.apereo.openlrs.storage.kinesis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apereo.openlrs.model.OpenLRSEntity;
import org.apereo.openlrs.model.caliper.CaliperEvent;
import org.apereo.openlrs.model.event.EventConversionService;
import org.apereo.openlrs.model.xapi.Statement;
import org.apereo.openlrs.storage.TierOneStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;

/**
 * @author ggilbert
 *
 */
@ConditionalOnProperty(name="openlrs.tierOneStorage", havingValue="AwsKinesisTierOneStorage")
@Component("AwsKinesisTierOneStorage")
public class AwsKinesisTierOneStorage implements TierOneStorage<OpenLRSEntity> {
  
  private static Logger log = Logger.getLogger(AwsKinesisTierOneStorage.class);
  
  @Autowired private AmazonKinesisClient kinesisClient;
  @Autowired private EventConversionService eventConversionService;

  @Value("${aws.kinesis.stream}") 
  private String stream;

  private String partitionKey = "TENANT_ID";
  
  private String convert(OpenLRSEntity entity) {
    String json = null;
    if (entity != null) {
      if (eventConversionService.isEvent(entity)) {
        json = entity.toJSON();
      }
      else if (eventConversionService.isCaliper(entity)) {
        json = eventConversionService.fromCaliper((CaliperEvent)entity).toJSON();
      }
      else if (eventConversionService.isXApi(entity)) {
        json = eventConversionService.fromXAPI((Statement)entity).toJSON();
      }
    }
    return json;
  }
  
  @Override
  public OpenLRSEntity save(OpenLRSEntity entity) {
    PutRecordRequest putRecordRequest = new PutRecordRequest();
    putRecordRequest.setStreamName(stream);
    putRecordRequest.setPartitionKey(partitionKey);
    String myData = convert(entity);
    putRecordRequest.setData(ByteBuffer.wrap(myData.getBytes()));
    PutRecordResult result = kinesisClient.putRecord(putRecordRequest);
    log.debug("Successfully putrecord, partition key : " + putRecordRequest.getPartitionKey() 
    + ", ShardID : " + result.getShardId() + "Sequence Number: "+result.getSequenceNumber());
    return entity;
  }

  //TODO this is untested!
  @Override
  public List<OpenLRSEntity> saveAll(Collection<OpenLRSEntity> entities) {
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
    putRecordsRequest.setStreamName(stream);
    putRecordsRequest.setRecords(createPutRecords(entities));
    PutRecordsResult result = kinesisClient.putRecords(putRecordsRequest);
    log.debug("Successfully putrecords, FaileRecordCount : " + result.getFailedRecordCount());
    return (List<OpenLRSEntity>) entities;
  }
  
  private Collection<PutRecordsRequestEntry> createPutRecords(Collection<OpenLRSEntity> entities) {
      List<PutRecordsRequestEntry> records = new ArrayList<PutRecordsRequestEntry>();
      
      for(OpenLRSEntity entity: entities){
          PutRecordsRequestEntry requestEntry = new PutRecordsRequestEntry();
          requestEntry.withExplicitHashKey(null);
          requestEntry.withData(ByteBuffer.wrap(convert(entity).getBytes()));
          requestEntry.withPartitionKey(partitionKey);
          records.add(requestEntry);
      }
      return records;
  }

}
