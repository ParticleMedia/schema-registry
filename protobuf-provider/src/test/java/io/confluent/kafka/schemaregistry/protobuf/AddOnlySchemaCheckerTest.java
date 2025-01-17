package io.confluent.kafka.schemaregistry.protobuf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


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

    private static final String openrtbv26Schema = "syntax = \"proto2\";\n" +
            "package com.google.openrtb;\n" +
            "\n" +
            "option java_package = \"com.particles.mes.protos.openrtb\";\n" +
            "option java_multiple_files = true;\n" +
            "option java_outer_classname = \"OpenRtb\";\n" +
            "\n" +
            "message BidRequest {\n" +
            "  required string id = 1;\n" +
            "  repeated Imp imp = 2;\n" +
            "  optional Device device = 5;\n" +
            "  optional Regs regs = 14;\n" +
            "  optional User user = 6;\n" +
            "  optional AuctionType at = 7 [default = SECOND_PRICE];\n" +
            "  optional int32 tmax = 8;\n" +
            "  repeated string wseat = 9;\n" +
            "  optional bool allimps = 10 [default = false];\n" +
            "  repeated string cur = 11;\n" +
            "  repeated string bcat = 12;\n" +
            "  optional CategoryTaxonomy cattax = 21 [default = IAB_CONTENT_1_0];\n" +
            "  repeated string badv = 13;\n" +
            "  repeated string bapp = 16;\n" +
            "  optional bool test = 15 [default = false];\n" +
            "  repeated string bseat = 17;\n" +
            "  repeated string wlang = 18;\n" +
            "  repeated string wlangb = 20;\n" +
            "  optional Source source = 19;\n" +
            "  optional string ext = 90;\n" +
            "\n" +
            "  oneof distributionchannel_oneof {\n" +
            "    Site site = 3;\n" +
            "    App app = 4;\n" +
            "  }\n" +
            "\n" +
            "  extensions 100 to 9999;\n" +
            "\n" +
            "  message Imp {\n" +
            "    required string id = 1;\n" +
            "    optional Banner banner = 2;\n" +
            "    optional Video video = 3;\n" +
            "    optional Audio audio = 15;\n" +
            "    optional string displaymanager = 4;\n" +
            "    optional string displaymanagerver = 5;\n" +
            "    optional bool instl = 6;\n" +
            "    optional string tagid = 7;\n" +
            "    optional double bidfloor = 8 [default = 0];\n" +
            "    optional string bidfloorcur = 9 [default = \"USD\"];\n" +
            "    optional bool clickbrowser = 16;\n" +
            "    optional bool secure = 12;\n" +
            "    repeated string iframebuster = 10;\n" +
            "    optional bool rwdd = 18 [default = false];\n" +
            "    optional ServerSideAdInsertionType ssai = 19 [default = SERVER_SIDE_AD_INSERTION_TYPE_UNKNOWN];\n" +
            "    optional Pmp pmp = 11;\n" +
            "    optional Native native = 13;\n" +
            "    optional int32 exp = 14;\n" +
            "    repeated Metric metric = 17;\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Banner {\n" +
            "      optional int32 w = 1;\n" +
            "      optional int32 h = 2;\n" +
            "      repeated Format format = 15;\n" +
            "      optional string id = 3;\n" +
            "      optional AdPosition pos = 4;\n" +
            "      repeated BannerAdType btype = 5 [packed = true];\n" +
            "      repeated CreativeAttribute battr = 6 [packed = true];\n" +
            "      repeated string mimes = 7;\n" +
            "      optional bool topframe = 8;\n" +
            "      repeated ExpandableDirection expdir = 9 [packed = true];\n" +
            "      repeated APIFramework api = 10 [packed = true];\n" +
            "      optional bool vcm = 16;\n" +
            "      optional int32 wmax = 11 [deprecated = true];\n" +
            "      optional int32 hmax = 12 [deprecated = true];\n" +
            "      optional int32 wmin = 13 [deprecated = true];\n" +
            "      optional int32 hmin = 14 [deprecated = true];\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    \n" +
            "      message Format {\n" +
            "        optional int32 w = 1;\n" +
            "        optional int32 h = 2;\n" +
            "        optional int32 wratio = 3;\n" +
            "        optional int32 hratio = 4;\n" +
            "        optional int32 wmin = 5;\n" +
            "      \n" +
            "        extensions 100 to 9999;\n" +
            "      }\n" +
            "    }\n" +
            "    message Video {\n" +
            "      repeated string mimes = 1;\n" +
            "      optional int32 minduration = 3 [default = 0];\n" +
            "      optional int32 maxduration = 4;\n" +
            "      optional int32 startdelay = 8;\n" +
            "      optional int32 maxseq = 28;\n" +
            "      optional int32 poddur = 29;\n" +
            "      repeated Protocol protocols = 21 [packed = true];\n" +
            "      optional int32 w = 6;\n" +
            "      optional int32 h = 7;\n" +
            "      optional string podid = 30;\n" +
            "      optional PodSequence podseq = 31 [default = POD_SEQUENCE_ANY];\n" +
            "      repeated int32 rqddurs = 32 [packed = true];\n" +
            "      optional VideoPlacementType placement = 26 [deprecated = true];\n" +
            "      optional Plcmt plcmt = 35 [default = PLCMT_UNKNOWN];\n" +
            "      optional VideoLinearity linearity = 2;\n" +
            "      optional bool skip = 23;\n" +
            "      optional int32 skipmin = 24;\n" +
            "      optional int32 skipafter = 25;\n" +
            "      optional int32 sequence = 9 [\n" +
            "        deprecated = true,\n" +
            "        default = 1\n" +
            "      ];\n" +
            "      optional SlotPositionInPod slotinpod = 33 [default = SLOT_POSITION_POD_ANY];\n" +
            "      optional double mincpmpersec = 34;\n" +
            "      repeated CreativeAttribute battr = 10 [packed = true];\n" +
            "      optional int32 maxextended = 11;\n" +
            "      optional int32 minbitrate = 12;\n" +
            "      optional int32 maxbitrate = 13;\n" +
            "      optional bool boxingallowed = 14 [default = true];\n" +
            "      repeated PlaybackMethod playbackmethod = 15 [packed = true];\n" +
            "      optional PlaybackCessationMode playbackend = 27;\n" +
            "      repeated ContentDeliveryMethod delivery = 16 [packed = true];\n" +
            "      optional AdPosition pos = 17;\n" +
            "      repeated Banner companionad = 18;\n" +
            "      repeated APIFramework api = 19 [packed = true];\n" +
            "      repeated CompanionType companiontype = 20 [packed = true];\n" +
            "      optional Protocol protocol = 5 [deprecated = true];\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Audio {\n" +
            "      repeated string mimes = 1;\n" +
            "      optional int32 minduration = 2 [default = 0];\n" +
            "      optional int32 maxduration = 3;\n" +
            "      optional int32 poddur = 25;\n" +
            "      repeated Protocol protocols = 4 [packed = true];\n" +
            "      optional int32 startdelay = 5;\n" +
            "      repeated int32 rqddurs = 26 [packed = true];\n" +
            "      optional string podid = 27;\n" +
            "      optional PodSequence podseq = 28 [default = POD_SEQUENCE_ANY];\n" +
            "      optional int32 sequence = 6 [\n" +
            "        deprecated = true,\n" +
            "        default = 1\n" +
            "      ];\n" +
            "      optional SlotPositionInPod slotinpod = 29 [default = SLOT_POSITION_POD_ANY];\n" +
            "      optional double mincpmpersec = 30;\n" +
            "      repeated CreativeAttribute battr = 7 [packed = true];\n" +
            "      optional int32 maxextended = 8;\n" +
            "      optional int32 minbitrate = 9;\n" +
            "      optional int32 maxbitrate = 10;\n" +
            "      repeated ContentDeliveryMethod delivery = 11 [packed = true];\n" +
            "      repeated Banner companionad = 12;\n" +
            "      repeated APIFramework api = 13 [packed = true];\n" +
            "      repeated CompanionType companiontype = 20 [packed = true];\n" +
            "      optional int32 maxseq = 21;\n" +
            "      optional FeedType feed = 22;\n" +
            "      optional bool stitched = 23;\n" +
            "      optional VolumeNormalizationMode nvol = 24;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Pmp {\n" +
            "      optional bool private_auction = 1 [default = false];\n" +
            "      repeated Deal deals = 2;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    \n" +
            "      message Deal {\n" +
            "        required string id = 1;\n" +
            "        optional double bidfloor = 2 [default = 0];\n" +
            "        optional string bidfloorcur = 3 [default = \"USD\"];\n" +
            "        repeated string wseat = 4;\n" +
            "        repeated string wadomain = 5;\n" +
            "        optional AuctionType at = 6;\n" +
            "      \n" +
            "        extensions 100 to 9999;\n" +
            "      }\n" +
            "    }\n" +
            "    message Native {\n" +
            "      optional string ver = 2;\n" +
            "      repeated APIFramework api = 3 [packed = true];\n" +
            "      repeated CreativeAttribute battr = 4 [packed = true];\n" +
            "    \n" +
            "      oneof request_oneof {\n" +
            "        string request = 1;\n" +
            "        NativeRequest request_native = 50;\n" +
            "      }\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Metric {\n" +
            "      optional string type = 1;\n" +
            "      optional double value = 2;\n" +
            "      optional string vendor = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "  message Publisher {\n" +
            "    optional string id = 1;\n" +
            "    optional string name = 2;\n" +
            "    optional CategoryTaxonomy cattax = 5 [default = IAB_CONTENT_1_0];\n" +
            "    repeated string cat = 3;\n" +
            "    optional string domain = 4;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "  message Content {\n" +
            "    optional string id = 1;\n" +
            "    optional int32 episode = 2;\n" +
            "    optional string title = 3;\n" +
            "    optional string series = 4;\n" +
            "    optional string season = 5;\n" +
            "    optional string artist = 21;\n" +
            "    optional string genre = 22;\n" +
            "    optional string album = 23;\n" +
            "    optional string isrc = 24;\n" +
            "    optional Producer producer = 15;\n" +
            "    optional string url = 6;\n" +
            "    optional CategoryTaxonomy cattax = 27 [default = IAB_CONTENT_1_0];\n" +
            "    repeated string cat = 7;\n" +
            "    optional ProductionQuality prodq = 25;\n" +
            "    optional ContentContext context = 20;\n" +
            "    optional string contentrating = 10;\n" +
            "    optional string userrating = 11;\n" +
            "    optional QAGMediaRating qagmediarating = 17;\n" +
            "    optional string keywords = 9;\n" +
            "    optional bool livestream = 13;\n" +
            "    optional bool sourcerelationship = 14;\n" +
            "    optional int32 len = 16;\n" +
            "    optional string language = 19;\n" +
            "    optional string langb = 29;\n" +
            "    optional bool embeddable = 18;\n" +
            "    repeated Data data = 28;\n" +
            "    optional Network network = 30;\n" +
            "    optional Channel channel = 31;\n" +
            "    optional ProductionQuality videoquality = 8 [deprecated = true];\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Producer {\n" +
            "      optional string id = 1;\n" +
            "      optional string name = 2;\n" +
            "      optional CategoryTaxonomy cattax = 5 [default = IAB_CONTENT_1_0];\n" +
            "      repeated string cat = 3;\n" +
            "      optional string domain = 4;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Network {\n" +
            "      optional string id = 1;\n" +
            "      optional string name = 2;\n" +
            "      optional string domain = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Channel {\n" +
            "      optional string id = 1;\n" +
            "      optional string name = 2;\n" +
            "      optional string domain = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "  message Site {\n" +
            "    optional string id = 1;\n" +
            "    optional string name = 2;\n" +
            "    optional string domain = 3;\n" +
            "    optional CategoryTaxonomy cattax = 16 [default = IAB_CONTENT_1_0];\n" +
            "    repeated string cat = 4;\n" +
            "    repeated string sectioncat = 5;\n" +
            "    repeated string pagecat = 6;\n" +
            "    optional string page = 7;\n" +
            "    optional bool privacypolicy = 8;\n" +
            "    optional string ref = 9;\n" +
            "    optional string search = 10;\n" +
            "    optional Publisher publisher = 11;\n" +
            "    optional Content content = 12;\n" +
            "    optional string keywords = 13;\n" +
            "    optional bool mobile = 15;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "  message App {\n" +
            "    optional string id = 1;\n" +
            "    optional string name = 2;\n" +
            "    optional string domain = 3;\n" +
            "    optional CategoryTaxonomy cattax = 17 [default = IAB_CONTENT_1_0];\n" +
            "    repeated string cat = 4;\n" +
            "    repeated string sectioncat = 5;\n" +
            "    repeated string pagecat = 6;\n" +
            "    optional string ver = 7;\n" +
            "    optional string bundle = 8;\n" +
            "    optional bool privacypolicy = 9;\n" +
            "    optional bool paid = 10;\n" +
            "    optional Publisher publisher = 11;\n" +
            "    optional Content content = 12;\n" +
            "    optional string keywords = 13;\n" +
            "    optional string storeurl = 16;\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "  message Geo {\n" +
            "    optional double lat = 1;\n" +
            "    optional double lon = 2;\n" +
            "    optional string country = 3;\n" +
            "    optional string region = 4;\n" +
            "    optional string regionfips104 = 5;\n" +
            "    optional string metro = 6;\n" +
            "    optional string city = 7;\n" +
            "    optional string zip = 8;\n" +
            "    optional LocationType type = 9;\n" +
            "    optional int32 accuracy = 11;\n" +
            "    optional int32 lastfix = 12;\n" +
            "    optional LocationService ipservice = 13;\n" +
            "    optional int32 utcoffset = 10;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "  message Device {\n" +
            "    optional Geo geo = 4;\n" +
            "    optional bool dnt = 1;\n" +
            "    optional bool lmt = 23;\n" +
            "    optional string ua = 2;\n" +
            "    optional UserAgent sua = 31;\n" +
            "    optional string ip = 3;\n" +
            "    optional string ipv6 = 9;\n" +
            "    optional DeviceType devicetype = 18;\n" +
            "    optional string make = 12;\n" +
            "    optional string model = 13;\n" +
            "    optional string os = 14;\n" +
            "    optional string osv = 15;\n" +
            "    optional string hwv = 24;\n" +
            "    optional int32 w = 25;\n" +
            "    optional int32 h = 26;\n" +
            "    optional int32 ppi = 27;\n" +
            "    optional double pxratio = 28;\n" +
            "    optional bool js = 16;\n" +
            "    optional bool geofetch = 29;\n" +
            "    optional string flashver = 19;\n" +
            "    optional string language = 11;\n" +
            "    optional string langb = 32;\n" +
            "    optional string carrier = 10;\n" +
            "    optional string mccmnc = 30;\n" +
            "    optional ConnectionType connectiontype = 17;\n" +
            "    optional string ifa = 20;\n" +
            "    optional string didsha1 = 5 [deprecated = true];\n" +
            "    optional string didmd5 = 6 [deprecated = true];\n" +
            "    optional string dpidsha1 = 7 [deprecated = true];\n" +
            "    optional string dpidmd5 = 8 [deprecated = true];\n" +
            "    optional string macsha1 = 21 [deprecated = true];\n" +
            "    optional string macmd5 = 22 [deprecated = true];\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message UserAgent {\n" +
            "      repeated BrandVersion browsers = 1;\n" +
            "      optional BrandVersion platform = 2;\n" +
            "      optional bool mobile = 3;\n" +
            "      optional string architecture = 4;\n" +
            "      optional string bitness = 5;\n" +
            "      optional string model = 6;\n" +
            "      optional UserAgentSource source = 7;\n" +
            "    \n" +
            "      message BrandVersion {\n" +
            "        optional string brand = 1;\n" +
            "        repeated string version = 2;\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  message Regs {\n" +
            "    optional bool coppa = 1;\n" +
            "    optional string gpp = 2;\n" +
            "    repeated GppSectionId gpp_sid = 3 [packed = true];\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    enum GppSectionId {\n" +
            "      TCFEUV1 = 1;\n" +
            "      TCFEUV2 = 2;\n" +
            "      GPP_HEADER = 3;\n" +
            "      GPP_SIGNAL = 4;\n" +
            "      TCFCA = 5;\n" +
            "      USPV1 = 6;\n" +
            "      USNAT = 7;\n" +
            "      USCA = 8;\n" +
            "      USVA = 9;\n" +
            "      USCO = 10;\n" +
            "      USUT = 11;\n" +
            "      USCT = 12;\n" +
            "    }\n" +
            "  }\n" +
            "  message Data {\n" +
            "    optional string id = 1;\n" +
            "    optional string name = 2;\n" +
            "    repeated Segment segment = 3;\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Segment {\n" +
            "      optional string id = 1;\n" +
            "      optional string name = 2;\n" +
            "      optional string value = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "  message User {\n" +
            "    optional string id = 1;\n" +
            "    optional string buyeruid = 2;\n" +
            "    optional int32 yob = 3 [deprecated = true];\n" +
            "    optional string gender = 4 [deprecated = true];\n" +
            "    optional string keywords = 5;\n" +
            "    repeated string kwarray = 9;\n" +
            "    optional string customdata = 6;\n" +
            "    optional Geo geo = 7;\n" +
            "    repeated Data data = 8;\n" +
            "    optional string consent = 10;\n" +
            "    repeated EID eids = 11;\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message EID {\n" +
            "      optional string source = 1;\n" +
            "      repeated UID uids = 2;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    \n" +
            "      message UID {\n" +
            "        optional string id = 1;\n" +
            "        optional AgentType atype = 2;\n" +
            "      \n" +
            "        extensions 100 to 9999;\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  message Source {\n" +
            "    optional bool fd = 1;\n" +
            "    optional string tid = 2;\n" +
            "    optional string pchain = 3;\n" +
            "    optional SupplyChain schain = 4;\n" +
            "    optional string ext = 90;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message SupplyChain {\n" +
            "      optional bool complete = 1;\n" +
            "      repeated SupplyChainNode nodes = 2;\n" +
            "      optional string ver = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    \n" +
            "      message SupplyChainNode {\n" +
            "        optional string asi = 1;\n" +
            "        optional string sid = 2;\n" +
            "        optional string rid = 3;\n" +
            "        optional string name = 4;\n" +
            "        optional string domain = 5;\n" +
            "        optional bool hp = 6;\n" +
            "      \n" +
            "        extensions 100 to 9999;\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "message BidResponse {\n" +
            "  required string id = 1;\n" +
            "  repeated SeatBid seatbid = 2;\n" +
            "  optional string bidid = 3;\n" +
            "  optional string cur = 4;\n" +
            "  optional string customdata = 5;\n" +
            "  optional NoBidReason nbr = 6;\n" +
            "  optional string ext = 90;\n" +
            "\n" +
            "  extensions 100 to 9999;\n" +
            "\n" +
            "  message SeatBid {\n" +
            "    repeated Bid bid = 1;\n" +
            "    optional string seat = 2;\n" +
            "    optional bool group = 3 [default = false];\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Bid {\n" +
            "      required string id = 1;\n" +
            "      required string impid = 2;\n" +
            "      required double price = 3;\n" +
            "      optional string nurl = 5;\n" +
            "      optional string burl = 22;\n" +
            "      optional string lurl = 23;\n" +
            "      optional string adid = 4;\n" +
            "      repeated string adomain = 7;\n" +
            "      optional string bundle = 14;\n" +
            "      optional string iurl = 8;\n" +
            "      optional string cid = 9;\n" +
            "      optional string crid = 10;\n" +
            "      optional string tactic = 24;\n" +
            "      optional CategoryTaxonomy cattax = 30 [default = IAB_CONTENT_1_0];\n" +
            "      repeated string cat = 15;\n" +
            "      repeated CreativeAttribute attr = 11 [packed = true];\n" +
            "      repeated APIFramework apis = 31 [packed = true];\n" +
            "      optional APIFramework api = 18 [deprecated = true];\n" +
            "      optional Protocol protocol = 19;\n" +
            "      optional QAGMediaRating qagmediarating = 20;\n" +
            "      optional string language = 25;\n" +
            "      optional string langb = 29;\n" +
            "      optional string dealid = 13;\n" +
            "      optional int32 w = 16;\n" +
            "      optional int32 h = 17;\n" +
            "      optional int32 wratio = 26;\n" +
            "      optional int32 hratio = 27;\n" +
            "      optional int32 exp = 21;\n" +
            "      optional int32 dur = 32;\n" +
            "      optional SlotPositionInPod slotinpod = 28;\n" +
            "      optional CreativeMarkupType mtype = 33;\n" +
            "      optional string ext = 90;\n" +
            "    \n" +
            "      oneof adm_oneof {\n" +
            "        string adm = 6;\n" +
            "        NativeResponse adm_native = 50;\n" +
            "      }\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "message NativeRequest {\n" +
            "  optional string ver = 1;\n" +
            "  optional ContextType context = 7;\n" +
            "  optional ContextSubtype contextsubtype = 8;\n" +
            "  optional PlacementType plcmttype = 9;\n" +
            "  optional int32 plcmtcnt = 4 [default = 1];\n" +
            "  optional int32 seq = 5 [default = 0];\n" +
            "  repeated Asset assets = 6;\n" +
            "  optional bool aurlsupport = 11;\n" +
            "  optional bool durlsupport = 12;\n" +
            "  repeated EventTrackers eventtrackers = 13;\n" +
            "  optional bool privacy = 14;\n" +
            "  optional LayoutId layout = 2 [deprecated = true];\n" +
            "  optional AdUnitId adunit = 3 [deprecated = true];\n" +
            "\n" +
            "  extensions 100 to 9999;\n" +
            "\n" +
            "  message Asset {\n" +
            "    required int32 id = 1;\n" +
            "    optional bool required = 2 [default = false];\n" +
            "  \n" +
            "    oneof asset_oneof {\n" +
            "      Title title = 3;\n" +
            "      Image img = 4;\n" +
            "      BidRequest.Imp.Video video = 5;\n" +
            "      Data data = 6;\n" +
            "    }\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Title {\n" +
            "      required int32 len = 1;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Image {\n" +
            "      optional ImageAssetType type = 1;\n" +
            "      optional int32 w = 2;\n" +
            "      optional int32 h = 3;\n" +
            "      optional int32 wmin = 4;\n" +
            "      optional int32 hmin = 5;\n" +
            "      repeated string mimes = 6;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Data {\n" +
            "      required DataAssetType type = 1;\n" +
            "      optional int32 len = 2;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "  message EventTrackers {\n" +
            "    required EventType event = 1;\n" +
            "    repeated EventTrackingMethod methods = 2;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "}\n" +
            "message NativeResponse {\n" +
            "  optional string ver = 1;\n" +
            "  repeated Asset assets = 2;\n" +
            "  optional string assetsurl = 6;\n" +
            "  optional string dcourl = 7;\n" +
            "  required Link link = 3;\n" +
            "  repeated string imptrackers = 4 [deprecated = true];\n" +
            "  optional string jstracker = 5 [deprecated = true];\n" +
            "  repeated EventTracker eventtrackers = 8;\n" +
            "  optional string privacy = 9;\n" +
            "\n" +
            "  extensions 100 to 9999;\n" +
            "\n" +
            "  message Link {\n" +
            "    required string url = 1;\n" +
            "    repeated string clicktrackers = 2;\n" +
            "    optional string fallback = 3;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "  message Asset {\n" +
            "    required int32 id = 1;\n" +
            "    optional bool required = 2 [default = false];\n" +
            "    optional Link link = 7;\n" +
            "  \n" +
            "    oneof asset_oneof {\n" +
            "      Title title = 3;\n" +
            "      Image img = 4;\n" +
            "      Video video = 5;\n" +
            "      Data data = 6;\n" +
            "    }\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  \n" +
            "    message Title {\n" +
            "      required string text = 1;\n" +
            "      optional int32 len = 2;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Image {\n" +
            "      optional ImageAssetType type = 4;\n" +
            "      required string url = 1;\n" +
            "      optional int32 w = 2;\n" +
            "      optional int32 h = 3;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Video {\n" +
            "      required string vasttag = 1;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "    message Data {\n" +
            "      optional DataAssetType type = 3;\n" +
            "      optional int32 len = 4;\n" +
            "      optional string label = 1 [deprecated = true];\n" +
            "      required string value = 2;\n" +
            "    \n" +
            "      extensions 100 to 9999;\n" +
            "    }\n" +
            "  }\n" +
            "  message EventTracker {\n" +
            "    optional EventType event = 1;\n" +
            "    required EventTrackingMethod method = 2;\n" +
            "    optional string url = 3;\n" +
            "  \n" +
            "    extensions 100 to 9999;\n" +
            "  }\n" +
            "}\n" +
            "enum AuctionType {\n" +
            "  FIRST_PRICE = 1;\n" +
            "  SECOND_PRICE = 2;\n" +
            "  FIXED_PRICE = 3;\n" +
            "}\n" +
            "enum BannerAdType {\n" +
            "  XHTML_TEXT_AD = 1;\n" +
            "  XHTML_BANNER_AD = 2;\n" +
            "  JAVASCRIPT_AD = 3;\n" +
            "  IFRAME = 4;\n" +
            "}\n" +
            "enum CreativeAttribute {\n" +
            "  AUDIO_AUTO_PLAY = 1;\n" +
            "  AUDIO_USER_INITIATED = 2;\n" +
            "  EXPANDABLE_AUTOMATIC = 3;\n" +
            "  EXPANDABLE_CLICK_INITIATED = 4;\n" +
            "  EXPANDABLE_ROLLOVER_INITIATED = 5;\n" +
            "  VIDEO_IN_BANNER_AUTO_PLAY = 6;\n" +
            "  VIDEO_IN_BANNER_USER_INITIATED = 7;\n" +
            "  POP = 8;\n" +
            "  PROVOCATIVE_OR_SUGGESTIVE = 9;\n" +
            "  ANNOYING = 10;\n" +
            "  SURVEYS = 11;\n" +
            "  TEXT_ONLY = 12;\n" +
            "  USER_INTERACTIVE = 13;\n" +
            "  WINDOWS_DIALOG_OR_ALERT_STYLE = 14;\n" +
            "  HAS_AUDIO_ON_OFF_BUTTON = 15;\n" +
            "  AD_CAN_BE_SKIPPED = 16;\n" +
            "  FLASH = 17;\n" +
            "  RESPONSIVE = 18;\n" +
            "}\n" +
            "enum APIFramework {\n" +
            "  VPAID_1 = 1;\n" +
            "  VPAID_2 = 2;\n" +
            "  MRAID_1 = 3;\n" +
            "  ORMMA = 4;\n" +
            "  MRAID_2 = 5;\n" +
            "  MRAID_3 = 6;\n" +
            "  OMID_1 = 7;\n" +
            "  SIMID_1_0 = 8;\n" +
            "  SIMID_1_1 = 9;\n" +
            "}\n" +
            "enum AdPosition {\n" +
            "  UNKNOWN = 0;\n" +
            "  ABOVE_THE_FOLD = 1;\n" +
            "  LOCKED = 2;\n" +
            "  BELOW_THE_FOLD = 3;\n" +
            "  HEADER = 4;\n" +
            "  FOOTER = 5;\n" +
            "  SIDEBAR = 6;\n" +
            "  AD_POSITION_FULLSCREEN = 7;\n" +
            "}\n" +
            "enum VideoLinearity {\n" +
            "  LINEAR = 1;\n" +
            "  NON_LINEAR = 2;\n" +
            "}\n" +
            "enum Protocol {\n" +
            "  VAST_1_0 = 1;\n" +
            "  VAST_2_0 = 2;\n" +
            "  VAST_3_0 = 3;\n" +
            "  VAST_1_0_WRAPPER = 4;\n" +
            "  VAST_2_0_WRAPPER = 5;\n" +
            "  VAST_3_0_WRAPPER = 6;\n" +
            "  VAST_4_0 = 7;\n" +
            "  VAST_4_0_WRAPPER = 8;\n" +
            "  DAAST_1_0 = 9;\n" +
            "  DAAST_1_0_WRAPPER = 10;\n" +
            "  VAST_4_1 = 11;\n" +
            "  VAST_4_1_WRAPPER = 12;\n" +
            "  VAST_4_2 = 13;\n" +
            "  VAST_4_2_WRAPPER = 14;\n" +
            "}\n" +
            "enum PlaybackMethod {\n" +
            "  AUTO_PLAY_SOUND_ON = 1;\n" +
            "  AUTO_PLAY_SOUND_OFF = 2;\n" +
            "  CLICK_TO_PLAY = 3;\n" +
            "  MOUSE_OVER = 4;\n" +
            "  ENTER_SOUND_ON = 5;\n" +
            "  ENTER_SOUND_OFF = 6;\n" +
            "  CONTINUOUS = 7;\n" +
            "}\n" +
            "enum StartDelay {\n" +
            "  PRE_ROLL = 0;\n" +
            "  GENERIC_MID_ROLL = -1;\n" +
            "  GENERIC_POST_ROLL = -2;\n" +
            "}\n" +
            "enum VideoPlacementType {\n" +
            "  UNDEFINED_VIDEO_PLACEMENT = 0;\n" +
            "  IN_STREAM_PLACEMENT = 1;\n" +
            "  IN_BANNER_PLACEMENT = 2;\n" +
            "  IN_ARTICLE_PLACEMENT = 3;\n" +
            "  IN_FEED_PLACEMENT = 4;\n" +
            "  FLOATING_PLACEMENT = 5;\n" +
            "}\n" +
            "enum Plcmt {\n" +
            "  PLCMT_UNKNOWN = 0;\n" +
            "  PLCMT_INSTREAM = 1;\n" +
            "  PLCMT_ACCOMPANYING_CONTENT = 2;\n" +
            "  PLCMT_INTERSTITIAL = 3;\n" +
            "  PLCMT_NO_CONTENT_STANDALONE = 4;\n" +
            "}\n" +
            "enum PlaybackCessationMode {\n" +
            "  COMPLETION_OR_USER = 1;\n" +
            "  LEAVING_OR_USER = 2;\n" +
            "  LEAVING_CONTINUES_OR_USER = 3;\n" +
            "}\n" +
            "enum SlotPositionInPod {\n" +
            "  SLOT_POSITION_POD_ANY = 0;\n" +
            "  SLOT_POSITION_POD_LAST = -1;\n" +
            "  SLOT_POSITION_POD_FIRST = 1;\n" +
            "  SLOT_POSITION_POD_FIRST_OR_LAST = 2;\n" +
            "}\n" +
            "enum PodSequence {\n" +
            "  POD_SEQUENCE_ANY = 0;\n" +
            "  POD_SEQUENCE_LAST = -1;\n" +
            "  POD_SEQUENCE_FIRST = 1;\n" +
            "}\n" +
            "enum ConnectionType {\n" +
            "  CONNECTION_UNKNOWN = 0;\n" +
            "  ETHERNET = 1;\n" +
            "  WIFI = 2;\n" +
            "  CELL_UNKNOWN = 3;\n" +
            "  CELL_2G = 4;\n" +
            "  CELL_3G = 5;\n" +
            "  CELL_4G = 6;\n" +
            "  CELL_5G = 7;\n" +
            "}\n" +
            "enum ExpandableDirection {\n" +
            "  LEFT = 1;\n" +
            "  RIGHT = 2;\n" +
            "  UP = 3;\n" +
            "  DOWN = 4;\n" +
            "  EXPANDABLE_FULLSCREEN = 5;\n" +
            "  RESIZE_MINIMIZE = 6;\n" +
            "}\n" +
            "enum ContentDeliveryMethod {\n" +
            "  STREAMING = 1;\n" +
            "  PROGRESSIVE = 2;\n" +
            "  DOWNLOAD = 3;\n" +
            "}\n" +
            "enum ContentContext {\n" +
            "  VIDEO = 1;\n" +
            "  GAME = 2;\n" +
            "  MUSIC = 3;\n" +
            "  APPLICATION = 4;\n" +
            "  TEXT = 5;\n" +
            "  OTHER = 6;\n" +
            "  CONTEXT_UNKNOWN = 7;\n" +
            "}\n" +
            "enum ProductionQuality {\n" +
            "  QUALITY_UNKNOWN = 0;\n" +
            "  PROFESSIONAL = 1;\n" +
            "  PROSUMER = 2;\n" +
            "  USER_GENERATED = 3;\n" +
            "}\n" +
            "enum LocationType {\n" +
            "  GPS_LOCATION = 1;\n" +
            "  IP = 2;\n" +
            "  USER_PROVIDED = 3;\n" +
            "}\n" +
            "enum LocationService {\n" +
            "  IP2LOCATION = 1;\n" +
            "  NEUSTAR = 2;\n" +
            "  MAXMIND = 3;\n" +
            "  NETACUITY = 4;\n" +
            "}\n" +
            "enum DeviceType {\n" +
            "  MOBILE = 1;\n" +
            "  PERSONAL_COMPUTER = 2;\n" +
            "  CONNECTED_TV = 3;\n" +
            "  HIGHEND_PHONE = 4;\n" +
            "  TABLET = 5;\n" +
            "  CONNECTED_DEVICE = 6;\n" +
            "  SET_TOP_BOX = 7;\n" +
            "  OOH_DEVICE = 8;\n" +
            "}\n" +
            "enum CompanionType {\n" +
            "  STATIC = 1;\n" +
            "  HTML = 2;\n" +
            "  COMPANION_IFRAME = 3;\n" +
            "}\n" +
            "enum QAGMediaRating {\n" +
            "  ALL_AUDIENCES = 1;\n" +
            "  EVERYONE_OVER_12 = 2;\n" +
            "  MATURE = 3;\n" +
            "}\n" +
            "enum NoBidReason {\n" +
            "  UNKNOWN_ERROR = 0;\n" +
            "  TECHNICAL_ERROR = 1;\n" +
            "  INVALID_REQUEST = 2;\n" +
            "  KNOWN_WEB_SPIDER = 3;\n" +
            "  SUSPECTED_NONHUMAN_TRAFFIC = 4;\n" +
            "  CLOUD_DATACENTER_PROXYIP = 5;\n" +
            "  UNSUPPORTED_DEVICE = 6;\n" +
            "  BLOCKED_PUBLISHER = 7;\n" +
            "  UNMATCHED_USER = 8;\n" +
            "  DAILY_READER_CAP = 9;\n" +
            "  DAILY_DOMAIN_CAP = 10;\n" +
            "}\n" +
            "enum LossReason {\n" +
            "  BID_WON = 0;\n" +
            "  INTERNAL_ERROR = 1;\n" +
            "  IMP_EXPIRED = 2;\n" +
            "  INVALID_BID = 3;\n" +
            "  INVALID_DEAL_ID = 4;\n" +
            "  INVALID_AUCTION_ID = 5;\n" +
            "  INVALID_ADOMAIN = 6;\n" +
            "  MISSING_MARKUP = 7;\n" +
            "  MISSING_CREATIVE_ID = 8;\n" +
            "  MISSING_PRICE = 9;\n" +
            "  MISSING_MIN_CREATIVE_APPROVAL_DATA = 10;\n" +
            "  BID_BELOW_FLOOR = 100;\n" +
            "  BID_BELOW_DEAL_FLOOR = 101;\n" +
            "  LOST_HIGHER_BID = 102;\n" +
            "  LOST_PMP_DEAL = 103;\n" +
            "  SEAT_BLOCKED = 104;\n" +
            "  CREATIVE_REASON_UNKNOWN = 200;\n" +
            "  CREATIVE_PENDING = 201;\n" +
            "  CREATIVE_DISAPPROVED = 202;\n" +
            "  CREATIVE_SIZE = 203;\n" +
            "  CREATIVE_FORMAT = 204;\n" +
            "  CREATIVE_ADVERTISER_EXCLUSION = 205;\n" +
            "  CREATIVE_APP_EXCLUSION = 206;\n" +
            "  CREATIVE_NOT_SECURE = 207;\n" +
            "  CREATIVE_LANGUAGE_EXCLUSION = 208;\n" +
            "  CREATIVE_CATEGORY_EXCLUSION = 209;\n" +
            "  CREATIVE_ATTRIBUTE_EXCLUSION = 210;\n" +
            "  CREATIVE_ADTYPE_EXCLUSION = 211;\n" +
            "  CREATIVE_ANIMATION_LONG = 212;\n" +
            "  CREATIVE_NOT_ALLOWED_PMP = 213;\n" +
            "}\n" +
            "enum FeedType {\n" +
            "  MUSIC_SERVICE = 1;\n" +
            "  BROADCAST = 2;\n" +
            "  PODCAST = 3;\n" +
            "}\n" +
            "enum VolumeNormalizationMode {\n" +
            "  NONE = 0;\n" +
            "  AVERAGE_VOLUME = 1;\n" +
            "  PEAK_VOLUME = 2;\n" +
            "  LOUDNESS = 3;\n" +
            "  CUSTOM_VOLUME = 4;\n" +
            "}\n" +
            "enum UserAgentSource {\n" +
            "  UNKNOWN_SOURCE = 0;\n" +
            "  CLIENT_HINTS_LOW_ENTROPY = 1;\n" +
            "  CLIENT_HINTS_HIGH_ENTROPY = 2;\n" +
            "  USER_AGENT_STRING = 3;\n" +
            "}\n" +
            "enum CreativeMarkupType {\n" +
            "  CREATIVE_MARKUP_BANNER = 1;\n" +
            "  CREATIVE_MARKUP_VIDEO = 2;\n" +
            "  CREATIVE_MARKUP_AUDIO = 3;\n" +
            "  CREATIVE_MARKUP_NATIVE = 4;\n" +
            "}\n" +
            "enum ServerSideAdInsertionType {\n" +
            "  SERVER_SIDE_AD_INSERTION_TYPE_UNKNOWN = 0;\n" +
            "  CLIENT_SIDE_ONLY = 1;\n" +
            "  SERVER_SIDE_STITCHED_CLIENT_TRACKER = 2;\n" +
            "  SERVER_SIDE_ONLY = 3;\n" +
            "}\n" +
            "enum AgentType {\n" +
            "  BROWSER_OR_DEVICE = 1;\n" +
            "  IN_APP_IMPRESSION = 2;\n" +
            "  STABLE_ID = 3;\n" +
            "}\n" +
            "enum CategoryTaxonomy {\n" +
            "  IAB_CONTENT_1_0 = 1 [deprecated = true];\n" +
            "  IAB_CONTENT_2_0 = 2 [deprecated = true];\n" +
            "  IAB_PRODUCT_1_0 = 3;\n" +
            "  IAB_AUDIENCE_1_1 = 4;\n" +
            "  IAB_CONTENT_2_1 = 5;\n" +
            "  IAB_CONTENT_2_2 = 6;\n" +
            "  CHROME_TOPICS = 600;\n" +
            "}\n" +
            "enum LayoutId {\n" +
            "  CONTENT_WALL = 1;\n" +
            "  APP_WALL = 2;\n" +
            "  NEWS_FEED = 3;\n" +
            "  CHAT_LIST = 4;\n" +
            "  CAROUSEL = 5;\n" +
            "  CONTENT_STREAM = 6;\n" +
            "  GRID = 7;\n" +
            "}\n" +
            "enum AdUnitId {\n" +
            "  PAID_SEARCH_UNIT = 1;\n" +
            "  RECOMMENDATION_WIDGET = 2;\n" +
            "  PROMOTED_LISTING = 3;\n" +
            "  IAB_IN_AD_NATIVE = 4;\n" +
            "  ADUNITID_CUSTOM = 5;\n" +
            "}\n" +
            "enum ContextType {\n" +
            "  CONTENT = 1;\n" +
            "  SOCIAL = 2;\n" +
            "  PRODUCT = 3;\n" +
            "}\n" +
            "enum ContextSubtype {\n" +
            "  CONTENT_GENERAL_OR_MIXED = 10;\n" +
            "  CONTENT_ARTICLE = 11;\n" +
            "  CONTENT_VIDEO = 12;\n" +
            "  CONTENT_AUDIO = 13;\n" +
            "  CONTENT_IMAGE = 14;\n" +
            "  CONTENT_USER_GENERATED = 15;\n" +
            "  SOCIAL_GENERAL = 20;\n" +
            "  SOCIAL_EMAIL = 21;\n" +
            "  SOCIAL_CHAT_IM = 22;\n" +
            "  PRODUCT_SELLING = 30;\n" +
            "  PRODUCT_MARKETPLACE = 31;\n" +
            "  PRODUCT_REVIEW = 32;\n" +
            "}\n" +
            "enum PlacementType {\n" +
            "  IN_FEED = 1;\n" +
            "  ATOMIC_UNIT = 2;\n" +
            "  OUTSIDE = 3;\n" +
            "  RECOMMENDATION = 4;\n" +
            "}\n" +
            "enum DataAssetType {\n" +
            "  SPONSORED = 1;\n" +
            "  DESC = 2;\n" +
            "  RATING = 3;\n" +
            "  LIKES = 4;\n" +
            "  DOWNLOADS = 5;\n" +
            "  PRICE = 6;\n" +
            "  SALEPRICE = 7;\n" +
            "  PHONE = 8;\n" +
            "  ADDRESS = 9;\n" +
            "  DESC2 = 10;\n" +
            "  DISPLAYURL = 11;\n" +
            "  CTATEXT = 12;\n" +
            "}\n" +
            "enum ImageAssetType {\n" +
            "  ICON = 1;\n" +
            "  LOGO = 2 [deprecated = true];\n" +
            "  MAIN = 3;\n" +
            "}\n" +
            "enum EventType {\n" +
            "  IMPRESSION = 1;\n" +
            "  VIEWABLE_MRC_50 = 2;\n" +
            "  VIEWABLE_MRC_100 = 3;\n" +
            "  VIEWABLE_VIDEO_50 = 4;\n" +
            "}\n" +
            "enum EventTrackingMethod {\n" +
            "  IMG = 1;\n" +
            "  JS = 2;\n" +
            "}\n";

    private static final String moneySchema1 = "syntax = \"proto3\";\n" +
            "package com.newsbreak.monetization.common;\n" +
            "\n" +
            "import \"openrtb-v26.proto\";\n" +
            "\n" +
            "option java_package = \"com.particles.mes.protos\";\n" +
            "option java_outer_classname = \"MonetizationCommon\";\n" +
            "option java_multiple_files = true;\n" +
            "\n" +
            "message RequestContext {\n" +
            "  uint64 ts_ms = 1;\n" +
            "  com.google.openrtb.BidRequest bid_request = 2;\n" +
            "  bool is_open_rtb_request = 3;\n" +
            "  RequestContextExt ext = 17;\n" +
            "}\n";

    private static final String moneySchema2 = "syntax = \"proto3\";\n" +
            "package com.newsbreak.monetization.common;\n" +
            "\n" +
            "import \"openrtb-v26.proto\";\n" +
            "\n" +
            "enum AdType {\n" +
            "    AD_TYPE_UNSPECIFIED = 0;\n" +
            "    AD_TYPE_NATIVE = 1;\n" +
            "    AD_TYPE_DISPLAY = 2;\n" +
            "    AD_TYPE_INTERSTITIAL = 3;\n" +
            "    AD_TYPE_VIDEO = 4;\n" +
            "}\n" +
            "\n" +
            "enum ImageType {\n" +
            "    IMAGE_TYPE_UNSPECIFIED = 0;\n" +
            "    // .jpeg or .jpg\n" +
            "    IMAGE_TYPE_JPEG = 1;\n" +
            "    IMAGE_TYPE_PNG = 2;\n" +
            "    IMAGE_TYPE_GIF = 3;\n" +
            "    IMAGE_TYPE_WEBP = 4;\n" +
            "}\n" +
            "\n" +
            "enum OsType {\n" +
            "    OS_TYPE_UNSPECIFIED = 0;\n" +
            "    OS_TYPE_IOS = 1;\n" +
            "    OS_TYPE_ANDROID = 2;\n" +
            "    OS_TYPE_IPADOS = 3;\n" +
            "}\n" +
            "\n" +
            "message RequestContext {\n" +
            "    // timestamp when request is sent\n" +
            "    uint64 ts_ms = 1;\n" +
            "\n" +
            "    com.google.openrtb.BidRequest bid_request = 2;\n" +
            "    bool is_open_rtb_request = 3;\n" +
            "\n" +
            "    // For application extension\n" +
            "    RequestContextExt ext = 17;\n" +
            "\n" +
            "    // The AuctionID is used to chain requests (both s2s and c2s), response,\n" +
            "    // impression, click, hide, report all together\n" +
            "    string auction_id = 18;\n" +
            "}\n" +
            "\n" +
            "// This object is designed for application extension, fields are bound to one or more application.\n" +
            "// Do not add common fields such as ts, os to this message, it is specifically designed for application extension.\n" +
            "message RequestContextExt {\n" +
            "    // required by Newsbreak\n" +
            "    string doc_id = 1;\n" +
            "\n" +
            "    // required by Newsbreak\n" +
            "    string session_id = 2;\n" +
            "\n" +
            "    // required by Newsbreak\n" +
            "    // e.g. \"article\", \"foryou\", \"local\" ...\n" +
            "    string source = 3;\n" +
            "\n" +
            "    // The index of the ads on certain slots such as in-feed, article-related and article-inside.\n" +
            "    uint32 position = 4;\n" +
            "\n" +
            "    string placement_id = 5;\n" +
            "\n" +
            "    repeated string buckets = 6;\n" +
            "\n" +
            "    string ad_slot_id = 7;\n" +
            "\n" +
            "    string user_id = 8;\n" +
            "}\n" +
            "\n" +
            "message Ad {\n" +
            "    // timestamp when bid is received\n" +
            "    uint64 ts_ms = 1;\n" +
            "    com.google.openrtb.BidResponse.SeatBid seat_bid = 2;\n" +
            "    string title = 3;\n" +
            "    string body = 4;\n" +
            "    AdType type = 5;\n" +
            "    string advertiser = 6;\n" +
            "    bytes full_screenshot = 7;\n" +
            "    bytes ad_screenshot = 8;\n" +
            "    string key = 9;\n" +
            "    ImageType full_screenshot_type = 10;\n" +
            "    ImageType ad_screenshot_type = 11;\n" +
            "    string adset_id = 12;\n" +
            "}";

    @Test
    public void testAddOnlyCheckAddScalar() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema originalSchema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringValidAddScalar1, schemaReferenceList, dependencies, 2, "nonLocalSample");
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddScalar1], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
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
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMap1], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
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
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMsg1], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
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
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringValidAddMixed1], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
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
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInvalidAddMixed1], errorMsg result:" + errorMessages);
        assertNotEquals(0, errorMessages.size());
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
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInValidAddMixedNonSequential], errorMsg result:" + errorMessages);
        assertNotEquals(0, errorMessages.size());
    }

    @Test
    public void testAddOnlyCheckNewValidSchema() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema schema = new ProtobufSchema(nonLocalSampleSchemaString, schemaReferenceList, dependencies, 1, "nonLocalSample");
        List<String> errorMessages = AddOnlySchemaChecker.sequentialSchemaInOrderCheck(schema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaString], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
    }
    @Test
    public void testAddOnlyCheckNewInvalidSchemaMixedNonSequential() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInValidAddMixedNonSequential, schemaReferenceList, dependencies, 2, "nonLocalSample");
        List<String> errorMessages = AddOnlySchemaChecker.sequentialSchemaInOrderCheck(updatedSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInValidAddMixedNonSequential], errorMsg result:" + errorMessages);
        assertNotEquals(0, errorMessages.size());
    }

    @Test
    //PB schema level does not enforce 1 schema 1 message logic. Enforced at CompatibilityResource Level.
    public void testAddOnlyCheckNewValidSchema2Msgs() {
        SchemaReference schemaReference = new SchemaReference("common","commonSchemaSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("common", commonSchemaString);
        ProtobufSchema updatedSchema = new ProtobufSchema(nonLocalSampleSchemaStringInvalid2Msgs, schemaReferenceList, dependencies, 2, "nonLocalSample");

        List<String> errorMessages = AddOnlySchemaChecker.sequentialSchemaInOrderCheck(updatedSchema);
        System.out.println("AddOnly schema check result for [nonLocalSampleSchemaStringInvalid2Msgs], errorMsg result:" + errorMessages);
        assertEquals(0, errorMessages.size());
    }

    //@Test
    //This test still need implementation.
    public void testAddOnlyCheckOpenrtbMsgs() {
        SchemaReference schemaReference = new SchemaReference("openrtb","openrtbSubject", 1);
        List<SchemaReference> schemaReferenceList = new LinkedList<>();
        schemaReferenceList.add(schemaReference);
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("openrtb", openrtbv26Schema);
        ProtobufSchema originalSchema = new ProtobufSchema(moneySchema1, schemaReferenceList, dependencies, 1, "moneySchemaSample");
        ProtobufSchema updatedSchema = new ProtobufSchema(moneySchema2, schemaReferenceList, dependencies, 2, "moneySchemaSample");
        List<String> errorMessages = updatedSchema.isAddOnlyCompatible(originalSchema);
        System.out.println("AddOnly schema check result for [moneySchemaSample], errorMsg result:" + errorMessages);
//        assertEquals(0, errorMessages.size());
    }
}
