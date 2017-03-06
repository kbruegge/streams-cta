package streams.cta;


import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Given a pmml file, this service provides two synchronized methods for predicting classification and
 * regression problems.
 *
 * It provides two methods. {@link #applyClassifier(Data)} and {@link #applyRegression(Data)} (Data)}.
 * If this service is given a classification model pmml file the {@link #applyRegression(Data)} method will
 * fail and vice versa.
 *
 * Created by kai on 02.02.16.
 */
public class PredictionService implements Service {
    private static Logger log = LoggerFactory.getLogger(PredictionService.class);

    private ModelEvaluator<? extends Model> modelEvaluator;

    private FieldName targetName;

    //fields used while training the model
    private List<InputField> activeFields;

    @Parameter(required = true, description = "URL point to the .pmml model")
    public SourceURL url;


    public void init() {
        log.debug("Loading pmml model from url: " + url);
        PMML pmml;
        try (InputStreamReader isr = new InputStreamReader(url.openStream())) {
            Source transformedSource = ImportFilter.apply(new InputSource(isr));
            pmml = JAXBUtil.unmarshalPMML(transformedSource);
        } catch (SAXException | IOException | JAXBException ex) {
            log.error("Could not load model from file provided at" + url);
            ex.printStackTrace();
            throw  new RuntimeException(ex);
        }

        //build a model evaluator from the loaded pmml file
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);


        log.debug("Loaded model requires the following fields: " + modelEvaluator.getActiveFields().toString());

        log.debug("Loaded model has targets: " + modelEvaluator.getTargetFields().toString());
        if(modelEvaluator.getTargetFields().size() > 1){
            log.error("Only models with one target variable are supported for now");

            throw new IllegalArgumentException
                    (
                        "Provided pmml model has more than 1 target variable. This is unsupported for now."
                    );
        }

        targetName = modelEvaluator.getTargetField().getName();
        activeFields = modelEvaluator.getActiveFields();
    }


    /**
     * Apply classifier to values in the data item.
     * Returns a ProbabilityDistribution containing the predicted class and the
     * prediction values (some call this probability or confidence of the prediction) for each
     * class
     * @param data the data item containing the required data to perform the classification.
     * @return the distribution containing the class predictions as calculated by the classifier
     */
    public synchronized ProbabilityDistribution applyClassifier(Data data){

        Map<FieldName, FieldValue> fieldsMap = transformData(data);


        try {
            Map<FieldName, ?> results = modelEvaluator.evaluate(fieldsMap);
            Object targetValue = results.get(targetName);
            return (ProbabilityDistribution) targetValue;

        } catch (MissingFieldException | TypeCheckException exception){
            String missingKeys = collectMissingNames(fieldsMap);

            log.warn(
                    "Event had missing fields or missing field values: \n" + missingKeys

            );

            return null;
        } catch (ClassCastException e){
            log.error("The model did not return a ProbalilityDistribution. Make sure its not a regression model.");
            throw new RuntimeException(e);
        }

    }

    /**
     * Convinience method to get missing names from fieldsmap.
     * @param fieldsMap the fieldsmap to check
     * @return missing names as on comma separated string.
     */
    private String collectMissingNames(Map<FieldName, FieldValue> fieldsMap) {
        return fieldsMap.entrySet()
                        .stream()
                        .filter(e -> e.getValue() == null)
                        .map(Map.Entry::getKey)
                        .map(FieldName::getValue)
                        .collect(Collectors.joining(", "));
    }

    /**
     * Apply regressor to values in the data item. Returns the output of the regression as a single Double.
     * If the input data item is missing required fields or values, Double.NaN is returned.
     * This method is thread safe.
     *
     * @param data the data item containing the required data to perform the regression.
     * @return the regressors prediction
     */
    public synchronized Double applyRegression(Data data){

        Map<FieldName, FieldValue> fieldsMap = transformData(data);

        try {

            Map<FieldName, ?> results = modelEvaluator.evaluate(fieldsMap);
            Object targetValue = results.get(targetName);
            return (Double) targetValue;

        } catch (MissingFieldException | TypeCheckException exception){
            String missingKeys = collectMissingNames(fieldsMap);

            log.warn(
                    "Event had missing fields or missing field values: \n" + missingKeys

            );

            return Double.NaN;

        } catch (ClassCastException e){
            log.error(
                    "The model did not return a double as target. \n " +
                    "Make sure its a regression model and not a classifier."
            );
            throw new RuntimeException("Model does not seem to support regression task.");
        }

    }

    /**
     * Transform data in data item to FieldNames and FieldValues for the evaluatoing the pmml model.
     * @param data the data item to transform
     * @return FieldNames and FieldValues in a map.
     */
    Map<FieldName, FieldValue> transformData(Data data) {

        //arguments to pass to the decision function
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        if (modelEvaluator == null){
            init();
        }
        for(InputField activeField : activeFields){

            Object rawValue = data.get(activeField.getName().toString());

            FieldValue activeValue = activeField.prepare(rawValue);

            arguments.put(activeField.getName(), activeValue);
        }

        return arguments;
    }


    @Override
    public void reset() throws Exception {

    }
}