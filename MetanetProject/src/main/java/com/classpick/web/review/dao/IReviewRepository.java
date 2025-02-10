package com.classpick.web.review.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.classpick.web.review.model.Review;

@Mapper
@Repository
public interface IReviewRepository {

    void registerReview(Review review);

    List<Review> getReviews(Long lectureId);

    void updateReview(Review review);

    void deleteReview(Review review);

}