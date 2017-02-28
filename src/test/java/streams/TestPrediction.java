package streams;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jpmml.evaluator.ProbabilityDistribution;
import org.junit.Before;
import org.junit.Test;
import stream.data.DataFactory;
import stream.io.SourceURL;

import java.io.Serializable;

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

    PredictionService s = new PredictionService();


    @Before
    public void setUp() throws Exception {
        s.url = new SourceURL(TestPrediction.class.getResource("/test_model.pmml"));

//        s.url = new SourceURL(CameraGeometry.class.getResource("/images.json.gz"));
        s.init();
    }

    @Test
    public void testClassify(){
        ImmutableMap<String, Serializable> map = ImmutableMap.of("Size", 1000.0, "Length", 200.0, "Width", 100.0);
        ProbabilityDistribution prediction = s.applyClassifier(DataFactory.create(map));
        assertThat(prediction, is(not(nullValue())));
    }

    @Test
    public void testMissingFieldClassify(){
        ImmutableMap<String, Serializable> map = ImmutableMap.of("Size", 1000.0, "Width", 100.0);
        ProbabilityDistribution prediction = s.applyClassifier(DataFactory.create(map));
        assertThat(prediction, is(nullValue()));

        map = ImmutableMap.of("Width", 100.0);
        prediction = s.applyClassifier(DataFactory.create(map));
        assertThat(prediction, is(nullValue()));
    }


    @Test
    public void testRegressor(){
        ImmutableMap<String, Serializable> map = ImmutableMap.of("Size", 1000.0, "Length", 200.0, "Width", 100.0);
        Double prediction = s.applyRegression(DataFactory.create(map));
        assertThat(prediction, is(not(nullValue())));
    }

}
