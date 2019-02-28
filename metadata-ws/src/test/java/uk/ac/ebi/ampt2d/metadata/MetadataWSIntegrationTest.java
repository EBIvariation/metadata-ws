package uk.ac.ebi.ampt2d.metadata;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "src/test/resources/features/analysis.feature",
                "src/test/resources/features/file.feature",
                "src/test/resources/features/reference-sequence.feature",
                "src/test/resources/features/sample.feature",
                "src/test/resources/features/study.feature",
                "src/test/resources/features/taxonomy.feature",
                "src/test/resources/features/web-resource.feature"
        },
        plugin = {
                "pretty",
                "html:target/cucumber"
        },
        tags = { "not @ignore" }
)
public class MetadataWSIntegrationTest {
}
