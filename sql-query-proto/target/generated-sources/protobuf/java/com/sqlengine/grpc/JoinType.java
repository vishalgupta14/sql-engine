// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: query_runner.proto

package com.sqlengine.grpc;

/**
 * Protobuf enum {@code queryrunner.JoinType}
 */
public enum JoinType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>INNER = 0;</code>
   */
  INNER(0),
  /**
   * <code>LEFT = 1;</code>
   */
  LEFT(1),
  /**
   * <code>RIGHT = 2;</code>
   */
  RIGHT(2),
  /**
   * <code>FULL = 3;</code>
   */
  FULL(3),
  /**
   * <code>CROSS = 4;</code>
   */
  CROSS(4),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>INNER = 0;</code>
   */
  public static final int INNER_VALUE = 0;
  /**
   * <code>LEFT = 1;</code>
   */
  public static final int LEFT_VALUE = 1;
  /**
   * <code>RIGHT = 2;</code>
   */
  public static final int RIGHT_VALUE = 2;
  /**
   * <code>FULL = 3;</code>
   */
  public static final int FULL_VALUE = 3;
  /**
   * <code>CROSS = 4;</code>
   */
  public static final int CROSS_VALUE = 4;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static JoinType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static JoinType forNumber(int value) {
    switch (value) {
      case 0: return INNER;
      case 1: return LEFT;
      case 2: return RIGHT;
      case 3: return FULL;
      case 4: return CROSS;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<JoinType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      JoinType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<JoinType>() {
          public JoinType findValueByNumber(int number) {
            return JoinType.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return com.sqlengine.grpc.QueryRunner.getDescriptor().getEnumTypes().get(1);
  }

  private static final JoinType[] VALUES = values();

  public static JoinType valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private JoinType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:queryrunner.JoinType)
}

