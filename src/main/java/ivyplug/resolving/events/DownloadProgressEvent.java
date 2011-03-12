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
package ivyplug.resolving.events;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 17.02.2011
 */
public class DownloadProgressEvent implements Event {

    private String uri;
    private long bytesCompleted;
    private long totalSize;

    public DownloadProgressEvent(String uri, long bytesCompleted, long totalSize) {
        this.uri = uri;
        this.bytesCompleted = bytesCompleted;
        this.totalSize = totalSize;
    }

    public String getUri() {
        return uri;
    }

    public long getBytesCompleted() {
        return bytesCompleted;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
