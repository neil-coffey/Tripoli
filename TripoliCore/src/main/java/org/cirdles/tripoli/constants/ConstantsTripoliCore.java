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

package org.cirdles.tripoli.constants;

import java.nio.CharBuffer;

/**
 * @author James F. Bowring
 */
public class ConstantsTripoliCore {
    public final static String SPACES_100 = CharBuffer.allocate(100).toString().replace('\0', ' ');

    public final static String MISSING_STRING_FIELD = "MISSING";

    public final static double ONE_JOULE = 6.24150934e18;

}