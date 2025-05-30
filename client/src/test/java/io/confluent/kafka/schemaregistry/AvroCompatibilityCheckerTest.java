package io.confluent.kafka.schemaregistry;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.junit.Assert;
import org.junit.Test;

public class AvroCompatibilityCheckerTest {

    @Test
    public void newSchemaNonUnionTypeTest() {
        String schemaStr = "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"test_schema\",\n" +
                "\t\"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"field1\",\n" +
                "\t\t\t\"type\": \"string\"\n" +
                "\t\t}\n" +
                "        ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        Assert.assertNotEquals(schema.fieldCheck().size(), 0);
    }

    @Test
    public void newSchemaUnionTypeTest() {
        String schemaStr = "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"test_schema\",\n" +
                "\t\"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"field1\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t}\n" +
                "        ]\n" +
                "}";
        AvroSchema schema = new AvroSchema(schemaStr);
        Assert.assertEquals(schema.fieldCheck().size(), 0);
    }

    @Test
    public void addNonUnionTypeTest() {
        String previousSchemaStr = "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"test_schema\",\n" +
                "\t\"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"field1\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t}\n" +
                "        ]\n" +
                "}";
        String newSchemaStr = "{\n" +
                "\t\"type\": \"record\",\n" +
                "\t\"name\": \"test_schema\",\n" +
                "\t\"namespace\": \"com.newsbreak.data.avro.test\",\n" +
                "\t\"fields\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"field1\",\n" +
                "\t\t\t\"type\": [\n" +
                "\t\t\t\t\"null\",\n" +
                "\t\t\t\t\"string\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"default\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"field2\",\n" +
                "\t\t\t\"type\": \"string\"\n" +
                "\t\t}\n" +
                "        ]\n" +
                "}";

        AvroSchema previousSchema = new AvroSchema(previousSchemaStr);
        AvroSchema newSchema = new AvroSchema(newSchemaStr);
        Assert.assertNotEquals(newSchema.isAddOnlyCompatible(previousSchema), 0);
    }
}
