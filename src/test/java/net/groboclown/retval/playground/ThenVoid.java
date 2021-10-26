// Released under the MIT License. 
package net.groboclown.retval.playground;

import javax.annotation.Nonnull;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import org.junit.jupiter.api.Test;

/**
 * Shows off why {@link net.groboclown.retval.RetVal#thenVoid} is deprecated, and why the
 * consume and produce versions are fine.
 */
class ThenVoid {
    @Test
    void showRequiredReturn_producer() {
        // Note ambiguity that prevents compiling.
        // The ambiguity only applies to the places where the value is explicitly thrown away.

        // RetVal.ok("x").thenVoid((x) -> voidProducer());
        RetVal.ok("x").thenVoid((x) -> {
            return voidProducer();
        });

        // RetVal.ok("x").thenVoid((x) -> voidConsumer());
        RetVal.ok("x").thenVoid((x) -> {
            voidConsumer();
        });

        // The versions which accept the argument aren't ambiguous.
        // But note the lack of clarity about expectations.
        RetVal.ok("x").thenVoid(this::voidProducerWithArg);
        RetVal.ok("x").thenVoid(this::voidConsumerWithArg);

        // These do not need disambiguation
        // Note that, due to how the lambdas are used, the producer can be used with
        // a consumer; so the consumer name removes that ambiguity.
        RetVal.ok("x").consume((x) -> voidProducer());
        RetVal.ok("x").consume(this::voidProducerWithArg);
        RetVal.ok("x").produceVoid((x) -> voidProducer());
    }


    @Nonnull
    RetVoid voidProducer() {
        return RetVoid.ok();
    }


    @Nonnull
    RetVoid voidProducerWithArg(@Nonnull final String arg) {
        return RetVoid.ok();
    }

    void voidConsumer() {
        // do nothing
    }

    void voidConsumerWithArg(@Nonnull final String arg) {
        // do nothing
    }
}
