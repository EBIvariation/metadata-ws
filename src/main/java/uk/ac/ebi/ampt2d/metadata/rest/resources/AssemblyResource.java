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
package uk.ac.ebi.ampt2d.metadata.rest.resources;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;

public class AssemblyResource extends Resource<Assembly> {

    public AssemblyResource(Assembly content, Link... links) {
        super(content, links);
    }

    public AssemblyResource(Assembly content, Iterable<Link> links) {
        super(content, links);
    }

}
