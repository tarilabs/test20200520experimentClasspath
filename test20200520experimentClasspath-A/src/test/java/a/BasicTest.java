package a;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicTest {

    public static final Logger LOG = LoggerFactory.getLogger(BasicTest.class);

    @Test
    public void testA() throws IOException {
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                                                 .buildConfiguration()
                                                 .fromResources(Arrays.asList(ResourceFactory.newClassPathResource("a.dmn")))
                                                 .getOrElseThrow(e -> new RuntimeException("Error compiling DMN model(s)", e));

        String namespace = "nsA";
        String modelName = "a";

        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);
        DMNContext context = dmnRuntime.newContext();

        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, context);
        LOG.info("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors(), is(false));
        assertThat(evaluateAll.getDecisionResultByName("DecisionA").getResult(), is("Hello, World"));
    }
}
