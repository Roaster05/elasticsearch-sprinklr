/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.test.hamcrest;

import org.elasticsearch.search.SearchHit;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.CombinableMatcher;

import java.util.Objects;
import java.util.function.Function;

public class ElasticsearchMatchers {

    public static class SearchHitHasIdMatcher extends TypeSafeMatcher<SearchHit> {
        private String id;

        public SearchHitHasIdMatcher(String id) {
            this.id = id;
        }

        @Override
        protected boolean matchesSafely(SearchHit searchHit) {
            return Objects.equals(searchHit.getId(), id);
        }

        @Override
        public void describeMismatchSafely(final SearchHit searchHit, final Description mismatchDescription) {
            mismatchDescription.appendText(" was ").appendValue(searchHit.getId());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("searchHit id should be ").appendValue(id);
        }
    }

    public static class SearchHitHasTypeMatcher extends TypeSafeMatcher<SearchHit> {
        private String type;

        public SearchHitHasTypeMatcher(String type) {
            this.type = type;
        }

        @Override
        public boolean matchesSafely(final SearchHit searchHit) {
            return searchHit.getType().equals(type);
        }

        @Override
        public void describeMismatchSafely(final SearchHit searchHit, final Description mismatchDescription) {
            mismatchDescription.appendText(" was ").appendValue(searchHit.getType());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("searchHit type should be ").appendValue(type);
        }
    }

    public static class SearchHitHasIndexMatcher extends TypeSafeMatcher<SearchHit> {
        private String index;

        public SearchHitHasIndexMatcher(String index) {
            this.index = index;
        }

        @Override
        public boolean matchesSafely(final SearchHit searchHit) {
            return searchHit.getIndex().equals(index);
        }

        @Override
        public void describeMismatchSafely(final SearchHit searchHit, final Description mismatchDescription) {
            mismatchDescription.appendText(" was ").appendValue(searchHit.getIndex());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("searchHit index should be ").appendValue(index);
        }
    }

    public static class SearchHitHasScoreMatcher extends TypeSafeMatcher<SearchHit> {
        private float score;

        public SearchHitHasScoreMatcher(float score) {
            this.score = score;
        }

        @Override
        protected boolean matchesSafely(SearchHit searchHit) {
            return searchHit.getScore() == score;
        }

        @Override
        public void describeMismatchSafely(final SearchHit searchHit, final Description mismatchDescription) {
            mismatchDescription.appendText(" was ").appendValue(searchHit.getScore());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("searchHit score should be ").appendValue(score);
        }
    }

    public static class HasPropertyLambdaMatcher<T, V> extends FeatureMatcher<T, V> {

        private final Function<? super T, ? extends V> property;

        private HasPropertyLambdaMatcher(Matcher<? super V> subMatcher, Function<? super T, ? extends V> property) {
            super(subMatcher, "object with", "lambda");
            this.property = property;
        }

        @Override
        protected V featureValueOf(T actual) {
            return property.apply(actual);
        }

        /**
         * @param valueMatcher The matcher to apply to the property
         * @param property     The lambda to fetch property
         */
        public static <T, V> CombinableMatcher<T> hasProperty(Function<? super T, ? extends V> property, Matcher<V> valueMatcher) {
            return new CombinableMatcher<>(new HasPropertyLambdaMatcher<>(valueMatcher, property));
        }
    }
}
