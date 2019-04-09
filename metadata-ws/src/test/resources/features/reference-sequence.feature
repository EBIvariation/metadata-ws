Feature: reference sequence

  Scenario: register a reference sequence successfully
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE
    And the response code should be 201
    And the Location header should be present with value of TEST_REFERENCE_SEQUENCE
    When I request GET with value of TEST_REFERENCE_SEQUENCE
    Then the response code should be 200
    And the result json should be:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    When I request GET /reference-sequences
    Then the result should contain one reference-sequence


  Scenario Outline: find one reference sequence by name
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2
    When I request search for the reference-sequences with the parameters: <query>
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items <url>
    And the <field1> field of reference-sequences 0 should be <value1>
    And the <field2> field of reference-sequences 0 should be <value2>

    Examples:
      | query | url | field1 | value1 | field2 | value2 |
      | name=GRCh37 | TEST_REFERENCE_SEQUENCE_1 | name | GRCh37 | patch | p2 |
      | name=GRCh38 | TEST_REFERENCE_SEQUENCE_2 | name | GRCh38 | patch | p2 |
      | name=GRCh37&patch=p2 | TEST_REFERENCE_SEQUENCE_1 | name | GRCh37 | patch | p2 |
      | name=GRCh38&patch=p2 | TEST_REFERENCE_SEQUENCE_2 | name | GRCh38 | patch | p2 |
      | name=GRCh37&patch=p2&accessions=GCA_000001405.3 | TEST_REFERENCE_SEQUENCE_1 | name | GRCh37 | patch | p2 |


  Scenario Outline: find one reference sequence by accession
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2
    When I request search for the reference-sequences with the parameters: <query>
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items <url>
    And the accessions field of reference-sequences 0 should have item <contained_accession>

    Examples:
      | query | url | contained_accession |
      | accessions=GCA_000001405.3 | TEST_REFERENCE_SEQUENCE_1 |  GCA_000001405.3 |
      | accessions=GCF_000001405.28 | TEST_REFERENCE_SEQUENCE_2 |  GCF_000001405.28 |
      | patch=p2&accessions=GCA_000001405.3 | TEST_REFERENCE_SEQUENCE_1 |  GCA_000001405.3 |


  Scenario Outline: find one reference sequence by type
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "BRCA1",
      "patch": "3",
      "accessions": ["NM_007294.3"],
      "type": "GENE"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2
    When I request search for the reference-sequences with the parameters: <query>
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items <url>
    And the type field of reference-sequences 0 should be <type>

    Examples:
      | query | url | type |
      | type=GENE | TEST_REFERENCE_SEQUENCE_1 | GENE |
      | type=ASSEMBLY | TEST_REFERENCE_SEQUENCE_2 | ASSEMBLY |

  Scenario Outline: find zero reference sequence
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2

    When I request search for the reference-sequences with the parameters: <query>
    Then the response code should be 200
    And the result should contain 0 reference-sequences

    Examples:
      | query |
      | name=NCBI36 |
      | name=NCBI36&patch=p2 |
      | name=GRCh37&patch=p3 |
      | name=GRCh38&patch=p3 |
      | accessions=GCA_000001405.2 |
      | name=GRCh37&patch=p3&accessions=GCA_000001405.3 |
      | type=GENE |
      | type=TRANSCRIPTOME |

  Scenario: find reference sequence by invalid type should fail
    When I request search for the reference-sequences with the parameters: type=UNKNOWN
    Then the response code should be 4xx
