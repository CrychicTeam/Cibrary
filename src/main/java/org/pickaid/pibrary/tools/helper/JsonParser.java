package org.pickaid.pibrary.tools.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.*;

import java.util.Map;

public class JsonParser {
    private static final byte TAG_BYTE = 1;
    private static final byte TAG_SHORT = 2;
    private static final byte TAG_INT = 3;
    private static final byte TAG_LONG = 4;
    private static final byte TAG_FLOAT = 5;
    private static final byte TAG_DOUBLE = 6;
    private static final byte TAG_BYTE_ARRAY = 7;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_LIST = 9;
    private static final byte TAG_COMPOUND = 10;
    private static final byte TAG_INT_ARRAY = 11;
    private static final byte TAG_LONG_ARRAY = 12;

    /**
     * Parse CompoundTag to JsonObject
     *
     * @param tag CompoundTag to be parsed
     * @return JsonObject representing the CompoundTag
     */
    public static JsonObject toJSON(CompoundTag tag) {
        JsonObject output = new JsonObject();

        for (String key : tag.getAllKeys()) {
            byte type = tag.getTagType(key);
            switch (type) {
                case TAG_BYTE:
                    if (tag.getByte(key) == 0 || tag.getByte(key) == 1) {
                        output.addProperty(key, tag.getBoolean(key));
                    } else {
                        output.addProperty(key, tag.getByte(key));
                    }
                    break;
                case TAG_SHORT:
                    output.addProperty(key, tag.getShort(key));
                    break;
                case TAG_INT:
                    output.addProperty(key, tag.getInt(key));
                    break;
                case TAG_LONG:
                    output.addProperty(key, tag.getLong(key));
                    break;
                case TAG_FLOAT:
                    output.addProperty(key, tag.getFloat(key));
                    break;
                case TAG_DOUBLE:
                    output.addProperty(key, tag.getDouble(key));
                    break;
                case TAG_BYTE_ARRAY:
                    output.add(key, byteArrayToJson(tag.getByteArray(key)));
                    break;
                case TAG_STRING:
                    output.addProperty(key, tag.getString(key));
                    break;
                case TAG_LIST:
                    output.add(key, listTagToJson(tag.getList(key, 0)));
                    break;
                case TAG_COMPOUND:
                    output.add(key, toJSON(tag.getCompound(key)));
                    break;
                case TAG_INT_ARRAY:
                    output.add(key, intArrayToJson(tag.getIntArray(key)));
                    break;
                case TAG_LONG_ARRAY:
                    output.add(key, longArrayToJson(tag.getLongArray(key)));
                    break;
                default:
                    output.addProperty(key, tag.get(key).toString());
                    break;
            }
        }

        return output;
    }

    private static JsonArray listTagToJson(ListTag listTag) {
        JsonArray array = new JsonArray();
        byte elementType = listTag.getElementType();

        for (int i = 0; i < listTag.size(); i++) {
            Tag element = listTag.get(i);

            switch (elementType) {
                case TAG_COMPOUND:
                    array.add(toJSON((CompoundTag) element));
                    break;
                case TAG_STRING:
                    array.add(new JsonPrimitive(((StringTag) element).getAsString()));
                    break;
                case TAG_BYTE:
                    ByteTag byteTag = (ByteTag) element;
                    if (byteTag.getAsByte() == 0 || byteTag.getAsByte() == 1) {
                        boolean bool = byteTag.getAsByte() != 0;
                        array.add(new JsonPrimitive(bool));
                    } else {
                        array.add(new JsonPrimitive(byteTag.getAsByte()));
                    }
                    break;
                case TAG_SHORT:
                    array.add(new JsonPrimitive(((ShortTag) element).getAsShort()));
                    break;
                case TAG_INT:
                    array.add(new JsonPrimitive(((IntTag) element).getAsInt()));
                    break;
                case TAG_LONG:
                    array.add(new JsonPrimitive(((LongTag) element).getAsLong()));
                    break;
                case TAG_FLOAT:
                    array.add(new JsonPrimitive(((FloatTag) element).getAsFloat()));
                    break;
                case TAG_DOUBLE:
                    array.add(new JsonPrimitive(((DoubleTag) element).getAsDouble()));
                    break;
                case TAG_LIST:
                    array.add(listTagToJson((ListTag) element));
                    break;
                case TAG_BYTE_ARRAY:
                    array.add(byteArrayToJson(((ByteArrayTag) element).getAsByteArray()));
                    break;
                case TAG_INT_ARRAY:
                    array.add(intArrayToJson(((IntArrayTag) element).getAsIntArray()));
                    break;
                case TAG_LONG_ARRAY:
                    array.add(longArrayToJson(((LongArrayTag) element).getAsLongArray()));
                    break;
                default:
                    array.add(new JsonPrimitive(element.toString()));
                    break;
            }
        }

        return array;
    }

    private static JsonArray byteArrayToJson(byte[] array) {
        JsonArray jsonArray = new JsonArray();
        for (byte b : array) {
            jsonArray.add(new JsonPrimitive(b));
        }
        return jsonArray;
    }

