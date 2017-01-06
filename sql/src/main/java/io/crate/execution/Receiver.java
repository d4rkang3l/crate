/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.execution;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Receiver<T> {

    enum Type {
        CONTINUE,
        SUSPEND,
        STOP
    }

    @FunctionalInterface
    interface ResumeHandle {
        void resume();
    }

    interface Result<T> {
        Result CONTINUE = new Result() {
            @Override
            public Type type() {
                return Type.CONTINUE;
            }

            @Override
            public CompletableFuture<Function> continuation() {
                throw new UnsupportedOperationException("CONTINUE result has no continuation");
            }

        };
        static <T> Result<T> getContinue() {
            //noinspection unchecked
            return (Result<T>) CONTINUE;
        }

        Type type();
        CompletableFuture<ResumeHandle> continuation();
    }

    Result<T> onNext(T item);

    void onFinish();

    void onError(Throwable t);
}
