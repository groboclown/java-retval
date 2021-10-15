// Released under the MIT License. 

/**
 * Behind-the-scenes implementations for the top-level interfaces.  These can change arbitrarily
 * between versions, even patch versions, so do not rely upon these for library stability.
 *
 * <p>The code here attempts to optimize memory and performance based on usage patterns.  The
 * primary motivation around the break-down is for optimizing memory, especially since these
 * objects are expected to have a short life-cycle and potentially large numbers of objects
 * created.
 */
package net.groboclown.retval.impl;
