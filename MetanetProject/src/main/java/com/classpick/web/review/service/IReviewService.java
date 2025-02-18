package com.classpick.web.review.service;

import java.util.List;

import com.classpick.web.review.model.Review;

public interface IReviewService {
    void registerReview(Review review);

    List<Review> getReviews(Long lectureId);

    void updateReview(Review review);

    void deleteReview(Review review);
}
