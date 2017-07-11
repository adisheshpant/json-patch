package com;

public class Operation {
   private String op;
   private String path;
   private Object value;

   public static Operation add(String path, Object value) {
      return new Operation("add", path, value);
   }

   public static Operation remove(String path) {
      return new Operation("remove", path);
   }

   public static Operation replace(String path, Object value) {
      return new Operation("replace", path, value);
   }

   public Operation(String op, String path, Object value) {
      this.op = op;
      this.path = path;
      this.value = value;
   }

   public Operation(String op, String path) {
      this.op = op;
      this.path = path;
   }

   public String getOp() {
      return op;
   }

   public void setOp(String op) {
      this.op = op;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   public Object getValue() {
      return value;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return "Operation{" +
              "op='" + op + '\'' +
              ", path='" + path + '\'' +
              ", value=" + value +
              '}';
   }
}
