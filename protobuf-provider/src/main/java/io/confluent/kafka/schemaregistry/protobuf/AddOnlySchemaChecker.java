package io.confluent.kafka.schemaregistry.protobuf;

import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.internal.parser.*;
//import com.sun.tools.javac.util.Pair;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.diff.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.confluent.kafka.schemaregistry.protobuf.diff.Difference.Type.*;

/**
 * Helper class to detect whether a PB schema change complies with "Append Only" logic.
 * Data that writes to offline table must use "Append Only" which only add new columns to the end of tables/structs.
 * Created by ricky on 2023/12/14.
 */
public class AddOnlySchemaChecker {
    private static Logger log = LoggerFactory.getLogger(AddOnlySchemaChecker.class);

    public static final Set<Difference.Type> COMPATIBLE_CHANGES;

    static {
        Set<Difference.Type> changes = new HashSet<>();

        changes.add(MESSAGE_ADDED);
        changes.add(ENUM_ADDED);
//        changes.add(ENUM_REMOVED);
        changes.add(ENUM_CONST_ADDED);
        changes.add(FIELD_ADDED);

        COMPATIBLE_CHANGES = Collections.unmodifiableSet(changes);
    }

    static class Pair<F,S> {
        private F first;
        private S second;

        private Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
        public static <F, S> Pair<F,S> of(F first, S second) {
            return new Pair<>(first, second);
        }
    }

    public static List<String> checkCompatibility(ParsedSchema previousSchema, ParsedSchema currentSchema) {
        final List<Difference> differences = SchemaDiff.compare(
                (ProtobufSchema) previousSchema, (ProtobufSchema) currentSchema
        );

        log.info("Starting basic field change checks.");
        //First check to see if basic change differences are accepted.
        final List<Difference> incompatibleDiffs = differences.stream()
                .filter(diff -> !COMPATIBLE_CHANGES.contains(diff.getType()))
                .collect(Collectors.toList());
        boolean isCompatible = incompatibleDiffs.isEmpty();
        if (!isCompatible) {
            List<String> errorMessages = new ArrayList<>();
            for (Difference incompatibleDiff : incompatibleDiffs) {
                log.warn("Found incompatible change: %s", incompatibleDiff);
                errorMessages.add(String.format("Found incompatible change: %s", incompatibleDiff));
            }
            return errorMessages;
        }

        log.info("Finished schema basic check, starting in order check.");

        //Then check to see if all schema changes are in order.
        return schemaSequenceInOrder(currentSchema, previousSchema);
    }

    //To enforce add-only logic, we enforce all fields must in numeric non-gaping increasing order.
    //E.g. user defined field 10, 20. Added field 11 later. This will make offline table alter logic super complex.
    private static List<String> schemaSequenceInOrder(ParsedSchema currentSchema, ParsedSchema previousSchema) {
        List<String> errorMsg = new LinkedList<>();
        errorMsg.addAll(sequentialSchemaInOrderCheck(currentSchema));
        errorMsg.addAll(sequentialSchemaInOrderCheck(previousSchema));
        return errorMsg;
    }

    //TODO When creating new schema, no need to compare between 2 but run sequential check.
    public static List<String> sequentialSchemaInOrderCheck(
            final ParsedSchema parsedSchema) {
        List<String> errorMsg = new LinkedList<>();
        log.info(String.format("Running sequentialSchemaInOrderCheck for schema:%s", parsedSchema));
        ProtobufSchema protobufSchema = (ProtobufSchema)parsedSchema;
        List<TypeElement> typeElements = protobufSchema.rawSchema().getTypes();
//        Map<String, SchemaReference> references = protobufSchema.references().stream()
//                .collect(Collectors.toMap(
//                        SchemaReference::getName,
//                        r -> r,
//                        (existing, replacement) -> replacement));
//        Map<String, ProtoFileElement> dependencies = protobufSchema.dependenciesWithLogicalTypes();
//        TypeElement typeElement;
        Context context = new Context();
        context.collectTypeInfo(protobufSchema, true);

        String msgName = typeElements.get(0).getName();
        ProtoType msgProtoType = ProtoType.get(msgName);
        checkMessageSequenceOrder(context, errorMsg, msgProtoType);

        return errorMsg;
    }

