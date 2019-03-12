Feature: study object

  Scenario: register a study successfully
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test study with TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY
    When user request GET with value of TEST_STUDY
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001


  Scenario Outline: search various study by taxonomy name and id
    When user request POST with /taxonomies for Uri "taxonomyId": 207598, "name": "Homininae" for stringData  for linkedObjectKey and ancestors for linkedObjectClassName
    And set the URL to TEST_TAXONOMY_1
    When user request POST with /taxonomies for Uri "taxonomyId": 9606, "name": "Homo Sapiens" for stringData TEST_TAXONOMY_1 for linkedObjectKey and ancestors for linkedObjectClassName
    And set the URL to TEST_TAXONOMY_2
    When user request POST with /taxonomies for Uri "taxonomyId": 9596, "name": "Pan" for stringData TEST_TAXONOMY_1 for linkedObjectKey and ancestors for linkedObjectClassName
    And set the URL to TEST_TAXONOMY_3
    When user request POST with /taxonomies for Uri "taxonomyId": 9597, "name": "Pan paniscus" for stringData TEST_TAXONOMY_1,TEST_TAXONOMY_3 for linkedObjectKey and ancestors for linkedObjectClassName
    And set the URL to TEST_TAXONOMY_4
    When user request POST with /taxonomies for Uri "taxonomyId": 9598, "name": "Pan troglodytes" for stringData TEST_TAXONOMY_1,TEST_TAXONOMY_3 for linkedObjectKey and ancestors for linkedObjectClassName
    And set the URL to TEST_TAXONOMY_5

    When user create a test parameterized study with testhuman for accession, 1 for version, test human study for name and TEST_TAXONOMY_2 for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with testbonobo for accession, 1 for version, test bonobo study for name and TEST_TAXONOMY_4 for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test parameterized study with testchimpanzee for accession, 1 for version, test chimpanzee study for name and TEST_TAXONOMY_5 for taxonomy
    And set the URL to TEST_STUDY3

    When user request elaborate search for the studies base <base> and with the parameters: <query>
    And the response code should be 200
    And the result should contain object studies with items <N>
    And the href list of the study of studies <N> contained in <url>

    Examples:
      | base | query | N | url |
      | taxonomy-id | id=9606 | 1 | TEST_STUDY1 |
      | taxonomy-id | id=9596 | 2 | TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-id | id=207598 | 3 | TEST_STUDY1,TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-id | id=0 | 0 |  |
      | taxonomy-name | name=Homo sapiens | 1 | TEST_STUDY1 |
      | taxonomy-name | name=Pan | 2 | TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-name | name=Homininae | 3 | TEST_STUDY1,TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-name | name=None | 0 |  |


  Scenario Outline: search various studies by release date
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test parameterized study with releasedYesterday for accession, 1 for version, nothing important for name -1 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with releasedToday for accession, 1 for version, nothing important for name 0 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test parameterized study with releasedTomorrow for accession, 1 for version, nothing important for name 1 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY3

    When user request exhaustive search for the studies base <base> and with the parameters: <query> and <day>
    And the response code should be 200
    And the result should contain object studies with items <N>
    And the href list of the study of studies <N> contained in <url>

    Examples:
      | base | day | query | N | url |
      | release-date | 0 | to= | 2 | TEST_STUDY1,TEST_STUDY2 |
      | release-date | 0 | from= | 1 | TEST_STUDY2 |


  Scenario Outline: find various studies by release date range
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test parameterized study with releasedYesterday for accession, 1 for version, nothing important for name -1 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with releasedToday for accession, 1 for version, nothing important for name 0 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test parameterized study with releasedTomorrow for accession, 1 for version, nothing important for name 1 for releaseDay and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY3

    When user request exhaustive search with dates for the studies base <base> and with the parameters: <query> and <day>
    And the response code should be 200
    And the result should contain object studies with items <N>
    And the href list of the study of studies <N> contained in <url>

    Examples:
      | base | day | query | N | url |
      | release-date | 0 | from= | 1 | TEST_STUDY2 |


  Scenario Outline: find various studies by analysis
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_2
    When user create a test parameterized study with EGAS0001 for accession, 1 for version, test human study for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with EGAS0001 for accession, 2 for version, test human study for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE_1 for reference sequence, TEST_STUDY1 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    And the response code should be 201
    When user create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE_2 for reference sequence, TEST_STUDY2 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    And the response code should be 201

    When user request elaborate find for the studies bases <bases> and with the parameters: <param>
    And the response code should be 200
    And the result should contain object studies with items <N>
    And the href list of the study of studies <N> contained in <url>

    Examples:
      | bases | param | N | url |
      | analyses.referenceSequences | name=GRCh37 | 1 | TEST_STUDY1 |
      | analyses.referenceSequences | name=GRCh38 | 1 | TEST_STUDY2 |
      | analyses.referenceSequences | name=NCBI36 | 0 |  |
      | analyses.referenceSequences,analyses.referenceSequences | name=GRCh37&patch=p2 | 1 | TEST_STUDY1 |
      | analyses.referenceSequences,analyses.referenceSequences | name=GRCh38&patch=p2 | 1 | TEST_STUDY2 |
      | analyses.referenceSequences,analyses.referenceSequences | name=GRCh37&patch=p3 | 0 |  |
      | analyses.referenceSequences,analyses.referenceSequences | name=NCBI36&patch=p2 | 0 |  |
      | analyses | type=CASE_CONTROL | 2 | TEST_STUDY1,TEST_STUDY2 |
      | analyses | type=TUMOR | 0 |  |
      | analyses | type=COLLECTION | 0 |  |
      | analyses.referenceSequences,analyses | name=GRCh38&type=CASE_CONTROL | 1 | TEST_STUDY2 |
      | analyses.referenceSequences,analyses | name=GRCh38&type=TUMOR | 0 |  |
      | analyses.referenceSequences,analyses | name=GRCh38&type=COLLECTION | 0 |  |
      | analyses.referenceSequences,analyses | name=NCBI36&type=CASE_CONTROL | 0 |  |


  Scenario: search various studies by name value pair for accession
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test parameterized study with EGAS0001 for accession, 1 for version, test human study based on GRCh37 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with EGAS0001 for accession, 2 for version, test human study based on GRCh37 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test parameterized study with EGAS0002 for accession, 3 for version, test human study based on GRCh38 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY3

    When user request search for the studies with base accession and name accession value EGAS0001
    And the response code should be 200
    And the href of the class study should be TEST_STUDY2
    And the result should contain accessionVersionId.accession with value EGAS0001
    And the result should contain accessionVersionId.version with value 2

    When user request search for the studies with base accession and name accession value EGAS0002
    And the response code should be 200
    And the href of the class study should be TEST_STUDY3
    And the result should contain accessionVersionId.accession with value EGAS0002
    And the result should contain accessionVersionId.version with value 3

    When user request search for the studies with base accession and name accession value EGAS0003
    And the response code should be 404


  Scenario Outline: search various studies by name value pair
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test parameterized study with EGAS0001 for accession, 1 for version, test human study based on GRCh37 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY1
    When user create a test parameterized study with EGAS0001 for accession, 2 for version, test human study based on GRCh37 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY2
    When user create a test parameterized study with EGAS0002 for accession, 1 for version, test human study based on GRCh38 for name and TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY3

    When user request search for the studies with base accession and name accession value EGAS0001
    And the response code should be 200
    And the href of the class study should be TEST_STUDY2
    And the result should contain accessionVersionId.accession with value EGAS0001
    And the result should contain accessionVersionId.version with value 2

    When user request search for the studies with base text and name searchTerm value grCh37
    And the response code should be 200
    And the result should contain object studies with items 2
    And the result should contain .studies[0].accessionVersionId.accession with value EGAS0001

    When user request search for the <class> with base <base> and name <name> value <value>
    And the response code should be 200
    And the result should contain object <class> with items <items>

    Examples:
      | class | base  | name | value | items |
      | studies | text | searchTerm | human | 3 |
      | studies | text | searchTerm | important | 3 |
      | studies | text | searchTerm | grCh39 | 0 |


