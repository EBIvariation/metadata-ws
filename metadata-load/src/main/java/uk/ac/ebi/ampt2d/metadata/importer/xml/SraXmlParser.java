/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.importer.xml;

import org.apache.xmlbeans.XmlException;

public abstract class SraXmlParser<SRA_OBJECT> {

    private final String XML_ROOT_TAGS = "(</ROOT>|<ROOT.*display=xml\">)";

    private final String XML_SET_TAGS = "(<[A-Z]+_SET>|</[A-Z]+_SET>|<DATASETS>|</DATASETS>)";

    public abstract SRA_OBJECT parseXml(String xmlString, String accession) throws XmlException;

    protected String removeRootTagsFromXmlString(String xmlString) {
        return (xmlString != null) ? xmlString.replaceAll(XML_ROOT_TAGS, "") : xmlString;
    }

    protected String removeSetTagsFromXmlString(String xmlString) {
        return (xmlString != null) ? xmlString.replaceAll(XML_SET_TAGS, "") : xmlString;
    }

}
