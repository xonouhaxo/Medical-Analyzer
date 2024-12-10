import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 **/
public class App {
    
    /**
     * This method takes as argument a POST request to the REST API,
     * whose body contains a JSON object formatted as follows:
     *
     *   {
     *      "celsius": 20
     *   }
     *
     * The method must convert the degrees Celsius both to degrees
     * Fahrenheit and to Kelvins, and must send its response with a
     * body that contains a JSON object formatted as follows:
     *
     *   {
     *      "fahrenheit": 68,
     *      "kelvin": 293.15
     *   }
     *
     * All the values are floating-point numbers. An "IOException"
     * exception must be thrown in the case of a badly formatted
     * request.
     *
     * Make sure to use the global helper functions that are provided
     * by the "be.uclouvain.HttpToolbox" class!
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/convert-celsius -d '{"celsius":41}'
     *   {
     *     "kelvin": 314.15,
     *     "fahrenheit": 105.8
     *   }
     *
     * @param exchange The context of this REST API call.
     **/
    public static void convertCelsius(HttpExchange exchange) throws IOException {
        try{
            JSONObject start = HttpToolbox.getRequestBodyAsJsonObject(exchange);

            int celsius = start.getInt("celsius");
            double fahrenheit = (celsius * (double) 9 / 5) + 32;
            double kelvin = (celsius + 273.15);

            JSONObject responseJson = new JSONObject();
            responseJson.put("fahrenheit", fahrenheit);
            responseJson.put("kelvin", kelvin);

            HttpToolbox.sendResponse(exchange, responseJson);

        } catch (JSONException e) {
            throw new IOException();
        }
    }


    /**
     * This method takes as argument a POST request to the REST API,
     * whose body contains a JSON object (dictionary) containing
     * exactly two among the following fields: "voltage",
     * "resistance", "current", and "power". Here is such a valid
     * body:
     *
     *   {
     *      "current": 6,
     *      "resistance": 5
     *   }
     *
     * The method must compute the two missing values using the
     * "V=R*I" (Ohm's law) and "P=I*V" (electric power dissipated in
     * resistive circuits), and must send these two missing values
     * as a JSON object. Here is the body of the response for the
     * example above:
     *
     *   {
     *      "power": 100,
     *      "voltage": 30
     *   }
     *
     * All the values are floating-point numbers. An "IOException"
     * exception must be thrown in the case of a badly formatted
     * request.
     *
     * Make sure to use the global helper functions that are provided
     * by the "be.uclouvain.HttpToolbox" class!
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/compute-electricity -d '{"power":50,"voltage":40}'
     *   {
     *     "current": 1.25,
     *     "resistance": 32
     *   }
     *
     * @param exchange The context of this REST API call.
     **/
    public static void computeElectricity(HttpExchange exchange) throws IOException {
        try {
            JSONObject start = HttpToolbox.getRequestBodyAsJsonObject(exchange);
            if (start.length() != 2){
                throw new IOException();
            }
            try{
                double current = start.getInt("current");
                double resistance = start.getInt("resistance");

                double voltage = current*resistance;
                double power = current*voltage;

                JSONObject responseJson = new JSONObject();
                responseJson.put("power", power);
                responseJson.put("voltage", voltage);

                HttpToolbox.sendResponse(exchange, responseJson);
            } catch (Exception e) {
                try{
                    double power = start.getInt("power");
                    double voltage = start.getInt("voltage");

                    double current = power/voltage;
                    double resistance = voltage/current;
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("current", current);
                    responseJson.put("resistance", resistance);

                    HttpToolbox.sendResponse(exchange, responseJson);
                } catch (Exception j) {
                    try {
                        double power = start.getInt("power");
                        double current = start.getInt("current");

                        double voltage = power / current;
                        double resistance = voltage / current;
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("voltage", voltage);
                        responseJson.put("resistance", resistance);

                        HttpToolbox.sendResponse(exchange, responseJson);
                    } catch (Exception l) {
                        try {
                            double voltage = start.getInt("voltage");
                            double current = start.getInt("current");

                            double power = voltage * current;
                            double resistance = voltage / current;
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("power", power);
                            responseJson.put("resistance", resistance);

                            HttpToolbox.sendResponse(exchange, responseJson);
                        } catch (Exception s) {
                            try {
                                double power = start.getInt("power");
                                double resistance = start.getInt("resistance");

                                double current = Math.sqrt(power / resistance);
                                double voltage = power / current;
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("current", current);
                                responseJson.put("voltage", voltage);

                                HttpToolbox.sendResponse(exchange, responseJson);
                            } catch (Exception z) {
                                try {
                                    double voltage = start.getInt("voltage");
                                    double resistance = start.getInt("resistance");

                                    double current = voltage / resistance;
                                    double power = current * voltage;
                                    JSONObject responseJson = new JSONObject();
                                    responseJson.put("current", current);
                                    responseJson.put("power", power);

                                    HttpToolbox.sendResponse(exchange, responseJson);
                                } catch (Exception x) {
                                    throw new IOException();
                                }
                            }
                        }
                    }
                }
            }

        } catch (JSONException e) {
            throw new IOException();
        }
    }

}
