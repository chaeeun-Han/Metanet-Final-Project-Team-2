package com.classpick.web.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classpick.web.review.dao.IReviewRepository;
import com.classpick.web.review.model.Review;
import com.classpick.web.review.service.ReviewService;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private IReviewRepository iReviewDao;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        review = new Review();
        review.setReviewId(1L);
        review.setLectureId(1L);
        review.setMemberId(1L);
        review.setContent("This is a test review");
        review.setReviewDate(new Date(System.currentTimeMillis()));
        review.setDeleted(0);
    }

    @Test
    void registerReview_ShouldRegisterSuccessfully() {
        doNothing().when(iReviewDao).registerReview(any(Review.class));

        reviewService.registerReview(review);

        verify(iReviewDao, times(1)).registerReview(any(Review.class));
    }

    @Test
    void registerReview_ShouldThrowException_WhenDatabaseFails() {
        doThrow(new RuntimeException("Database error")).when(iReviewDao).registerReview(any(Review.class));

        assertThrows(RuntimeException.class, () -> reviewService.registerReview(review));
        verify(iReviewDao, times(1)).registerReview(any(Review.class));
    }

    @Test
    void getReviews_ShouldReturnReviewList_WhenReviewsExist() {
        List<Review> mockReviews = Arrays.asList(review, review);
        when(iReviewDao.getReviews(anyLong())).thenReturn(mockReviews);

        List<Review> result = reviewService.getReviews(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(iReviewDao, times(1)).getReviews(anyLong());
    }

    @Test
    void getReviews_ShouldReturnEmptyList_WhenNoReviewsExist() {
        when(iReviewDao.getReviews(anyLong())).thenReturn(Collections.emptyList());

        List<Review> result = reviewService.getReviews(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(iReviewDao, times(1)).getReviews(anyLong());
    }

    @Test
    void updateReview_ShouldUpdateSuccessfully() {
        doNothing().when(iReviewDao).updateReview(any(Review.class));

        reviewService.updateReview(review);

        verify(iReviewDao, times(1)).updateReview(any(Review.class));
    }

    @Test
    void updateReview_ShouldThrowException_WhenDatabaseFails() {
        doThrow(new RuntimeException("Database error")).when(iReviewDao).updateReview(any(Review.class));

        assertThrows(RuntimeException.class, () -> reviewService.updateReview(review));
        verify(iReviewDao, times(1)).updateReview(any(Review.class));
    }

    @Test
    void deleteReview_ShouldDeleteSuccessfully() {
        doNothing().when(iReviewDao).deleteReview(any(Review.class));

        reviewService.deleteReview(review);

        verify(iReviewDao, times(1)).deleteReview(any(Review.class));
    }

    @Test
    void deleteReview_ShouldThrowException_WhenDatabaseFails() {
        doThrow(new RuntimeException("Database error")).when(iReviewDao).deleteReview(any(Review.class));

        assertThrows(RuntimeException.class, () -> reviewService.deleteReview(review));
        verify(iReviewDao, times(1)).deleteReview(any(Review.class));
    }
}
