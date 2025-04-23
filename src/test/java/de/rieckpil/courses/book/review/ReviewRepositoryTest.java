package de.rieckpil.courses.book.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
 * Whenever the entity manager is involved we have
 * a persistent context with flush mode configured
 * which might not work perfectly nice with your test.
 *
 * 🕵️ What is P6Spy?
P6Spy is a Java library that logs all SQL statements executed by your application — including:

Actual SQL queries (with real parameter values)

Timing information (how long each query takes)

Connections and transactions

It's super useful for:

✅ Debugging
✅ Finding N+1 problems
✅ Seeing generated queries from JPA/Hibernate
✅ Tuning performance
 * */

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver",
        "spring.datasource.url=jdbc:p6spy:h2:mem:testdb"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        assertEquals(0, reviewRepository.count());
    }

    @Test
    void shouldNotResultInNull() throws SQLException {

        assertNotNull(testEntityManager);
        assertNotNull(dataSource);
        assertNotNull(reviewRepository);
        System.out.println(dataSource.getConnection().getMetaData().getDatabaseProductName());

    }

    @Test
    void shouldSaveReview() {
        // Review result = testEntityManager.persistAndFlush(saveReview());
        Review result = reviewRepository.save(saveReview());
        assertNotNull(result.getId());
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
