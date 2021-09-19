// Released under the MIT License. 
package net.groboclown.retval.ideas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.monitor.NoOpObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitor;


/**
 * A container for multiple
 * {@link java.lang.AutoCloseable} (and thus {@link java.io.Closeable})
 * objects.
 *
 * <p>The collection is primarily used to keep track of values that should be closed
 * at the end of a <code>Ret*</code> call chain.
 */
@Immutable
public class CloseableCollection implements AutoCloseable {
    // Place holder until this becomes a real boy.
    public static final ObservedMonitor<AutoCloseable> CLOSEABLE_MONITOR
            = new NoOpObservedMonitor<>();

    private final List<AutoCloseable> closeables;
    private final ObservedMonitor.Listener listener;

    // Argument must be an immutable list.
    private CloseableCollection(
            @Nonnull final List<AutoCloseable> closeables
    ) {
        this.closeables = closeables;
        // When this becomes a real boy,
        this.listener = CLOSEABLE_MONITOR.registerInstance(this);
    }

    /**
     * Continues execution by returning another collection joined with flat closable objects.
     *
     * @param closeables list of closable values.
     * @return a collection containing the current closables along with those in the argument.
     */
    @Nonnull
    public CloseableCollection joinWith(@Nonnull final AutoCloseable... closeables) {
        if (closeables.length <= 0) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        ret.addAll(Arrays.asList(closeables));
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onObserved();
        return new CloseableCollection(ret);
    }

    /**
     * Combine this collection with the argument's closeables.
     *
     * @param closeables collection of closeables to join to this one.
     * @return a (possibly new) collection combining all the closeables together.
     */
    @Nonnull
    public CloseableCollection joinWith(@Nonnull final Collection<AutoCloseable> closeables) {
        if (closeables.isEmpty()) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        ret.addAll(closeables);
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onObserved();
        return new CloseableCollection(ret);
    }

    /**
     * Add one or more collections of AutoCloseable instances with this
     * collection, and return a new value.
     *
     * @param closeableSets list of closeable collections.
     * @return a (possibly new) instance
     */
    @SafeVarargs
    @Nonnull
    public final CloseableCollection joinWith(
            @Nonnull final Collection<AutoCloseable>... closeableSets
    ) {
        if (closeableSets.length <= 0) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        for (final Collection<AutoCloseable> closableSet : closeableSets) {
            ret.addAll(closableSet);
        }
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onObserved();
        return new CloseableCollection(ret);
    }

    /**
     * Join this collection of closeables with the passed in collection.
     *
     * @param collection closeables to join with this one.
     * @return the combined closeables, possibly a new instance.
     */
    @Nonnull
    public final CloseableCollection joinWith(@Nonnull final CloseableCollection collection) {
        if (collection.closeables.isEmpty()) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        ret.addAll(collection.closeables);
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onObserved();
        return new CloseableCollection(ret);
    }

    @Override
    public void close() throws Exception {
        this.listener.onObserved();
        final List<Exception> errors = new ArrayList<>();
        boolean includesNonIoExceptions = false;
        for (final AutoCloseable closeable : this.closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final IOException e) {
                errors.add(e);
            } catch (final Exception e) {
                includesNonIoExceptions = true;
                errors.add(e);
            }
        }
        if (errors.size() == 1) {
            throw errors.get(0);
        }
        if (! errors.isEmpty()) {
            // Allow for java.io.Closeable compatibility.
            final Exception ret = includesNonIoExceptions
                    ? new Exception("multiple errors during close")
                    : new IOException("multiple errors during close");
            for (final Exception e : errors) {
                ret.addSuppressed(e);
            }
            throw ret;
        }
        // Else no exception generated, so exit normally.
    }
}
