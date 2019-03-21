Feature: Web resource object

  Scenario: register a web resource successfully
    When user request POST /webResources with json data:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "http://www.ebi.ac.uk"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_WEB_RESOURCE

    When user request GET with value of TEST_WEB_RESOURCE
    Then the response code should be 200
    And the result should have type with value CENTER_WEB
    And the result should have resourceUrl with value http://www.ebi.ac.uk

  Scenario Outline: register a web resource with valid URL should succeed
    When user request POST /webResources with json data:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "<url>"
    }
    """
    And the response code should be 201

    Examples:
      | url |
      | http://api.plos.org/search?q=title:%22Drosophila%22%20and%20body:%22RNA%22&fl=id,abstract |
      | https://localhost:8090/swagger-ui.html#/WebResource_Entity/saveWebResourceUsingPOST |
      | ftp://ftp.sra.ebi.ac.uk/meta/xsd/sra_1_5/SRA.study.xsd                              |
      | http://MVSXX.COMPANY.COM:04445/CICSPLEXSM//JSMITH/VIEW/OURLOCTRAN?A_TRANID=P*&O_TRANID=NE |


  Scenario Outline: register a web resource with invalid URL should fail
    When user request POST /webResources with json data:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "<malformed_url>"
    }
    """
    And the response code should be 4xx

    Examples:
      | malformed_url |
      |               |
      | htttps://www.ebi.ac.uk |
      | www.google.com         |
      | http://www.space address.org |
      | //fileserver/code/src/main/app.java |


  # Few common tests
  Scenario: perform options
    When user request OPTIONS with header Access-Control-Request-Method,Origin value GET,http://www.evil-url.com
    And the response code should be 200
    Then the Access-Control-Allow-Origin header should be present with actual value http://www.evil-url.com
    Then the Allow header should contain GET

  Scenario Outline: verify metadata objects are auditable
    When user request set start time
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test parameterized study with testhuman for accession, 1 for version, test human stud for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY
    When user create a test analysis with testhuman for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    And set the URL to TEST_ANALYSIS
    When user request POST /files with json data:
    """
    {
      "accessionVersionId": {
        "accession": "testhuman",
        "version": 1
        },
      "hash": "asd123",
      "name": "file1",
      "size": 100,
      "type": "TSV"
    }
    """
    And set the URL to TEST_FILE
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 1,
      "name": "Species1"
    }
    """
    And set the URL to TEST_TAXONOMY1
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 2,
      "name": "Species2"
    }
    """
    And set the URL to TEST_TAXONOMY2
    When user create a test parameterized sample with testhuman for accession, 1 for version, test human sample for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    And set the URL to TEST_SAMPLE
    When user request POST /webResources with json data:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "http://www.ebi.ac.uk"
    }
    """
    And set the URL to TEST_WEB_RESOURCE
    When user request set end time

    When user request GET with value of <url>
    And the response code should be 200
    And the href of the class <class> should be <url>
    And the result should have lastModifiedDate non empty
    And the lastModifiedDate should be within times

    Given user request set start time
    When user request PATCH <url> with content <content>
    And the response code should be 2xx
    When user request set end time

    When user request GET with value of <url>
    And the response code should be 200
    And the href of the class <class> should be <url>
    And the result should have lastModifiedDate non empty
    And the lastModifiedDate should be within times

    Examples:
    | url | class | content |
    | TEST_REFERENCE_SEQUENCE | referenceSequence | {"name": "nothing important"} |
    | TEST_TAXONOMY | taxonomy | {"name": "nothing important"} |
    | TEST_STUDY | study | {"name": "nothing important"} |
    | TEST_ANALYSIS | analysis | {"name": "nothing important"} |
    | TEST_FILE | file | {"name": "nothing important"} |
    | TEST_SAMPLE | sample | {"name": "nothing important"} |
    | TEST_WEB_RESOURCE | webResource | {"resourceUrl": "http://nothing.important.com"} |
