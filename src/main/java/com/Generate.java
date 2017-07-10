package com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

class Change {

   private String op;
   private String path;
   private String value;

   public Change(String op, String path, String value) {
      this.op = op;
      this.path = path;
      this.value = value;
   }

   public String getOp() {
      return op;
   }

   public void setOp(String op) {
      this.op = op;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   @Override
   public String toString() {
      String s = "Change{" +
              "op='" + op + '\'' +
              ", path='" + path + '\'';
      if( value != null ) {
         s +=  ", value='" + value + '\'';
      }
      s += "}" ;
      return s;
   }

}

class Delta {

   private List<Change> changes;

   public List<Change> getChanges() {
      return changes;
   }

   public void setChanges(List<Change> changes) {
      this.changes = changes;
   }

   public void add(String op, String path) {
      add(op, path, null);
   }

   public void add(String op, String path, String value) {
      if( changes == null ) {
         changes = new ArrayList<>();
      }
      changes.add(new Change(op, path, value));
   }

   @Override
   public String toString() {
      return changes.stream().map(Change::toString).reduce("", (x,y) -> x+"\n"+y );
   }
}

public class Generate {

   public static void main(String[] args) throws IOException {
      String s1 = "{ \"_id\" : \"83395053\", \"junk\":[ [1,2], [3,4] ], \"requestType\" : \"regular\", \"startDate\" : { \"$date\" : \"2016-09-23T16:00:00.000+0000\" }, \"endDate\" : { \"$date\" : \"2016-09-24T00:00:00.000+0000\" }, \"totalDaysRequested\" : \"1\", \"totalHoursRequested\" : \"8\", \"ranges\" : [ { \"weekendIncluded\" : true, \"days\" : [ { \"startDate\" : { \"$date\" : \"2016-09-23T16:00:00.000+0000\" }, \"endDate\" : { \"$date\" : \"2016-09-24T00:00:00.000+0000\" }, \"hoursPerDay\" : \"8\", \"halfDay\" : false } ] } ], \"linkedRequests\" : [  ], \"advancesalaryValue\" : \"no\", \"integratedCashOutValue\" : \"no\", \"integratedCOrequestId\" : { \"$numberLong\" : \"0\" }, \"requestId\" : { \"$numberLong\" : \"83395053\" }, \"dsId\" : \"530452284\", \"initiatorId\" : \"1167494068\", \"businessGroupID\" : \"businessGroup-FRR\", \"timeAwayTypeCode\" : \"H\", \"status\" : \"draft\", \"routeId\" : \"2010492484\", \"action\" : \"save\", \"tzId\" : \"America/Los_Angeles\", \"metadata\" : { \"createdOn\" : { \"$date\" : \"2016-09-23T21:34:04.000+0000\" }, \"modifiedBy\" : \"1167494068\", \"modifiedOn\" : { \"$date\" : \"2016-09-23T21:34:04.000+0000\" }, \"lang\" : \"en-us\" } }";
      String s2 = "{ \"_id\" : \"823395053\",\"junk\":[ [4,5] ], \"junky\":{\"asdf\":124}, \"requestType\" : \"regular\", \"startDate\" : { \"far\":124, \"$date\" : \"2016-09-23T16:00:00.000+0000\" }, \"endDate\" : { \"$date\" : \"2016-09-24T00:00:00.000+0000\" }, \"totalDaysRequested\" : \"1\", \"totalHoursRequested\" : \"8\", \"ranges\" : [ { \"weekendIncluded\" : false, \"days\" : [ { \"startDate\" : { \"$date\" : \"2016-09-23T16:00:00.000+0000\" }, \"endDate\" : { \"$date\" : \"2016-09-24T00:00:00.000+0000\" }, \"hoursPerDay\" : \"8\", \"halfDay\" : false } ] } ], \"linkedRequests\" : [  ], \"advancesalaryValue\" : \"no\", \"integratedCashOutValue\" : \"no\", \"integratedCOrequestId\" : { \"$numberLong\" : \"0\" }, \"requestId\" : { \"$numberLong\" : \"83395053\" }, \"dsId\" : \"530452284\", \"initiatorId\" : \"1167494068\", \"businessGroupID\" : \"businessGroup-FRR\", \"timeAwayTypeCode\" : \"H\", \"status\" : \"draft\", \"routeId\" : \"2010492484\", \"action\" : \"save\", \"tzId\" : \"America/Los_Angeles\", \"metadata\" : { \"createdOn\" : { \"$date\" : \"2016-09-23T21:34:04.000+0000\" }, \"modifiedBy\" : \"11674940689\", \"modifiedOn\" : { \"$date\" : \"2016-09-23T21:34:04.000+0000\" }, \"lang\" : \"en-us\" } }";

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode1 = objectMapper.readTree(s1);
      JsonNode jsonNode2 = objectMapper.readTree(s2);
      Delta delta = new Delta();
      compareObjects(jsonNode1, jsonNode2, "/", delta);
      System.out.println(delta);

      for(Change change: delta.getChanges()) {

         if( change.getOp().equals("replace") ) {
            replace(jsonNode2, change.getPath(), objectMapper.readTree(change.getValue()));
         } else if( change.getOp().equals("add") ) {
            add(jsonNode2, change.getPath(), objectMapper.readTree(change.getValue()));
         } else if( change.getOp().equals("remove") ) {
            remove(jsonNode2, change.getPath());
         }

      }

      System.out.println(jsonNode1.equals(jsonNode2));


   }

