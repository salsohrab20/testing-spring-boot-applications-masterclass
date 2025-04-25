package de.rieckpil.courses.book.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
//@Testcontainers //Manages the lifecycle on its own - starts before test and ends after test run ends
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryNoInMemoryTest {

    //@Container //used with testcontainers
    static PostgreSQLContainer container = (PostgreSQLContainer) new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test")
            .withUsername("duke")
            .withPassword("s3cret")
            .withReuse(true); //improves build time

    static {
        container.start(); //used when we want to manage the lifecycle of the container ourselves
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void shouldNotResultInNull() throws SQLException {
        assertNotNull(reviewRepository);
    }

    @Test
    void shouldSaveReview() {
        // Review result = testEntityManager.persistAndFlush(saveReview());
        Review result = reviewRepository.save(saveReview());
        assertNotNull(result.getId());
    }

    @Test
    @Sql(scripts = "/scripts/INIT_REVIEW_EACH_BOOK.sql")
    void shouldGetTwoReviewStatisticsWhenDatabaseContainsTwoBooksWithReview() {
        List<ReviewStatistic> result = reviewRepository.getReviewStatistics();
        assertEquals(3, reviewRepository.count());
        assertEquals(2, reviewRepository.getReviewStatistics().size());

        reviewRepository.getReviewStatistics().forEach(reviewStatistic -> {
            System.out.println(reviewStatistic.getId());
            System.out.println(reviewStatistic.getAvg());
            System.out.println(reviewStatistic.getIsbn());
            System.out.println(reviewStatistic.getRatings());

        });

        assertEquals(2, result.get(0).getRatings());
        assertEquals(new BigDecimal("3.00"), result.get(0).getAvg());
    }


    private Review saveReview() {
        Review review = new Review();
        review.setTitle("Title");
        review.setContent("Content");
        review.setRating(1);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }


}
