package b;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.compiler.ImportDMNResolverUtil;
import org.kie.dmn.core.compiler.ImportDMNResolverUtil.ImportType;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.model.api.Import;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicTest {

    public static final Logger LOG = LoggerFactory.getLogger(BasicTest.class);

    @Test
    public void test() throws IOException {
        List<Resource> resources = new ArrayList<>();
        resources.add(ResourceFactory.newClassPathResource("b.dmn"));

        // PoC
        DMNMarshaller marshaller = DMNMarshallerFactory.newDefaultMarshaller();
        List<Resource> thisCPResources = new ArrayList<>(resources);
        for (Resource thisClasspathResource : thisCPResources) {
            Definitions definitions = marshaller.unmarshal(thisClasspathResource.getReader());
            for (Import i : definitions.getImport()) {
                if (ImportDMNResolverUtil.whichImportType(i) == ImportType.DMN) {
                    String locationURI = i.getLocationURI();
                    if (locationURI.startsWith("classpath:")) {
                        resources.add(ResourceFactory.newClassPathResource(locationURI.substring("classpath:".length())));
                    } else {
                        resources.add(ResourceFactory.newUrlResource(locationURI));
                    }
                }
            }
        }
        // end PoC

        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                                                 .buildConfiguration()
                                                 .fromResources(resources)
                                                 .getOrElseThrow(e -> new RuntimeException("Error compiling DMN model(s)", e));
        String namespace = "nsB";
        String modelName = "b";

        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);
        DMNContext context = dmnRuntime.newContext();

        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, context);
        LOG.info("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors(), is(false));
        assertThat(evaluateAll.getDecisionResultByName("DecisionB").getResult(), is("Doe, John"));
    }
}