   static void remove(JsonNode source, String path) {
      String _path = path.substring(0, path.lastIndexOf("/"));
      String leaf = path.substring(path.lastIndexOf("/")+1);

      if( !_path.isEmpty() ) {
         source = source.at(_path);
      }

      if( source.getNodeType() == JsonNodeType.OBJECT ) {
         ((ObjectNode)source).remove(leaf);
      } else if( source.getNodeType() == JsonNodeType.ARRAY ) {
         ArrayNode a = (ArrayNode) source;
         a.remove(Integer.parseInt(leaf));
      }

   }

   static void add(JsonNode source, String path, JsonNode value) {

      String _path = path.substring(0, path.lastIndexOf("/"));
      String leaf = path.substring(path.lastIndexOf("/")+1);

      if( !_path.isEmpty() ) {
         source = source.at(_path);
      }

      if( source.getNodeType() == JsonNodeType.OBJECT ) {
         ((ObjectNode)source).set(leaf, value);
      }
      else if( source.getNodeType() == JsonNodeType.ARRAY ) {
         ArrayNode a = (ArrayNode) source;
         int i = Integer.parseInt(leaf);
         if( i >= a.size()) {
            ( (ArrayNode) source ).add(value);
         } else {
            ( (ArrayNode) source ).set(Integer.parseInt(leaf), value);
         }
      }

   }
   static void replace(JsonNode source, String path, JsonNode value) {

      String _path = path.substring(0, path.lastIndexOf("/"));
      String leaf = path.substring(path.lastIndexOf("/")+1);

      if( !_path.isEmpty() ) {
         source = source.at(_path);
      }

      if( source.getNodeType() == JsonNodeType.OBJECT ) {
         ((ObjectNode)source).set(leaf, value);
      }
      else if( source.getNodeType() == JsonNodeType.ARRAY ) {
         ((ArrayNode)source).set(Integer.parseInt(leaf), value);
      }

   }

   static void compareObjects(JsonNode jsonNode1, JsonNode jsonNode2, String path, Delta delta) {

      if ( jsonNode1.getNodeType() != JsonNodeType.OBJECT ) {
         return;
      }
      if ( jsonNode1.equals(jsonNode2) ) {
         return;
      }

      Iterator<Map.Entry<String, JsonNode>> f = jsonNode1.fields();
      while ( f.hasNext() ) {
         Map.Entry<String, JsonNode> e = f.next();

         if ( !jsonNode2.has(e.getKey()) ) {
            delta.add("add", path + e.getKey(), e.getValue().toString());
            continue;
         }

         JsonNode v = jsonNode2.get(e.getKey());
         if ( e.getValue().getNodeType() == JsonNodeType.OBJECT ) {
            compareObjects(e.getValue(), v, path + e.getKey() + "/", delta);
            continue;
         }

         if ( e.getValue().getNodeType() == JsonNodeType.ARRAY ) {
            compareArrays(e.getValue(), v, path + e.getKey() + "/", delta);
            continue;
         }

         if ( !v.equals(e.getValue()) ) {
            delta.add("replace", path + e.getKey(), e.getValue().toString());
         }

      }

      Iterator<Map.Entry<String, JsonNode>> f2 = jsonNode2.fields();
      while ( f2.hasNext() ) {
         Map.Entry<String, JsonNode> e = f2.next();

         if ( !jsonNode1.has(e.getKey()) ) {
            delta.add("remove", path + e.getKey());
         }
      }
   }

   static void compareArrays(JsonNode j1, JsonNode j2, String path, Delta delta) {

      if ( j1.getNodeType() != JsonNodeType.ARRAY ) {
         return;
      }

      if ( j1.equals(j2) ) {
         return;
      }

      ArrayNode a1 = (ArrayNode)j1;
      ArrayNode a2 = (ArrayNode)j2;

      for(int i = 0; i < a1.size(); i++) {

         if( i >= a2.size() ) {
            delta.add("add", path + i, a1.get(i).toString());
            continue;
         }

         if ( !a1.get(i).equals(a2.get(i)) ) {

            if( a1.get(i).getNodeType() == JsonNodeType.OBJECT ) {
               compareObjects(a1.get(i), a2.get(i), path + i +"/", delta);
            } else if( a1.get(i).getNodeType() == JsonNodeType.ARRAY ) {
               compareArrays(a1.get(i), a2.get(i), path + i + "/", delta);
            } else {
               delta.add("replace", path + i, a1.get(i).toString());
            }
         }

      }

      if( a2.size() > a1.size() ) {
         for(int i = a1.size(); i< a2.size(); i++) {
            delta.add("remove", path + i);
         }
      }

   }



}