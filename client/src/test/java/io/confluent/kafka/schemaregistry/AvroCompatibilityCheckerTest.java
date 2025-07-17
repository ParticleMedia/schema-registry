package io.confluent.kafka.schemaregistry;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.junit.Assert;
import org.junit.Test;

public class AvroCompatibilityCheckerTest {

    @Test
    public void notUnionTypeTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": \"string\",\n" +
                "      \"default\": null" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertNotEquals(schema.check().size(), 0);
    }

    @Test
    public void noDefaultValueTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertNotEquals(schema.check().size(), 0);
    }

    @Test
    public void validFieldTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        Assert.assertEquals(schema.check().size(), 0);
    }

    @Test
    public void enumTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\"type\":\"enum\",\"name\":\"enum_type\",\"symbols\":[\"a\",\"b\",\"c\"]}\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertEquals(schema.check().size(), 0);
    }

    @Test
    public void fixedTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\"type\":\"fixed\",\"name\":\"fixed_type\",\"size\":16}\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertEquals(schema.check().size(), 0);
    }

    @Test
    public void addNonUnionTypeTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "        ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"field2\",\n" +
                "      \"type\": \"string\",\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "        ]\n" +
                "}";

        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void addUnionTypeTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"field2\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"int\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
//        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void addButNotAppendTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field2\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"int\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void modifyExistingFieldTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"string\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        \"int\"\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void mapTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [ " +
                "        \"null\", " +
                "        { " +
                "          \"type\": \"map\", " +
                "          \"values\": [\"null\",\"float\"]" +
                "        }" +
                "      ], " +
                "      \"default\": null " +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [ " +
                "        \"null\", " +
                "        { " +
                "          \"type\": \"map\", " +
                "          \"values\": \"float\"" +
                "        }" +
                "      ], " +
                "      \"default\": null " +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void mapRecordTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\n" +
                "          \"type\": \"map\",\n" +
                "          \"values\": [\"null\",\n" +
                "            {\n" +
                "              \"type\": \"record\",\n" +
                "              \"name\": \"sub_schema\",\n" +
                "              \"fields\": [\n" +
                "              {\n" +
                "                \"name\": \"sub_field\",\n" +
                "                \"type\": [\"null\", \"int\"],\n" +
                "                \"default\": null\n" +
                "              }\n" +
                "              ]\n" +
                "              }]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\n" +
                "          \"type\": \"map\",\n" +
                "          \"values\": [\"null\",\n" +
                "            {\n" +
                "              \"type\": \"record\",\n" +
                "              \"name\": \"sub_schema\",\n" +
                "              \"fields\": [\n" +
                "              {\n" +
                "                \"name\": \"sub_field\",\n" +
                "                \"type\": [\"null\", \"int\"],\n" +
                "                \"default\": null\n" +
                "              },\n" +
                "              {\n" +
                "                \"name\": \"sub_field_2\",\n" +
                "                \"type\": [\"null\", \"string\"],\n" +
                "                \"default\": null\n" +
                "              }\n" +
                "              ]\n" +
                "              }]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void mapRecordNotAppendTest() {
        String previousSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\n" +
                "          \"type\": \"map\",\n" +
                "          \"values\": [\"null\",\n" +
                "            {\n" +
                "              \"type\": \"record\",\n" +
                "              \"name\": \"sub_schema\",\n" +
                "              \"fields\": [\n" +
                "              {\n" +
                "                \"name\": \"sub_field\",\n" +
                "                \"type\": [\"null\", \"int\"],\n" +
                "                \"default\": null\n" +
                "              }\n" +
                "              ]\n" +
                "              }]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\n" +
                "          \"type\": \"map\",\n" +
                "          \"values\": [\"null\",\n" +
                "            {\n" +
                "              \"type\": \"record\",\n" +
                "              \"name\": \"sub_schema\",\n" +
                "              \"fields\": [\n" +
                "              {\n" +
                "                \"name\": \"sub_field_2\",\n" +
                "                \"type\": [\"null\", \"int\"],\n" +
                "                \"default\": null\n" +
                "              },\n" +
                "              {\n" +
                "                \"name\": \"sub_field\",\n" +
                "                \"type\": [\"null\", \"int\"],\n" +
                "                \"default\": null\n" +
                "              }\n" +
                "              ]\n" +
                "              }]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        System.out.println(newSchema.isAddOnlyCompatible(previousSchema).toString());
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema).size(), 0);
    }

    @Test
    public void mapArrayTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [ " +
                "        \"null\", " +
                "        { " +
                "          \"type\": \"map\", " +
                "          \"values\": [\"null\",{\"type\":\"array\",\"items\":[\"null\",\"string\"]}]" +
                "        }" +
                "      ], " +
                "      \"default\": null " +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertEquals(schema.check().size(), 0);
    }

    @Test
    public void mapArrayRecordTest() {
        String schemaStr = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"test_schema\",\n" +
                "  \"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"field1\",\n" +
                "      \"type\": [\n" +
                "        \"null\",\n" +
                "        {\n" +
                "          \"type\": \"map\",\n" +
                "          \"values\": [\"null\",{\"type\":\"array\",\"items\":[\"null\",\n" +
                "          {\"type\":\"record\",\"name\":\"sub_record\",\"fields\":[{\"name\":\"sub_field\",\"type\":[\"null\",\"string\"],\"default\":null}]}]}]" +
                "        }\n" +
                "      ],\n" +
                "      \"default\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        System.out.println(schema.check().toString());
        Assert.assertEquals(schema.check().size(), 0);
    }

    @Test
    public void testNova1stRankingOnlineSample() {
        String schemaStr = "" +
                "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"record\",\n" +
                "\t\"namespace\": \"org.apache.flink.avro.generated\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"timestamp\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"biz_date\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"device_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"placement_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_unit\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"request_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_type\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_name\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_version\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_dense_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_sparse_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\t\"items\": [\n" +
                "\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_embedding_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\t\"items\": [\n" +
                "\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_model_result\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"record\",\n" +
                "\t\t\t\t\t\t\t\"name\": \"record_ad_model_result\",\n" +
                "\t\t\t\t\t\t\t\"fields\": [\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"name\": \"score\",\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"creative_fea_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"time_used_ms\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"int\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_dense_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_sparse_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\t\"items\": [\n" +
                "\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_embedding_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\t\"items\": [\n" +
                "\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_info\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"type\": \"record\",\n" +
                "\t\t\t\t\t\t\t\"name\": \"record_model_info\",\n" +
                "\t\t\t\t\t\t\t\"fields\": [\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"name\": \"model_name\",\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"name\": \"model_version\",\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"bundle\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"slot_request_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"rank_info\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": [\n" +
                "\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ab_tag\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"app\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_objective\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"sample_method\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ctr_score\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"double\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"cvr_score\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"double\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"sample_tag\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"data_type\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        AvroSchema avroSchema = new AvroSchema(schemaStr);
        System.out.println(avroSchema.check());
        Assert.assertEquals(avroSchema.check().size(), 0);
    }

    @Test
    public void testPreviousNova1stRankingOnlineSample() {
        String schemaStr = "" +
                "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"nova_1st_ranking_online_sample\",\n" +
                "\t\"namespace\": \"com.newsbreak.data.nova.avro\",\n" +
                "\t\"doc\": \"1st ranking online sample\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"timestamp\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"biz_date\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"device_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"placement_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_unit\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"request_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_type\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_name\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_version\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_dense_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": \"float\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_sparse_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\"items\": \"string\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_embedding_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\"items\": \"float\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_model_result\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"record\",\n" +
                "\t\t\t\t\t\t\"name\": \"ad_model_result_record\",\n" +
                "\t\t\t\t\t\t\"fields\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"score\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\"float\"\n" +
                "\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"creative_fea_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": \"string\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"time_used_ms\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"int\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_dense_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": \"float\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_sparse_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\"items\": \"string\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"user_embedding_feature_map\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\t\t\"items\": \"float\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"model_info\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": {\n" +
                "\t\t\t\t\t\t\"type\": \"record\",\n" +
                "\t\t\t\t\t\t\"name\": \"model_info_record\",\n" +
                "\t\t\t\t\t\t\"fields\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"model_name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"model_version\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": [\n" +
                "\t\t\t\t\t\t\t\t\t\"null\",\n" +
                "\t\t\t\t\t\t\t\t\t\"string\"\n" +
                "\t\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\t\"default\": null\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"bundle\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"slot_request_id\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"rank_info\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"map\",\n" +
                "\t\t\t\t\t\"values\": \"string\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ab_tag\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"app\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ad_objective\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"sample_method\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"ctr_score\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"double\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"cvr_score\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"double\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"sample_tag\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"data_type\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        AvroSchema avroSchema = new AvroSchema(schemaStr);
        System.out.println(avroSchema.check());
        Assert.assertNotEquals(avroSchema.check().size(), 0);
    }
}
