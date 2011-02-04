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
package ivyplug.ui.messages;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 04.02.2011
 */
public class Message {

    private Type type;
    private String[] message;

    public Message(Type type, String... message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String[] getMessage() {
        return message;
    }

    public enum Type {
        INFO(ErrorTreeElementKind.INFO),
        ERROR(ErrorTreeElementKind.ERROR),
        WARNING(ErrorTreeElementKind.WARNING),
        GENERIC(ErrorTreeElementKind.GENERIC);

        private ErrorTreeElementKind internalType;

        Type(ErrorTreeElementKind internalType) {
            this.internalType = internalType;
        }

        public ErrorTreeElementKind getInternalType() {
            return internalType;
        }
    }
}
