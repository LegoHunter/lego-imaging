package io.legohunter.imaging.model;

/**
 * How many items the hosting account holds against how many it may hold.
 *
 * @param itemCount items uploaded to the account; null when the provider did not say
 * @param itemLimit the most items the account may hold; null when the account has no limit
 */
public record HostedAccountUsage(Integer itemCount, Integer itemLimit) {

    /** True when the account has an item limit, as a free Flickr account does. */
    public boolean limited() {
        return itemLimit != null;
    }

    /** Items the account can still take; null when it has no limit or the count is unknown. */
    public Integer remaining() {
        return limited() && itemCount != null ? Math.max(0, itemLimit - itemCount) : null;
    }
}
