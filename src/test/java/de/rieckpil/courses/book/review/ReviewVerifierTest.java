package de.rieckpil.courses.book.review;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.Matchers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/*
 * Test Lifecycle
 * @BeforeEach - runs before each test to initialize reusable instances
 * @AfterEach - runs after each test to free resources
 * @BeforeAll - static : runs once , used to define constants which doesn't changes for all the tests before the test run starts
 * @AfterAll - static : runs once , used to define constants which doesn't changes for all the tests after the test run completes
 * */

@ExtendWith(RandomReviewResolverExtension.class)
class ReviewVerifierTest {

    ReviewVerifier reviewVerifier;

    @BeforeEach
    void setUp() {
        reviewVerifier = new ReviewVerifier();
    }

    @Test
    @DisplayName("Should fail when review contains swears words")
    void shouldFailWhenReviewContainsSwearWord() {
        String review = "This book is shit";
        Assertions.assertFalse(reviewVerifier.doesMeetQualityStandards(review));

    }

    @ParameterizedTest
    @CsvFileSource(resources = "/badReview.csv")
    void shouldFailWhenReviewDoesNotMeetQualityStandard(String review) {
        Assertions.assertFalse(reviewVerifier.doesMeetQualityStandards(review), "Review did not detect bad quality");
    }

    @RepeatedTest(5)
    void shouldFailWhenRandomReviewQualityIsBad(@RandomReviewResolverExtension.RandomReview String review) {
        System.out.println(review);
        Assertions.assertFalse(reviewVerifier.doesMeetQualityStandards(review), "Review did not detect bad quality");
    }


    @Test
    void shouldPassWhenReviewDoesNotContainsSwearWordAndMeetsQualityStandards() {
        String review = "This book is very good, I would recommend it to everyone";
        Assertions.assertTrue(reviewVerifier.doesMeetQualityStandards(review));
    }

    /*
          Different Assertions libraries
    * Junit5 - (expectedValue, actualValue)
    * AssertJ - (actualValue, expectedValue)
    * Hamcrest - (actualValue, expectedValue)
    * */

    @Test
    void shouldPassWhenReviewIsInGoodHamcrest() {
        String review = "This book is very good, I would recommend it to everyone";
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        MatcherAssert.assertThat("ReviewVerifier did not pass a good review", result, Matchers.equalTo(true));
        MatcherAssert.assertThat("Lorem ipsum dolor sit amet", Matchers.endsWith("amet"));
        MatcherAssert.assertThat("Lorem ipsum dolor sit amet", Matchers.startsWith("Lorem"));
        MatcherAssert.assertThat(List.of(1, 2, 3, 5, 4), Matchers.hasSize(5));
        MatcherAssert.assertThat(List.of(1, 2, 3, 5, 4), Matchers.anyOf(Matchers.hasSize(5), Matchers.emptyIterable()));
    }

    @Test
    void shouldPassWhenReviewIsInGoodAssertJ() {
        String review = "This book is very good, I would recommend it to everyone";
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        assertThat(result)
                .withFailMessage("ReviewVerifier did not pass a good review")
                //.isEqualTo(true)
                .isTrue();
    }

}
