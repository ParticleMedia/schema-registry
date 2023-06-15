/**
 * Copyright 2014 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.kafka.schemaregistry.avro;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;

import org.apache.avro.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroSchema implements ParsedSchema {

  private static final Logger log = LoggerFactory.getLogger(AvroSchema.class);

  public static final String TYPE = "AVRO";

  private static final SchemaValidator BACKWARD_VALIDATOR =
      new SchemaValidatorBuilder().canReadStrategy().validateLatest();

  private final Schema schemaObj;
  private String canonicalString;
  private final Integer version;
  private final List<SchemaReference> references;
  private final Map<String, String> resolvedReferences;

  public AvroSchema(String schemaString) {
    this(schemaString, Collections.emptyList(), Collections.emptyMap(), null);
  }

  public AvroSchema(String schemaString,
                    List<SchemaReference> references,
                    Map<String, String> resolvedReferences,
                    Integer version) {
    Schema.Parser parser = getParser();
    for (String schema : resolvedReferences.values()) {
      parser.parse(schema);
    }
    this.schemaObj = parser.parse(schemaString);
    this.references = Collections.unmodifiableList(references);
    this.resolvedReferences = Collections.unmodifiableMap(resolvedReferences);
    this.version = version;
  }

  public AvroSchema(Schema schemaObj) {
    this(schemaObj, null);
  }

  public AvroSchema(Schema schemaObj, Integer version) {
    this.schemaObj = schemaObj;
    this.references = Collections.emptyList();
    this.resolvedReferences = Collections.emptyMap();
    this.version = version;
  }

  private AvroSchema(
      Schema schemaObj,
      String canonicalString,
      List<SchemaReference> references,
      Map<String, String> resolvedReferences,
      Integer version
  ) {
    this.schemaObj = schemaObj;
    this.canonicalString = canonicalString;
    this.references = references;
    this.resolvedReferences = resolvedReferences;
    this.version = version;
  }

  public AvroSchema copy() {
    return new AvroSchema(
        this.schemaObj,
        this.canonicalString,
        this.references,
        this.resolvedReferences,
        this.version
    );
  }

  protected Schema.Parser getParser() {
    Schema.Parser parser = new Schema.Parser();
    parser.setValidateDefaults(false);
    return parser;
  }

  @Override
  public Schema rawSchema() {
    return schemaObj;
  }

  @Override
  public String schemaType() {
    return TYPE;
  }

  @Override
  public String name() {
    if (schemaObj != null && schemaObj.getType() == Schema.Type.RECORD) {
      return schemaObj.getFullName();
    }
    return null;
  }

  @Override
  public String canonicalString() {
    if (schemaObj == null) {
      return null;
    }
    if (canonicalString == null) {
      Schema.Parser parser = getParser();
      List<Schema> schemaRefs = new ArrayList<>();
      for (String schema : resolvedReferences.values()) {
        Schema schemaRef = parser.parse(schema);
        schemaRefs.add(schemaRef);
      }
      canonicalString = Schemas.toString(schemaObj, schemaRefs);
    }
    return canonicalString;
  }

  public Integer version() {
    return version;
  }

  @Override
  public List<SchemaReference> references() {
    return references;
  }

  public Map<String, String> resolvedReferences() {
    return resolvedReferences;
  }

  @Override
  public boolean isBackwardCompatible(ParsedSchema previousSchema) {
    if (!schemaType().equals(previousSchema.schemaType())) {
      return false;
    }
    try {
      BACKWARD_VALIDATOR.validate(this.schemaObj,
          Collections.singleton(((AvroSchema) previousSchema).schemaObj));
      return true;
    } catch (SchemaValidationException e) {
      return false;
    } catch (Exception e) {
      log.error("Unexpected exception during compatibility check", e);
      return false;
    }
  }

  public boolean isAddOnlyCompatible(ParsedSchema previousSchema) {
    log.info("is add only compatible");
    if (!schemaType().equals(previousSchema.schemaType())) {
      return false;
    }
    try {
      if (this.schemaObj.getFields()
              .size() < ((AvroSchema) previousSchema).schemaObj.getFields().size()) {
        log.info("New schema fields size is less than previous schema");
        return false;
      }
      int oldSchemaSize = ((AvroSchema) previousSchema).schemaObj.getFields().size();
      for (int i = 0; i < oldSchemaSize; i++) {
        Schema.Field oldField = ((AvroSchema) previousSchema).schemaObj.getFields().get(i);
        Schema.Field newField = this.schemaObj.getFields().get(i);
        // change field validate
        if (!oldField.equals(newField)) {
          log.info("New schema fields: {} is not equal to previous schema {}", newField.toString(),
                  oldField.toString());
          // complex type can be require
          if (Schema.Type.RECORD == newField.schema().getType() || Schema.Type.ARRAY == newField.schema().getType() || Schema.Type.MAP == newField.schema().getType()) {
            Schema newSchema = newField.schema();
            Schema oldSchema = oldField.schema();
            if (Schema.Type.RECORD == newSchema.getType()) { // if old = new = record
              AvroSchema newAvroSchema = new AvroSchema(newSchema);
              boolean flag = newAvroSchema
                      .isAddOnlyCompatible(new AvroSchema(oldSchema));
              if (!flag) {
                return false;
              }
            } else if (Schema.Type.ARRAY == newSchema.getType()) { // if old = new = array
              boolean flag = new AvroSchema(newSchema.getElementType())
                      .isAddOnlyCompatible(new AvroSchema(oldSchema.getElementType()));
              if (!flag) {
                return false;
              }
            } else if (Schema.Type.MAP == newSchema.getType()) { // if old = new = array
              // todo
              return false;
            } else {
              return false;
            }
          } else if (Schema.Type.UNION == newField.schema().getType()) { // if new = optional
            if (!"null".equals(newField.schema().getTypes().get(0).getType().getName())) { // new schema must be optional
              log.info("New schema fields: {} is not optional", newField.toString());
              return false;
            }
            if (!newField.hasDefaultValue() || !JsonProperties.Null.class.getName().equals(newField.defaultVal().getClass().getName())) {
              log.info("New schema fields default value: {} is not null", newField.toString());
              return false;
            }
            Schema newSchema = newField.schema().getTypes().get(1);
            Schema oldSchema = oldField.schema().getTypes().get(1);
            if (Schema.Type.RECORD == newSchema.getType()) { // if old = new = record
              AvroSchema newAvroSchema = new AvroSchema(newSchema);
              boolean flag = newAvroSchema
                      .isAddOnlyCompatible(new AvroSchema(oldSchema));
              if (!flag) {
                return false;
              }
            } else if (Schema.Type.ARRAY == newSchema.getType()) { // if old = new = array
              boolean flag = new AvroSchema(newSchema.getElementType())
                      .isAddOnlyCompatible(new AvroSchema(oldSchema.getElementType()));
              if (!flag) {
                return false;
              }
            } else if (Schema.Type.MAP == newSchema.getType()) { // if old = new = array
              // todo
              return false;
            } else {
              return false;
            }
          } else { // if new is basic required type
            log.info("New schema fields: {} is not UNION type", newField.toString());
            return false;
          }
        }
      }
      // add field validate
      int newSchemaSize = this.schemaObj.getFields().size();
      for (int i = oldSchemaSize; i < newSchemaSize; i++) {
        Schema.Field newField = this.schemaObj.getFields().get(i);
        if (Schema.Type.UNION != newField.schema().getType()) { // add field must be optional
          log.info("New schema fields: {} is not UNION type", newField.toString());
          return false;
        }
        if (!"null".equals(newField.schema().getTypes().get(0).getType().getName())) { // new schema must be optional
          log.info("New schema fields: {} is not optional", newField.toString());
          return false;
        }
        if (!newField.hasDefaultValue() || !JsonProperties.Null.class.getName().equals(newField.defaultVal().getClass().getName())) {
          log.info("New schema fields default value: {} is not null", newField.toString());
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      log.error("Unexpected exception during compatibility check", e);
      return false;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AvroSchema that = (AvroSchema) o;
    return Objects.equals(schemaObj, that.schemaObj)
        && Objects.equals(references, that.references)
        && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaObj, references, version);
  }

  @Override
  public String toString() {
    return canonicalString();
  }
}
