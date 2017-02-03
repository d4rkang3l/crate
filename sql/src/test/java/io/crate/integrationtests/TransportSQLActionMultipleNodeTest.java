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

package io.crate.integrationtests;


import com.carrotsearch.randomizedtesting.annotations.Repeat;
import io.crate.action.sql.SQLActionException;
import io.crate.action.sql.SQLOperations;
import io.crate.testing.SQLTransportExecutor;
import org.elasticsearch.client.Client;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.util.HashMap;

@ESIntegTestCase.ClusterScope(numDataNodes = 2, numClientNodes = 1)
public class TransportSQLActionMultipleNodeTest extends SQLTransportIntegrationTest {

    public TransportSQLActionMultipleNodeTest() {
        super(new SQLTransportExecutor(
            new SQLTransportExecutor.ClientProvider() {
                @Override
                public Client client() {
                    // make sure we use a client node (started with client=true)
                    return internalCluster().clientNodeClient();
                }

                @Override
                public String pgUrl() {
                    return null;
                }

                @Override
                public SQLOperations sqlOperations() {
                    return internalCluster().getInstance(
                        SQLOperations.class,
                        internalCluster().clientNodeClient().settings().get("node.name"));
                }
            }
        ));
    }

    @Test
    public void testInsertBulkDifferentTypes() throws Exception {
        execute("create table foo (bar integer) clustered into 1 shards with (number_of_replicas=0)");
        ensureYellow();
        expectedException.expect(SQLActionException.class);
        expectedException.expectMessage("Bulk arguments don't match column types");
        execute("insert into foo (value) values (?)",
            new Object[][]{
                new Object[]{new HashMap<String, Object>() {{
                    put("foo", 127);
                }}},
                new Object[]{1},
            });
    }
}
