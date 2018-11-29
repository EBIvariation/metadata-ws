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
package uk.ac.ebi.ampt2d.metadata.loader.api;

import uk.ac.ebi.ena.sra.xml.ANALYSISDocument;

import java.util.List;
import java.util.Map;

public abstract class SraObjectLoaderFromAnalysisDocument<SRA_OBJECT> {

    protected SraObjectLoaderByAccession<ANALYSISDocument> sraAnalysisDocumentLoader;

    public SraObjectLoaderFromAnalysisDocument(SraObjectLoaderByAccession sraAnalysisDocumentLoader) {
        this.sraAnalysisDocumentLoader = sraAnalysisDocumentLoader;
    }

    public abstract Map<String, List<SRA_OBJECT>> getSraObjectsFromAnalysisDocument(List<String> analysisAccessionIds);
}
