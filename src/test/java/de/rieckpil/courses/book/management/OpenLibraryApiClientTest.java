package de.rieckpil.courses.book.management;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryApiClientTest {

  private MockWebServer server;

  private OpenLibraryApiClient cut; //Class under test

  private static String VALID_RESPONSE;

  private static final String ISBN = "9780596004651";

  static {
    try {
      VALID_RESPONSE = new String(OpenLibraryApiClientTest.class
        .getClassLoader()
        .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json")
        .readAllBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @BeforeEach
  void setUp() throws Exception {

    TcpClient tcpClient = TcpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000)
      .doOnConnected(
        connection -> {
          connection.addHandlerLast(new ReadTimeoutHandler(1))
            .addHandlerLast(new WriteTimeoutHandler(1));
        }
      );

    this.server = new MockWebServer();
    this.server.start();

    this.cut = new OpenLibraryApiClient(
      WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
        .baseUrl(server.url("/").toString())
        .build()
    );
  }

  @Test
  void notNull() {
    assertNotNull(cut);
    assertNotNull(server);
  }


  @Test
  void shouldReturnBookWhenResultIsSuccess() throws InterruptedException {
    MockResponse mockResponse = new MockResponse()
      .addHeader("Content-Type", "application/json")
      .setBody(VALID_RESPONSE);

    this.server.enqueue(mockResponse);

    Book result = cut.fetchMetadataForBook(ISBN);

    assertEquals(ISBN, result.getIsbn());
    assertNull(result.getId());

    assertNotNull(result);

    RecordedRequest recordedRequest = this.server.takeRequest();
    assertEquals("/api/books?jscmd=data&format=json&bibkeys=" + ISBN, recordedRequest.getPath());
  }

  @Test
  void shouldReturnBookWhenResultIsSuccessButLackingAllInformation() {
    //Doesn't contains description
    String response =
      """
         {
          "9780596004651": {
            "publishers": [
              {
                "name": "O'Reilly"
              }
            ],
            "title": "Head second Java",
            "authors": [
              {
                "url": "https://openlibrary.org/authors/OL1400543A/Kathy_Sierra",
                "name": "Kathy Sierra"
              }
            ],
            "number_of_pages": 42,
            "cover": {
              "small": "https://covers.openlibrary.org/b/id/388761-S.jpg",
              "large": "https://covers.openlibrary.org/b/id/388761-L.jpg",
              "medium": "https://covers.openlibrary.org/b/id/388761-M.jpg"
            }
           }
         }
        """;

    this.server.enqueue(new MockResponse()
      .addHeader("Content-Type", "application/json")
      .setResponseCode(200)
      .setBody(response));

    Book result = cut.fetchMetadataForBook(ISBN);
    assertEquals(ISBN, result.getIsbn());
    assertEquals("N.A", result.getDescription());
    assertNull(result.getId());

  }

  @Test
  void shouldPropagateExceptionWhenRemoteSystemIsDown() {
    assertThrows(RuntimeException.class, () -> {
      this.server.enqueue(new MockResponse()
        .setResponseCode(500)
        .setBody("Sorry, system is down :("));

      cut.fetchMetadataForBook(ISBN);

    });
  }

}
