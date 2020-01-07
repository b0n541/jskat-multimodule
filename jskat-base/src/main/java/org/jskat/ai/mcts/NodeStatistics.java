/**
 * Copyright (C) 2020 Jan Schäfer (jansch@users.sourceforge.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jskat.ai.mcts;

public class NodeStatistics {

    private final static double EXPLORATION_PARAMETER = Math.sqrt(2.0);

    private double rewards = 0.0;
    private long visits = 0L;

    public double getUctValue(final long parentNodeSimulationCount) {
        if (visits == 0) {
            return Double.MAX_VALUE;
        }
        return (rewards / visits) + EXPLORATION_PARAMETER * Math.sqrt(Math.log(parentNodeSimulationCount) / visits);
    }

    public synchronized void addReward(final double reward) {
        rewards += reward;
        visits++;
    }
}
