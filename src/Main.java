import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Upsource RPC client sample.
 * Requires HTTP client library (Apache Commons in this case), and optionally JSON library (Jackson in this case).
 */
public class Main {
  private static final String UPSOURCE_URL = "http://localhost:8080/";
  private static final String CREDENTIALS_BASE64 = Base64.getEncoder().encodeToString("admin:admin".getBytes());

  public static void main(String[] args) {
    try {
      // Perform a raw request: string -> string.
      String response = doRequest("getRevisionsList", "{\"projectId\": \"project\", \"limit\": 30}");
      System.out.println("getRevisionsList: " + response);

      // Pass a java object that will be encoded into JSON. The result is also a java object.
      Map<Object, Object> params = new HashMap<>();
      params.put("projectId", "project");
      params.put("limit", 30);
      Object responseObject = doRequestJson("getReviews", params);
      System.out.println("getReviews: " + responseObject);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Performs a request to Upsource.
   *
   * @param method RPC method name
   * @param paramsJson input JSON as a string
   * @return output JSON as a string
   * @throws IOException if I/O error occurs
   */
  private static String doRequest(String method, String paramsJson) throws IOException {
    // Upsource URL: http://<upsource-host>/~rpc/<method>
    String url = UPSOURCE_URL + "~rpc/" + method;
    // Perform a POST request to pass a payload in the body.
    // Alternatively can make a GET request with "?params=paramsJson" query.
    PostMethod post = new PostMethod(url);
    // Basic authorization header. If not provided, the request will be executed with guest permissions.
    post.addRequestHeader("Authorization", "Basic " + CREDENTIALS_BASE64);
    post.setRequestBody(paramsJson);

    // Execute and return the response body.
    HttpClient client = new HttpClient();
    client.executeMethod(post);
    return post.getResponseBodyAsString();
  }

  /**
   * Same as {@link #doRequest(String, String)}, but accepts a map rather than string.
   * The map is encoded into JSON, and method result is also decoded from JSON.
   *
   * @param method RPC method name
   * @param params input JSON as an object
   * @return output JSON as an object
   * @throws IOException if I/O error occurs
   */
  private static Object doRequestJson(String method, Map<Object, Object> params) throws IOException {
    String inputJson = new ObjectMapper().writeValueAsString(params);
    String response = doRequest(method, inputJson);
    return new ObjectMapper().readValue(response, Map.class);
  }
}
