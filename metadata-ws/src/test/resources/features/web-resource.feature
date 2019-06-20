Feature: Web resource object

  Scenario: register a web resource successfully
    When I request POST /webResources with JSON payload:
    """
    {
      "resourceUrl": "http://www.ebi.ac.uk"
    }
    """
    Then the response code should be 201
    And set the URL to WEB_RESOURCE

    When I request GET with value of WEB_RESOURCE
    Then the response code should be 200
    And the response should contain field resourceUrl with value http://www.ebi.ac.uk


  Scenario Outline: register a web resource with valid URL should succeed
    When I request POST /webResources with JSON payload:
    """
    {
      "resourceUrl": "<url>"
    }
    """
    Then the response code should be 201

    Examples:
      | url                                                                                       |
      | http://api.plos.org/search?q=title:%22Drosophila%22%20and%20body:%22RNA%22&fl=id,abstract |
      | https://localhost:8090/swagger-ui.html#/WebResource_Entity/saveWebResourceUsingPOST       |
      | ftp://ftp.sra.ebi.ac.uk/meta/xsd/sra_1_5/SRA.study.xsd                                    |
      | http://MVSXX.COMPANY.COM:04445/CICSPLEXSM//JSMITH/VIEW/OURLOCTRAN?A_TRANID=P*&O_TRANID=NE |


  Scenario Outline: register a web resource with invalid URL should fail
    When I request POST /webResources with JSON payload:
    """
    {
      "resourceUrl": "<malformed_url>"
    }
    """
    Then the response code should be 4xx

    Examples:
      | malformed_url                       |
      |                                     |
      | htttps://www.ebi.ac.uk              |
      | www.google.com                      |
      | http://www.space address.org        |
      | //fileserver/code/src/main/app.java |
