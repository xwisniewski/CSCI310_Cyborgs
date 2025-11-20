package com.example.csci310_teamproj.util;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Utility class for matching views within RecyclerView items.
 * This allows Espresso to find views at specific positions in a RecyclerView.
 */
public class RecyclerViewMatcher {
    
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public static RecyclerViewMatcher withRecyclerView(int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public Matcher<View> atPosition(int position) {
        return atPositionOnView(position, -1);
    }

    public Matcher<View> atPositionOnView(int position, int targetViewId) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
            }

            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                View childView = viewHolder.itemView;
                if (targetViewId == -1) {
                    return true;
                } else {
                    View targetView = childView.findViewById(targetViewId);
                    return targetView != null;
                }
            }
        };
    }

    public Matcher<View> atPositionOnView(int position, int targetViewId, Matcher<View> viewMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + " with view id " + targetViewId + ": ");
                viewMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                View childView = viewHolder.itemView;
                if (targetViewId == -1) {
                    return viewMatcher.matches(childView);
                } else {
                    View targetView = childView.findViewById(targetViewId);
                    return targetView != null && viewMatcher.matches(targetView);
                }
            }
        };
    }
}

