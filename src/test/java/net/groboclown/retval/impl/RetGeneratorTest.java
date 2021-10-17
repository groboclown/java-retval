// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class RetGeneratorTest {
    ReturnTypeFactory original;

    @Test
    void setFactory_null() {
        try {
            RetGenerator.setFactory(null);
            fail("Did not throw NPE");
        } catch (final NullPointerException e) {
            // Skip introspecting the exception
        }
    }

    @Test
    void get_set_factory() {
        final MockFactory factory = new MockFactory();
        RetGenerator.setFactory(factory);
        assertSame(factory, RetGenerator.getFactory());
    }


    @BeforeEach
    void beforeEach() {
        // Kind of putting the cart before the horse - the
        // test depends on functionality that is being proved
        // to work in the test.
        this.original = RetGenerator.getFactory();
    }

    @AfterEach
    void afterEach() {
        RetGenerator.setFactory(this.original);
    }


    static class MockFactory implements ReturnTypeFactory {

        @Nonnull
        @Override
        public <T> RetNullable<T> createNullableOk(@Nullable final T value) {
            throw new IllegalStateException("should not be called");
        }

        @Nonnull
        @Override
        public <T> RetNullable<T> createNullableFromProblems(
                @Nonnull final List<Problem> problems) {
            throw new IllegalStateException("should not be called");
        }

        @Nonnull
        @Override
        public <T> RetVal<T> createValOk(@Nonnull final T value) {
            throw new IllegalStateException("should not be called");
        }

        @Nonnull
        @Override
        public <T> RetVal<T> createValFromProblems(@Nonnull final List<Problem> problems) {
            throw new IllegalStateException("should not be called");
        }

        @Nonnull
        @Override
        public RetVoid createVoidOk() {
            throw new IllegalStateException("should not be called");
        }

        @Nonnull
        @Override
        public RetVoid createVoidFromProblems(@Nonnull final List<Problem> problems) {
            throw new IllegalStateException("should not be called");
        }
    }
}
