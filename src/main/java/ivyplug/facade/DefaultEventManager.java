/*
 * Copyright 2011 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ivyplug.facade;

import org.apache.ivy.core.event.*;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.plugins.repository.TransferListener;
import org.apache.ivy.util.filter.Filter;

import javax.swing.event.EventListenerList;
import java.util.Arrays;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 03.02.2011
 */
public class DefaultEventManager extends EventManager {

    private DefaultEventListenerList listeners = new DefaultEventListenerList();

    public void addIvyListener(IvyListener listener) {
        listeners.add(IvyListener.class, listener);
    }

    public void addIvyListener(IvyListener listener, String eventName) {
        addIvyListener(listener, new IvyEventFilter(eventName, null, null));
    }

    public void addIvyListener(IvyListener listener, Filter filter) {
        listeners.add(IvyListener.class, new FilteredIvyListener(listener, filter));
    }

    public void removeIvyListener(IvyListener listener) {
        listeners.remove(IvyListener.class, listener);
        IvyListener[] listeners = this.listeners.getListeners(IvyListener.class);
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof FilteredIvyListener) {
                if (listener.equals(((FilteredIvyListener) listeners[i]).getIvyListener())) {
                    this.listeners.remove(IvyListener.class, listeners[i]);
                }
            }
        }
    }

    public boolean hasIvyListener(IvyListener listener) {
        return Arrays.asList(listeners.getListeners(IvyListener.class)).contains(listener);
    }

    public void fireIvyEvent(IvyEvent evt) {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == IvyListener.class) {
                ((IvyListener) listeners[i + 1]).progress(evt);
            }
        }
    }

    public void addTransferListener(TransferListener listener) {
        listeners.add(TransferListener.class, listener);
    }

    public void removeTransferListener(TransferListener listener) {
        listeners.remove(TransferListener.class, listener);
    }

    public boolean hasTransferListener(TransferListener listener) {
        return Arrays.asList(listeners.getListeners(TransferListener.class)).contains(listener);
    }

    protected void fireTransferEvent(TransferEvent evt) {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TransferListener.class) {
                ((TransferListener) listeners[i + 1]).transferProgress(evt);
            }
        }
    }

    public void transferProgress(TransferEvent evt) {
        fireTransferEvent(evt);
        fireIvyEvent(evt);
    }

    public void removeAllListeners() {
        listeners.removeAll();
    }

    public class DefaultEventListenerList extends EventListenerList {

        public synchronized void removeAll() {
            listenerList = new Object[0];
        }
    }
}
