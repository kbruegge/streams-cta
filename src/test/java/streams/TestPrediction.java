package streams;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.ProbabilityDistribution;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import stream.data.DataFactory;
import stream.io.SourceURL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test some aspects of the predictors
 *
 * Created by kbruegge on 2/28/17.
 */
public class TestPrediction {

    private static PredictionService s = new PredictionService();
    private HashMap<String, Serializable> m = new HashMap<>();

    @BeforeClass
    public static void setUpModel() throws Exception {
        s.url = new SourceURL(TestPrediction.class.getResource("/test_model_cta.pmml"));
        s.init();
    }

    @Before
    public void setupData(){
        m.put("shower:number_of_pixel", 10);
        m.put("shower:width", 10);
        m.put("shower:length", 20);
        m.put("shower:skewness", 0.23);
        m.put("shower:kurtosis", 0.02);
        m.put("shower:size", 500);
        m.put("shower:miss", 1);
        m.put("array:num_triggered_telescopes", 4);
    }

    @Test
    public void testClassify(){
        ProbabilityDistribution prediction = s.applyClassifier(DataFactory.create(m));
        assertThat(prediction, is(not(nullValue())));
    }


    @Test
    public void testTransform(){
        Map<FieldName, FieldValue> fieldValueMap = s.transformData(DataFactory.create(m));

        FieldName name = FieldName.create("shower:length");
        Double length = fieldValueMap.get(name).asDouble();

        assertThat(length, is(20.0));
    }

    @Test
    public void testMissingFieldClassify(){
        ProbabilityDistribution prediction = s.applyClassifier(DataFactory.create(m));
        assertThat(prediction, is(not(nullValue())));

        m.remove("shower:width");
        m.remove("shower:length");
        prediction = s.applyClassifier(DataFactory.create(m));
        assertThat(prediction, is(nullValue()));
    }


    @Test(expected = RuntimeException.class)
    public void testRegressor(){
        Double prediction = s.applyRegression(DataFactory.create(m));
        assertThat(prediction, is(not(nullValue())));
    }

}
