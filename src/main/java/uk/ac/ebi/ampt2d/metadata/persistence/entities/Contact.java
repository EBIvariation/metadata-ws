/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.metadata.persistence.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Contact extends Auditable<Long> {

    @ApiModelProperty(position = 1, value = "Contact auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @Size(min = 1, max = 64)
    @NotNull
    @Email
    @JsonProperty
    @Column(nullable = false, unique = true)
    private String email;

    @ApiModelProperty(position = 3, required = true)
    @Size(min = 1, max = 16)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String title;

    @ApiModelProperty(position = 4, required = true)
    @Size(min = 1, max = 32)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String firstName;

    @ApiModelProperty(position = 5, required = true)
    @Size(min = 1, max = 32)
    @JsonProperty
    @Column(nullable = true)
    private String middleInitials;

    @ApiModelProperty(position = 5, required = true)
    @Size(min = 1, max = 32)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String surname;

    @ApiModelProperty(position = 6, required = true)
    @Size(min = 1, max = 256)
    @JsonProperty
    @Column(nullable = true)
    private String address;

    @ApiModelProperty(position = 7, required = true)
    @Size(min = 1, max = 16)
    @JsonProperty
    @Column(nullable = true)
    private String telephone;

    @ApiModelProperty(position = 8, required = false)
    @Size(min = 1, max = 256)
    @JsonProperty
    @Column(nullable = true)
    private String organisation;

    public Long getId() {
        return id;
    }

}
