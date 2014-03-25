/*
 * Cross, common runtime object support system.
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.event;

/**
 * Abstract implementation of a typed Event.
 *
 * @author Nils Hoffmann
 * @param <V> the type transmitted by the event
 *
 */
public class AEvent<V> implements IEvent<V> {

    /**
     *
     */
    public static final String DEFAULT_EVENT_NAME = "EVENT_DEFAULT";

    private final long ts = System.currentTimeMillis();
    private final V v;
    private final IEventSource<V> ies;
    private final String eventName;

    /**
     * Creates a new event instance.
     *
     * @param v         the transmitted type
     * @param ies       the typed event source
     * @param eventName the name of the event
     */
    public AEvent(final V v, final IEventSource<V> ies, final String eventName) {
        this.v = v;
        this.ies = ies;
        this.eventName = eventName;
    }

    /**
     * Creates a new event instance. The event name will be "EVENT_DEFAULT".
     *
     * @param v   the transmitted type
     * @param ies the typed event source
     */
    public AEvent(final V v, final IEventSource<V> ies) {
        this(v, ies, DEFAULT_EVENT_NAME);
    }

    @Override
    public V get() {
        return this.v;
    }

    @Override
    public String getEventName() {
        return this.eventName;
    }

    @Override
    public IEventSource<V> getSource() {
        return this.ies;
    }

    @Override
    public long getWhen() {
        return this.ts;
    }
}
