Feature: Web resource object

  Scenario: register a web resource successfully
    When user request POST /webResources with json data:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "http://www.ebi.ac.uk"
    }
    """
    And the response code should be 201
    And set the URL to TEST_WEB_RESOURCE

    When user request GET with value of TEST_WEB_RESOURCE
    Then the response code should be 200
    Then the result should contain type with value CENTER_WEB
    Then the result should contain resourceUrl with value http://www.ebi.ac.uk

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
