Feature: sample object

  Scenario: register a sample successfully
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
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
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    Then set the URL to TAXONOMY_1
    When I create a sample with TAXONOMY_1 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY_1

    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY_2
    When I request PATCH SAMPLE with list TAXONOMY_2 of taxonomies
    Then the response code should be 2xx
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain one taxonomy
    And the href of the taxonomy of taxonomies has items TAXONOMY_2


  Scenario Outline: post a sample with invalid taxonomy list should fail
    When I create a sample with <list> for taxonomy
    Then the response code should be 4xx
    And the response should contain field exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException

    Examples:
      | list |
      | NONE |
      | BLANK |

  Scenario: delete all of a sample's taxonomies should fail
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    Then set the URL to TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY_2
    When I create a sample with TAXONOMY_1,TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field accessionVersionId.accession with value EGAS0001
    And the response should contain field name with value test_human_sample
    When I request GET for taxonomies of SAMPLE
    Then the response code should be 200
    And the response should contain 2 taxonomies
    And the href of the taxonomy of taxonomies has items TAXONOMY_1,TAXONOMY_2

    When I request DELETE for the taxonomies of TAXONOMY_1 of the SAMPLE
    Then the response code should be 2xx

    When I request DELETE for the taxonomies of TAXONOMY_2 of the SAMPLE
    Then the response code should be 4xx
    And the response should contain field exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException


  Scenario Outline: update a sample with invalid taxonomies should fail
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
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
      | list | exception |
      | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException |
      | BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |
      | TAXONOMY,BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |


  Scenario Outline: find two samples by taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY_2
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_1,TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TAXONOMY_4
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_3,TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2
    When I create a parameterized sample with Species3 for accession, 1 for version, Species collection3 for name and TAXONOMY_1,TAXONOMY_3 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE3

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the response should contain 2 samples
    And the href of the sample of samples has items <url>

    Examples:
      | query | url |
      | taxonomies.name=Homininae | SAMPLE1,SAMPLE3 |
      | taxonomies.name=Pan paniscus | SAMPLE2,SAMPLE3 |
      | taxonomies.taxonomyId=207598 | SAMPLE1,SAMPLE3 |
      | taxonomies.taxonomyId=9597 | SAMPLE2,SAMPLE3 |


  Scenario Outline: find one sample by taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY_2
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_1,TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TAXONOMY_4
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_3,TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the response should contain one sample
    And the href of the sample of samples has items <url>

    Examples:
      | query | url |
      | taxonomies.name=Homo sapiens | SAMPLE1 |
      | taxonomies.name=Pan troglodytes | SAMPLE2 |
      | taxonomies.taxonomyId=9606 | SAMPLE1 |
      | taxonomies.taxonomyId=9598 | SAMPLE2 |


  Scenario Outline: find no sample by non-existing taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY_2
    When I create a parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TAXONOMY_1,TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TAXONOMY_4
    When I create a parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TAXONOMY_3,TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE2

    When I request search for the samples with the parameters: <query>
    And the response code should be 200
    And the response should contain no sample

    Examples:
      | query |
      | taxonomies.name=NonExisting |
      | taxonomies.taxonomyId=0 |


  Scenario Outline: verify various accession version with samples
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    And set the URL to TAXONOMY1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TAXONOMY2

    When I create a no accession sample with Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE
    When I request GET with value of SAMPLE
    Then the response code should be 200
    And the response should contain field id with a numeric value
    And the response should contain field accessionVersionId with null value

    When I create a null accession sample with Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be accessionVersionId.accession
    And the message field of errors 0 should be may not be null

    When I create a parameterized sample with <accession> for accession, <version> for version, Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be <property>
    And the message field of errors 0 should be <message>


    Examples:
    | accession | version | property | message |
    |  | 1 | accessionVersionId.accession | size must be between 1 and 255 |
    | EGAN0001 | 0 |accessionVersionId.version | must be greater than or equal to 1 |