/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
 *
 */
package uk.ac.ebi.ampt2d.metadata.persistence.entities;

import java.util.Comparator;

public class AccessionVersionEntityIdComparator implements Comparator<AccessionVersionEntityId> {

    @Override
    public int compare(AccessionVersionEntityId o1, AccessionVersionEntityId o2) {
        if ( o1 == null || o2 == null ) {
            throw new NullPointerException();
        }

        String accession1 = o1.getAccession();
        String accession2 = o2.getAccession();

        if ( accession1.equalsIgnoreCase(accession2) ) {
            return o1.getVersion() - o2.getVersion();
        }

        return accession1.compareToIgnoreCase(accession2);
    }

}
