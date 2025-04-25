package de.rieckpil.courses.book.review;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ReviewService reviewService;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.objectMapper = new ObjectMapper();
  }

  @Test
  void shouldReturnTwentyReviewsWithoutOrderWhenNoParameterAreSpecified() throws Exception {

    ArrayNode result = objectMapper.createArrayNode();

    ObjectNode statistics = objectMapper.createObjectNode();
    statistics.put("bookId", 1);
    statistics.put("isbn", "42");
    statistics.put("avg", 89.3);
    statistics.put("ratings", 2);

    result.add(statistics);

    when(reviewService.getAllReviews(20, "none")).thenReturn(result);

    this.mockMvc.perform(MockMvcRequestBuilders.get("/api/books/reviews"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.size()").value(1));

    verify(reviewService, atLeastOnce()).getAllReviews(20, "none");
  }


  @Test
  void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {

    this.mockMvc
      .perform(MockMvcRequestBuilders.get("/api/books/reviews/statistics"))
      .andExpect(status().isUnauthorized());

    verifyNoInteractions(reviewService);

  }

  @Test
  @WithMockUser(username = "salman")
  void shouldReturnReviewStatisticsWhenUserIsAuthenticatedOne() throws Exception {

    this.mockMvc.perform(MockMvcRequestBuilders.get("/api/books/reviews/statistics"))
      .andExpect(status().isOk());

    verify(reviewService).getReviewStatistics();
  }

  @Test
  void shouldReturnReviewStatisticsWhenUserIsAuthenticatedTwo() throws Exception {

    this.mockMvc.perform(MockMvcRequestBuilders.get("/api/books/reviews/statistics")
        .with(user("salman")))
      .andExpect(status().isOk());

    verify(reviewService).getReviewStatistics();
  }

  @Test
  void shouldReturnReviewStatisticsWhenUserIsAuthenticatedThree() throws Exception {

    this.mockMvc.perform(MockMvcRequestBuilders.get("/api/books/reviews/statistics")
        //  .with(oauth2Login()))
        //.with(httpBasic("salman","password")))
        .with(jwt()))
      .andExpect(status().isOk());

    verify(reviewService).getReviewStatistics();
  }


  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {

    String requestPayload = """
      {
        "reviewTitle" : "Great Java Book",
        "reviewContent" : "This is a great Java Book",
        "rating" : 4
      }
      """;

    when(reviewService.createBookReview(eq("42"), any(BookReviewRequest.class), eq("salman"), endsWith("spring.io")))
      .thenReturn(0L);


    this.mockMvc
      .perform(MockMvcRequestBuilders.post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestPayload)
        .with(jwt().jwt(builder -> builder.claim("email", "duke@spring.io")
          .claim("preferred_username", "duke"))))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andExpect(header().string("Location", Matchers.containsString("/books/42/reviews/0")));

  }


  @Test
  void shouldRejectNewBookReviewForAuthenticatedUserWithInvalidPayload() throws Exception {

    String payload = """
      {
        "reviewTitle" : "Great Java Book",
        "reviewContent" : "This is a great Java Book",
        "rating" : -1
      }
      """;

    this.mockMvc
      .perform(MockMvcRequestBuilders.post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload)
        .with(jwt().jwt(builder -> builder.claim("email", "duke@spring.io")
          .claim("preferred_username", "duke"))))
      .andExpect(status().isBadRequest());

  }

  @Test
  void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {

    this.mockMvc
      .perform(MockMvcRequestBuilders.delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        .with(jwt()))
      .andExpect(status().isForbidden());

    verifyNoInteractions(reviewService);

  }

  @Test
  void shouldAllowDeletingReviewWhenUserIsAuthenticatedAndHasModeratorRole1() throws Exception {
    this.mockMvc.perform(
      MockMvcRequestBuilders.delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_moderator")))
    ).andExpect(status().isOk());

    verify(reviewService).deleteReview("42",3L);
  }

  @Test
  @WithMockUser(roles = "moderator")
  void shouldAllowDeletingReviewWhenUserIsAuthenticatedAndHasModeratorRole2() throws Exception {
    this.mockMvc.perform(
      MockMvcRequestBuilders.delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        //.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_moderator")))
    ).andExpect(status().isOk());

    verify(reviewService).deleteReview("42",3L);
  }


}
