package com.test;

import com.JsonPatch;
import com.JsonPatchImpl;
import com.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JsonPatchTest {

  private JsonPatch jsonPatch = new JsonPatchImpl(new ObjectMapper());

  @org.junit.Test
  public void test() throws IOException {
    String s1 = "{ \"id\" : \"213213\", \"junk\":[ [1,2], [3,4] ], \"a\" : { \"b\" : \"2016-09-23T16:00:00.00Z\" }}";
    String s2 = "{ \"id\" : \"321312\", \"junk\":[ [1,2,4], [3,4] ], \"a\" : { \"extra\": 213, \"b\" : \"2016-09-23T16:00:00.00Z\" }}";
    List<Operation> operationList = jsonPatch.getDiff(s1, s2);
    operationList.forEach(System.out::println);
  }
}
