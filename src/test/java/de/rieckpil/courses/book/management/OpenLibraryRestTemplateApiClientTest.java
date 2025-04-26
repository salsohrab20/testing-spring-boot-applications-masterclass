package de.rieckpil.courses.book.management;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;

import static org.junit.jupiter.api.Assertions.*;

@RestClientTest(OpenLibraryRestTemplateApiClient.class)
class OpenLibraryRestTemplateApiClientTest {

    @Autowired
    private OpenLibraryRestTemplateApiClient openLibraryRestTemplateApiClient;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    private static final String ISBN = "9780596004651";


    @Test
    void shouldInjectBeans() {
        assertNotNull(openLibraryRestTemplateApiClient);
        assertNotNull(mockRestServiceServer);
    }

    @Test
    void shouldReturnBookWhenResultIsSuccess() {
        this.mockRestServiceServer
                .expect(MockRestRequestMatchers.requestTo(Matchers.containsString(ISBN)))
                // .expect(MockRestRequestMatchers.requestTo("/api/books?jscmd=data&format=json&bibkeys=" +ISBN))
                .andRespond(MockRestResponseCreators.withSuccess(new ClassPathResource("/stubs/openlibrary/success-" + ISBN + ".json"),
                        MediaType.APPLICATION_JSON));

        Book result = openLibraryRestTemplateApiClient.fetchMetadataForBook(ISBN);

        assertEquals(ISBN, result.getIsbn());
        assertNull(result.getId());

        assertNotNull(result);
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

        this.mockRestServiceServer.expect(
                        MockRestRequestMatchers.requestTo(Matchers.containsString("/api/books")))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON));

        Book result = openLibraryRestTemplateApiClient.fetchMetadataForBook(ISBN);
        assertEquals(ISBN, result.getIsbn());
        assertEquals("N.A", result.getDescription());
        this.mockRestServiceServer.verify();
    }


    @Test
    void shouldPropagateExceptionWhenServerIsDown() {

        assertThrows(HttpServerErrorException.class, () -> {
            this.mockRestServiceServer
                    .expect(MockRestRequestMatchers.requestTo(Matchers.containsString(ISBN)))
                    .andRespond(MockRestResponseCreators.withServerError());

            openLibraryRestTemplateApiClient.fetchMetadataForBook(ISBN);
        });
    }


    @Test
    void shouldContainCorrectHeaderWhenRemoteSystemIsInvoked() {

        this.mockRestServiceServer
                .expect(MockRestRequestMatchers.requestTo("/api/books?jscmd=data&format=json&bibkeys=" + ISBN))
                .andExpect(MockRestRequestMatchers.header("X-Custom-Auth", "Duke42"))
                .andExpect(MockRestRequestMatchers.header("X-Customer-Id", "42"))
                .andRespond(MockRestResponseCreators.withSuccess(new ClassPathResource("/stubs/openlibrary/success-" + ISBN + ".json")
                        , MediaType.APPLICATION_JSON));

        Book result = openLibraryRestTemplateApiClient.fetchMetadataForBook(ISBN);
        assertNotNull(result);

    }

}
