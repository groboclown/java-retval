// Released under the MIT License.
package net.groboclown.retval.monitor;


import javax.annotation.Nonnull;

/**
 * Utilities to help with stack operations.
 */
class StackUtil {
    private StackUtil() {
        // utility class; not instantiated.
    }

    /**
     * Get the stack trace for the call, up to the parameter class & method.
     *
     * @return munged stack up to but not including the requested class/method call.
     */
    static StackTraceElement[] mungedStack(
            @Nonnull final String className, @Nonnull final String methodName) {
        final StackTraceElement[] currentStack = new Exception().fillInStackTrace().getStackTrace();

        int remove = 0;
        // item [0] is the call stack place that created the exception, which is this
        // method.
        // Note: "- 1" here because the remove index will be the position in the stack after
        // the value found.
        while (remove < currentStack.length) {
            if (className.equals(currentStack[remove].getClassName())
                    && methodName.equals(currentStack[remove].getMethodName())) {
                // Skip over this removed item; the point is to not include it.
                final int startPos = remove + 1;
                final int length = currentStack.length - startPos;
                final StackTraceElement[] ret = new StackTraceElement[length];
                System.arraycopy(currentStack, startPos, ret, 0, ret.length);
                return ret;
            }
            remove++;
        }
        // Not found.  This is a usage problem - the called class and method must
        // always be in the stack.
        throw new IllegalArgumentException(
                "Not found in stack: " + className + "." + methodName + "()");
    }
}
