import com.sun.net.httpserver.HttpExchange;

import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF file to be used in the context of this project:
 * "n170_epochs.edf".
 **/
public class App {
    
    /**
     * This POST route in the REST API uploads and parses the epoched
     * EDF file that is provided in the body of the request, then
     * computes the corresponding ERP. You can use
     * "HttpToolbox.getMultipartFile()" to retrieve the bytes of the
     * EDF file, its name will be "data" (cf. "app.js").
     * 
     * The response must contain a JSON dictionary. This dictionary
     * maps the label of each channel in the EDF file, to a JSON array
     * containing the samples of the ERP for this channel. More
     * precisely, each element of the JSON array must be a JSON
     * dictionary with two fields: "x" indicates the timecode of the
     * sample (expressed in seconds), and "y" indicates the value of
     * the sample (expressed in physical values).
     *
     * The number of epochs in the source EDF file can be obtained by
     * using "EDFTimeSeries.lookupNumberOfEpochs()". If this method
     * returns "null", the method must answer with a 400 "Bad Request"
     * HTTP status.
     *
     * Sample command-line session using the "curl" tool (where
     * "n170_epochs.edf" corresponds to some epoched EDF file in the
     * current directory):
     *
     *   $ curl http://localhost:8000/compute-erp -F data=@n170_epochs.edf
     *   {
     *     "O1": [
     *       {
     *         "x": 0,
     *         "y": 0.45617424687252767
     *       },
     *       [...]
     *       {
     *         "x": 0.99609375,
     *         "y": -7.62939453125E-5
     *       }
     *     ],
     *     "O2": [
     *       {
     *         "x": 0,
     *         "y": -0.39939281608484967
     *       },
     *       [...]
     *       {
     *         "x": 0.99609375,
     *         "y": -7.62939453125E-5
     *       }
     *     ],
     *     [...]
     *   }
     *
     **/
    public static void computeERP(HttpExchange exchange) throws IOException {
        byte[] data = HttpToolbox.getMultipartFile(exchange, "data");
        EDFTimeSeries timeSeries = new EDFTimeSeries(data);

        JSONObject response = new JSONObject();

        if (data == null){
            HttpToolbox.sendBadRequest(exchange);
            return;
        }

        if (timeSeries.lookupNumberOfEpochs() == null) {
            HttpToolbox.sendBadRequest(exchange);
            return;
        }

        for (int i = 0; i < timeSeries.getNumberOfChannels(); i++) {
            JSONArray jsonArray = new JSONArray();
            int n_samples = timeSeries.getNumberOfSamples(i)/timeSeries.lookupNumberOfEpochs();
            double timecode = 1.0/timeSeries.getSamplingFrequency(i);
            for (int j = 0; j < n_samples; j++) {
                double sample_value = 0.0;
                for (int k = 0; k < timeSeries.lookupNumberOfEpochs(); k++) {
                    int digital_value = timeSeries.getDigitalValue(i, j + k * n_samples);
                    double physical_value = timeSeries.getChannel(i).getPhysicalValue(digital_value);
                    sample_value += physical_value;
                }

                JSONObject element = new JSONObject();
                element.put("x", j*timecode);
                element.put("y", sample_value/timeSeries.lookupNumberOfEpochs());
                jsonArray.put(element);
            }
            response.put(timeSeries.getChannel(i).getLabel(), jsonArray);

        }

        HttpToolbox.sendResponse(exchange, "application/json", String.valueOf(response));

    }
}
