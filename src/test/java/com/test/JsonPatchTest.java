package com.test;

import com.JsonPatch;
import com.JsonPatchImpl;
import com.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonPatchTest {

  private JsonPatch jsonPatch = new JsonPatchImpl(new ObjectMapper());

  @org.junit.Test
  public void test() throws IOException {
    String s1 = new String(Files.readAllBytes(Paths.get(getClass().getResource("/complex1.json").getFile())));
    String s2 = new String(Files.readAllBytes(Paths.get(getClass().getResource("/simple1.json").getFile())));
    System.out.println(s1);
    System.out.println(s2);
    List<Operation> operationList = jsonPatch.getDiff(s1, s2);
    operationList.forEach(System.out::println);
  }
}
