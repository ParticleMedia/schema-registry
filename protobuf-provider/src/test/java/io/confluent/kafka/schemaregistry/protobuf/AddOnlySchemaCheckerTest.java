package io.confluent.kafka.schemaregistry.protobuf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ricky on 2023/12/22.
 */
public class AddOnlySchemaCheckerTest {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String commonSchemaString =
            "syntax = \"proto3\";\n" +
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

    private static final String nonLocalSampleSchemaString =
            "syntax = \"proto3\";\n" +
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

    private static final String nonLocalSampleSchemaStringInvalid2Msgs =
            "syntax = \"proto3\";\n" +
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
            "}\n" +
            "message TheSecondMessage {\n" +
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

    private static final String nonLocalSampleSchemaStringValidAddScalar1 =
            "syntax = \"proto3\";\n" +
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
            "  repeated string added_new_string = 17;\n" +
            "  int64 added_new_int = 18;\n" +
            "}\n";

    private static final String nonLocalSampleSchemaStringValidAddMap1 =
            "syntax = \"proto3\";\n" +
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
            "  map<string, string> added_simple_map = 17;\n" +
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 18;\n" +
            "}\n";

    private static final String nonLocalSampleSchemaStringValidAddMsg1 =
            "syntax = \"proto3\";\n" +
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
            "}\n";

    private static final String nonLocalSampleSchemaStringValidAddMixed1 =
            "syntax = \"proto3\";\n" +
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
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 22;\n" +
            "}\n";


    private static final String nonLocalSampleSchemaStringInvalidAddMixed1 =
            "syntax = \"proto3\";\n" +
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
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 13;\n" +

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


    private static final String nonLocalSampleSchemaStringInValidAddMixedNonSequential =
            "syntax = \"proto3\";\n" +
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
            "  map<string, .com.newsbreak.schema.HistoryDoc> added_msg_map = 100;\n" +
            "}\n";

    @Test
    public void testAddOnlyCheckAddScalar() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringValidAddScalar1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddScalar1], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckAddMap() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringValidAddMap1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMap1], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckAddMsg() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringValidAddMsg1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMsg1], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckAddMixed() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringValidAddMixed1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMixed1], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckAddMixedInMiddle() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInvalidAddMixed1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInvalidAddMixed1], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckAddMixedNonSequential() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInValidAddMixedNonSequential, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInValidAddMixedNonSequential], errorMsg result:" + updatedSchema.isAddOnlyCompatible(originalSchema));
    }

    @Test
    public void testAddOnlyCheckNewValidSchema() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema schema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaString], errorMsg result:" + AddOnlySchemaChecker.sequentialSchemaInOrderCheck(schema));
    }
    @Test
    public void testAddOnlyCheckNewInvalidSchemaMixedNonSequential() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInValidAddMixedNonSequential, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInValidAddMixedNonSequential], errorMsg result:" + AddOnlySchemaChecker.sequentialSchemaInOrderCheck(updatedSchema));
    }

    @Test
    public void testAddOnlyCheckNewInvalidSchema2Msgs() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInvalid2Msgs, schemaReferenceList, dependencies, 2, "nonLocalSample");
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInvalid2Msgs], errorMsg result:" + AddOnlySchemaChecker.sequentialSchemaInOrderCheck(updatedSchema));
    }
}
