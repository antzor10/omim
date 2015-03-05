package com.mapswithme.maps.widget.placepage;

import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.widget.placepage.PlacePageView.State;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class BottomPlacePageAnimationController extends BasePlacePageAnimationController
{
  public BottomPlacePageAnimationController(@NonNull PlacePageView placePage)
  {
    super(placePage);
  }

  @Override
  boolean onInterceptTouchEvent(MotionEvent event)
  {
    switch (event.getAction())
    {
    case MotionEvent.ACTION_DOWN:
      mIsGestureHandled = false;
      mDownCoord = event.getRawY();
      break;
    case MotionEvent.ACTION_MOVE:
      if (mDownCoord < ViewHelper.getY(mPreview) || mDownCoord > ViewHelper.getY(mButtons))
        return false;
      if (Math.abs(mDownCoord - event.getRawY()) > mTouchSlop)
        return true;
      break;
    }

    return false;
  }

  @Override
  protected boolean onTouchEvent(@NonNull MotionEvent event)
  {
    if (mDownCoord < ViewHelper.getY(mPreview) || mDownCoord > ViewHelper.getY(mButtons))
      return false;

    super.onTouchEvent(event);
    return true;
  }

  @Override
  protected void initGestureDetector()
  {
    mGestureDetector = new GestureDetectorCompat(mPlacePage.getContext(), new GestureDetector.SimpleOnGestureListener()
    {
      private static final int Y_MIN = 1;
      private static final int Y_MAX = 100;
      private static final int X_TO_Y_SCROLL_RATIO = 2;

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
      {
        final boolean isVertical = Math.abs(distanceY) > X_TO_Y_SCROLL_RATIO * Math.abs(distanceX);
        final boolean isInRange = Math.abs(distanceY) > Y_MIN && Math.abs(distanceY) < Y_MAX;

        if (isVertical && isInRange)
        {
          if (!mIsGestureHandled)
          {
            if (distanceY < 0f)
            {
              if (mPlacePage.getState() == State.PREVIEW)
              {
                Framework.deactivatePopup();
                mPlacePage.setState(State.HIDDEN);
              }
              else
                mPlacePage.setState(State.PREVIEW);
            }
            else
              mPlacePage.setState(State.DETAILS);

            mIsGestureHandled = true;
          }

          return true;
        }

        return false;
      }

      @Override
      public boolean onSingleTapConfirmed(MotionEvent e)
      {
        if (mDownCoord < ViewHelper.getY(mPreview) && mDownCoord < ViewHelper.getY(mDetails))
          return false;

        if (mPlacePage.getState() == State.PREVIEW)
          mPlacePage.setState(State.DETAILS);
        else
          mPlacePage.setState(State.PREVIEW);

        return true;
      }
    });
  }

  @Override
  void setState(State currentState, State newState)
  {
    switch (newState)
    {
    case HIDDEN:
      hidePlacePage();
      break;
    case PREVIEW:
      showPreview(currentState);
      break;
    case BOOKMARK:
      showBookmark(currentState);
      break;
    case DETAILS:
      showDetails(currentState);
      break;
    }
  }

  protected void showPreview(final State currentState)
  {
    mPlacePage.setVisibility(View.VISIBLE);
    mPreview.setVisibility(View.VISIBLE);

    ValueAnimator animator;
    if (currentState == State.HIDDEN)
    {
      mDetails.setVisibility(View.INVISIBLE);
      animator = ValueAnimator.ofFloat(mPreview.getHeight() + mButtons.getHeight(), 0f);
      animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          ViewHelper.setTranslationY(mPreview, (Float) animation.getAnimatedValue());
          ViewHelper.setTranslationY(mButtons, (Float) animation.getAnimatedValue());
        }
      });
    }
    else
    {
      final float detailsHeight = mDetails.getHeight();
      animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mPreview), 0f);
      animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          ViewHelper.setTranslationY(mPreview, (Float) animation.getAnimatedValue());
          ViewHelper.setTranslationY(mDetails, (Float) animation.getAnimatedValue() + detailsHeight);

          if (animation.getAnimatedFraction() > .99f)
            mDetails.setVisibility(View.INVISIBLE);
        }
      });
    }
    animator.setDuration(SHORT_ANIM_DURATION);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.start();
  }

  protected void showDetails(final State currentState)
  {
    mPlacePage.setVisibility(View.VISIBLE);
    mPreview.setVisibility(View.VISIBLE);
    mDetails.setVisibility(View.VISIBLE);

    ValueAnimator animator;
    final float bookmarkHeight = mBookmarkDetails.getHeight();
    final float detailsHeight = mDetails.getHeight();
    if (currentState == State.PREVIEW)
      animator = ValueAnimator.ofFloat(detailsHeight, bookmarkHeight);
    else
      animator = ValueAnimator.ofFloat(0f, bookmarkHeight);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
    {
      @Override
      public void onAnimationUpdate(ValueAnimator animation)
      {
        ViewHelper.setTranslationY(mPreview, (Float) animation.getAnimatedValue() - detailsHeight);
        ViewHelper.setTranslationY(mDetails, (Float) animation.getAnimatedValue());
      }
    });

    animator.setDuration(SHORT_ANIM_DURATION);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.start();
  }

  void showBookmark(final State currentState)
  {
    mPlacePage.setVisibility(View.VISIBLE);
    mPreview.setVisibility(View.VISIBLE);
    mDetails.setVisibility(View.VISIBLE);
    mBookmarkDetails.setVisibility(View.VISIBLE);

    ValueAnimator animator;
    final float bookmarkHeight = mBookmarkDetails.getHeight();
    final float detailsHeight = mDetails.getHeight();

    if (currentState == State.DETAILS)
      animator = ValueAnimator.ofFloat(bookmarkHeight, 0f);
    else
      animator = ValueAnimator.ofFloat(detailsHeight, 0f);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
    {
      @Override
      public void onAnimationUpdate(ValueAnimator animation)
      {
        ViewHelper.setTranslationY(mPreview, (Float) animation.getAnimatedValue() - detailsHeight);
        ViewHelper.setTranslationY(mDetails, (Float) animation.getAnimatedValue());
      }
    });

    animator.setDuration(SHORT_ANIM_DURATION);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.start();
  }

  protected void hidePlacePage()
  {
    final float animHeight = mPlacePage.getHeight() - mPreview.getTop() - ViewHelper.getTranslationY(mPreview);
    final ValueAnimator animator = ValueAnimator.ofFloat(0f, animHeight);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
    {
      @Override
      public void onAnimationUpdate(ValueAnimator animation)
      {
        ViewHelper.setTranslationY(mPlacePage, (Float) animation.getAnimatedValue());

        if (animation.getAnimatedFraction() > .99f)
        {
          mIsPlacePageVisible = false;
          mIsPreviewVisible = false;

          mPlacePage.setVisibility(View.INVISIBLE);
          ViewHelper.setTranslationY(mPlacePage, 0);
          if (mVisibilityChangedListener != null)
          {
            mVisibilityChangedListener.onPreviewVisibilityChanged(false);
            mVisibilityChangedListener.onPlacePageVisibilityChanged(false);
          }
        }
      }
    });
    animator.setDuration(SHORT_ANIM_DURATION);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.start();
  }
}
