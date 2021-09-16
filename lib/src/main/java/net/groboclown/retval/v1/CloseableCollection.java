// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * A container for multiple {@link java.lang.AutoCloseable} (and thus {@link java.io.Closeable}) objects.
 */
@Immutable
public class CloseableCollection implements AutoCloseable {
    private final List<AutoCloseable> closeables;
    private final CheckMonitor.CloseableListener listener;

    // Argument must be an immutable list.
    private CloseableCollection(
            @Nonnull final List<AutoCloseable> closeables
    ) {
        this.closeables = closeables;
        this.listener = CheckMonitor.getInstance().registerCloseableInstance(this);
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
        this.listener.onClosed();
        return new CloseableCollection(ret);
    }

    @Nonnull
    public CloseableCollection joinWith(@Nonnull final Collection<AutoCloseable> closeables) {
        if (closeables.isEmpty()) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        ret.addAll(closeables);
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onClosed();
        return new CloseableCollection(ret);
    }

    @SafeVarargs
    @Nonnull
    public final CloseableCollection joinWith(@Nonnull final Collection<AutoCloseable>... closeableSets) {
        if (closeableSets.length <= 0) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        for (final Collection<AutoCloseable> closableSet: closeableSets) {
            ret.addAll(closableSet);
        }
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onClosed();
        return new CloseableCollection(ret);
    }

    @Nonnull
    public final CloseableCollection joinWith(@Nonnull final CloseableCollection collection) {
        if (collection.closeables.isEmpty()) {
            // Memory efficiency
            return this;
        }
        final List<AutoCloseable> ret = new ArrayList<>(this.closeables);
        ret.addAll(collection.closeables);
        // Because this returns a new instance, do not mark this one as still pending close.
        this.listener.onClosed();
        return new CloseableCollection(ret);
    }

    @Override
    public void close() throws Exception {
        this.listener.onClosed();
        final List<Exception> errors = new ArrayList<>();
        boolean includesNonIOExceptions = false;
        for (final AutoCloseable closeable: this.closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final IOException e) {
                errors.add(e);
            } catch (final Exception e) {
                includesNonIOExceptions = true;
                errors.add(e);
            }
        }
        if (errors.size() == 1) {
            throw errors.get(0);
        }
        if (! errors.isEmpty()) {
            // Allow for java.io.Closeable compatibility.
            final Exception ret = includesNonIOExceptions
                    ? new Exception("multiple errors during close")
                    : new IOException("multiple errors during close");
            for (final Exception e: errors) {
                ret.addSuppressed(e);
            }
            throw ret;
        }
        // No exception generated.
    }
}
