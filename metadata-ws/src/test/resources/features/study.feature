Feature: study object

  Scenario: register a study successfully and check its fields
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    # Create a taxonomy
    When I request POST taxonomy with 9606 for ID
    Then set the URL to TAXONOMY
    # Create a reference sequence
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001405.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY"
    """
    Then set the URL to REFERENCE_SEQUENCE
    # Create a study
    When I create a study
    Then set the URL to STUDY
    # Create analyses to link study to taxonomy
    When I create an analysis with Analysis for accession, REFERENCE_SEQUENCE for reference sequence and STUDY for study
    Then set the URL to ANALYSIS
    # Check that the study is retrievable and contains the correct accession
    When I request GET with value of STUDY
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    # Check that the study contains the correct taxonomy
    When I request GET for taxonomy of STUDY
    And the href of the class taxonomy should be TAXONOMY


  Scenario Outline: search various study by taxonomy name and id
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 9606 for ID
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS
    When I request POST taxonomy with 9598 for ID
    Then set the URL to TAXONOMY_SPECIES_PAN_TROGLODYTES
    When I request POST taxonomy with 9597 for ID
    Then set the URL to TAXONOMY_SPECIES_PAN_PANISCUS
    When I request POST taxonomy with 37010 for ID
    Then set the URL to TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII

    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001405.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY_SPECIES_HOMO_SAPIENS"
    """
    Then set the URL to REFERENCE_SEQUENCE_HOMO_SAPIENS

    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "Pan_tro 3.0",
      "patch": "null",
      "accession": "GCA_000001515.5",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY_SPECIES_PAN_TROGLODYTES"
    """
    Then set the URL to REFERENCE_SEQUENCE_PAN_TROGLODYTES

    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "panpan1.1",
      "patch": "null",
      "accession": "GCA_000258655.2",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY_SPECIES_PAN_PANISCUS"
    """
    Then set the URL to REFERENCE_SEQUENCE_PAN_PANISCUS

    # Create studies
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 1
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY_HOMO_SAPIENS
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testbonobo",
      "version": 1
    },
    "name": "test bonobo study",
    "deprecated": false,
    "releaseDate": today
    """
    And the response code should be 2xx
    Then set the URL to STUDY_PAN_TROGLODYTES
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testchimpanzee",
      "version": 1
    },
    "name": "test chimpanzee study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY_PAN_PANISCUS

    # Create analyses to link studies to taxonomies
    When I create an analysis with Analysis1 for accession, REFERENCE_SEQUENCE_HOMO_SAPIENS for reference sequence and STUDY_HOMO_SAPIENS for study
    When I create an analysis with Analysis2 for accession, REFERENCE_SEQUENCE_PAN_TROGLODYTES for reference sequence and STUDY_PAN_TROGLODYTES for study
    When I create an analysis with Analysis3 for accession, REFERENCE_SEQUENCE_PAN_PANISCUS for reference sequence and STUDY_PAN_PANISCUS for study

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | base          | query             | N | url                                                         |
      | taxonomy-id   | id=9606           | 1 | STUDY_HOMO_SAPIENS                                          |
      | taxonomy-id   | id=9596           | 2 | STUDY_PAN_TROGLODYTES,STUDY_PAN_PANISCUS                    |
      | taxonomy-id   | id=40674          | 3 | STUDY_HOMO_SAPIENS,STUDY_PAN_TROGLODYTES,STUDY_PAN_PANISCUS |
      | taxonomy-id   | id=0              | 0 | NONE                                                        |
      | taxonomy-name | name=Homo sapiens | 1 | STUDY_HOMO_SAPIENS                                          |
      | taxonomy-name | name=Pan          | 2 | STUDY_PAN_TROGLODYTES,STUDY_PAN_PANISCUS                    |
      | taxonomy-name | name=Primates     | 3 | STUDY_HOMO_SAPIENS,STUDY_PAN_TROGLODYTES,STUDY_PAN_PANISCUS |
      | taxonomy-name | name=None         | 0 | NONE                                                        |

  Scenario Outline: search various studies by release date
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedYesterday",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": yesterday
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedToday",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedTomorrow",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": tomorrow
    """
    Then set the URL to STUDY3

    When I set authorization with testuser having default role
    And I request search studies having release <query> today
    Then the response code should be 200
    And the response should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | query | N | url           |
      | to    | 2 | STUDY1,STUDY2 |
      | from  | 1 | STUDY2        |


  Scenario: find various studies by release date range
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedYesterday",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": yesterday
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedToday",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "releasedTomorrow",
      "version": 1
    },
    "name": "nothing important",
    "deprecated": false,
    "releaseDate": tomorrow
    """
    Then set the URL to STUDY3

    When I set authorization with testuser having default role
    And I request search studies having release from today
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items STUDY2


  Scenario Outline: find various studies by analysis
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001405.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY"
    """
    Then set the URL to REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh38",
      "patch": "p2",
      "accession": "GCA_000001405.17",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY"
    """
    Then set the URL to REFERENCE_SEQUENCE_2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 2
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I create an analysis with EGAA0001 for accession, REFERENCE_SEQUENCE_1 for reference sequence, STUDY1 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then the response code should be 201
    When I create an analysis with EGAA0002 for accession, REFERENCE_SEQUENCE_2 for reference sequence, STUDY2 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then the response code should be 201

    When I request elaborate find for the studies with the parameters: <param>
    Then the response code should be 200
    And the response should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | param                                                                        | N | url           |
      | analyses.referenceSequences.name=GRCh37                                      | 1 | STUDY1        |
      | analyses.referenceSequences.name=GRCh38                                      | 1 | STUDY2        |
      | analyses.referenceSequences.name=NCBI36                                      | 0 | NONE          |
      | analyses.referenceSequences.name=GRCh37&analyses.referenceSequences.patch=p2 | 1 | STUDY1        |
      | analyses.referenceSequences.name=GRCh38&analyses.referenceSequences.patch=p2 | 1 | STUDY2        |
      | analyses.referenceSequences.name=GRCh37&analyses.referenceSequences.patch=p3 | 0 | NONE          |
      | analyses.referenceSequences.name=NCBI36&analyses.referenceSequences.patch=p2 | 0 | NONE          |
      | analyses.type=CASE_CONTROL                                                   | 2 | STUDY1,STUDY2 |
      | analyses.type=TUMOR                                                          | 0 | NONE          |
      | analyses.type=COLLECTION                                                     | 0 | NONE          |
      | analyses.referenceSequences.name=GRCh38&analyses.type=CASE_CONTROL           | 1 | STUDY2        |
      | analyses.referenceSequences.name=GRCh38&analyses.type=TUMOR                  | 0 | NONE          |
      | analyses.referenceSequences.name=GRCh38&analyses.type=COLLECTION             | 0 | NONE          |
      | analyses.referenceSequences.name=NCBI36&analyses.type=CASE_CONTROL           | 0 | NONE          |


  Scenario: search various studies by name value pair for accession
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test human study based on GRCh37",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 2
    },
    "name": "test human study based on GRCh37",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0002",
      "version": 3
    },
    "name": "test human study based on GRCh38",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY3

    When I request search for the studies with base accession and name accession value EGAS0001
    Then the response code should be 200
    And the href of the class study should be STUDY2
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field accessionVersionId.version with value 2

    When I request search for the studies with base accession and name accession value EGAS0002
    Then the response code should be 200
    And the href of the class study should be STUDY3
    And the response should contain field accessionVersionId.accession with value EGAS0002
    And the response should contain field accessionVersionId.version with value 3

    When I request search for the studies with base accession and name accession value EGAS0003
    Then the response code should be 404


  Scenario Outline: search various studies by name value pair
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test human study based on GRCh37",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 2
    },
    "name": "test human study based on GRCh37",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0002",
      "version": 1
    },
    "name": "test human study based on GRCh38",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY3

    When I request search for the studies with base accession and name accession value EGAS0001
    Then the response code should be 200
    And the href of the class study should be STUDY2
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field accessionVersionId.version with value 2

    When I request search for the studies with base text and name searchTerm value grCh37
    Then the response code should be 200
    And the response should contain 2 studies
    And the accessionVersionId.accession field of studies 0 should be EGAS0001

    When I request search for the studies with base text and name searchTerm value <value>
    Then the response code should be 200
    And the response should contain <items> studies

    Examples:
      | value     | items |
      | human     | 3     |
      | important | 3     |
      | grCh39    | 0     |


  Scenario Outline: search various studies by paging and sorting
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test human B",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0002",
      "version": 1
    },
    "name": "test human A",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1

    When I request GET /studies
    Then the response code should be 200
    And the response should contain 2 studies
    And the response should contain field page.size with value 20
    And the response should contain field page.totalElements with value 2
    And the response should contain field page.totalPages with value 1

    When I request GET for the studies with query parameter <param>
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items <url>

    When I request GET for the studies with query parameter page=1
    Then the response code should be 200
    And the response should contain no study

    Examples:
      | param            | url    |
      | size=1           | STUDY2 |
      | size=1&sort=name | STUDY1 |

  Scenario Outline: search various public studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    # Create a common taxonomy
    When I request POST taxonomy with 9606 for ID
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS

    # Create a common reference sequence
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001405.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY_SPECIES_HOMO_SAPIENS"
    """
    Then the response code should be 201
    And set the URL to REFERENCE_SEQUENCE_1

    # Create three studies
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": false,
    "releaseDate": yesterday
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 2
    },
    "name": "1kg phase 1",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 3
    },
    "name": "1kg phase 3",
    "deprecated": false,
    "releaseDate": tomorrow
    """
    Then set the URL to STUDY3

    # Create three analyses to link studies to reference sequences and taxonomies
    When I create an analysis with analysisReleasedYesterday for accession, REFERENCE_SEQUENCE_1 for reference sequence, STUDY1 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then set the URL to ANALYSIS1
    When I create an analysis with Analysis2 for accession, REFERENCE_SEQUENCE_1 for reference sequence and STUDY2 for study
    Then set the URL to ANALYSIS2

    When I set authorization with testuser having default role
    And I request GET /studies
    Then the response should contain 2 studies
    And the href of the study of studies has items STUDY1,STUDY2

    When I request GET with value of STUDY1
    Then the response code should be 200

    When I request GET for analyses of STUDY1
    Then the response should contain one analysis
    And the href of the analysis of analyses has items ANALYSIS1

    When I request GET for analyses of STUDY2
    Then the response should contain one analysis
    And the href of the analysis of analyses has items ANALYSIS2

    When I request search for the studies with the parameters: taxonomy.taxonomyId=9606
    Then the response code should be 200
    And the response should contain 2 studies
    And the href of the study of studies has items STUDY1,STUDY2

    When I request elaborate search for the studies base accession and with the parameters: accession=1kg
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value 1kg
    And the response should contain field accessionVersionId.version with value 2
    And the href of the class study should be STUDY2

    When I request elaborate search with date range for the studies base release-date and with the parameters: 0
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items STUDY2

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain 2 studies
    And the href of the study of studies has items <url>

    Examples:
      | base          | query             | url           |
      | taxonomy-id   | id=9606           | STUDY1,STUDY2 |
      | taxonomy-name | name=Homo sapiens | STUDY1,STUDY2 |
      | text          | searchTerm=1kg    | STUDY1,STUDY2 |

  Scenario Outline: search various undeprecated studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    # Create a common taxonomy
    When I request POST taxonomy with 9606 for ID
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS

    # Create common reference sequence
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001405.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY_SPECIES_HOMO_SAPIENS"
    """
    Then set the URL to REFERENCE_SEQUENCE

    # Create two studies
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": true,
    "releaseDate": today
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 2
    },
    "name": "1kg phase 1",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2

    # Create two analyses to link studies with reference sequences and taxonomies
    When I create an analysis with Analysis1 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY1 for study
    Then set the URL to ANALYSIS1
    When I create an analysis with Analysis2 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY2 for study
    Then set the URL to ANALYSIS2

    When I request GET /studies
    Then the response should contain one study
    And the href of the study of studies has items STUDY2

    When I request GET with value of STUDY2
    Then the response code should be 200
    And the href of the class study should be STUDY2

    When I request GET for analyses of STUDY2
    Then the response should contain one analysis
    And the href of the analysis of analyses has items ANALYSIS2

    When I request search for the studies with the parameters: taxonomy.taxonomyId=9606
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items STUDY2

    When I request elaborate search for the studies base accession and with the parameters: accession=1kg
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value 1kg
    And the response should contain field accessionVersionId.version with value 2
    And the href of the class study should be STUDY2

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items <url>

    Examples:
      | base          | query             | url    |
      | taxonomy-id   | id=9606           | STUDY2 |
      | taxonomy-name | name=Homo sapiens | STUDY2 |
      | text          | searchTerm=1kg    | STUDY2 |


  Scenario: search various yet to publish studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 3
    },
    "name": "1kg phase 3",
    "deprecated": false,
    "releaseDate": tomorrow
    """
    Then set the URL to STUDY1

    When I set authorization with testuser having default role
    And I request GET with value of STUDY1
    Then the response code should be 404

    When I request GET for analyses of STUDY1
    Then the response code should be 404


  Scenario: search studies invalid dates
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request search for studies that have been released
    Then the response code should be 4xx
    And the response should contain field exception with value java.lang.IllegalArgumentException
    And the response should contain field message with value Either from or to needs to be non-null

    When I request elaborate search for the studies base release-date and with the parameters: from=wrong-format-date
    Then the response should contain field exception with value java.lang.IllegalArgumentException
    And the response should contain field message with value Please provide a date in the form yyyy-mm-dd


  Scenario: search various deprecated studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": true,
    "releaseDate": today
    """
    Then set the URL to STUDY1

    When I request GET with value of STUDY1
    Then the response code should be 404

    When I request GET for analyses of STUDY1
    Then the response code should be 404


  Scenario: search studies on various deprecated fields
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1

    When I request GET with value of STUDY1
    Then the response code should be 200
    And the href of the class study should be STUDY1
    And the response should contain field description with value Nothing important
    And the response should not contain field deprecated

    When I request PATCH STUDY1 with content {"deprecated": "true"}
    Then the response code should be 2xx

    When I request GET with value of STUDY1
    Then the response code should be 4xx


  Scenario Outline: find linked studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    And there is an URL https://nohost//studies/999 with key STUDY_NON_EXISTING
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 1
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY1
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 2
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY2
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 3
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY3
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 4
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY4

    When I request PATCH STUDY1 with list STUDY2,STUDY3 of childStudies
    Then the response code should be 2xx

    When I request GET for linkedStudies of <url>
    Then the response code should be 200
    And the response should contain 2 studies
    And the href of the study of studies has items <linkedStudies>

    When I request GET for linkedStudies of STUDY4
    Then the response code should be 200
    And the response should contain no study

    When I request PATCH STUDY1 with list STUDY_NON_EXISTING of childStudies
    Then the response code should be 2xx

    When I request GET for linkedStudies of STUDY1
    Then the response code should be 200
    And the response should contain no study

    Examples:
      | url    | linkedStudies |
      | STUDY1 | STUDY2,STUDY3 |
      | STUDY2 | STUDY1,STUDY3 |
      | STUDY3 | STUDY1,STUDY2 |


  Scenario: deprecate to undeprecate studies
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY

    When I request GET with value of STUDY
    Then the response code should be 200
    And the href of the class study should be STUDY

    When I request PATCH STUDY with patch and content {"deprecated": "true"}
    Then the response code should be 2xx
    And the href of the class study should be STUDY

    When I request GET with value of STUDY
    Then the response code should be 4xx

    When I request PATCH STUDY with patch and content {"deprecated": "false"}
    Then the response code should be 2xx
    And the href of the class study should be STUDY

    When I request GET with value of STUDY
    Then the response code should be 200


  Scenario: verify browsable is a property of study
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 1
    },
    "name": "1kg pilot",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY

    When I request GET with value of STUDY
    Then the response code should be 200
    And the response should contain field browsable with a false boolean value

    When I request search for the studies with the parameters: browsable=true
    Then the response code should be 200
    And the response should contain no study

    When I request PATCH STUDY with content {"browsable": "true"}
    Then the response code should be 2xx

    When I request search for the studies with the parameters: browsable=true
    Then the response code should be 200
    And the response should contain one study
    And the href of the study of studies has items STUDY

  Scenario: verify study release date could be changed
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 3
    },
    "name": "1kg phase 3",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY

    When I request GET with value of STUDY
    Then the response code should be 2xx
    And the response should contain field releaseDate

    When I request PATCH STUDY with patch and day tomorrow
    Then the response code should be 2xx
    When I request GET with value of STUDY
    And the response should contain field releaseDate
    And the difference between releaseDate and today should be 1 day

    When I set authorization with testuser having default role
    And I request GET with value of STUDY
    Then the response code should be 404

    When I set authorization with testoperator having SERVICE_OPERATOR role
    And I request PATCH STUDY with patch and day today
    Then the response code should be 2xx

    When I set authorization with testuser having default role
    And I request GET with value of STUDY
    Then the response code should be 200
    And the response should contain field releaseDate
    And the difference between releaseDate and today should be 0 day


  Scenario: verify non-existing study with patch
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    And there is an URL https://nohost//studies/999 with key STUDY_NON_EXISTING
    When I request PATCH STUDY_NON_EXISTING with patch and day today
    Then the response code should be 4xx


  Scenario: patch study with invalid request
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "1kg",
      "version": 3
    },
    "name": "1kg phase 3",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY

    When I request PATCH STUDY with content {"releaseDate": 2001}
    Then the response code should be 400

    When I request PATCH STUDY with content {""}
    Then the response code should be 400


  Scenario: Trying to create a study with an invalid accession name (containing a comma symbol) must fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I attempt to create a study with INVALID,ACCESSION for accession
    Then the response code should be 4xx
