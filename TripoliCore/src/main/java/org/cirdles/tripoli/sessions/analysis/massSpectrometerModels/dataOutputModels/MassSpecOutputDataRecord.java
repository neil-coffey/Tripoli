/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels;

import jama.Matrix;

public record MassSpecOutputDataRecord(
        Matrix rawDataColumn,
        Matrix timeColumn,
        Matrix timeIndColumn,
        Matrix signalIndicesForRawDataColumn,
        Matrix blockIndicesForRawDataColumn,
        Matrix isotopeIndicesForRawDataColumn,
        Matrix isotopeFlagsForRawDataColumn,
        Matrix detectorIndicesForRawDataColumn,
        Matrix detectorFlagsForRawDataColumn,
        Matrix baseLineFlagsForRawDataColumn,
        Matrix axialFlagsForRawDataColumn,
        Matrix firstBlockInterpolations,
        int faradayCount,
        int isotopeCount,
        int blockCount
) {

}