    private static void checkMessageSequenceOrder(Context context,
                                                  List<String> errorMsg,
                                                  ProtoType msgProtoType) {
        log.info(String.format("Running checkMessageSequenceOrder for schema:%s", msgProtoType));
//        System.out.println(String.format("DEBUG, dealing with %s", msgProtoType));
        String originalFullName = context.resolve(msgProtoType.toString(), true);
        log.info(String.format("DEBUG, dealing with %s", originalFullName));
//        System.out.println(String.format("DEBUG, dealing with %s", originalFullName));
        Context.TypeElementInfo originalType = context.getType(originalFullName, true);

        MessageElement messageElement = (MessageElement) originalType.type();

        List<Pair<Integer, FieldElement>> fieldOrderSequence = messageElement.getFields().stream()
                .map(fieldElement -> Pair.of(fieldElement.getTag(), fieldElement)).collect(Collectors.toList());

        for(int i = 1; i <= fieldOrderSequence.size(); i++) {
            if (fieldOrderSequence.get(i-1).first != i) {
                errorMsg.add(String.format("Schema is not in sequential increasing order, field:%s in schema:%s", fieldOrderSequence.get(i-1).second, messageElement));
                errorMsg.add("\n");
            }
            FieldElement fieldElement = fieldOrderSequence.get(i-1).second;
            String fieldType = fieldElement.getType();
            ProtoType protoType = ProtoType.get(fieldType);

            Type type = type(context, protoType, true);
            switch (type) {
                case SCALAR:
                    log.info(String.format("No need to compare sequence for simple scalar type:%s", protoType));
                    break;
                case MESSAGE:
                    checkMessageSequenceOrder(context, errorMsg, protoType);
                    break;
                case MAP:
                    checkMapSequenceOrder(context, errorMsg, protoType);
                    break;
                default:
                    break;
            }
        }
    }

    private static void checkMapSequenceOrder(Context context,
                                              List<String> errorMsg,
                                              ProtoType mapProtoType) {
        log.info(String.format("Running checkMapSequenceOrder for schema:%s", mapProtoType));
        if (mapProtoType != null) {
            ProtoType valueType = mapProtoType.getValueType();
            if (valueType != null) {
                Type type = type(context, valueType, true);
                switch (type) {
                    case SCALAR:
                        log.info(String.format("No need to compare sequence for simple scalar type:%s", valueType));
                        break;
                    case MESSAGE:
                        checkMessageSequenceOrder(context, errorMsg, valueType);
                        break;
                    case MAP:
                        checkMapSequenceOrder(context, errorMsg, valueType);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    //TODO remove isOriginal
    static Type type(final Context ctx, ProtoType type, boolean isOriginal) {
        if (type.isScalar()) {
            return Type.SCALAR;
        } else if (type.isMap()) {
            return Type.MAP;
        } else {
            //TODO here
            Context.TypeElementInfo typeInfo = ctx.getType(type.toString(), isOriginal);
            if (typeInfo != null && typeInfo.type() instanceof EnumElement) {
                return Type.SCALAR;
            }
            return Type.MESSAGE;
        }
    }

    enum Type {
        SCALAR, MAP, MESSAGE
    }

    private static Pair<MessageElement, Map<String, EnumElement>> parseMessageElements(List<TypeElement> types) {
        int index = 0;
        MessageElement messageElement = null;
        Map<String, EnumElement> enums = new HashMap<>();
        for (TypeElement typeElement : types) {
            if (typeElement instanceof MessageElement) {
                //TODO Only enable this when there's a dedicated ETL config UI page.
                // only 1 msg per AutoETL schema, otherwise offline not able to convert to table.
//                if(index >= 1) {
//                    throw new IllegalStateException(String.format("Seeing more than 1 msg in a schema, unable to trigger offline table creation:%s", types));
//                }
                messageElement = (MessageElement) typeElement;
            } else if (typeElement instanceof EnumElement) {
                EnumElement enumElement = (EnumElement) typeElement;
                enums.put(enumElement.getName(), enumElement);
            }
        }
        if (messageElement == null) {
            throw new IllegalStateException(String.format("No msg in a schema, unable to trigger offline table creation:%s", types));
        }
        return Pair.of(messageElement, enums);
    }
}
