import com.sun.net.httpserver.HttpExchange;

import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;

import java.io.IOException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF file to be used in the context of this project:
 * "eeglab_data.edf".
 **/
public class App {
    private EDFTimeSeries timeSeries;  // Current EDF file

    /**
     * This POST route in the REST API will discard the current EDF
     * file. It must answer with an empty text response.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/clear -d ''
     *
     **/
    public synchronized void postClear(HttpExchange exchange) throws IOException {
        try{
            timeSeries = null;
            HttpToolbox.sendResponse(exchange,"text/plain", "");
            //HttpExchange.sendResponseHeaders(200, -1);
            // might have to add a response header with 200 if
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This POST route in the REST API uploads and parses the EDF file
     * that is provided in the body of the request. The method must
     * answer with an empty text response. You can use
     * "HttpToolbox.getMultipartFile()" to retrieve the bytes of the
     * EDF file, its name will be "data" (cf. "app.js").
     *
     * Sample command-line session using the "curl" tool (where
     * "eeglab_data.edf" corresponds to some EDF file in the current
     * directory):
     *
     *   $ curl http://localhost:8000/upload -F data=@eeglab_data.edf
     *
     **/
    public synchronized void postUpload(HttpExchange exchange) throws IOException {
        byte[] edfData = HttpToolbox.getMultipartFile(exchange, "data");
        timeSeries = new EDFTimeSeries(edfData);
        if(edfData == null){
            HttpToolbox.sendBadRequest(exchange);
        }


        HttpToolbox.sendResponse(exchange,"text/plain", "");

    }

    /**
     * This GET route in the REST API returns a JSON dictionary that
     * maps the labels of the channels/electrodes to their index in
     * the current EDF file.
     *
     * If no EDF file is currently uploaded, the method must answer
     * with a 404 "Not Found" HTTP status.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/channels
     *   {
     *     "O1": 29,
     *     "O2": 31,
     *     [...]
     *     "FPz": 0,
     *     "Fz": 3
     *   }
     *
     **/
    public synchronized void getChannels(HttpExchange exchange) throws IOException {
        if (timeSeries == null) {
            //HttpToolbox.sendResponse(exchange, "text/plain", "No EDF file uploaded", 404);
            //HttpToolbox.sendBadRequest(exchange);
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        JSONObject jsonResponse = new JSONObject();
        for (int i=0; i < timeSeries.getNumberOfChannels(); i++ ){
            String label = timeSeries.getChannel(i).getLabel();
            jsonResponse.put(label, i);
        }
        HttpToolbox.sendResponse(exchange, jsonResponse);

    }

    /**
     * This GET route in the REST API returns all the samples that are
     * stored in one channel/electrode of interest, in the current EDF
     * file. The channel of interest is contained in the GET argument
     * "channel" provided in the URI (to help you, in this exercise,
     * the GET arguments of the URI are already parsed in the
     * "arguments" parameter of the method).
     *
     * The function must answer with a JSON array, each element of it
     * being a JSON dictionary with two fields: "x" indicates the
     * timecode of the sample (expressed in seconds), and "y"
     * indicates the value of the sample (expressed in physical
     * values).
     *
     * If the "channel" GET argument is absent or incorrectly
     * formatted (i.e. not an integer), the method must answer with a
     * 400 "Bad Request" HTTP status.
     *
     * If no EDF file is currently uploaded, or if the index "channel"
     * is non-existent in the currently uploaded EDF file, the method
     * must answer with a 404 "Not Found" HTTP status.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/samples?channel=25
     *   [
     *     {
     *       "x": 0,
     *       "y": -13.808136
     *     },
     *     {
     *       "x": 0.0078125,
     *       "y": -4.907959
     *     },
     *     [...]
     *     {
     *       "x": 238.99219,
     *       "y": 0.012023926
     *     }
     *   ]
     *
     **/
    public synchronized void getSamples(HttpExchange exchange,
                                        Map<String, String> arguments) throws IOException {

        
        String channel = arguments.get("channel");

        if (channel == null || channel.isEmpty()) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        int i;

        try {
            i = Integer.parseInt(channel);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        if (timeSeries == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if (i < 0 || i >= timeSeries.getNumberOfChannels()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }



        int samples = timeSeries.getNumberOfSamples(i);
        if (samples == 0) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        

        JSONArray jsonArray = new JSONArray();


        double samplingFrequency = timeSeries.getSamplingFrequency(i);
        double timeInterval = 1.0 / samplingFrequency;

        for (int j = 0; j < samples; j++) {
            int digitalValue = timeSeries.getDigitalValue(i, j);
            float physicalValue = timeSeries.getChannel(i).getPhysicalValue(digitalValue);

            JSONObject sampleJson = new JSONObject();
            sampleJson.put("x", j * timeInterval);
            sampleJson.put("y", physicalValue);
            jsonArray.put(sampleJson);
        }

        HttpToolbox.sendResponse(exchange, "application/json", jsonArray.toString());
    }

}
