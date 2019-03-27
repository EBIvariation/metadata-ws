Feature: sample object

  Scenario: register a sample successfully
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test sample with TEST_TAXONOMY for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE
    When I request GET with value of TEST_SAMPLE
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have name with value test_human_sample
    When I request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    And the result should contain 1 taxonomies
    And the href of the taxonomy of taxonomies has items TEST_TAXONOMY


  Scenario: update a sample successfully
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    Then set the URL to TEST_TAXONOMY_1
    When I create a test sample with TEST_TAXONOMY_1 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE
    When I request GET with value of TEST_SAMPLE
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have name with value test_human_sample
    When I request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    And the result should contain 1 taxonomies
    And the href of the taxonomy of taxonomies has items TEST_TAXONOMY_1

    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY_2
    When I request PATCH TEST_SAMPLE with list TEST_TAXONOMY_2 for taxonomies
    Then the response code should be 2xx
    When I request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    And the result should contain 1 taxonomies
    And the href of the taxonomy of taxonomies has items TEST_TAXONOMY_2


  Scenario Outline: post a test sample with invalid taxonomy list should fail
    When I create a test sample with <list> for taxonomy
    Then the response code should be 4xx
    And the result should have exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException

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
    Then set the URL to TEST_TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY_2
    When I create a test sample with TEST_TAXONOMY_1,TEST_TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE
    When I request GET with value of TEST_SAMPLE
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have name with value test_human_sample
    When I request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    And the result should contain 2 taxonomies
    And the href of the taxonomy of taxonomies has items TEST_TAXONOMY_1,TEST_TAXONOMY_2

    When I request DELETE for the taxonomies of TEST_TAXONOMY_1 of the TEST_SAMPLE
    Then the response code should be 2xx

    When I request DELETE for the taxonomies of TEST_TAXONOMY_2 of the TEST_SAMPLE
    Then the response code should be 4xx
    And the result should have exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException


  Scenario Outline: update a sample with invalid taxonomies should fail
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test sample with TEST_TAXONOMY for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE
    When I request GET with value of TEST_SAMPLE
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have name with value test_human_sample
    When I request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    And the result should contain 1 taxonomies
    And the href of the taxonomy of taxonomies has items TEST_TAXONOMY

    When I request PATCH TEST_SAMPLE with list <list> for taxonomies
    Then the response code should be 4xx
    And the result should have exception with value <exception>

    Examples:
      | list | exception |
      | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException |
      | BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |
      | TEST_TAXONOMY,BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |


  Scenario Outline: find two samples by taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TEST_TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY_2
    When I create a test parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TEST_TAXONOMY_1,TEST_TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TEST_TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TEST_TAXONOMY_4
    When I create a test parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TEST_TAXONOMY_3,TEST_TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE2
    When I create a test parameterized sample with Species3 for accession, 1 for version, Species collection3 for name and TEST_TAXONOMY_1,TEST_TAXONOMY_3 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE3

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the result should contain 2 samples
    And the href of the sample of samples has items <url>

    Examples:
      | query | url |
      | taxonomies.name=Homininae | TEST_SAMPLE1,TEST_SAMPLE3 |
      | taxonomies.name=Pan paniscus | TEST_SAMPLE2,TEST_SAMPLE3 |
      | taxonomies.taxonomyId=207598 | TEST_SAMPLE1,TEST_SAMPLE3 |
      | taxonomies.taxonomyId=9597 | TEST_SAMPLE2,TEST_SAMPLE3 |


  Scenario Outline: find one sample by taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TEST_TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY_2
    When I create a test parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TEST_TAXONOMY_1,TEST_TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TEST_TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TEST_TAXONOMY_4
    When I create a test parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TEST_TAXONOMY_3,TEST_TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE2

    When I request search for the samples with the parameters: <query>
    Then the response code should be 200
    And the result should contain 1 samples
    And the href of the sample of samples has items <url>

    Examples:
      | query | url |
      | taxonomies.name=Homo sapiens | TEST_SAMPLE1 |
      | taxonomies.name=Pan troglodytes | TEST_SAMPLE2 |
      | taxonomies.taxonomyId=9606 | TEST_SAMPLE1 |
      | taxonomies.taxonomyId=9598 | TEST_SAMPLE2 |


  Scenario Outline: find no sample by non-existing taxonomy
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 207598,
      "name": "Homininae"
    }
    """
    Then set the URL to TEST_TAXONOMY_1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY_2
    When I create a test parameterized sample with Species1 for accession, 1 for version, Species collection1 for name and TEST_TAXONOMY_1,TEST_TAXONOMY_2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9597,
      "name": "Pan paniscus"
    }
    """
    Then set the URL to TEST_TAXONOMY_3
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9598,
      "name": "Pan troglodytes"
    }
    """
    Then set the URL to TEST_TAXONOMY_4
    When I create a test parameterized sample with Species2 for accession, 1 for version, Species collection2 for name and TEST_TAXONOMY_3,TEST_TAXONOMY_4 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE2

    When I request search for the samples with the parameters: <query>
    And the response code should be 200
    And the result should contain 0 samples

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
    And set the URL to TEST_TAXONOMY1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY2

    When I create a test sample no or null accession false, Sample1 for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE
    When I request GET with value of TEST_SAMPLE
    Then the response code should be 200
    And the result should have id as number
    And the result should have accessionVersionId as null

    When I create a test sample no or null accession true, Sample1 for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be accessionVersionId.accession
    And the message field of errors 0 should be may not be null

    When I create a test parameterized sample with <accession> for accession, <version> for version, Sample1 for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then the response code should be 4xx
    And the property field of errors 0 should be <property>
    And the message field of errors 0 should be <message>


    Examples:
    | accession | version | property | message |
    |  | 1 | accessionVersionId.accession | size must be between 1 and 255 |
    | EGAN0001 | 0 |accessionVersionId.version | must be greater than or equal to 1 |