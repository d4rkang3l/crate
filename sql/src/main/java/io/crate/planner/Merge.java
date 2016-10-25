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

package io.crate.planner;

import io.crate.planner.node.ExecutionPhases;
import io.crate.planner.node.dql.MergePhase;
import io.crate.planner.projection.Projection;

import java.util.Collections;
import java.util.UUID;

public class Merge implements Plan {

    private final Plan subPlan;
    private final MergePhase mergePhase;

    public static Plan mergeToHandler(Plan subPlan, Planner.Context plannerContext) {
        ResultDescription resultDescription = subPlan.resultDescription();
        assert resultDescription != null : "all plans must have a result description. Plan without: " + subPlan;
        if (!ExecutionPhases.isRemote(plannerContext.handlerNode(), resultDescription.executionNodes())) {
            return subPlan;
        }
        /*
        MergePhase mergePhase = new MergePhase(
            plannerContext.jobId(),
            plannerContext.nextExecutionPhaseId(),
            "localMerge",
            resultDescription.executionNodes().size(),
            resultDescription.streamedTypes(),
            Collections.<Projection>emptyList(),
            DistributionInfo.DEFAULT_SAME_NODE
        );
        */
        // TODO: order by would only need indices / reverseFlags / nullsFirst
        MergePhase mergePhase = MergePhase.mergePhase(
            plannerContext,
            Collections.singletonList(plannerContext.handlerNode()),
            resultDescription.executionNodes().size(),
            resultDescription.orderBy(),
            null,
            Collections.<Projection>emptyList(),
            resultDescription.outputs(),
            null
        );
        return new Merge(subPlan, mergePhase);
    }

    public Merge(Plan subPlan, MergePhase mergePhase) {
        this.subPlan = subPlan;
        this.mergePhase = mergePhase;
    }

    @Override
    public <C, R> R accept(PlanVisitor<C, R> visitor, C context) {
        return visitor.visitMerge(this, context);
    }

    @Override
    public UUID jobId() {
        return subPlan.jobId();
    }

    @Override
    public void addProjection(Projection projection) {
        mergePhase.addProjection(projection);
    }

    @Override
    public ResultDescription resultDescription() {
        return mergePhase;
    }

    public MergePhase mergePhase() {
        return mergePhase;
    }

    public Plan subPlan() {
        return subPlan;
    }
}