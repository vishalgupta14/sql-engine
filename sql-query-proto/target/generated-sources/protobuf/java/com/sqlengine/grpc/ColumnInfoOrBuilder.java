// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: query_runner.proto

package com.sqlengine.grpc;

public interface ColumnInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:queryrunner.ColumnInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string type = 2;</code>
   * @return The type.
   */
  java.lang.String getType();
  /**
   * <code>string type = 2;</code>
   * @return The bytes for type.
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>int32 size = 3;</code>
   * @return The size.
   */
  int getSize();

  /**
   * <code>bool nullable = 4;</code>
   * @return The nullable.
   */
  boolean getNullable();

  /**
   * <code>string remarks = 5;</code>
   * @return The remarks.
   */
  java.lang.String getRemarks();
  /**
   * <code>string remarks = 5;</code>
   * @return The bytes for remarks.
   */
  com.google.protobuf.ByteString
      getRemarksBytes();
}
