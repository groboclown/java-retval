// Released under the MIT License. 
package net.groboclown.retval;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A testable version of the {@link ValuedProblemContainer}; allows for
 * using a custom container to test for full compatibility with this container,
 * rather than making assumptions about {@link RetVal} and {@link RetNullable}.
 *
 * @param <T> value type
 */
public class TestableValuedProblemContainer<T>
        extends TestableProblemContainer
        implements ValuedProblemContainer<T> {
    private final T value;

    public TestableValuedProblemContainer(final T value) {
        super();
        this.value = value;
    }

    /** Constructor with 0 or more, possibly null, values. */
    public TestableValuedProblemContainer(final Problem... problems) {
        super(problems);
        this.value = null;
    }


    @Nullable
    @Override
    public T getValue() {
        return this.value;
    }


    // For test compatibility
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass().equals(TestableValuedProblemContainer.class)) {
            final TestableValuedProblemContainer<?> that =
                    (TestableValuedProblemContainer<?>) other;
            return Objects.equals(this.value, that.getValue())
                    && anyProblems().equals(that.anyProblems());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(this.value);
    }
}
