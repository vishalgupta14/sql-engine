// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: query_runner.proto

package com.sqlengine.grpc;

public interface QueryConditionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:queryrunner.QueryCondition)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string fieldName = 1;</code>
   * @return The fieldName.
   */
  java.lang.String getFieldName();
  /**
   * <code>string fieldName = 1;</code>
   * @return The bytes for fieldName.
   */
  com.google.protobuf.ByteString
      getFieldNameBytes();

  /**
   * <code>string value = 2;</code>
   * @return The value.
   */
  java.lang.String getValue();
  /**
   * <code>string value = 2;</code>
   * @return The bytes for value.
   */
  com.google.protobuf.ByteString
      getValueBytes();

  /**
   * <code>string operator = 3;</code>
   * @return The operator.
   */
  java.lang.String getOperator();
  /**
   * <code>string operator = 3;</code>
   * @return The bytes for operator.
   */
  com.google.protobuf.ByteString
      getOperatorBytes();

  /**
   * <code>string filterOperator = 4;</code>
   * @return The filterOperator.
   */
  java.lang.String getFilterOperator();
  /**
   * <code>string filterOperator = 4;</code>
   * @return The bytes for filterOperator.
   */
  com.google.protobuf.ByteString
      getFilterOperatorBytes();
}
