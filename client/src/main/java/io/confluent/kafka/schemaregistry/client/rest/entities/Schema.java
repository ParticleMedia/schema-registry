/*
 * Copyright 2018 Confluent Inc.
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

package io.confluent.kafka.schemaregistry.client.rest.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@io.swagger.v3.oas.annotations.media.Schema(description = "Schema")
public class Schema implements Comparable<Schema> {

  public static final String SUBJECT_DESC = "Name of the subject";
  public static final String VERSION_DESC = "Version number";
  public static final String ID_DESC = "Globally unique identifier of the schema";
  public static final String TYPE_DESC = "Schema type";
  public static final String REFERENCES_DESC = "References to other schemas";
  public static final String SCHEMA_DESC = "Schema definition string";

  private static final String DEFAULT_BUSINESS = "default";

  private static final boolean DEFAULT_AUTO_ETL_ENABLED = false;

  private String subject;
  private Integer version;
  private Integer id;
  private String schemaType;
  private List<SchemaReference> references;
  private String schema;

  //Following are additional information provided for internal management.
  private String business;

  private boolean autoETLEnabled;

//  @JsonCreator
  public Schema(@JsonProperty("subject") String subject,
                @JsonProperty("version") Integer version,
                @JsonProperty("id") Integer id,
                @JsonProperty("schemaType") String schemaType,
                @JsonProperty("references") List<SchemaReference> references,
                @JsonProperty("schema") String schema) {
    this.subject = subject;
    this.version = version;
    this.id = id;
    this.schemaType = schemaType != null ? schemaType : AvroSchema.TYPE;
    this.references = references != null ? references : Collections.emptyList();
    this.schema = schema;
    this.business = DEFAULT_BUSINESS;
    this.autoETLEnabled = DEFAULT_AUTO_ETL_ENABLED;
  }

  @JsonCreator
  public Schema(@JsonProperty("subject") String subject,
                @JsonProperty("version") Integer version,
                @JsonProperty("id") Integer id,
                @JsonProperty("schemaType") String schemaType,
                @JsonProperty("references") List<SchemaReference> references,
                @JsonProperty("schema") String schema,
                @JsonProperty("business") String business,
                @JsonProperty("autoETLEnabled") Boolean autoETLEnabled) {
    this.subject = subject;
    this.version = version;
    this.id = id;
    this.schemaType = schemaType != null ? schemaType : AvroSchema.TYPE;
    this.references = references != null ? references : Collections.emptyList();
    this.schema = schema;
    if (business != null) {
      this.business = business;
    } else {
      this.business = DEFAULT_BUSINESS;
    }
    this.business = business == null ? DEFAULT_BUSINESS : business;
    this.autoETLEnabled = autoETLEnabled == null ? DEFAULT_AUTO_ETL_ENABLED : autoETLEnabled;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = SUBJECT_DESC)
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }

  @JsonProperty("subject")
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = VERSION_DESC)
  @JsonProperty("version")
  public Integer getVersion() {
    return this.version;
  }

  @JsonProperty("version")
  public void setVersion(Integer version) {
    this.version = version;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = ID_DESC)
  @JsonProperty("id")
  public Integer getId() {
    return this.id;
  }

  @JsonProperty("id")
  public void setId(Integer id) {
    this.id = id;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = TYPE_DESC)
  @JsonProperty("schemaType")
  @JsonSerialize(converter = SchemaTypeConverter.class)
  public String getSchemaType() {
    return this.schemaType;
  }

  @JsonProperty("schemaType")
  public void setSchemaType(String schemaType) {
    this.schemaType = schemaType;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = REFERENCES_DESC)
  @JsonProperty("references")
  public List<SchemaReference> getReferences() {
    return this.references;
  }

  @JsonProperty("references")
  public void setReferences(List<SchemaReference> references) {
    this.references = references;
  }

  @io.swagger.v3.oas.annotations.media.Schema(description = SCHEMA_DESC)
  @JsonProperty("schema")
  public String getSchema() {
    return this.schema;
  }

  @JsonProperty("schema")
  public void setSchema(String schema) {
    this.schema = schema;
  }

  @JsonProperty("business")
  public String getBusiness() {
    return this.business;
  }

  @JsonProperty("business")
  public void setBusiness(String business) {
    this.business = business;
  }

  @JsonProperty("autoETLEnabled")
  public boolean getAutoETLEnabled() {
    return this.autoETLEnabled;
  }

  @JsonProperty("autoETLEnabled")
  public void setAutoETLEnabled(Boolean business) {
    this.autoETLEnabled = autoETLEnabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Schema schema1 = (Schema) o;
    return Objects.equals(subject, schema1.subject)
        && Objects.equals(version, schema1.version)
        && Objects.equals(id, schema1.id)
        && Objects.equals(schemaType, schema1.schemaType)
        && Objects.equals(references, schema1.references)
        && Objects.equals(schema, schema1.schema)
        && Objects.equals(business, schema1.business)
        && Objects.equals(autoETLEnabled, schema1.autoETLEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, version, id, schemaType, references, schema, business, autoETLEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{subject=" + this.subject + ",");
    sb.append("version=" + this.version + ",");
    sb.append("id=" + this.id + ",");
    sb.append("schemaType=" + this.schemaType + ",");
    sb.append("references=" + this.references + ",");
    sb.append("business=" + this.business + ",");
    sb.append("autoETLEnabled=" + this.autoETLEnabled + ",");
    sb.append("schema=" + this.schema + "}");
    return sb.toString();
  }

  @Override
  public int compareTo(Schema that) {
    int result = this.subject.compareTo(that.subject);
    if (result != 0) {
      return result;
    }
    result = this.version - that.version;
    return result;
  }
}
