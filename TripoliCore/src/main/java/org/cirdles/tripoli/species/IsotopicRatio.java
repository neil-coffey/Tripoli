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

package org.cirdles.tripoli.species;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class IsotopicRatio implements Serializable {
    @Serial
    private static final long serialVersionUID = 676151940943728743L;
    private final SpeciesRecordInterface numerator;
    private final SpeciesRecordInterface denominator;

    public IsotopicRatio(SpeciesRecordInterface numerator, SpeciesRecordInterface denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public String prettyPrint() {
        return numerator.prettyPrintShortForm() + " / " + denominator.prettyPrintShortForm();
    }

//    private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(IsotopicRatio.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.err.println("Customized De-serialization of IsotopicRatio "
//                + theSUID);
//    }
}