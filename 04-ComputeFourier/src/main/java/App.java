import be.uclouvain.HttpToolbox;
import com.sun.net.httpserver.HttpExchange;

import be.uclouvain.ComplexNumber;
import be.uclouvain.EDFTimeSeries;
import be.uclouvain.Signal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF file to be used in the context of this project:
 * "test_generator_2.edf".
 **/
public class App {


    /**
     * This method must compute the Discrete Fourier Transform (DFT)
     * of one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be quadratic in the number of
     * elements in the input vector.
     *
     * @param The input signal in the time domain.
     * @return The output of the DFT, i.e. the Fourier spectrum.
     **/
    public static Signal computeDFT(Signal source) {
        Signal signal = new Signal(source.getLength());
        for (int k = 0; k < source.getLength(); k++) {
            ComplexNumber result = new ComplexNumber(0,0) ;
            for (int n = 0; n < source.getLength(); n++) {
                ComplexNumber num = new ComplexNumber(0, -2 * Math.PI * k * n / source.getLength());
                result = add_img(result, multi_img(source.getValue(n), exp_img(num)));
            }
            signal.setValue(k, result);
        }
        return signal;
    }

    public static ComplexNumber add_img(ComplexNumber a, ComplexNumber b){
        double reel = a.getReal() + b.getReal();
        double img = a.getImag() + b.getImag();
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber subs_img(ComplexNumber a, ComplexNumber b){
        double reel = a.getReal() - b.getReal();
        double img = a.getImag() - b.getImag();
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber multi_img(ComplexNumber a, ComplexNumber b){
        double reel = (a.getReal()*b.getReal() - a.getImag()*b.getImag());
        double img = (a.getReal()*b.getImag() + a.getImag()*b.getReal());
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber exp_img(ComplexNumber a){
        double reel = (Math.cos(a.getImag()));
        double img = Math.sin(a.getImag());
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber divide(ComplexNumber z1, ComplexNumber z2) {
        double a = z1.getReal();
        double b = z1.getImag();
        double c = z2.getReal();
        double d = z2.getImag();

        // Denominator is (c^2 + d^2)
        double denominator = c * c + d * d;

        // If denominator is zero, we cannot divide (to avoid division by zero)
        if (denominator == 0) {
            throw new ArithmeticException("Cannot divide by zero (complex denominator).");
        }

        // Real part of the result
        double realPart = (a * c + b * d) / denominator;

        // Imaginary part of the result
        double imagPart = (b * c - a * d) / denominator;

        // Return the result as a new complex number
        return new ComplexNumber(realPart, imagPart);
    }


    /**
     * This method must compute the Fast Fourier Transform (FFT) of
     * one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be O(N * log(N)), where N is
     * the number of elements in the vector.
     *
     * N must be a power of 2. If it is not the case, an
     * "IllegalArgumentException" must be thrown.
     *
     * @param The input signal in the time domain.
     * @return The output of the FFT, i.e. the Fourier spectrum.
     **/
    public static Signal computeFFT(Signal source) {
        int N = source.getLength();

        if ((N == 0) || (N ==1)){
            return source;
        }

        if (! (N > 0 && (N & (N - 1)) == 0)) {
            throw new IllegalArgumentException("N must be a power of 2.");
        }

        Signal even_signal = new Signal(N / 2);
        Signal odd_signal = new Signal(N / 2);

        for (int m = 0; m < N / 2; m++) {
            even_signal.setValue(m, source.getValue(m * 2));
            odd_signal.setValue(m, source.getValue(m * 2 + 1));
        }

        Signal even_fft = computeFFT(even_signal);
        Signal odd_fft = computeFFT(odd_signal);

        Signal result = new Signal(N);
        for (int k = 0; k < N / 2; k++) {
            double theta = -2 * Math.PI * k / N;
            ComplexNumber exp = exp_img(new ComplexNumber(0, theta));

            ComplexNumber evenValue = even_fft.getValue(k);
            ComplexNumber oddValue = multi_img(odd_fft.getValue(k), exp);

            result.setValue(k, add_img(evenValue, oddValue));
            result.setValue(k + N / 2, subs_img(evenValue, oddValue));
        }

        return result;
    }

    /**
     * This method must compute the power spectrum of one vector of
     * complex numbers. The normalization constant must be "T/N",
     * where "T" is the sampling period (in seconds), and "N" is the
     * number of elements in the vector. The output must be an array
     * of doubles containing the values of the power spectrum.
     **/
    public static double[] computePowerSpectrum(Signal fourier, double samplingFrequency) {
        int N = fourier.getLength();

        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0.");
        }

        double[] powerSpectrum = new double[N];

        double T = 1.0 / samplingFrequency;
        double normalizationFactor = T / N;

        for (int k = 0; k < N; k++) {
            ComplexNumber value = fourier.getValue(k);

            powerSpectrum[k] = normalizationFactor * (Math.pow(value.getReal(),2) + Math.pow(value.getImag(),2));
        }

        return powerSpectrum;
    }

    /**
     * This method must implement a POST route in the REST API that
     * computes the power spectrum of one channel of the provided EDF
     * file. You must use the FFT (because DFT would take too much
     * time to run).
     *
     * If the length of the channel of interest is not a power of 2,
     * it must be padded with zeros. If there are "N" samples in the
     * channel, the function must only output the "N/2" first values
     * of the power spectrum (because the "N/2" next values are a
     * reversed copy of the first ones, by virtue of the properties of
     * the DFT of real-valued signals).
     *
     * Note that you don't have to implement the uploading of the EDF
     * file, neither the parsing of the channel index by yourself:
     * This is already implemented for you in the "AppLauncher.java"
     * file. Consequently, the "exchange" argument must only be used
     * to send data to the client of the REST API (in other words,
     * "exchange" must *not* be used as an input, but only as an
     * output).
     *
     * The response must contain a JSON array that contains the power
     * spectrum of the channel of interest in the EDF file. More
     * precisely, each element of the JSON array must be a JSON
     * dictionary with two fields: "x" indicates the frequency
     * (expressed in Hertz), and "y" indicates the value of the power
     * spectrum at that frequency (after normalization by "T/N"). Pay
     * attention to the fact that "x" must take into account the
     * sampling frequency of the channel of interest.
     *
     * Sample command-line session using the "curl" tool to get the
     * power spectrum of the 6th channel in an EDF file (where
     * "test_generator_2.edf" corresponds to some EDF file in the
     * current directory):
     *
     *   $ curl http://localhost:8000/upload -F data=@test_generator_2.edf
     *   $ curl http://localhost:8000/compute-power-spectrum -d '{"channel":6}'
     *   [
     *     {
     *       "x": 0,
     *       "y": 0.12789769243681803
     *     },
     *     {
     *       "x": 0.00152587890625,
     *       "y": 0.002706872936643377
     *     },
     *     [...],
     *     {
     *       "x": 100,
     *       "y": 0
     *     }
     *   ]
     *
     **/
    public static void computePowerSpectrum(HttpExchange exchange,
                                            EDFTimeSeries timeSeries,
                                            int channelIndex) throws IOException {
        if (timeSeries == null) {
            HttpToolbox.sendNotFound(exchange);
            //throw new IOException();
        } else if (timeSeries.getNumberOfChannels() < channelIndex) {
            HttpToolbox.sendNotFound(exchange);
        } else {

            EDFTimeSeries.Channel channel = timeSeries.getChannel(channelIndex);

            int N = timeSeries.getNumberOfSamples(channelIndex);

            int new_N = N;
            while (! (new_N > 0 && (new_N & (new_N - 1)) == 0)){
                new_N++;
            }
            Signal signal = new Signal(new_N);

            for (int i = 0; i < N; i++) {
                signal.setValue(i, channel.getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)));
            }
            for (int j = N; j < new_N; j++) {
                signal.setValue(j, new ComplexNumber(0,0));
            }

            Signal FFT_signal = computeFFT(signal);
            double[] powerspectrum = computePowerSpectrum(FFT_signal, timeSeries.getSamplingFrequency(channelIndex));

            JSONArray response = new JSONArray();

            for (int i = 0; i <= powerspectrum.length/2; i++) {
                JSONObject element = new JSONObject();
                double frequency = timeSeries.getSamplingFrequency(channelIndex)/ powerspectrum.length  * i;
                element.put("x", frequency);
                element.put("y", powerspectrum[i]);
                response.put(element);
            }
            HttpToolbox.sendResponse(exchange, "application/json", String.valueOf(response));
        }
    }
}
