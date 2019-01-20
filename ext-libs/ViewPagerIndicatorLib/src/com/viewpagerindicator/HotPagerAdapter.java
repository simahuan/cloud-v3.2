package com.viewpagerindicator;

public interface HotPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    boolean isHot(int index);

    // From PagerAdapter
    int getCount();
}
