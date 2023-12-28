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
package io.confluent.kafka.schemaregistry.utils;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroUtils;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * For general utility methods used in unit tests.
 */
public class TestUtils {

  private static final String IoTmpDir = System.getProperty("java.io.tmpdir");
  private static final Random random = new Random();

  /**
   * Create a temporary directory
   */
  public static File tempDir(String namePrefix) {
    final File f = new File(IoTmpDir, namePrefix + "-" + random.nextInt(1000000));
    f.mkdirs();
    f.deleteOnExit();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        rm(f);
      }
    });
    return f;
  }

  /**
   * Recursively deleteSchemaVersion the given file/directory and any subfiles (if any exist)
   *
   * @param file The root file at which to begin deleting
   */
  public static void rm(File file) {
    if (file == null) {
      return;
    } else if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          rm(f);
        }
      }
    } else {
      file.delete();
    }
  }

  /**
   * Wait until a callable returns true or the timeout is reached.
   */
  public static void waitUntilTrue(Callable<Boolean> callable, long timeoutMs, String errorMsg) {
    try {
      long startTime = System.currentTimeMillis();
      Boolean state = false;
      do {
        state = callable.call();
        if (System.currentTimeMillis() > startTime + timeoutMs) {
          fail(errorMsg);
        }
        Thread.sleep(50);
      } while (!state);
    } catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  /**
   * Helper method which checks the number of versions registered under the given subject.
   */
  public static void checkNumberOfVersions(RestService restService, int expected, String subject)
      throws IOException, RestClientException {
    List<Integer> versions = restService.getAllVersions(subject);
    assertEquals("Expected " + expected + " registered versions under subject " + subject +
                 ", but found " + versions.size(),
                 expected, versions.size());
  }

  /**
   * Register a new schema and verify that it can be found on the expected version.
   */
  public static void registerAndVerifySchema(RestService restService, String schemaString,
                                             int expectedId, String subject)
      throws IOException, RestClientException {
    int registeredId = restService.registerSchema(schemaString, subject);
    assertEquals("Registering a new schema should succeed", expectedId, registeredId);

    // the newly registered schema should be immediately readable on the leader
    assertEquals("Registered schema should be found",
            schemaString,
            restService.getId(expectedId).getSchemaString());
  }

  public static void registerAndVerifySchema(RestService restService, String schemaString,
                                             List<SchemaReference> references, int expectedId,
                                             String subject)
      throws IOException, RestClientException {
    int registeredId = restService.registerSchema(schemaString,
        AvroSchema.TYPE,
        references,
        subject
    );
    assertEquals("Registering a new schema should succeed", expectedId, registeredId);

    // the newly registered schema should be immediately readable on the leader
    assertEquals("Registered schema should be found",
        schemaString,
        restService.getId(expectedId).getSchemaString());
  }

  public static List<String> getRandomCanonicalAvroString(int num) {
    List<String> avroStrings = new ArrayList<String>();

    for (int i = 0; i < num; i++) {
      String schemaString = "{\"type\":\"record\","
                            + "\"name\":\"myrecord\","
                            + "\"fields\":"
                            + "[{\"type\":\"string\",\"name\":"
                            + "\"f" + random.nextInt(Integer.MAX_VALUE) + "\"}]}";
      avroStrings.add(AvroUtils.parseSchema(schemaString).canonicalString());
    }
    return avroStrings;
  }

  public static List<String> getAvroSchemaWithReferences() {
    List<String> schemas = new ArrayList<>();
    String reference = "{\"type\":\"record\","
        + "\"name\":\"Subrecord\","
        + "\"namespace\":\"otherns\","
        + "\"fields\":"
        + "[{\"name\":\"field2\",\"type\":\"string\"}]}";
    schemas.add(reference);
    String schemaString = "{\"type\":\"record\","
        + "\"name\":\"MyRecord\","
        + "\"namespace\":\"ns\","
        + "\"fields\":"
        + "[{\"name\":\"field1\",\"type\":\"otherns.Subrecord\"}]}";
    schemas.add(schemaString);
    return schemas;
  }

  public static String getBadSchema() {
    String schemaString = "{\"type\":\"bad-record\","
        + "\"name\":\"myrecord\","
        + "\"fields\":"
        + "[{\"type\":\"string\",\"name\":"
        + "\"f" + random.nextInt(Integer.MAX_VALUE) + "\"}]}";
    return schemaString;
  }

  public static String getProtobufCommonSchema() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message HistoryDoc {\n" +
            "  string docid = 1;\n" +
            "  string tag = 2;\n" +
            "  int32 day = 3;\n" +
            "  int32 test_column = 4;\n" +
            "  int32 test_column2 = 5;\n" +
            "}\n" +
            "message UserPosPoi {\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.ChnWeightsEntry chn_weights = 1;\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.TpcmWeightsEntry tpcm_weights = 2;\n" +
            "\n" +
            "  message ChnWeightsEntry {\n" +
            "    option map_entry = true;\n" +
            "  \n" +
            "    string key = 1;\n" +
            "    double value = 2;\n" +
            "  }" +
            "  message TpcmWeightsEntry {\n" +
            "    option map_entry = true;\n" +
            "  \n" +
            "    string key = 1;\n" +
            "    double value = 2;\n" +
            "  }\n" +
            "}\n" +
            "message PoiWeights {\n" +
            "  double chn_weight = 1;\n" +
            "  double tcat_weight = 2;\n" +
            "  double tpcm_weight = 3;\n" +
            "}\n" +
            "enum OsType {\n" +
            "  unknown = 0;\n" +
            "  android = 1;\n" +
            "  ios = 2;\n" +
            "}\n" +
            "enum GenderType {\n" +
            "  gender_unknown = 0;\n" +
            "  male = 1;\n" +
            "  fema = 2;\n" +
            "}\n";
  }

  public static String getProtobufCommonSchemaAddMsg() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message HistoryDoc {\n" +
            "  string docid = 1;\n" +
            "  string tag = 2;\n" +
            "  int32 day = 3;\n" +
            "  int32 test_column = 4;\n" +
            "  int32 test_column2 = 5;\n" +
            "}\n" +
            "message UserPosPoi {\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.ChnWeightsEntry chn_weights = 1;\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.TpcmWeightsEntry tpcm_weights = 2;\n" +
            "\n" +
            "  message ChnWeightsEntry {\n" +
            "    option map_entry = true;\n" +
            "  \n" +
            "    string key = 1;\n" +
            "    double value = 2;\n" +
            "  }" +
            "  message TpcmWeightsEntry {\n" +
            "    option map_entry = true;\n" +
            "  \n" +
            "    string key = 1;\n" +
            "    double value = 2;\n" +
            "  }\n" +
            "}\n" +
            "message PoiWeights {\n" +
            "  double chn_weight = 1;\n" +
            "  double tcat_weight = 2;\n" +
            "  double tpcm_weight = 3;\n" +
            "}\n" +
            "enum OsType {\n" +
            "  unknown = 0;\n" +
            "  android = 1;\n" +
            "  ios = 2;\n" +
            "}\n" +
            "enum GenderType {\n" +
            "  gender_unknown = 0;\n" +
            "  male = 1;\n" +
            "  fema = 2;\n" +
            "  trans = 3;\n" +
            "}\n" +
            "message NewMessageAdded1234 {\n" +
            "  int32 int_feature = 1;\n" +
            "  double double_feature = 2;\n" +
            "  string string_feature = 3;\n" +
            "}" +
            "\n";
  }

  public static String getProtobufNonLocalSampleSchemaString() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "import \"common.proto\";\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message D2dNonlocalSample {\n" +
            "  int32 label = 1;\n" +
            "  string impid = 2;\n" +
            "  int64 userid = 3;\n" +
            "  int64 ts = 4;\n" +
            "  .com.newsbreak.schema.OsType os = 5;\n" +
            "  repeated string recent_like = 6;\n" +
            "  repeated string recent_share = 7;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks = 8;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks = 9;\n" +
            "  .com.newsbreak.schema.GenderType gender = 10;\n" +
            "  .com.newsbreak.schema.UserPosPoi user_pos_poi = 11;\n" +
            "  int64 pv_time = 12;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks_idx100 = 13;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks_idx100 = 14;\n" +
            "  string src_docid = 15;\n" +
            "  repeated string recent_click_docids = 16;\n" +
            "}\n";
  }

  public static String getProtobufBloomSampleSchemaString() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "import \"common.proto\";\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message BloomEventSample {\n" +
            "  int32 label = 1;\n" +
            "  string impid = 2;\n" +
            "  int64 userid = 3;\n" +
            "  int64 ts = 4;\n" +
            "  .com.newsbreak.schema.OsType os = 5;\n" +
            "  repeated string recent_like = 6;\n" +
            "  repeated string recent_share = 7;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks = 8;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks = 9;\n" +
            "  .com.newsbreak.schema.GenderType gender = 10;\n" +
            "  .com.newsbreak.schema.UserPosPoi user_pos_poi = 11;\n" +
            "  int64 pv_time = 12;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks_idx100 = 13;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks_idx100 = 14;\n" +
            "  string src_docid = 15;\n" +
            "  repeated string recent_click_docids = 16;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc video_clicks = 17;\n" +
            "}\n";
  }

  public static String getProtobufNonLocalSampleSchemaStringValidAddMixed1() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "import \"common.proto\";\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message D2dNonlocalSample {\n" +
            "  int32 label = 1;\n" +
            "  string impid = 2;\n" +
            "  int64 userid = 3;\n" +
            "  int64 ts = 4;\n" +
            "  .com.newsbreak.schema.OsType os = 5;\n" +
            "  repeated string recent_like = 6;\n" +
            "  repeated string recent_share = 7;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks = 8;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks = 9;\n" +
            "  .com.newsbreak.schema.GenderType gender = 10;\n" +
            "  .com.newsbreak.schema.UserPosPoi user_pos_poi = 11;\n" +
            "  int64 pv_time = 12;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks_idx100 = 13;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks_idx100 = 14;\n" +
            "  string src_docid = 15;\n" +
            "  repeated string recent_click_docids = 16;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc added_external_msg = 17;\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.TpcmWeightsEntry tpcm_weights = 18;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 19;\n" +
            "  repeated string added_new_string = 20;\n" +
            "  int64 added_new_int = 21;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map2 = 22;\n" +
            "}\n";
  }

  public static String getProtobufNonLocalSampleSchemaStringInvalidAddMixed1() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "import \"common.proto\";\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message D2dNonlocalSample {\n" +
            "  int32 label = 1;\n" +
            "  string impid = 2;\n" +
            "  int64 userid = 3;\n" +
            "  int64 ts = 4;\n" +
            "  .com.newsbreak.schema.OsType os = 5;\n" +
            "  repeated string recent_like = 6;\n" +
            "  repeated string recent_share = 7;\n" +

            //Following are inserted in the middle
            "  repeated .com.newsbreak.schema.HistoryDoc added_external_msg = 8;\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.TpcmWeightsEntry tpcm_weights = 9;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 10;\n" +
            "  repeated string added_new_string = 11;\n" +
            "  int64 added_new_int = 12;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map2 = 13;\n" +

            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks = 14;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks = 15;\n" +
            "  .com.newsbreak.schema.GenderType gender = 16;\n" +
            "  .com.newsbreak.schema.UserPosPoi user_pos_poi = 17;\n" +
            "  int64 pv_time = 18;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks_idx100 = 19;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks_idx100 = 20;\n" +
            "  string src_docid = 21;\n" +
            "  repeated string recent_click_docids = 22;\n" +
            "}\n";

  }

  public static String getProtobufNonLocalSampleSchemaStringInValidAddMixedNonSequential() {
    return "syntax = \"proto3\";\n" +
            "package com.newsbreak.schema;\n" +
            "\n" +
            "import \"common.proto\";\n" +
            "\n" +
            "option java_package = \"com.newsbreak.schema.feature\";\n" +
            "option java_multiple_files = true;\n" +
            "option go_package = \"newsbreak.com/schema/feature\";\n" +
            "\n" +
            "message D2dNonlocalSample {\n" +
            "  int32 label = 1;\n" +
            "  string impid = 2;\n" +
            "  int64 userid = 3;\n" +
            "  int64 ts = 4;\n" +
            "  .com.newsbreak.schema.OsType os = 5;\n" +
            "  repeated string recent_like = 6;\n" +
            "  repeated string recent_share = 7;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks = 8;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks = 9;\n" +
            "  .com.newsbreak.schema.GenderType gender = 10;\n" +
            "  .com.newsbreak.schema.UserPosPoi user_pos_poi = 11;\n" +
            "  int64 pv_time = 12;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc long_clicks_idx100 = 13;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc ann_long_clicks_idx100 = 14;\n" +
            "  string src_docid = 15;\n" +
            "  repeated string recent_click_docids = 16;\n" +
            "  repeated .com.newsbreak.schema.HistoryDoc added_external_msg = 20;\n" +
            "  repeated .com.newsbreak.schema.UserPosPoi.TpcmWeightsEntry tpcm_weights = 21;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 30;\n" +
            "  repeated string added_new_string = 31;\n" +
            "  int64 added_new_int = 40;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map2 = 100;\n" +
            "}\n";
  }

}
