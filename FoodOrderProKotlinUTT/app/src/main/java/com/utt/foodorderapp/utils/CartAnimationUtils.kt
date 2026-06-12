package com.utt.foodorderapp.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * ShopeeFood-style "fly to cart" effect: a clone of the food image flies from the
 * product view along a curved arc into the cart icon, shrinking and fading, then the
 * cart icon does a little bump. Purely visual — callers run their real cart logic first.
 */
object CartAnimationUtils {

    fun flyToCart(
            activity: Activity,
            source: View,
            target: View,
            image: Drawable?,
            onEnd: (() -> Unit)? = null
    ) {
        val root = activity.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)
        if (root == null || image == null || source.width == 0 || target.width == 0) {
            onEnd?.invoke()
            return
        }

        val rootLoc = IntArray(2).also { root.getLocationOnScreen(it) }
        val srcLoc = IntArray(2).also { source.getLocationOnScreen(it) }
        val dstLoc = IntArray(2).also { target.getLocationOnScreen(it) }

        val cloneSize = dp(activity, 96f)
        val flyer = ImageView(activity).apply {
            setImageDrawable(image.constantState?.newDrawable()?.mutate() ?: image)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        root.addView(flyer, FrameLayout.LayoutParams(cloneSize, cloneSize))

        val startX = (srcLoc[0] - rootLoc[0]) + source.width / 2f - cloneSize / 2f
        val startY = (srcLoc[1] - rootLoc[1]) + source.height / 2f - cloneSize / 2f
        val endX = (dstLoc[0] - rootLoc[0]) + target.width / 2f - cloneSize / 2f
        val endY = (dstLoc[1] - rootLoc[1]) + target.height / 2f - cloneSize / 2f

        flyer.x = startX
        flyer.y = startY

        // Control point lifts the arc above both endpoints for a natural curve.
        val ctrlX = startX + (endX - startX) * 0.4f
        val ctrlY = minOf(startY, endY) - cloneSize * 1.1f

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650
            interpolator = AccelerateInterpolator(1.3f)
            addUpdateListener { va ->
                val t = va.animatedValue as Float
                val inv = 1f - t
                flyer.x = inv * inv * startX + 2 * inv * t * ctrlX + t * t * endX
                flyer.y = inv * inv * startY + 2 * inv * t * ctrlY + t * t * endY
                val scale = 1f - 0.78f * t
                flyer.scaleX = scale
                flyer.scaleY = scale
                flyer.alpha = 1f - 0.2f * t
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    (flyer.parent as? ViewGroup)?.removeView(flyer)
                    bump(target)
                    onEnd?.invoke()
                }
            })
            start()
        }
    }

    /** Quick scale-down → overshoot bounce, e.g. on the cart icon when an item lands. */
    fun bump(view: View) {
        view.animate().cancel()
        view.scaleX = 0.6f
        view.scaleY = 0.6f
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(OvershootInterpolator(3.5f))
                .setDuration(320)
                .start()
    }

    private fun dp(activity: Activity, value: Float): Int =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, value, activity.resources.displayMetrics
            ).toInt()
}
