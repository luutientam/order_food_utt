package com.utt.foodorderapp.data.repository

import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.model.Review

/**
 * Lưu đánh giá theo orderId để 1 đơn chỉ có 1 review (ghi đè khi re-rate).
 */
class ReviewRepository {

    fun submitReview(review: Review, callback: (DatabaseError?) -> Unit) {
        val key = review.orderId.toString()
        ControllerApplication.getInstance().reviewDatabaseReference
                .child(key)
                .setValue(review) { error, _ -> callback(error) }
    }

    fun findReviewForOrder(orderId: Long, callback: (Review?) -> Unit) {
        ControllerApplication.getInstance().reviewDatabaseReference
                .child(orderId.toString())
                .get()
                .addOnSuccessListener { callback(it.getValue(Review::class.java)) }
                .addOnFailureListener { callback(null) }
    }

    fun submitFoodReview(foodId: Long, userEmail: String?, rating: Int, comment: String, callback: (DatabaseError?) -> Unit) {
        val review = Review()
        review.id = System.currentTimeMillis()
        review.foodId = foodId
        review.userEmail = userEmail
        review.rating = rating
        review.comment = comment
        review.createdAt = review.id
        val key = "food_${foodId}_${review.id}"
        ControllerApplication.getInstance().reviewDatabaseReference
                .child(key)
                .setValue(review) { error, _ -> callback(error) }
    }

    fun submitRestaurantReview(restaurantId: Long, userEmail: String?, rating: Int, comment: String, callback: (DatabaseError?) -> Unit) {
        val review = Review()
        review.id = System.currentTimeMillis()
        review.restaurantId = restaurantId
        review.userEmail = userEmail
        review.rating = rating
        review.comment = comment
        review.createdAt = review.id
        val key = "rest_${restaurantId}_${review.id}"
        ControllerApplication.getInstance().reviewDatabaseReference
                .child(key)
                .setValue(review) { error, _ -> callback(error) }
    }

    fun observeReviewsForFood(foodId: Long, onResult: (List<Review>) -> Unit) {
        ControllerApplication.getInstance().reviewDatabaseReference.get().addOnSuccessListener { snapshot ->
            val list = ArrayList<Review>()
            for (child in snapshot.children) {
                val review = child.getValue(Review::class.java) ?: continue
                if (review.foodId == foodId) list.add(review)
            }
            list.sortByDescending { it.createdAt }
            onResult(list)
        }.addOnFailureListener { onResult(emptyList()) }
    }

    fun observeReviewsForRestaurant(restaurantId: Long, onResult: (List<Review>) -> Unit) {
        ControllerApplication.getInstance().reviewDatabaseReference.get().addOnSuccessListener { snapshot ->
            val list = ArrayList<Review>()
            for (child in snapshot.children) {
                val review = child.getValue(Review::class.java) ?: continue
                if (review.restaurantId == restaurantId) list.add(review)
            }
            list.sortByDescending { it.createdAt }
            onResult(list)
        }.addOnFailureListener { onResult(emptyList()) }
    }
}
