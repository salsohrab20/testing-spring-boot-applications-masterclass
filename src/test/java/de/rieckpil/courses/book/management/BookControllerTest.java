package de.rieckpil.courses.book.management;

import de.rieckpil.courses.book.review.ReviewController;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(WebSecurityConfig.class)
class BookControllerTest {

  @MockBean
  private BookManagementService bookManagementService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetEmptyArrayWhenNoBooksExists() throws Exception {
    MvcResult mvcResult =
      this.mockMvc
        .perform(MockMvcRequestBuilders.get("/api/books").header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()").value(0))
        .andDo(print())
        .andReturn();

  }

  @Test
  void shouldNotReturnXML() throws Exception {

    this.mockMvc
      .perform(MockMvcRequestBuilders.get("/api/book")
        .header(ACCEPT, MediaType.APPLICATION_XML))
      .andExpect(status().isNotAcceptable());

  }


  @Test
  void shouldReturnBooksWhenServiceReturnsBooks() throws Exception {

    Book bookOne =
      createBook(
        1L,
        "42",
        "Java 14",
        "Mike",
        "Good book",
        "Software Engineering",
        200L,
        "Oracle",
        "ftp://localhost:42");

    Book bookTwo =
      createBook(
        2L,
        "84",
        "Java 15",
        "Duke",
        "Good book",
        "Software Engineering",
        200L,
        "Oracle",
        "ftp://localhost:42");

    Mockito.when(bookManagementService.getAllBooks()).thenReturn(List.of(bookOne, bookTwo));

    this.mockMvc
      .perform(MockMvcRequestBuilders.get("/api/books")
        .header(ACCEPT, APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.size()").value(2))
      .andExpect(jsonPath("$[0].id").doesNotExist())
      .andExpect(jsonPath("$[0].title").value(bookOne.getTitle()))
      .andExpect(jsonPath("$[0].author").value(bookOne.getAuthor()))
      .andExpect(jsonPath("$[0].description").value(bookOne.getDescription()))
      .andExpect(jsonPath("$[0].isbn").value(bookOne.getIsbn()))
      .andExpect(jsonPath("$[1].isbn").value(bookTwo.getIsbn()));

  }


  private Book createBook(Long id, String isbn, String title, String author, String description, String genre, Long pages, String publisher, String thumbnailUrl) {
    Book book = new Book();
    book.setId(id);
    book.setIsbn(isbn);
    book.setTitle(title);
    book.setAuthor(author);
    book.setDescription(description);
    book.setGenre(genre);
    book.setPages(pages);
    book.setPublisher(publisher);
    book.setThumbnailUrl(thumbnailUrl);
    return book;
  }
}
