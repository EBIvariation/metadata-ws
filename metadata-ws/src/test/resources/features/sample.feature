Feature: sample object

  Scenario: register a sample successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    When I create a sample with TAXONOMY for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY


  Scenario: update a sample successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_MUS_MUSCULUS
    When I create a sample with TAXONOMY_MUS_MUSCULUS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY_MUS_MUSCULUS

    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_HOMO_SAPIENS
    When I request PATCH SAMPLE with list TAXONOMY_HOMO_SAPIENS of taxonomies
    Then the response code should be 2xx
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY_HOMO_SAPIENS


  Scenario Outline: post a sample with invalid taxonomy list should fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I create a sample with <list> for taxonomy
    Then the response code should be 4xx
    And the response should contain field exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException

    Examples:
      | list  |
      | NONE  |
      | BLANK |


  Scenario: delete all of a sample's taxonomies should fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_MUS_MUSCULUS
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_HOMO_SAPIENS
    When I create a sample with TAXONOMY_MUS_MUSCULUS,TAXONOMY_HOMO_SAPIENS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain 2 taxonomies
    And the href of the taxonomy of taxonomies has items TAXONOMY_MUS_MUSCULUS,TAXONOMY_HOMO_SAPIENS

    When I request DELETE for the taxonomies of TAXONOMY_MUS_MUSCULUS of the SAMPLE
    Then the response code should be 2xx

    When I request DELETE for the taxonomies of TAXONOMY_HOMO_SAPIENS of the SAMPLE
    Then the response code should be 4xx
    And the response should contain field exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException


  Scenario Outline: update a sample with invalid taxonomies should fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    When I create a sample with TAXONOMY for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY

    When I request PATCH SAMPLE with list <list> of taxonomies
    Then the response code should be 4xx
    And the response should contain field exception with value <exception>

    Examples:
      | list           | exception                                                                  |
      | NONE           | uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException |
      | BLANK          | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException       |
      | TAXONOMY,BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException       |


  Scenario Outline: find two samples by taxonomy
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9593,
      "name": "Gorilla gorilla",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_GORILLA
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_HOMO_SAPIENS
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_GORILLA,TAXONOMY_HOMO_SAPIENS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_PANISCUS
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_TROGLODYTES
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_PAN_PANISCUS,TAXONOMY_PAN_TROGLODYTES for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2
    When I create a parameterized sample with Species3 for accession, 1 for version, Species collection3 for name and TAXONOMY_GORILLA,TAXONOMY_PAN_PANISCUS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE3

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the response should contain 2 samples
    And the href of the sample of samples has items <url>

    Examples:
      | query                           | url             |
      | taxonomies.name=Gorilla gorilla | SAMPLE1,SAMPLE3 |
      | taxonomies.name=Pan paniscus    | SAMPLE2,SAMPLE3 |
      | taxonomies.taxonomyId=9593      | SAMPLE1,SAMPLE3 |
      | taxonomies.taxonomyId=9597      | SAMPLE2,SAMPLE3 |


  Scenario Outline: find one sample by taxonomy
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9593,
      "name": "Gorilla gorilla",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_GORILLA
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_HOMO_SAPIENS
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_GORILLA,TAXONOMY_HOMO_SAPIENS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_PANISCUS
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_TROGLODYTES
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 37010,
      "name": "Pan troglodytes schweinfurthii",
      "rank": "SUBSPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_TROGLODYTES_SCWEINFURTHII
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_PAN_PANISCUS,TAXONOMY_PAN_TROGLODYTES,TAXONOMY_PAN_TROGLODYTES_SCWEINFURTHII for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the response should contain one sample
    And the href of the sample of samples has items <url>

    Examples:
      | query                           | url     |
      | taxonomies.name=Homo sapiens    | SAMPLE1 |
      | taxonomies.name=Pan troglodytes | SAMPLE2 |
      | taxonomies.taxonomyId=9606      | SAMPLE1 |
      | taxonomies.taxonomyId=9598      | SAMPLE2 |
      | taxonomies.taxonomyId=37010     | SAMPLE2 |


  Scenario Outline: find no sample by non-existing taxonomy
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9593,
      "name": "Gorilla gorilla",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_GORILLA
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_HOMO_SAPIENS
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_GORILLA,TAXONOMY_HOMO_SAPIENS for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_PANISCUS
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY_PAN_TROGLODYTES
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_PAN_PANISCUS,TAXONOMY_PAN_TROGLODYTES for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2

    When I request search for the samples with the parameters: <query>
    And the response code should be 200
    And the response should contain no sample

    Examples:
      | query                       |
      | taxonomies.name=NonExisting |
      | taxonomies.taxonomyId=0     |


  Scenario Outline: verify various accession version with samples
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9593,
      "name": "Gorilla gorilla",
      "rank": "SPECIES"
    }
    """
    And set the URL to TAXONOMY1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY2

    When I create a non-accessioned sample with Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field id with a numeric value
    And the response should contain field accessionVersionId with null value

    When I provide a null accession for a sample with Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be accessionVersionId.accession
    And the message field of errors 0 should be may not be null

    When I create a parameterized sample with <accession> for accession, <version> for version, Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be <property>
    And the message field of errors 0 should be <message>


    Examples:
      | accession | version | property                     | message                            |
      |           | 1       | accessionVersionId.accession | size must be between 1 and 255     |
      | EGAN0001  | 0       | accessionVersionId.version   | must be greater than or equal to 1 |
