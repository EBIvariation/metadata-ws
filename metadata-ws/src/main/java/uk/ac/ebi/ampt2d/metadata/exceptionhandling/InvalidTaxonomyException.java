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
package uk.ac.ebi.ampt2d.metadata.exceptionhandling;

public class InvalidTaxonomyException extends IllegalArgumentException {

    private static final long serialVersionUID = 5133665429591765156L;

    public InvalidTaxonomyException() {
        super("At least one of the taxonomy URL is invalid");
    }

    public InvalidTaxonomyException(String message) {
        super(message);
    }

    public InvalidTaxonomyException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
