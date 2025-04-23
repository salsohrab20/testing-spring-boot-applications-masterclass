package de.rieckpil.courses.book;

import com.jayway.jsonpath.JsonPath;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/*
* Documentation : https://github.com/json-path/JsonPath
* */
public class JsonTest {

  @Test
  void testWithJSONAssert() throws JSONException {
    String result = """
      {
        "name" : "salman",
        "fathersname" : "sohrab"
      }
      """;

    JSONAssert.assertEquals("""
      {
        "name" : "salman"
       }
      """, result, false);

    JSONAssert.assertEquals("""
      {
         "fathersname" : "sohrab"
       }
      """, result, false);

    /*
    * Order of the Json doesn't matters , just check if the fields and values are there or not
    * */
    JSONAssert.assertEquals("""
      {
         "fathersname" : "sohrab",
          "name" : "salman"
       }
      """, result, false);
  }


  @Test
  void testWithJsonPath(){
    String result = """
            {
              "name":  ["salman", "sohrab", "ansari"],
              "college" : "jgec"

            }
            """;

    Assertions.assertEquals(3, JsonPath.parse(result).read("$.name.length()", Long.class));
    Assertions.assertEquals("jgec", JsonPath.parse(result).read("$.college", String.class));
  //  Assertions.assertEquals(7, JsonPath.parse(result).read("$.college.name.length()", Long.class));
  }
}
