/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.kafka.schemaregistry.rest;

import io.confluent.kafka.schemaregistry.ClusterTestHarness;
import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.avro.AvroUtils;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.rest.exceptions.RestIncompatibleSchemaException;
import io.confluent.kafka.schemaregistry.rest.exceptions.RestInvalidSchemaException;
import io.confluent.kafka.schemaregistry.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RestApiCompatibilityTest extends ClusterTestHarness {

  public RestApiCompatibilityTest() {
    super(1, true, CompatibilityLevel.BACKWARD.name);
  }

  @Test
  public void testCompatibility() throws Exception {
    String subject = "testSubject";

    // register a valid avro
    String schemaString1 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"}]}").canonicalString();
    int expectedIdSchema1 = 1;
    assertEquals("Registering should succeed",
            expectedIdSchema1,
            restApp.restClient.registerSchema(schemaString1, subject));

    // register an incompatible avro
    String incompatibleSchemaString = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"},"
        + " {\"type\":\"string\",\"name\":\"f2\"}]}").canonicalString();
    try {
      restApp.restClient.registerSchema(incompatibleSchemaString, subject);
      fail("Registering an incompatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
                   RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
                   e.getStatus());
    }

    // register a non-avro
    String nonAvroSchemaString = "non-avro schema string";
    try {
      restApp.restClient.registerSchema(nonAvroSchemaString, subject);
      fail("Registering a non-avro schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a bad request status",
                   RestInvalidSchemaException.ERROR_CODE,
                   e.getErrorCode());
    }

    // register a backward compatible avro
    String schemaString2 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"},"
        + " {\"type\":\"string\",\"name\":\"f2\", \"default\": \"foo\"}]}").canonicalString();
    int expectedIdSchema2 = 2;
    assertEquals("Registering a compatible schema should succeed",
                 expectedIdSchema2,
                 restApp.restClient.registerSchema(schemaString2, subject));
  }

  @Test
  public void testCompatibilityLevelChangeToNone() throws Exception {
    String subject = "testSubject";

    // register a valid avro
    String schemaString1 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"}]}").canonicalString();
    int expectedIdSchema1 = 1;
    assertEquals("Registering should succeed",
            expectedIdSchema1,
            restApp.restClient.registerSchema(schemaString1, subject));

    // register an incompatible avro
    String incompatibleSchemaString = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"},"
        + " {\"type\":\"string\",\"name\":\"f2\"}]}").canonicalString();
    try {
      restApp.restClient.registerSchema(incompatibleSchemaString, subject);
      fail("Registering an incompatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
                   RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
                   e.getStatus());
    }

    // change compatibility level to none and try again
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.NONE.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.NONE.name, null)
                    .getCompatibilityLevel());

    try {
      restApp.restClient.registerSchema(incompatibleSchemaString, subject);
    } catch (RestClientException e) {
      fail("Registering an incompatible schema should succeed after bumping down the compatibility "
           + "level to none");
    }
  }

  @Test
  public void testCompatibilityLevelChangeToBackward() throws Exception {
    String subject = "testSubject";

    String schemaString1 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"}]}").canonicalString();
    int expectedIdSchema1 = 1;
    assertEquals("Registering should succeed",
            expectedIdSchema1,
            restApp.restClient.registerSchema(schemaString1, subject));
    // verify that default compatibility level is backward
    assertEquals("Default compatibility level should be backward",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());
    // change it to forward
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.FORWARD.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.FORWARD.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is forward
    assertEquals("New compatibility level should be forward",
            CompatibilityLevel.FORWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    // register schema that is forward compatible with schemaString1
    String schemaString2 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"},"
        + " {\"type\":\"string\",\"name\":\"f2\"}]}").canonicalString();
    int expectedIdSchema2 = 2;
    assertEquals("Registering should succeed",
                 expectedIdSchema2,
                 restApp.restClient.registerSchema(schemaString2, subject));

    // change compatibility to backward
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.updateCompatibility(CompatibilityLevel.BACKWARD.name,
                    null).getCompatibilityLevel());

    // verify that new compatibility level is backward
    assertEquals("Updated compatibility level should be backward",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

            // register forward compatible schema, which should fail
            String schemaString3 = AvroUtils.parseSchema("{\"type\":\"record\","
                + "\"name\":\"myrecord\","
                + "\"fields\":"
                + "[{\"type\":\"string\",\"name\":\"f1\"},"
                + " {\"type\":\"string\",\"name\":\"f2\"},"
                + " {\"type\":\"string\",\"name\":\"f3\"}]}").canonicalString();
    try {
      restApp.restClient.registerSchema(schemaString3, subject);
      fail("Registering a forward compatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
                   RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
                   e.getStatus());
    }

    // now try registering a backward compatible schema (add a field with a default)
    String schemaString4 = AvroUtils.parseSchema("{\"type\":\"record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":\"f1\"},"
        + " {\"type\":\"string\",\"name\":\"f2\"},"
        + " {\"type\":\"string\",\"name\":\"f3\", \"default\": \"foo\"}]}").canonicalString();
    int expectedIdSchema4 = 3;
    assertEquals("Registering should succeed with backwards compatible schema",
            expectedIdSchema4,
            restApp.restClient.registerSchema(schemaString4, subject));
  }

  @Test
  public void testRegisterProtobufAddOnlySchemaSuccess() throws Exception {
    //First register common schema which will be referenced later.
    String commonSchemaStr = TestUtils.getProtobufCommonSchema();
    assertEquals("Registering should succeed",
            1,
            restApp.restClient.registerSchema(commonSchemaStr, "PROTOBUF", null, "common.proto"));

    String subject = "nonLocalSampleSubject";
    String nonLocalSampleSchemaString = TestUtils.getProtobufNonLocalSampleSchemaString();
    List<SchemaReference> schemaReferenceList = new LinkedList<>();
    SchemaReference schemaReference = new SchemaReference("common.proto", "common.proto", 1);
    schemaReferenceList.add(schemaReference);
    assertEquals("Registering should succeed",
            2,
            restApp.restClient.registerSchema(nonLocalSampleSchemaString, "PROTOBUF",
                    schemaReferenceList,subject, true));

    // verify that default compatibility level is backward
    assertEquals("Default compatibility level should be backward",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());
    // change it to addonly
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.ADDONLY.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is addonly
    assertEquals("New compatibility level should be addonly",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    // register schema that is addonly compatible with schemaString1
    String nonLocalSampleSchemaStringValidAddMixed1 = TestUtils.getProtobufNonLocalSampleSchemaStringValidAddMixed1();
    assertEquals("Registering should succeed",
            3,
            restApp.restClient.registerSchema(nonLocalSampleSchemaStringValidAddMixed1, "PROTOBUF",
                    schemaReferenceList,subject, true));
  }

  @Test
  public void testRegisterProtobufAddOnlySchemaFail() throws Exception {
    //First register common schema which will be referenced later.
    String commonSchemaStr = TestUtils.getProtobufCommonSchema();
    assertEquals("Registering should succeed",
            1,
            restApp.restClient.registerSchema(commonSchemaStr, "PROTOBUF", null,"common.proto"));

    String subject = "nonLocalSampleSubject";
    String nonLocalSampleSchemaString = TestUtils.getProtobufNonLocalSampleSchemaString();
    List<SchemaReference> schemaReferenceList = new LinkedList<>();
    SchemaReference schemaReference = new SchemaReference("common.proto", "common.proto", 1);
    schemaReferenceList.add(schemaReference);
    assertEquals("Registering should succeed",
            2,
            restApp.restClient.registerSchema(nonLocalSampleSchemaString, "PROTOBUF",
                    schemaReferenceList,subject, true));

    // verify that default compatibility level is backward
    assertEquals("Default compatibility level should be backward",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());
    // change it to addonly
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.ADDONLY.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is addonly
    assertEquals("New compatibility level should be addonly",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    // register schema that is not addonly compatible with schemaString1
    String nonLocalSampleSchemaStringInvalidAddMixed1 = TestUtils.getProtobufNonLocalSampleSchemaStringInvalidAddMixed1();
    try {
      restApp.restClient.registerSchema(nonLocalSampleSchemaStringInvalidAddMixed1, "PROTOBUF",
              schemaReferenceList,subject, true);
      fail("Registering a not addonly compatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
              RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
              e.getStatus());
    }

    // register schema that is not addonly compatible with out of order schema
    String nonLocalSampleSchemaStringInValidAddMixedNonSequential = TestUtils.getProtobufNonLocalSampleSchemaStringInValidAddMixedNonSequential();
    try {
      restApp.restClient.registerSchema(nonLocalSampleSchemaStringInValidAddMixedNonSequential, "PROTOBUF",
              schemaReferenceList,subject, true);
      fail("Registering a not addonly compatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
              RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
              e.getStatus());
    }

    // register schema that contains 2 msgs which is not valid AutoETLEnabled setting.
    String nonLocalSampleSchema2MessagesString = TestUtils.getProtobufNonLocalSample2MessagesSchemaString();
    try {
      restApp.restClient.registerSchema(nonLocalSampleSchema2MessagesString, "PROTOBUF",
              schemaReferenceList, "NonLocalSchemaWith2Msgs", true, true);
      fail("Registering addonly compatible schema with 2 msgs should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
              422,
              e.getStatus());
    }
  }

  @Test
  public void testRegisterProtobufAddOnlySchemaFail2() throws Exception {
    // verify that default compatibility level is backward
    assertEquals("Default compatibility level should be backward",
            CompatibilityLevel.BACKWARD.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());
    // change it to addonly
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.ADDONLY.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is addonly
    assertEquals("New compatibility level should be addonly",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    //First register common schema which will be referenced later.
    String commonSchemaStr = TestUtils.getProtobufCommonSchema();
    assertEquals("Registering should succeed",
            1,
            restApp.restClient.registerSchema(commonSchemaStr, "PROTOBUF", null,"common.proto"));

//    String subject = "nonLocalSampleSubject";
    String nonLocalSampleSchemaString = TestUtils.getProtobufNonLocalSampleSchemaString();
    List<SchemaReference> schemaReferenceList = new LinkedList<>();
    SchemaReference schemaReference = new SchemaReference("common.proto", "common.proto", 1);
    schemaReferenceList.add(schemaReference);
    assertEquals("Registering should succeed",
            2,
            restApp.restClient.registerSchema(nonLocalSampleSchemaString, "PROTOBUF",
                    schemaReferenceList, "nonLocalSampleSubject", true));

    // register schema that is not addonly compatible with schemaString1
    String nonLocalSampleSchemaStringInvalidAddMixed1 = TestUtils.getProtobufNonLocalSampleSchemaStringInvalidAddMixed1();
    try {
      restApp.restClient.registerSchema(nonLocalSampleSchemaStringInvalidAddMixed1, "PROTOBUF",
              schemaReferenceList, "nonLocalSampleSubject", true);
      fail("Registering a not addonly compatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
              RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
              e.getStatus());
    }

    // register a new schema that is not addonly compatible with out of order schema
    String nonLocalSampleSchemaStringInValidAddMixedNonSequential = TestUtils.getProtobufNonLocalSampleSchemaStringInValidAddMixedNonSequential();
    try {
      restApp.restClient.registerSchema(nonLocalSampleSchemaStringInValidAddMixedNonSequential, "PROTOBUF",
              schemaReferenceList,"nonLocalSampleSubject2", true);
      fail("Registering a not addonly compatible schema should fail");
    } catch (RestClientException e) {
      // this is expected.
      assertEquals("Should get a conflict status",
              RestIncompatibleSchemaException.DEFAULT_ERROR_CODE,
              e.getStatus());
    }

  }

  @Test
  public void testUpgradeProtobufCommonSchema() throws Exception {
    //First register common schema which will be referenced later.
    String commonSchemaStr = TestUtils.getProtobufCommonSchema();
    assertEquals("Registering should succeed",
            1,
            restApp.restClient.registerSchema(commonSchemaStr, "PROTOBUF", null, "common.proto"));

    String nonLocalSampleSchemaString = TestUtils.getProtobufNonLocalSampleSchemaString();
    List<SchemaReference> schemaReferenceList = new LinkedList<>();
    SchemaReference schemaReference = new SchemaReference("common.proto", "common.proto", 1);
    schemaReferenceList.add(schemaReference);
    assertEquals("Registering should succeed",
            2,
            restApp.restClient.registerSchema(nonLocalSampleSchemaString, "PROTOBUF",
                    schemaReferenceList, "nonLocalSampleSubject", true));

    // change it to addonly
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.ADDONLY.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is addonly
    assertEquals("New compatibility level should be addonly",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    //Now get dependencies of common schema, it should tell that there's a downstream dependency affect.
    List<String> dependentSchemaIdList = restApp.restClient.getDependedBy("common.proto", 1);
    System.out.println("Dependency check result:" + dependentSchemaIdList);
    assertEquals("Registering should succeed",
            1,
            dependentSchemaIdList.size());

    //UI should always call getDependedBy, show user the Warning Info like "Change of common schema will affect schema 1"

    //After user clicks "acknowledge", then make another call to register this schema.
    String commonSchemaWithNewMessage = TestUtils.getProtobufCommonSchemaAddMsg();
    assertEquals("Registering should succeed",
            3,
            restApp.restClient.registerSchema(commonSchemaWithNewMessage, "PROTOBUF",
                    null, "common.proto", true));
  }

  @Test
  public void testUpgradeProtobufCommonSchemaWith2Dependencies() throws Exception {
    //First register common schema which will be referenced later.
    String commonSchemaStr = TestUtils.getProtobufCommonSchema();
    assertEquals("Registering should succeed",
            1,
            restApp.restClient.registerSchema(commonSchemaStr, "PROTOBUF", null, "common.proto"));

    //Register the first downstream schema
    String nonLocalSampleSchemaString = TestUtils.getProtobufNonLocalSampleSchemaString();
    List<SchemaReference> schemaReferenceList = new LinkedList<>();
    SchemaReference schemaReference = new SchemaReference("common.proto", "common.proto", 1);
    schemaReferenceList.add(schemaReference);
    assertEquals("Registering should succeed",
            2,
            restApp.restClient.registerSchema(nonLocalSampleSchemaString, "PROTOBUF",
                    schemaReferenceList, "nonLocalSampleSubject", true));

    //Register the second downstream schema
    String bloomSchemaString = TestUtils.getProtobufBloomSampleSchemaString();
    assertEquals("Registering should succeed",
            3,
            restApp.restClient.registerSchema(bloomSchemaString, "PROTOBUF",
                    schemaReferenceList, "bloomSampleSubject", true));

    // change it to addonly
    assertEquals("Changing compatibility level should succeed",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient
                    .updateCompatibility(CompatibilityLevel.ADDONLY.name, null)
                    .getCompatibilityLevel());

    // verify that new compatibility level is addonly
    assertEquals("New compatibility level should be addonly",
            CompatibilityLevel.ADDONLY.name,
            restApp.restClient.getConfig(null).getCompatibilityLevel());

    //Now get dependencies of common schema, it should tell that there's a downstream dependency affect.
    List<String> dependentSchemaIdList = restApp.restClient.getDependedBy("common.proto", 1);
    System.out.println("Dependency check result:" + dependentSchemaIdList);
    assertEquals("Registering should succeed",
            2,
            dependentSchemaIdList.size());

    //UI should always call getDependedBy, show user the Warning Info like "Change of common schema will affect schema 1"

    //After user clicks "acknowledge", then make another call to register this schema.
    String commonSchemaWithNewMessage = TestUtils.getProtobufCommonSchemaAddMsg();
    assertEquals("Registering should succeed",
            4,
            restApp.restClient.registerSchema(commonSchemaWithNewMessage, "PROTOBUF",
                    null, "common.proto", true));

    //Now check the version and corresponding information for depending schemas
    List<Integer> nonLocalVersions = restApp.restClient.getAllVersions("nonLocalSampleSubject");
    assertEquals(2, nonLocalVersions.size());
    List<Schema> latestNonLocalSchema = restApp.restClient.getSchemas("nonLocalSampleSubject", false, true);
    assertEquals(1, latestNonLocalSchema.size());
    assertEquals(2, latestNonLocalSchema.get(0).getReferences().get(0).getVersion().intValue());
    List<Integer> bloomVersions = restApp.restClient.getAllVersions("bloomSampleSubject");
    assertEquals(2, bloomVersions.size());
    List<Schema> latestBloomSchema = restApp.restClient.getSchemas("bloomSampleSubject", false, true);
    assertEquals(1, latestBloomSchema.size());
    assertEquals(2, latestBloomSchema.get(0).getReferences().get(0).getVersion().intValue());
  }


}