    private static JsonArray intArrayToJson(int[] array) {
        JsonArray jsonArray = new JsonArray();
        for (int i : array) {
            jsonArray.add(new JsonPrimitive(i));
        }
        return jsonArray;
    }

    private static JsonArray longArrayToJson(long[] array) {
        JsonArray jsonArray = new JsonArray();
        for (long l : array) {
            jsonArray.add(new JsonPrimitive(l));
        }
        return jsonArray;
    }

    /**
     * Parse JsonObject to CompoundTag
     *
     * @param object JsonObject to be parsed
     * @return CompoundTag representing the JsonObject
     */
    public static CompoundTag fromJSON(JsonObject object) {
        CompoundTag output = new CompoundTag();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive()) {
                addPrimitiveToTag(output, key, element.getAsJsonPrimitive());
            } else if (element.isJsonObject()) {
                output.put(key, fromJSON(element.getAsJsonObject()));
            } else if (element.isJsonArray()) {
                addArrayToTag(output, key, element.getAsJsonArray());
            }
        }

        return output;
    }

    private static void addPrimitiveToTag(CompoundTag tag, String key, JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            tag.putBoolean(key, primitive.getAsBoolean());
        } else if (primitive.isNumber()) {
            addNumberToTag(tag, key, primitive.getAsNumber());
        } else if (primitive.isString()) {
            tag.putString(key, primitive.getAsString());
        }
    }

    private static void addNumberToTag(CompoundTag tag, String key, Number value) {
        double doubleVal = value.doubleValue();
        long longVal = value.longValue();

        if (doubleVal == longVal) {
            // Integer type
            if (longVal >= Byte.MIN_VALUE && longVal <= Byte.MAX_VALUE) {
                tag.putByte(key, value.byteValue());
            } else if (longVal >= Short.MIN_VALUE && longVal <= Short.MAX_VALUE) {
                tag.putShort(key, value.shortValue());
            } else if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                tag.putInt(key, value.intValue());
            } else {
                tag.putLong(key, longVal);
            }
        } else {
            // Floating point type
            if (doubleVal >= Float.MIN_VALUE && doubleVal <= Float.MAX_VALUE) {
                tag.putFloat(key, value.floatValue());
            } else {
                tag.putDouble(key, doubleVal);
            }
        }
    }

    private static void addArrayToTag(CompoundTag tag, String key, JsonArray array) {
        if (array.size() == 0) {
            tag.put(key, new ListTag());
            return;
        }

        JsonElement firstElement = array.get(0);

        if (firstElement.isJsonObject()) {
            addCompoundListToTag(tag, key, array);
        } else if (firstElement.isJsonPrimitive()) {
            JsonPrimitive primitive = firstElement.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                addNumberArrayToTag(tag, key, array);
            } else if (primitive.isString()) {
                addStringListToTag(tag, key, array);
            } else if (primitive.isBoolean()) {
                addBooleanListToTag(tag, key, array);
            }
        } else if (firstElement.isJsonArray()) {
            tag.put(key, new ListTag());
        }
    }

    private static void addCompoundListToTag(CompoundTag tag, String key, JsonArray array) {
        ListTag listTag = new ListTag();
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                listTag.add(fromJSON(element.getAsJsonObject()));
            }
        }
        tag.put(key, listTag);
    }

    private static void addNumberArrayToTag(CompoundTag tag, String key, JsonArray array) {
        boolean canBeByteArray = true;
        boolean canBeIntArray = true;
        boolean canBeLongArray = true;

        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                canBeByteArray = canBeIntArray = canBeLongArray = false;
                break;
            }

            long value = element.getAsLong();
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                canBeByteArray = false;
            }
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                canBeIntArray = false;
            }
        }

        if (canBeByteArray) {
            byte[] byteArray = new byte[array.size()];
            for (int i = 0; i < array.size(); i++) {
                byteArray[i] = array.get(i).getAsByte();
            }
            tag.putByteArray(key, byteArray);
        } else if (canBeIntArray) {
            int[] intArray = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                intArray[i] = array.get(i).getAsInt();
            }
            tag.putIntArray(key, intArray);
        } else if (canBeLongArray) {
            long[] longArray = new long[array.size()];
            for (int i = 0; i < array.size(); i++) {
                longArray[i] = array.get(i).getAsLong();
            }
            tag.putLongArray(key, longArray);
        } else {
            ListTag listTag = new ListTag();
            for (JsonElement element : array) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    Number num = element.getAsNumber();
                    if (num.doubleValue() == num.longValue()) {
                        listTag.add(IntTag.valueOf(num.intValue()));
                    } else {
                        listTag.add(FloatTag.valueOf(num.floatValue()));
                    }
                }
            }
            tag.put(key, listTag);
        }
    }

    private static void addStringListToTag(CompoundTag tag, String key, JsonArray array) {
        ListTag listTag = new ListTag();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                listTag.add(StringTag.valueOf(element.getAsString()));
            }
        }
        tag.put(key, listTag);
    }

    private static void addBooleanListToTag(CompoundTag tag, String key, JsonArray array) {
        ListTag listTag = new ListTag();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                listTag.add(ByteTag.valueOf(element.getAsBoolean()));
            }
        }
        tag.put(key, listTag);
    }
}