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
public class StartingDownloadEvent implements Event {

    private String org;
    private String module;
    private String rev;
    private String branch;
    private String name;
    private String ext;
    private String type;

    public StartingDownloadEvent(String org, String module, String rev, String branch, String name, String ext, String type) {
        this.org = org;
        this.module = module;
        this.rev = rev;
        this.branch = branch;
        this.name = name;
        this.ext = ext;
        this.type = type;
    }

    public String getOrg() {
        return org;
    }

    public String getModule() {
        return module;
    }

    public String getRev() {
        return rev;
    }

    public String getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public String getExt() {
        return ext;
    }

    public String getType() {
        return type;
    }
}
