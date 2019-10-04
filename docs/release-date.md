# Transitive release date control

In certain situations studies are not released immediately but rather need to be held until a certain date. Until that
date, known as the **release date,** the study, as well as its child objects, must not be retrievable through the metadata
API.

To enforce that, transitive release date control is implemented. It works in the following way:
* For **Study,** `releaseDate` field is set explicitly.
  + If the `releaseDate` is earlier than or equal to the current date, the Study is considered to be released and 
    becomes retrievable through the API.
  + If the `releaseDate` is not set (equal to `null`), the Study is considered to be released.
  + Child studies of a Study are not treated in any special way. Each Study, either top level or child of another Study,
    needs to have its `releaseDate` set explicitly if required. `releaseDate` does not propagate through the chain of 
    child studies in either direction.
* For all other **Auditable** entities:
  + If at least one released Study links to an entity (including transitive linking, e. g. Study linking to Analysis
    linking to File), this entity is considered to be released and becomes retrievable through the API.
  + Otherwise, the entity is not released and not retrievable through the API.
  
## Access to Entities before release date

In certain cases the studies and its related entities should be accessible before release date for owners and 
journals. And by default admins will be able to access the entities regardless of the release date.

* To make a study accessible before release date for any user (owner or journals) , corresponding study accessions 
should be added in keycloak Users -> Attributes section of a particular user . Its a key value pair ,key being study 
and value being comma separated list of unreleased study accessions the user has access to.
